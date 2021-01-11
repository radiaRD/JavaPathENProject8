package tourGuide.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jsoniter.output.JsonStream;

import tourGuide.beans.Provider;
import tourGuide.beans.VisitedLocation;
import tourGuide.dto.AllUsersCurrentLocations;
import tourGuide.dto.NearAttractionsDTO;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@RestController
public class TourGuideController {

    @Autowired
    TourGuideService tourGuideService;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation")
    public String getLocation(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(visitedLocation.getLocation());
    }


    @RequestMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam String userName) {
        tourGuide.beans.VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
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
        return tourGuideService.getAllCurrentLocations();
    }

    @RequestMapping("/getTripDeals")
    @ResponseBody
    public List<Provider> getTripDeals(@RequestParam(value = "userName") String userName, @RequestParam(value = "tripDuration") int tripDuration, @RequestParam(value = "numberOfAdults") int numberOfAdults, @RequestParam(value = "numberOfChildren") int numberOfChildren) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName), tripDuration, numberOfAdults, numberOfChildren);
        return providers;
    }

    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }

}