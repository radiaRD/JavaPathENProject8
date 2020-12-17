package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tourGuide.beans.Attraction;
import tourGuide.beans.Location;
import tourGuide.beans.Provider;
import tourGuide.beans.VisitedLocation;
import tourGuide.dto.AllUsersCurrentLocations;
import tourGuide.dto.NearAttractionsDTO;
import tourGuide.helper.InternalTestHelper;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.RewardCentralProxy;
import tourGuide.proxies.TripPricerProxy;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
    ExecutorService executorService = Executors.newFixedThreadPool(100000);
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;
    boolean testMode = true;

    @Autowired
    GpsUtilProxy gpsUtilProxy;

    @Autowired
    RewardCentralProxy rewardCentralProxy;

    @Autowired
    TripPricerProxy tripPricerProxy;

    public TourGuideService(GpsUtilProxy gpsUtilProxy, RewardsService rewardsService) {
        this.gpsUtilProxy = gpsUtilProxy;
        this.rewardsService = rewardsService;

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    public List<UserReward> getUserRewards(User user) {
        rewardsService.calculateRewards(user);
        return user.getUserRewards();
    }


    public tourGuide.beans.VisitedLocation getUserLocation(User user)  {
        tourGuide.beans.VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
                user.getLastVisitedLocation() :
                trackUserLocation(user);
        return visitedLocation;
    }

    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    public List<tourGuide.beans.Provider> getTripDeals(User user, int tripDuration, int numberOfAdults, int numberOfChildren) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        UserPreferences preferences = new UserPreferences();
        preferences.setTripDuration(tripDuration);
        preferences.setNumberOfAdults(numberOfAdults);
        preferences.setNumberOfChildren(numberOfChildren);
        user.setUserPreferences(preferences);
        List<Provider> providers = tripPricerProxy.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public tourGuide.beans.VisitedLocation trackUserLocation(User user) {
        tourGuide.beans.VisitedLocation visitedLocation = gpsUtilProxy.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    public void trackUserLocationWithThread(User user) {
       executorService.execute(new Runnable() {
            public void run() {
                    trackUserLocation(user);
            }
        });
    }

    public void shutdown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

    }

    public List<tourGuide.beans.Attraction> getNearByAttractions(tourGuide.beans.VisitedLocation visitedLocation) {
        List<tourGuide.beans.Attraction> nearbyAttractions = new ArrayList<>();
        for (tourGuide.beans.Attraction attraction : gpsUtilProxy.getAttractions()) {
            if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.getLocation())) {
                nearbyAttractions.add(attraction);
            }
        }

        return nearbyAttractions;
    }


    public List<NearAttractionsDTO> getNeardistance(tourGuide.beans.VisitedLocation visitedLocation, User user) {
        List<NearAttractionsDTO> nearAttractions = new ArrayList<>();
        for (Attraction attraction : gpsUtilProxy.getAttractions()) {
            double distance = rewardsService.getDistance(new tourGuide.beans.Location(attraction.latitude, attraction.longitude), new Location(visitedLocation.getLocation().getLatitude(), visitedLocation.getLocation().getLongitude()));
            NearAttractionsDTO nearAttraction = new NearAttractionsDTO(attraction.getAttractionName(), distance, attraction.latitude, attraction.longitude, visitedLocation.getLocation(), rewardCentralProxy.getAttractionRewardPoints(attraction.getAttractionId(), user.getUserId()));
            nearAttractions.add(nearAttraction);
            Collections.sort(nearAttractions, Comparator.comparingDouble(NearAttractionsDTO::getDistance));
        }
        return nearAttractions;
    }

    public List<NearAttractionsDTO> getNearFiveAttractions(VisitedLocation visitedLocation, User user) {
        List<NearAttractionsDTO> nearAttractions = this.getNeardistance(visitedLocation, user);
        List<NearAttractionsDTO> fiveNearAttractions = new ArrayList<>();

        for (NearAttractionsDTO nearAttractionsDTO : nearAttractions) {
            for (int i = 0; fiveNearAttractions.size() < 5; i++) {
                fiveNearAttractions.add(nearAttractionsDTO);
                break;
            }
        }
        return fiveNearAttractions;

    }

    public List<AllUsersCurrentLocations> getAllCurrentLocations() {
        List<User> users = this.getAllUsers();
        List<AllUsersCurrentLocations> currentLocations = new ArrayList<>();
        for (User user : users) {
            this.generateUserLocationHistory(user);
            tourGuide.beans.VisitedLocation lastLocation = user.getLastVisitedLocation();
            AllUsersCurrentLocations currentLocation = new AllUsersCurrentLocations(lastLocation.getUserId(), lastLocation.getLocation().getLatitude(), lastLocation.getLocation().getLongitude());
            currentLocations.add(currentLocation);
        }
        return currentLocations;

    }


    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new tourGuide.beans.VisitedLocation(user.getUserId(), new tourGuide.beans.Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
