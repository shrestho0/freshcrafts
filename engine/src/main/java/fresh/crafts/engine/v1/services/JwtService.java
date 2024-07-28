package fresh.crafts.engine.v1.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import fresh.crafts.engine.v1.entities.JwtPayload;
import fresh.crafts.engine.v1.entities.Tokens;
import fresh.crafts.engine.v1.utils.EnvProps;

@Service
public class JwtService {

    @Autowired
    private EnvProps envProps;

    public Tokens generate(JwtPayload payload) throws Exception {
        // Tokens tokens = new Tokens(_generateRefreshToken(payload),
        // _generateAccessToken(payload));
        // return tokens;
        return new Tokens(
                _generateRefreshToken(payload),
                _generateAccessToken(payload));
    }

    private String _generateAccessToken(JwtPayload payload) {

        Algorithm algorithm = Algorithm.HMAC512(envProps.getAccessSecret());

        String token = JWT.create()
                .withIssuer(envProps.getIssuer())
                .withClaim("systemUserName", payload.getSystemUserName())
                .withClaim("systemUserEmail", payload.getSystemUserEmail())
                .withClaim("provider", payload.getProvider().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() +
                        Long.parseLong(envProps.getAccessExpiresIn()) * 1000))
                .withSubject("ACCESS_TOKEN")
                .sign(algorithm);

        return token;
    }

    private String _generateRefreshToken(JwtPayload payload) {

        Algorithm algorithm = Algorithm.HMAC512(envProps.getRefreshSecret());

        String token = JWT.create()
                .withIssuer(envProps.getIssuer())
                .withClaim("systemUserName", payload.getSystemUserName())
                .withClaim("systemUserEmail", payload.getSystemUserEmail())
                .withClaim("provider", payload.getProvider().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() +
                        Long.parseLong(envProps.getRefreshExpiresIn()) * 1000))
                .withSubject("REFRESH_TOKEN")
                .sign(algorithm);

        return token;
    }
}
