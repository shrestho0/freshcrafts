package fresh.crafts.engine.v1.consumers;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fresh.crafts.engine.v1.models.KEvent;

@Component
public class EngineMessageConsumer {

    @KafkaListener(topics = "ENGINE", groupId = "freshCrafts")
    public void listen(String message) {

        System.err.println("[DEBUG] Received message from ENGINE: " + message);

        System.out.println("Parsing Event: " + KEvent.fromJson(message));

    }

}