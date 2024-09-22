package fresh.crafts.redwiz.consumers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fresh.crafts.redwiz.controllers.RedWizMessageController;
import fresh.crafts.redwiz.entities.KEvent;
import fresh.crafts.redwiz.entities.KEventRedWizPayload;

@Component
public class RedWizMessageConsumer {

    @Autowired
    RedWizMessageController controller;

    @KafkaListener(topics = "REDWIZ", groupId = "freshCrafts")
    public void listen(String message) {
        System.err.println("[DEBUG] Received message from REDWIZ: " + message);
        System.err.println("[DEBUG] Controller autowired: " + controller);

        try {
            KEvent kEvent = KEvent.fromJson(message, KEventRedWizPayload.class);

            System.err.println("[DEBUG] Parsed KEvent: " + kEvent);

            if (kEvent == null || kEvent.getId() == null) {
                System.err.println("[DEBUG] Error: KEvent is null or id invalid. kEvent: " + kEvent);
                return;
            }

            if (controller == null) {
                System.err.println("[DEBUG] Error: WizardMySQLMessageController is null[\0m]");
                return;
            }

            // Handle event if all okay
            controller.handleStuff(kEvent);
        } catch (Exception e) {
            System.err.println("[DEBUG] Error: Exception in parsing event: " + e.getMessage());
            // e.printStackTrace();
        }

    }

}