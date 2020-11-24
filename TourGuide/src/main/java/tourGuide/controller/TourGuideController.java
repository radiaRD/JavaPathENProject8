package tourGuide.controller;

import java.util.List;

import gpsUtil.GpsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.dto.AllUsersCurrentLocations;
import tourGuide.dto.NearAttractionsDTO;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;

@RestController
public class TourGuideController {

    @Autowired
    TourGuideService tourGuideService;

    @Autowired
    GpsUtil gpsUtil;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation")
    public String getLocation(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(visitedLocation.location);
    }

    //  TODO: Change this method to no longer return a List of Attractions.
    @RequestMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
    }

    @RequestMapping("/getNearFiveAttractions")
    public List<NearAttractionsDTO> getDistance(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return tourGuideService.getNearFiveAttractions(visitedLocation, this.getUser(userName));

    }

    @RequestMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
        return tourGuideService.getUserRewards(getUser(userName));
    }

    @RequestMapping("/getAllCurrentLocations")
    public List<AllUsersCurrentLocations> getAllCurrentLocations() {
        // TODO: Get a list of every user's most recent location as JSON
        return tourGuideService.getAllCurrentLocations();
    }

    @RequestMapping("/getTripDeals/{tripDuration}/{numberOfAdults}/{numberOfChildren}")
    public String getTripDeals(@RequestParam String userName, @PathVariable int tripDuration, @PathVariable int numberOfAdults, @PathVariable int numberOfChildren) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName), tripDuration, numberOfAdults, numberOfChildren);
        return JsonStream.serialize(providers);
    }

    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }


}