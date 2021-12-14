package tourGuide.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tourGuide.service.TourGuideService;
import tourGuide.user.UserPreferences;

@RestController
public class UserController {

    @Autowired
	TourGuideService tourGuideService;

    @RequestMapping("/getUserPreferences") 
    public UserPreferences getPreferences(@RequestParam String userName) {
        return tourGuideService.getUser(userName).getUserPreferences();
    }

    @PostMapping("/getUserPreferences") 
    public UserPreferences submitPreferences(@RequestParam String userName, @RequestBody UserPreferences pref) {
    	tourGuideService.getUser(userName).setUserPreferences(pref);
        return pref;
    }
}
