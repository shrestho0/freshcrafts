package fresh.crafts.engine.v1.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProperties {

    // @Value("${jwt.secret}")
    // private String secret;

    // @Value("${jwt.expiration.days}")
    // private int expirationDays;

    // public String getSecret() {
    // return secret;
    // }

    @Value("${freshCrafts.JWT_ACCESS_SECRET}")
    private String accessSecret;
    @Value("${freshCrafts.JWT_ISSUER}")
    private String issuer;
    @Value("${freshCrafts.JWT_ACCESS_EXPIRES_IN}")
    private String accessExpiresIn;
    @Value("${freshCrafts.JWT_REFRESH_SECRET}")
    private String refreshSecret;
    @Value("${freshCrafts.JWT_REFRESH_EXPIRES_IN}")
    private String refreshExpiresIn;

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

}