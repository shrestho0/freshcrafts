package fresh.crafts.engine.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.services.DashboardService;

@RestController
@RequestMapping(value = "/api/v1/dashboard", consumes = "application/json", produces = "application/json")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("dbs")
    public ResponseEntity<CommonResponseDto> getActiveDBs() {

        CommonResponseDto res = new CommonResponseDto();
        dashboardService.getActiveDBs(res);

        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @GetMapping("deployments")
    public ResponseEntity<CommonResponseDto> getDeployments() {

        CommonResponseDto res = new CommonResponseDto();
        dashboardService.getDeployments(res);

        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

}
