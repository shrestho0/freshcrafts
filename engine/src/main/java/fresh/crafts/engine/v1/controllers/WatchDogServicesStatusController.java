package fresh.crafts.engine.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.models.WatchDogServicesStatus;
import fresh.crafts.engine.v1.services.WatchDogServicesStatusService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/watchdog-service-status", consumes = "application/json", produces = "application/json")
public class WatchDogServicesStatusController {

    @Autowired
    private WatchDogServicesStatusService watchDogServicesStatusService;

    @GetMapping("")
    public ResponseEntity<CommonResponseDto> getStatus() {
        // return "WatchDog Services Status Controller";
        CommonResponseDto res = new CommonResponseDto();

        List<WatchDogServicesStatus> xx = watchDogServicesStatusService.getWatchDogServicesStatus();
        // System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        // System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        if (xx.size() > 0) {
            res.setSuccess(true);
            // remove null values for all values of xx
            // System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
            // CraftUtils.jsonLikePrint(xx);
            // System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
            res.setPayload(xx);
        } else {
            res.setSuccess(false);
            res.setStatusCode(404);
            res.setMessage("No WatchDog Services Status found");
        }

        return ResponseEntity.status(res.getStatusCode()).body(res);
    }
}
