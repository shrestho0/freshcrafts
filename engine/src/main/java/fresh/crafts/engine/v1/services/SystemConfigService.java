package fresh.crafts.engine.v1.services;

import java.util.Date;
import java.util.Optional;

import fresh.crafts.engine.v1.controllers.SystemConfigController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
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
        systemConfig.setId(CONFIG_ID); // TODO: possibly redundant
        systemConfig.setSystemUserSetupComplete(false);
        systemConfig.setSetupKey(COCKPIT_SETUP_KEY);
        systemConfig.setCreated(new Date());
        systemConfig.setUpdated(new Date());
        
        return systemConfigRepository.save(systemConfig);
    }

    public SystemConfig setupConfig(SystemConfig systemConfig) {
        Optional<SystemConfig> optionalExistingConfig = getOnly();
        SystemConfig existingConfig = optionalExistingConfig.orElse(null);

        if(existingConfig == null){
            // throw new SystemConfigurationsNotSetException("System configurations not set");
            // create default
            existingConfig = createDefault();
        }

        // save new config
        systemConfig.setId(existingConfig.getId());
        String passwordHash = BCrypt.hashpw(systemConfig.getSystemUserPasswordHash() , BCrypt.gensalt());

        systemConfig.setSystemUserPasswordHash(passwordHash);


        systemConfig.setUpdated(new Date());
        return systemConfigRepository.save(systemConfig);

    }

    public SystemConfig update(SystemConfig systemConfig) throws SystemConfigurationsNotSetException {
        // partial update
        Optional<SystemConfig> optionalExistingConfig = getOnly();
        SystemConfig existingConfig = optionalExistingConfig.orElse(null);

        if(existingConfig == null){
            // throw new SystemConfigurationsNotSetException("System configurations not set");
            // create default
            existingConfig = createDefault();
        }



        System.out.println("============================================");
        System.out.println("New Config: " + systemConfig);
        System.out.println("Old Config: " + existingConfig);
        System.out.println("============================================");
 
        // Update if not null
        if (systemConfig.getSystemUserSetupComplete() != null) {
            existingConfig.setSystemUserSetupComplete(systemConfig.getSystemUserSetupComplete());
        }

        // We won't let setupKey set from outside

        if (systemConfig.getSystemUserName() != null) {
            existingConfig.setSystemUserName(systemConfig.getSystemUserName());
        }
        
        if (systemConfig.getSystemUserEmail() != null) {
            existingConfig.setSystemUserEmail(systemConfig.getSystemUserEmail());
        }
        
        if (systemConfig.getSystemUserEmail() != null) {
            existingConfig.setSystemUserEmail(systemConfig.getSystemUserEmail());
        }

        if (systemConfig.getSystemUserPasswordHash() != null) {
            // Hash Password Before Saving
            String passwordHash = BCrypt.hashpw(systemConfig.getSystemUserPasswordHash() , BCrypt.gensalt());
            existingConfig.setSystemUserPasswordHash(passwordHash);
        }

        if (systemConfig.getSystemUserOauthGoogleEnabled() != null) {
            existingConfig.setSystemUserOauthGoogleEnabled(systemConfig.getSystemUserOauthGoogleEnabled());
        }
        if (systemConfig.getSystemUserOauthGithubEnabled() != null) {
            existingConfig.setSystemUserOauthGithubEnabled(systemConfig.getSystemUserOauthGithubEnabled());
        }

        // TODO: Fix these
//
//
//        if(systemConfig.getOAuthGoogleEmail() != null){
//            System.out.println("Setting oauth google email");
//            existingConfig.setOAuthGoogleEmail(systemConfig.getOAuthGoogleEmail());
//        }
//        if(systemConfig.getOAuthGithubId() !=null){
//            System.out.println("Setting oauth google email");
//            existingConfig.setOAuthGithubId(systemConfig.getOAuthGithubId());
//        }


        if(systemConfig.getSystemUserOAuthGoogleEmail() != null){
            existingConfig.setSystemUserOAuthGoogleEmail(systemConfig.getSystemUserOAuthGoogleEmail());
        }
        if(systemConfig.getSystemUserOAuthGithubId() != null){
            existingConfig.setSystemUserOAuthGithubId(systemConfig.getSystemUserOAuthGithubId());
        }

        if (systemConfig.getSystemUserOauthGoogleData() != null) {
            existingConfig.setSystemUserOauthGoogleData(systemConfig.getSystemUserOauthGoogleData());
        }
        if (systemConfig.getSystemUserOauthGithubData() != null) {
            existingConfig.setSystemUserOauthGithubData(systemConfig.getSystemUserOauthGithubData());
        }




        existingConfig.setUpdated(new Date());
        return systemConfigRepository.save(existingConfig);


    }

}   
