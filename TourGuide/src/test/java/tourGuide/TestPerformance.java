package tourGuide;

import static org.junit.Assert.assertEquals;
import java.util.Locale;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;

public class TestPerformance {
	
	/*
	 * A note on performance improvements:
	 *     
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *     
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *     
	 *     
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent. 
	 * 
	 *     These are performance metrics that we are trying to hit:
	 *     
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	@BeforeAll
	public void setup() {
		Locale.setDefault(Locale.US);
	}
	
	@Test
	public void highVolumeTrackLocationAndRewardCalculation() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(100000);
		/* The Tracker starts every time the TourGuideService gets created. In the Tracker
		we invoke the method <<trackUserLocation>> and <<calculateRewards>>for each user. 
		So, basically, we have to make sure that the Threads completed. */
		
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		/* It takes around 350-400 seconds for all the Threads to process 100 000 users.
		Here we make the test method wait the completion of all Threads in Tracker. 
		If it takes more than 410 seconds to process all the users this test method will fail
		due to InterruptedException. */
		try {
			Thread.sleep(410000); // Tracker runs about 350-400 seconds
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		/* By default all the users have 3 visited locations. If the Tracker completed successfully
		all the users will have +1 visited location (i.e. 4 visited locations). We can check that. */
		tourGuideService.getAllUsers().forEach(u -> {
			assertEquals(4, u.getVisitedLocations().size());
		});
	}
}
