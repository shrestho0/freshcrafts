package fresh.crafts.engine.v1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import fresh.crafts.engine.v1.models.KEvent;

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

        // DEBUG
        // System.err.println("\n================== Event before sending: \n" + event +
        // "\n");
        // System.err.println("\n================== Event before sending json:\n" +
        // event.toJson() + " :type "+ event.toJson().getClass());
        // System.err.println("\n");

        System.err.println("[DEBUG]: sending Event from MessageProducer.sendEvent :: " + event);

        kafkaTemplate.send(event.getEventDestination().toString(), event.toJson());
    }

}