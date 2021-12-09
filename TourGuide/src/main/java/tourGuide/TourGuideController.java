package tourGuide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.DTO.AttractionDTO;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;

	@Autowired
	private RewardCentral rewardCentral;
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public String getLocation(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		return JsonStream.serialize(visitedLocation.location);
    }
    
 		//  Return a new JSON object that contains:
    	// Name of Tourist attraction, 
        // Tourist attractions lat/long, 
        // The user's location lat/long, 
        // The distance in miles between the user's location and each of the attractions.
        // The reward points for visiting each Attraction.
        //    Note: Attraction reward points can be gathered from RewardsCentral
    @RequestMapping("/getNearbyAttractions") 
    public List<AttractionDTO> getNearbyAttractions(@RequestParam String userName) {
    	VisitedLocation currentLocation = tourGuideService.getUserLocation(getUser(userName));
		Map<Double, Attraction> fiveClosestLocations = tourGuideService.getNearByAttractions(currentLocation);
		List<AttractionDTO> result = new ArrayList<>();

		// For each of 5 locations we create DTO.
		fiveClosestLocations.forEach((k, v) -> {
		AttractionDTO attractionDTO = new AttractionDTO();
		attractionDTO.setAttractionName(v.attractionName);
		attractionDTO.setAttractionLocation(new Location(v.latitude, v.longitude));
		attractionDTO.setUserLocation(currentLocation.location);
		attractionDTO.setDistanceInMiles(k); // Distance in kilometers acts as the KEY in the map of fiveClosestLocations.
		attractionDTO.setRewardsPoints(rewardCentral.getAttractionRewardPoints(v.attractionId, tourGuideService.getUser(userName).getUserId()));
		result.add(attractionDTO);
		});
	   return result;
    }
    
    @RequestMapping("/getRewards") 
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return tourGuideService.getUserRewards(getUser(userName));
    }
    
    @RequestMapping("/getAllCurrentLocations")
    public HashMap<UUID, Location> getAllCurrentLocations() {
    	// Get a list of every user's most recent location as JSON
		HashMap<UUID, Location> latestLocationsPerUser = new HashMap<>();
		tourGuideService.getAllUsers()
				.forEach(u -> latestLocationsPerUser.put(u.getUserId(), u.getLastVisitedLocation().location));

    	//return JsonStream.serialize(getUser(userName).getUserId() + ": " + getUser(userName).getVisitedLocations().get(0));
		return latestLocationsPerUser;
    }
    
    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(getUser(userName));
    }
    
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
}