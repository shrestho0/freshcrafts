package fresh.crafts.engine.v1.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import fresh.crafts.engine.v1.entities.JwtPayload;
import fresh.crafts.engine.v1.types.Tokens;
import fresh.crafts.engine.v1.utils.JwtProperties;

@Service
public class JwtService {

    @Autowired
    private JwtProperties jwtProperties;

    public Tokens generate(JwtPayload payload) throws Exception {
        // Tokens tokens = new Tokens(_generateRefreshToken(payload),
        // _generateAccessToken(payload));
        // System.out.println("JWT_ACCESS_SECRET: " + jwtProperties.getAccessSecret());
        // System.out.println("JWT_ACCESS_EXPIRES_IN: " +
        // jwtProperties.getAccessExpiresIn());
        // System.out.println("JWT_REFRESH_SECRET: " +
        // jwtProperties.getRefreshSecret());
        // System.out.println("JWT_REFRESH_EXPIRES_IN: " +
        // jwtProperties.getRefreshExpiresIn());
        Tokens tokens = new Tokens(_generateRefreshToken(payload), _generateAccessToken(payload));
        return tokens;
    }

    private String _generateAccessToken(JwtPayload payload) {

        Algorithm algorithm = Algorithm.HMAC512(jwtProperties.getAccessSecret());

        String token = JWT.create()
                .withIssuer(jwtProperties.getIssuer())
                .withClaim("systemUserName", payload.getSystemUserName())
                .withClaim("systemUserEmail", payload.getSystemUserEmail())
                .withClaim("provider", payload.getProvider().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() +
                        Long.parseLong(jwtProperties.getAccessExpiresIn()) * 1000))
                .withSubject("ACCESS_TOKEN")
                .sign(algorithm);

        return token;
    }

    private String _generateRefreshToken(JwtPayload payload) {

        Algorithm algorithm = Algorithm.HMAC512(jwtProperties.getRefreshSecret());

        String token = JWT.create()
                .withIssuer(jwtProperties.getIssuer())
                .withClaim("systemUserName", payload.getSystemUserName())
                .withClaim("systemUserEmail", payload.getSystemUserEmail())
                .withClaim("provider", payload.getProvider().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() +
                        Long.parseLong(jwtProperties.getRefreshExpiresIn()) * 1000))
                .withSubject("REFRESH_TOKEN")
                .sign(algorithm);

        return token;
    }
}
