package fresh.crafts.engine.Wizard_Mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fresh.crafts.engine.v1.entities.WizardMySQLKEventPayload;
import fresh.crafts.engine.v1.entities.WizardMySQLKEventPayloadFeedback;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.utils.enums.KEventProducers;
import fresh.crafts.engine.v1.utils.enums.WizardMySQLEventCommands;

/**
 * WizardMySQLController, WIZARD_MYSQL
 * Controller for the consumer
 * This is temporary, will be transfered to it's own service
 * 
 */

@Controller
public class WizardMySQLController {

    // @Autowired has some issue here
    private WizardMySQLService wizardMySQLService = new WizardMySQLService();

    public void handleStuff(KEvent kEventReceived) {

        // Checking event received
        if (kEventReceived.getPayload() == null) {
            System.out.println("Request event payload is null. Terminating operation.");
            return;
        }

        // Casting payload to required type to use
        WizardMySQLKEventPayload payload = (WizardMySQLKEventPayload) kEventReceived.getPayload();

        if (payload.getCommand().equals(WizardMySQLEventCommands.CREATE_USER_AND_DB)) {
            wizardMySQLService.createUserAndDB(kEventReceived);
        } else if (payload.getCommand().equals(WizardMySQLEventCommands.UPDATE_DATABASE_NAME)) {
            wizardMySQLService.updateDB(kEventReceived);
        } else if (payload.getCommand().equals(WizardMySQLEventCommands.UPDATE_USER_PASSWORD)) {
            wizardMySQLService.updateUserPassword(kEventReceived);

        } else {
            //
        }

        // messageProducer.sendEvent(kEvent);

    }

}
