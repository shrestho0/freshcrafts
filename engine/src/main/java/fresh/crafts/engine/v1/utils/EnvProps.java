package fresh.crafts.engine.v1.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Be careful with this, failing to set properties properly throwslike 1000
 * lines of error
 */
@Component
public class EnvProps {

    @Value("${JWT_ACCESS_SECRET}")
    private String accessSecret;
    @Value("${JWT_ISSUER}")
    private String issuer;
    @Value("${JWT_ACCESS_EXPIRES_IN}")
    private String accessExpiresIn;
    @Value("${JWT_REFRESH_SECRET}")
    private String refreshSecret;
    @Value("${JWT_REFRESH_EXPIRES_IN}")
    private String refreshExpiresIn;

    @Value("${COCKPIT_URL_LOCAL}")
    private String cockpitLocalUrl;

    @Value("${COCKPIT_SSE_TOKEN}")
    private String cockpitAuthorizationToken;

    public String getAccessSecret() {
        return accessSecret;
    }

    public String getAccessExpiresIn() {
        return accessExpiresIn;
    }

    public String getRefreshSecret() {
        return refreshSecret;
    }

    public String getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getCockpitLocalUrl() {
        return cockpitLocalUrl;
    }

    public String getCockpitAuthorzationToken() {
        return cockpitAuthorizationToken;
    }

}