package fresh.crafts.depwiz.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fresh.crafts.depwiz.entities.KEvent;
import fresh.crafts.depwiz.entities.KEventDepWizardPayload;
import fresh.crafts.depwiz.entities.KEventFeedbackPayload;
import fresh.crafts.depwiz.services.DepWizardMessageService;
import fresh.crafts.depwiz.utils.CraftUtils;
import fresh.crafts.depwiz.enums.DepWizKEventCommands;
import fresh.crafts.depwiz.entities.KEventFeedbackPayload;
import fresh.crafts.depwiz.services.DepWizardMessageService;
import fresh.crafts.depwiz.enums.KEventProducers;

@Controller
public class DepwizMessageController {

    @Autowired
    private DepWizardMessageService service;

    public void handleStuff(KEvent kEvent) {
        System.err.println("[DEBUG] DepWizardMessageController: Handling stuff");
        System.err.println("[DEBUG] Handling event: " + kEvent);

        KEvent feedbackKEvent = CraftUtils.generateFeedbackKEvent(kEvent);
        if (service == null) {
            System.err.println("[DEBUG] Error: DepWizardMessageService is null");
            return;
        }

        if (kEvent.getPayload() == null) {
            System.err.println("[DEBUG] Error: Payload is null");
            return;
        }

        KEventDepWizardPayload kEventPayload = (KEventDepWizardPayload) kEvent.getPayload();

        if (kEventPayload.getCommand() == DepWizKEventCommands.DEPLOY) {
            // first deploy, for now
            service.firstDeploy(kEvent, feedbackKEvent);
        } else if (kEventPayload.getCommand() == DepWizKEventCommands.RE_DEPLOY) {
            // delete deployment
        } else {
            // do nothing
            // invalid command
        }

        // service.sayHello(kEvent, feedbackKEvent);

    }

}
