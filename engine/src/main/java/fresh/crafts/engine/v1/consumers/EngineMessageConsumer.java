package fresh.crafts.engine.v1.consumers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fresh.crafts.engine.v1.controllers.EngineMessageController;
import fresh.crafts.engine.v1.entities.KEventFeedbackPayload;
import fresh.crafts.engine.v1.models.KEvent;

@Component
public class EngineMessageConsumer {

    @Autowired
    EngineMessageController controller;

    @KafkaListener(topics = "ENGINE", groupId = "freshCrafts")
    public void listen(String message) {

        System.err.println("[DEBUG] Received message from ENGINE topic: " + message);
        System.err.println("[DEBUG] Controller autowired: " + controller);
        try {

            // KEvent kEvent = KEvent.fromJson(message);
            KEvent kEventFeedback = KEvent.fromJson(message, KEventFeedbackPayload.class);

            System.err.println("[DEBUG] Parsed KEvent: " + kEventFeedback);
            // Handle event if all okay
            try {
                controller.handleStuff(kEventFeedback);
            } catch (Exception e) {
                System.err.println("[DEBUG] Error: Exception in handling event: " + e.getMessage());

            }
        } catch (Exception e) {
            System.err.println("[DEBUG] Error: Exception in parsing event: " + e.getMessage());
            // e.printStackTrace();
        }

        // System.err.println("\tParsing Event: " + KEvent.fromJson(message));

    }

}