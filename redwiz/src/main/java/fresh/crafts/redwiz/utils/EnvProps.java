package fresh.crafts.redwiz.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class EnvProps {

    @Value("${REDIS_HOST}")
    private String redisHost;

    @Value("${REDIS_PORT}")
    private String redisPort;

    @Value("${REDIS_PASSWORD}")
    private String redisPassword;

}