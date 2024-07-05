package fresh.crafts.engine.v1.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.models.SystemConfig;
import fresh.crafts.engine.v1.repositories.SystemConfigRepository;
import fresh.crafts.engine.v1.utils.exceptions.SystemConfigurationsNotSetException;



@Service
public class SystemConfigService {


    
    @Autowired
    private SystemConfigRepository systemConfigRepository;
    // @Autowired
    // private static Environment env;
    


    @Value("${freshCrafts.MONGO_CONFIG_SYSTEM_CONFIG_COLLECTION_ID}")
    private String CONFIG_ID;
    @Value("${freshCrafts.COCKPIT_SETUP_KEY}")
    private String COCKPIT_SETUP_KEY; 

    public  Optional<SystemConfig> getOnly(){
        if(CONFIG_ID == null){
            return Optional.empty();
        }
        return systemConfigRepository.findById(CONFIG_ID);
    }

    public SystemConfig createDefault(){

        System.out.println("CONFIG_ID: " + CONFIG_ID);
        System.out.println("COCKPIT_SETUP_KEY: " + COCKPIT_SETUP_KEY);
        
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setId(CONFIG_ID); // TODO: redundant
        systemConfig.setSystemSetupComplete(false);
        systemConfig.setSetupKey(COCKPIT_SETUP_KEY);
        systemConfig.setCreated(new Date());
        systemConfig.setUpdated(new Date());
        
        return systemConfigRepository.save(systemConfig);
    }
     
    public SystemConfig update(SystemConfig systemConfig) throws SystemConfigurationsNotSetException {
        // partial update
        Optional<SystemConfig> optionalExistingConfig = getOnly();
        SystemConfig existingConfig = optionalExistingConfig.orElse(null);

        if(existingConfig == null){
            throw new SystemConfigurationsNotSetException("System configurations not set");
        }

        

        if (systemConfig.getSystemSetupComplete() != null) {
            existingConfig.setSystemSetupComplete(systemConfig.getSystemSetupComplete());
        }
        if (systemConfig.getSystemUserEmail() != null) {
            existingConfig.setSystemUserEmail(systemConfig.getSystemUserEmail());
        }
        if (systemConfig.getSystemUserPasswordHash() != null) {
            // TODO: Hash passwords before saving
            existingConfig.setSystemUserPasswordHash(systemConfig.getSystemUserPasswordHash());
        }
        if (systemConfig.getSystemUserOauthProviders() != null) {
            existingConfig.setSystemUserOauthProviders(systemConfig.getSystemUserOauthProviders());
        }
        existingConfig.setUpdated(new Date());
        return systemConfigRepository.save(existingConfig);


    }

}   
