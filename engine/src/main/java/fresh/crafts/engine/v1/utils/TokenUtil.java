package fresh.crafts.engine.v1.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import fresh.crafts.engine.v1.types.Tokens;
import fresh.crafts.engine.v1.utils.exceptions.ApplicationPropertyNotSetException;

public class TokenUtil {

    @Autowired
    private static Environment env;
    

    public static Tokens generate(Object payload) throws Exception {
        Tokens tokens = new Tokens( _generateRefreshToken(payload), _generateAccessToken(payload));
        return tokens;
    }

    private static String _generateAccessToken(Object payload) throws ApplicationPropertyNotSetException {
        String accessSecret = env.getProperty("freshCrafts.JWT_ACCESS_SECRET");
        String expiresIn = env.getProperty("freshCrafts.JWT_ACCESS_EXPIRES_IN");

        if(accessSecret == null){
            throw new ApplicationPropertyNotSetException("freshCrafts.JWT_ACCESS_SECRET is not defined in application.properties");
        }
        
        if(expiresIn == null){
            throw new ApplicationPropertyNotSetException("freshCrafts.JWT_ACCESS_EXPIRES_IN is not defined in application.properties");

        }

        // TODO: generate token with payload and expires in

        return "ACCESS_TOKEN";
    }
    
    private static String _generateRefreshToken(Object payload) throws ApplicationPropertyNotSetException {
        String accessSecret = env.getProperty("freshCrafts.JWT_REFRESH_SECRET");
        String expiresIn = env.getProperty("freshCrafts.JWT_REFRESH_EXPIRES_IN");

        if(accessSecret == null){
            throw new ApplicationPropertyNotSetException("freshCrafts.JWT_REFRESH_SECRET is not defined in application.properties");
        }
        if(expiresIn == null){
            throw new ApplicationPropertyNotSetException("freshCrafts.JWT_REFRESH_EXPIRES_IN is not defined in application.properties");
        }

        // TODO: generate token with payload and expires in

        return "REFRESH_TOKEN";
    }
}
