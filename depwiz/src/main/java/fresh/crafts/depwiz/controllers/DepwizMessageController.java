package fresh.crafts.depwiz.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fresh.crafts.depwiz.entities.KEvent;
import fresh.crafts.depwiz.entities.KEventDepWizardPayload;
import fresh.crafts.depwiz.services.DepWizardMessageService;
import fresh.crafts.depwiz.utils.CraftUtils;
import fresh.crafts.depwiz.enums.DepWizKEventCommands;

@Controller
public class DepwizMessageController {

    @Autowired
    private DepWizardMessageService service;

    public void handleStuff(KEvent kEvent) {
        System.err.println("[DEBUG] DepWizardMessageController: Handling stuff");
        System.err.println("[DEBUG] Handling event: " + kEvent);

        if (service == null) {
            System.err.println("[DEBUG] Error: DepWizardMessageService is null");
            return;
        }

        if (kEvent.getPayload() == null) {
            System.err.println("[DEBUG] Error: Payload is null");
            return;
        }

        HashMap<DepWizKEventCommands, DepWizKEventCommands> eventFeedbackMap = new HashMap<>();

        eventFeedbackMap.put(DepWizKEventCommands.DEPLOY, DepWizKEventCommands.FEEDBACK_DEPLOYMENT);
        eventFeedbackMap.put(DepWizKEventCommands.RE_DEPLOY, DepWizKEventCommands.FEEDBACK_RE_DEPLOYMENT);
        eventFeedbackMap.put(DepWizKEventCommands.DELETE_DEPLOYMENTS, DepWizKEventCommands.FEEDBACK_DELETE_DEPLOYMENTS);
        // add more when needed

        KEventDepWizardPayload kEventPayload = (KEventDepWizardPayload) kEvent.getPayload();

        KEvent feedbackKEvent = CraftUtils.generateFeedbackKEvent(kEvent,
                eventFeedbackMap.get(kEventPayload.getCommand()));

        if (kEventPayload.getCommand() == DepWizKEventCommands.DEPLOY) {
            // first deploy, for now
            service.firstDeploy(kEvent, feedbackKEvent);
        } else if (kEventPayload.getCommand() == DepWizKEventCommands.RE_DEPLOY) {
            // re-deploy failed deployment
            service.reDeploy(kEvent, feedbackKEvent);
        } else if (kEventPayload.getCommand() == DepWizKEventCommands.DELETE_DEPLOYMENTS) {
            // delete deployment
            service.deleteDeployments(kEvent, feedbackKEvent);
        } else {
            // do nothing
            // invalid command
            System.err.println("[DEBUG] Error: Invalid command");
        }

        // service.sayHello(kEvent, feedbackKEvent);

    }

}
