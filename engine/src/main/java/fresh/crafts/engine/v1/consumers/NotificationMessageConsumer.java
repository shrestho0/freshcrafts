package fresh.crafts.engine.v1.consumers;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationMessageConsumer {

    @KafkaListener(topics = "notification", groupId = "engine")
    public void listen(String message) {

        System.out.println("Received message from notification: " + message);
    }

}
