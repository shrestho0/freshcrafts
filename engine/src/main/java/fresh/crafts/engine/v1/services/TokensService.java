package fresh.crafts.engine.v1.services;

import java.util.ArrayList;
import java.util.List;

import fresh.crafts.engine.v1.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.entities.JwtPayload;
import fresh.crafts.engine.v1.models.BlacklistedToken;
import fresh.crafts.engine.v1.models.SystemConfig;
import fresh.crafts.engine.v1.repositories.BlacklistedTokenRepository;
import fresh.crafts.engine.v1.types.Tokens;
import fresh.crafts.engine.v1.types.enums.OAuthProviderEnum;

@Service
public class TokensService {

    @Autowired
    SystemConfigService systemConfigService;

    @Autowired
    JwtService jwtService;

    @Autowired
    BlacklistedTokenRepository blacklistedTokenRepository;

    public List<OAuthProviderEnum> getEnabledOAuthProviders() {
        List<OAuthProviderEnum> l = new ArrayList<>();
        SystemConfig conf = systemConfigService.getOnly().orElse(null);
        if (conf != null) {
            if (conf.getSystemUserOauthGithubEnabled()) {
                l.add(OAuthProviderEnum.OAUTH_GITHUB);
            }
            if (conf.getSystemUserOauthGoogleEnabled()) {
                l.add(OAuthProviderEnum.OAUTH_GOOGLE);
            }
        }
        return l;

    }

    public GenerateTokenResponseDto generateToken(GenerateTokenRequestDto tokenDto) {

        GenerateTokenResponseDto res = new GenerateTokenResponseDto();

        SystemConfig conf = systemConfigService.getOnly().orElse(null);

        if (conf == null) {
            res.setSuccess(false);
            res.setMessage("System Config not found");
            return res;
        }

        System.out.println("[DEBUG] TokensService - generateToken: " + tokenDto);

        if (tokenDto == null) {
            res.setMessage("Invalid Data");
            return res;
        }

        JwtPayload payload = new JwtPayload();
        payload.setSystemUserName(conf.getSystemUserName());
        payload.setSystemUserEmail(conf.getSystemUserEmail());
        payload.setProvider(tokenDto.getProvider());

        switch (tokenDto.getProvider()) {
            case EMAIL_PASSWORD -> {
                // check email password
                if (BCrypt.checkpw(tokenDto.getPassword(), conf.getSystemUserPasswordHash())
                        && tokenDto.getEmail().equals(conf.getSystemUserEmail())) {
                    try {
                        // Generate jwt payload with sys name, email, provider

                        Tokens tokens = jwtService.generate(payload);
                        res.setTokens(tokens);
                        res.setSuccess(true);
                        res.setMessage("Token Generated");
                        return res;
                    } catch (Exception e) {
                        res.setSuccess(false);
                        res.setMessage("Token Generation Failed with message: " + e.getMessage());
                        return res;
                    }

                    // Generate Token Here

                } else {
                    res.setMessage("Invalid Email or Password");
                    return res;
                }
            }
            case OAUTH_GITHUB -> {
                System.out.println("TOKEN DTO FOR OAUTH_GITHUB " + tokenDto);
                // we'll use this to check, email for Google and id for GitHub
                if (tokenDto.getGithubId().equals(conf.getSystemUserOAuthGithubId())) {
                    // Valid
                    System.out.println("Valid User");
                    try {
                        Tokens tokens = jwtService.generate(payload);
                        res.setTokens(tokens);
                        res.setSuccess(true);
                        res.setMessage("Token Generated");
                        return res;
                    } catch (Exception e) {
                        res.setMessage("Invalid Email or Password");
                        return res;
                    }
                } else {
                    // invalid user
                    res.setSuccess(false);
                    System.out.println("Oauth ID Did not match");
                    return res;
                }

            }
            case OAUTH_GOOGLE -> {
                System.out.println("TOKEN DTO FOR OAUTH_GOOGLE " + tokenDto);
                // we'll use this to check, email for Google and id for GitHub
                if (tokenDto.getGoogleEmail().equals(conf.getSystemUserOAuthGoogleEmail())) {
                    // Valid
                    System.out.println("Valid User");
                    try {
                        Tokens tokens = jwtService.generate(payload);
                        res.setTokens(tokens);
                        res.setSuccess(true);
                        res.setMessage("Token Generated");
                        return res;
                    } catch (Exception e) {
                        res.setMessage("Invalid Email or Password");
                        return res;
                    }
                } else {
                    // invalid user
                    res.setSuccess(false);
                    System.out.println("Oauth ID Did not match");
                    return res;
                }
            }
            default -> {
                res.setMessage("Invalid Provider");
                return res;
            }

        }
    }

    public GenerateRefreshTokenResponseDto refreshToken(GenerateRefreshTokenRequestDto refreshToken) {
        GenerateRefreshTokenResponseDto res = new GenerateRefreshTokenResponseDto();
        System.out.println("[DEBUG] TokensService - refreshToken: " + refreshToken);

        SystemConfig conf = systemConfigService.getOnly().orElse(null);

        if (conf == null) {
            res.setSuccess(false);
            res.setMessage("System Config not found");
            return res;
        }

        // Verification is being done in cockpit as handling auth is cockpits
        // responsibility
        //

        // TODO: Blacklist token
        // Check if token already blacklisted
        BlacklistedToken blacklistedToken = blacklistedTokenRepository.findByToken(refreshToken.getRefreshToken());
        if (blacklistedToken != null) {
            res.setSuccess(false);
            res.setMessage("Token already blacklisted");
            return res;
        } else {
            // blacklist token
            blacklistedTokenRepository.save(new BlacklistedToken(refreshToken.getRefreshToken()));
        }

        try {
            JwtPayload payload = new JwtPayload();
            // Set payload data here
            // setting from system config
            // as we want this to be latest
            payload.setSystemUserEmail(conf.getSystemUserEmail());
            payload.setSystemUserName(conf.getSystemUserName());
            payload.setProvider(refreshToken.getProvider());
            Tokens tokens = jwtService.generate(payload);

            res.setTokens(tokens);
            res.setSuccess(true);
            res.setMessage("Token Refreshed Generated");

        } catch (Exception e) {
            res.setMessage(e.getMessage());
        }
        return res;
    }

    public CommonResponseDto invalidateToken(InvalidateTokenRequestDto token) {

        CommonResponseDto res = new CommonResponseDto();
        BlacklistedToken blacklistedToken = blacklistedTokenRepository.findByToken(token.getRefreshToken());

        if (blacklistedToken != null) {
            res.setSuccess(false);
            res.setMessage("Token already blacklisted");
            return res;
        } else {
            // blacklist token
            blacklistedTokenRepository.save(new BlacklistedToken(token.getRefreshToken()));
            res.setSuccess(true);
            res.setMessage("Token Blacklisted Successfully");
        }
        return res;
    }


}
