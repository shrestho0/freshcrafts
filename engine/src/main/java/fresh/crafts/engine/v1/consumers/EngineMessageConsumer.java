package fresh.crafts.engine.v1.consumers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EngineMessageConsumer {

    @KafkaListener(topics = "engine", groupId = "engine")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }

    private HashMap<String, Object> _processStringToMap(String message) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        // Parse JSON from String

        return map;
    }

}