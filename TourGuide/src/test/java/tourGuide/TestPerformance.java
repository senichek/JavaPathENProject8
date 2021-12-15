package tourGuide;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.time.StopWatch;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

public class TestPerformance {
	
	@BeforeAll
	public void setup() {
		Locale.setDefault(Locale.US);
	}
	
	@Test
	public void highVolumeTrackLocationTest() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(100000);

		 /* Every time we create the instance of TourGuideService the Tracker gets created
		 and the Tracker launches the Thread (ExecutorService) which in its turn launches 
		 <trackUserLocationMultiThreading> and <calculateRewardsMultiThreading>. We have 
		 to test the method <trackUserLocationMultiThreading> separately, this is why we 
		 have to stop the Tracker. */
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		tourGuideService.tracker.stopTracking();

		List<User> users = tourGuideService.getAllUsers();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		tourGuideService.trackUserLocationMultiThreading(users);
		stopWatch.stop();
		
		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	@Test
	public void highVolumeGetRewardsTest() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(100000);

		//See the comment in highVolumeTrackLocation Test method.
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		tourGuideService.tracker.stopTracking();

		List<User> users = tourGuideService.getAllUsers();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		rewardsService.calculateRewardsMultiThreading(users);
		stopWatch.stop();
		
		System.out.println("highVolumeGetRewardsTest: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
}
