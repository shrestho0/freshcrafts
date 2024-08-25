package fresh.crafts.dep_wizard.consumers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fresh.crafts.dep_wizard.controllers.DepWizardMessageController;
import fresh.crafts.dep_wizard.entities.KEventCommandsDepWizard;
import fresh.crafts.dep_wizard.models.KEvent;

@Component
public class DepWizardMessageConsumer {

    @Autowired
    DepWizardMessageController controller;

    @KafkaListener(topics = "DEP_WIZARD", groupId = "freshCrafts")
    public void listen(String message) {
        System.err.println("[DEBUG] Received message from DEP_WIZARD: " + message);
        System.err.println("[DEBUG] Controller autowired: " + controller);

        try {
            KEvent kEvent = KEvent.fromJson(message, KEventCommandsDepWizard.class);

            System.err.println("[DEBUG] Parsed KEvent: " + kEvent);

            if (kEvent == null || kEvent.getId() == null) {
                System.err.println("[DEBUG] Error: KEvent is null or id invalid. kEvent: " + kEvent);
                return;
            }

            if (controller == null) {
                System.err.println("[DEBUG] Error: DEP_WIZARDMessageController is null[\0m]");
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