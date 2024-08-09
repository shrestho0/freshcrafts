package fresh.crafts.wiz_mongo.consumers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fresh.crafts.wiz_mongo.controllers.WizardMongoMessageController;
import fresh.crafts.wiz_mongo.entities.KEventWizardMongoPayload;
import fresh.crafts.wiz_mongo.models.KEvent;

@Component
public class WizardMongoMessageConsumer {

    @Autowired
    WizardMongoMessageController controller;

    @KafkaListener(topics = "WIZARD_MONGO", groupId = "freshCrafts")
    public void listen(String message) {
        System.err.println("[DEBUG] Received message from WIZARD_MONGO: " + message);
        System.err.println("[DEBUG] Controller autowired: " + controller);

        try {
            KEvent kEvent = KEvent.fromJson(message, KEventWizardMongoPayload.class);

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