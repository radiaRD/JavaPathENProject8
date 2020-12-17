package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Ignore;
import org.junit.Test;

import gpsUtil.GpsUtil;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rewardCentral.RewardCentral;
import tourGuide.beans.Attraction;
import tourGuide.beans.Location;
import tourGuide.beans.VisitedLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.RewardCentralProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;


@RunWith(SpringRunner.class)
@SpringBootTest
public class TestPerformance {
	@Autowired
	GpsUtilProxy gpsUtilProxy;
	@Autowired
	RewardCentralProxy rewardCentralProxy;
	@Autowired
	RewardsService rewardsService;
	@Autowired
	TourGuideService tourGuideService;
	
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

	@Test
	public void highVolumeTrackLocation() throws InterruptedException {
		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		InternalTestHelper.setInternalUserNumber(100000);

		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

	    StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		for(User user : allUsers) {
			tourGuideService.trackUserLocationWithThread(user);
		}
		tourGuideService.shutdown();
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}


		@Test
		public void highVolumeGetRewards() throws InterruptedException {

	//		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

			// Users should be incremented up to 100,000, and test finishes within 20 minutes
			InternalTestHelper.setInternalUserNumber(100000);
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
	//		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

			Attraction attraction = gpsUtilProxy.getAttractions().get(0);
			List<User> allUsers = new ArrayList<>();
			allUsers = tourGuideService.getAllUsers();

			allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), new Location(attraction.latitude,attraction.longitude), new Date())));

			allUsers.forEach(u ->
			//	rewardsService.calculateRewards(u));
						rewardsService.calculateRewardsWithThread(u));

				for(User user : allUsers) {
				    assertTrue(user.getUserRewards().size() > 0);
			}

			stopWatch.stop();
			tourGuideService.tracker.stopTracking();

			System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
			assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
		}

}
