package fresh.crafts.wiz_postgres.consumers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fresh.crafts.wiz_postgres.controllers.WizardPostgresMessageController;
import fresh.crafts.wiz_postgres.entities.KEventWizardPostgresPayload;
import fresh.crafts.wiz_postgres.models.KEvent;

/*
 * WizardPostgresMessageConsumer, WIZARD_POSTGRES
 */
@Component
public class WizardPostgresMessageConsumer {

    @Autowired
    WizardPostgresMessageController controller;

    @KafkaListener(topics = "WIZARD_POSTGRES", groupId = "freshCrafts")
    public void listen(String message) {
        System.err.println("[DEBUG] Received message from WIZARD_POSTGRES: " + message);
        System.err.println("[DEBUG] Controller autowired: " + controller);

        try {
            KEvent kEvent = KEvent.fromJson(message, KEventWizardPostgresPayload.class);

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
        }

    }

}