package tourGuide.user;

import tourGuide.beans.Provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class User {
    private final UUID userId;
    private final String userName;
    private String phoneNumber;
    private String emailAddress;
    private Date latestLocationTimestamp;
    private List<tourGuide.beans.VisitedLocation> visitedLocations = new ArrayList<>();
    private List<UserReward> userRewards = new ArrayList<>();
    private UserPreferences userPreferences = new UserPreferences();
    private List<tourGuide.beans.Provider> tripDeals = new ArrayList<>();

    public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setLatestLocationTimestamp(Date latestLocationTimestamp) {
        this.latestLocationTimestamp = latestLocationTimestamp;
    }

    public Date getLatestLocationTimestamp() {
        return latestLocationTimestamp;
    }

    public void addToVisitedLocations(tourGuide.beans.VisitedLocation visitedLocation) {
        visitedLocations.add(visitedLocation);
    }

    public List<tourGuide.beans.VisitedLocation> getVisitedLocations() {
        return visitedLocations;
    }

    public void clearVisitedLocations() {
        visitedLocations.clear();
    }

    public void addUserReward(UserReward userReward) {
        if (userRewards.stream().filter(r -> r.attraction.getAttractionName().equals(userReward.attraction)).count() == 0) {
            userRewards.add(userReward);
        }
    }

    public List<UserReward> getUserRewards() {
        return userRewards;
    }

    public UserPreferences getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    public tourGuide.beans.VisitedLocation getLastVisitedLocation() {
        return visitedLocations.get(visitedLocations.size() - 1);
    }

    public void setTripDeals(List<tourGuide.beans.Provider> tripDeals) {
        this.tripDeals = tripDeals;
    }

    public List<Provider> getTripDeals() {
        return tripDeals;
    }

}
