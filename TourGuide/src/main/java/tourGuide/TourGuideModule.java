package tourGuide;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.RewardCentralProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;

@Configuration
public class TourGuideModule {

	@Autowired
	GpsUtilProxy gpsUtilProxy;
	@Autowired
	RewardCentralProxy rewardCentralProxy;

//	@Bean
//	public GpsUtil getGpsUtil() {
//		return new GpsUtil();
//	}

	@Bean
	public RewardsService getRewardsService() {
		return new RewardsService(gpsUtilProxy, rewardCentralProxy);
	}

	@Bean
	public TourGuideService getTourGuideService(){return new TourGuideService(gpsUtilProxy,getRewardsService());}

//	@Bean
//	public RewardCentral getRewardCentral() {
//		return new RewardCentral();
	}



