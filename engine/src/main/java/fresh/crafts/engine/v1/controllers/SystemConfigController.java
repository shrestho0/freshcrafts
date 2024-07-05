package fresh.crafts.engine.v1.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.models.SystemConfig;
import fresh.crafts.engine.v1.services.SystemConfigService;



@RestController
@RequestMapping(value="/api/v1/config", consumes = "application/json", produces="application/json")
public class SystemConfigController {

        @Autowired
        private SystemConfigService systemConfigService;

        @GetMapping("/sysconf")
        public SystemConfig checkSysConf(){
            Optional<SystemConfig> systemConfig = systemConfigService.getOnly();

            if(systemConfig.isEmpty() || systemConfig.get() == null){
                return systemConfigService.createDefault();
            }

            return systemConfig.get();
        }

        @PatchMapping("/sysconf")
        public CommonResponseDto updateSysConf(@RequestBody SystemConfig systemConfig){
            CommonResponseDto commonResponseDto = new CommonResponseDto();
            try{

                SystemConfig sc = systemConfigService.update(systemConfig);
                commonResponseDto.setSuccess(true);
                commonResponseDto.setMessage("System configuration updated successfully");
                commonResponseDto.setData(sc);
                return commonResponseDto;
            }catch(Exception e){
            }
            commonResponseDto.setSuccess(false);
            commonResponseDto.setMessage("System configuration update failed");
            return commonResponseDto;
        }
    
        
}
