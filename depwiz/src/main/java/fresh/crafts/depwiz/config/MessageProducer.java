package fresh.crafts.depwiz.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import fresh.crafts.depwiz.entities.KEvent;
import fresh.crafts.depwiz.utils.CraftUtils;

@Component
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String msg) {
        if (topic == null || msg == null) {
            System.err.println("[DEBUG]: MessageProducer topic and msg are required to send event.");
            return;
        }
        kafkaTemplate.send(topic, msg);
    }

    public void sendEvent(KEvent event) {
        System.out.println("---------- Sending KEvent to " + event.getEventDestination() + " ----------");
        // CraftUtils.jsonLikePrint(event);
        kafkaTemplate.send(event.getEventDestination().toString(), event.toJson());
        System.out.println("------------ KEvent Sent ------------");
    }

}