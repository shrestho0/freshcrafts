package fresh.crafts.engine.Wizard_Mysql;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fresh.crafts.engine.v1.entities.WizardMySQLKEventPayload;
import fresh.crafts.engine.v1.models.KEvent;

/**
 * @Deprecated New file on consumers/WizarMySQLMessageConsumer
 * 
 */

@Component
public class WizardMySQLMessageConsumer {

    private WizardMySQLController controller = new WizardMySQLController();

    @KafkaListener(topics = "WIZARD_MYSQL", groupId = "freshCrafts")
    public void listen(String message) {
        System.out.println("Received message from WIZARD_MYSQL: " + message);

        KEvent kEvent = KEvent.fromJson(message);
        if (kEvent == null) {
            System.err.println("============= WizardMySQLMessageConsumer kEvent null =============");
            return;
        }

        if (controller == null) {
            System.err.println("============= WizardMySQLMessageConsumer controller null =============");
            return;
        }

        System.out.println("Trying to parse json: " + kEvent + " controller: " + controller);
        try {
            controller.handleStuff(kEvent);
        } catch (Exception e) {

            System.out.println("error while using controller");
            System.out.println(e.getMessage());
        }

    }

}
