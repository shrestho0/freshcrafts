package fresh.crafts.depwiz.controllers;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fresh.crafts.depwiz.entities.KEvent;
import fresh.crafts.depwiz.entities.KEventDepWizardPayload;
import fresh.crafts.depwiz.services.DepwizMessageService;
import fresh.crafts.depwiz.utils.CraftUtils;
import fresh.crafts.depwiz.enums.DepWizKEventCommands;

@Controller
public class DepwizMessageController {

    @Autowired
    private DepwizMessageService service;

    public void handleStuff(KEvent kEvent) {
        System.err.println("[DEBUG] DepWizardMessageController: Handling stuff");
        System.err.println("[DEBUG] Event Received");
        CraftUtils.jsonLikePrint(kEvent);
        System.err.println("[DEBUG] Event Received Finished");

        if (service == null) {
            System.err.println("[DEBUG] Error: DepwizMessageService is null");
            return;
        }

        if (kEvent.getPayload() == null) {
            System.err.println("[DEBUG] Error: Payload is null");
            return;
        }

        HashMap<DepWizKEventCommands, DepWizKEventCommands> eventFeedbackMap = new HashMap<>();

        eventFeedbackMap.put(DepWizKEventCommands.DEPLOY, DepWizKEventCommands.FEEDBACK_DEPLOYMENT);
        eventFeedbackMap.put(DepWizKEventCommands.RE_DEPLOY, DepWizKEventCommands.FEEDBACK_RE_DEPLOYMENT);
        eventFeedbackMap.put(DepWizKEventCommands.UPDATE_DEPLOYMENT, DepWizKEventCommands.FEEDBACK_UPDATE_DEPLOYMENT);
        eventFeedbackMap.put(DepWizKEventCommands.DELETE_DEPLOYMENTS, DepWizKEventCommands.FEEDBACK_DELETE_DEPLOYMENTS);
        eventFeedbackMap.put(DepWizKEventCommands.ROLLBACK, DepWizKEventCommands.FEEDBACK_ROLLBACK);
        eventFeedbackMap.put(DepWizKEventCommands.ROLLFORWARD, DepWizKEventCommands.FEEDBACK_ROLLFORWARD);
        eventFeedbackMap.put(DepWizKEventCommands.MODIFY_DOMAINS, DepWizKEventCommands.FEEDBACK_MODIFY_DOMAINS);

        // add more when needed

        KEventDepWizardPayload kEventPayload = (KEventDepWizardPayload) kEvent.getPayload();

        KEvent feedbackKEvent = CraftUtils.generateFeedbackKEvent(kEvent,
                eventFeedbackMap.get(kEventPayload.getCommand()));

        KEventDepWizardPayload feedbackPayload = (KEventDepWizardPayload) feedbackKEvent.getPayload();

        // setting up common stuff
        feedbackPayload.setProject(kEventPayload.getProject());
        feedbackPayload.setCurrentDeployment(kEventPayload.getCurrentDeployment());

        DepWizKEventCommands cmd = kEventPayload.getCommand();

        ArrayList<DepWizKEventCommands> redeps = new ArrayList<>() {
            {
                add(DepWizKEventCommands.RE_DEPLOY);
                add(DepWizKEventCommands.UPDATE_DEPLOYMENT);
                add(DepWizKEventCommands.ROLLBACK);
                add(DepWizKEventCommands.ROLLFORWARD);
            }
        };

        if (cmd == DepWizKEventCommands.DEPLOY) {
            // first deploy, for now
            System.err.println("[DEBUG] First deploying");
            service.firstDeploy(kEvent, feedbackKEvent);
        } else if (redeps.contains(cmd)) {
            System.err.println("[DEBUG] Re-deploying");
            // re-deploy failed deployment
            service.reDeploy(kEvent, feedbackKEvent);
        } else if (cmd == DepWizKEventCommands.DELETE_DEPLOYMENTS) {
            // delete deployment
            System.err.println("[DEBUG] Deleting deployments");
            service.deleteDeployments(kEvent, feedbackKEvent);
        } else if (cmd == DepWizKEventCommands.MODIFY_DOMAINS) {
            // modify domains
            System.err.println("[DEBUG] Modifying domains");
            service.modifyDomains(kEvent, feedbackKEvent);
        } else {
            // invalid command
            System.err.println("[DEBUG] Error: Invalid command");
        }

    }

}
