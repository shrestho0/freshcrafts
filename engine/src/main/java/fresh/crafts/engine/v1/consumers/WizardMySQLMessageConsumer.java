package fresh.crafts.engine.v1.consumers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fresh.crafts.engine.v1.controllers.WizardMySQLMessageController;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.services.WizardMySQLMessageService;

/*
 * WizarMySQLMessageConsumer, WIZARD_MYSQL
 * This is temporary, will be transfered to it's own service
 * Doing such due to no internet connection
 */
@Component
public class WizardMySQLMessageConsumer {

    // @Autowired
    // WizardMySQLMessageService service;

    @Autowired
    WizardMySQLMessageController controller;

    @KafkaListener(topics = "WIZARD_MYSQL", groupId = "freshCrafts")
    public void listen(String message) {
        System.err.println("[DEBUG] Received message from WIZARD_MYSQL: " + message);
        System.err.println("[DEBUG] Controller autowired: " + controller);

        KEvent kEvent = KEvent.fromJson(message);

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

    }

}