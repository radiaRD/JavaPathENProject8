package tourGuide.proxies;

import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import tourGuide.beans.Attraction;
import tourGuide.beans.VisitedLocation;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "gpsUtil", url = "localhost:8081")
public interface GpsUtilProxy {

    @RequestMapping("/getAttractions")
     List<Attraction> getAttractions();

    @RequestMapping("/getLocation/{userId}")
    VisitedLocation getUserLocation(@PathVariable("userId") UUID userId);
    }
