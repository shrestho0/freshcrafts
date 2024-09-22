package fresh.crafts.engine.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fresh.crafts.engine.v1.entities.KEventPayloadDepWiz;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.services.EngineDepwizMessageService;
import fresh.crafts.engine.v1.services.EngineMessageService;
import fresh.crafts.engine.v1.utils.CraftUtils;
import fresh.crafts.engine.v1.utils.enums.KEventCommandsDepWiz;
import fresh.crafts.engine.v1.utils.enums.KEventProducers;

@Controller
public class EngineMessageController {

    @Autowired
    private EngineMessageService engineMessageService;

    @Autowired
    private EngineDepwizMessageService engineDepwizMessageService;

    public void handleStuff(KEvent kEvent) {
        System.err.println("[DEBUG]: Engine Message Controller");
        System.err.println("[DEBUG] Event Received");
        CraftUtils.jsonLikePrint(kEvent);
        System.err.println("[DEBUG] Event Received Finished");
        // // save or update event
        // kEventService.createOrUpdate(kevent);

        if (engineMessageService == null) {
            System.err.println("[DEBUG]: Service null");
            return;
        }

        if (kEvent.getPayload() == null) {
            System.err.println("[DEBUG]: Payload null");
            return;
        }

        if (kEvent.getEventSource() == KEventProducers.WIZARD_MYSQL) {
            engineMessageService.serveForWizardMySQL(kEvent);
        } else if (kEvent.getEventSource() == KEventProducers.WIZARD_POSTGRES) {
            engineMessageService.serveForWizardPostgres(kEvent);
        } else if (kEvent.getEventSource() == KEventProducers.WIZARD_MONGO) {
            engineMessageService.serveForWizardMongo(kEvent);
        } else if (kEvent.getEventSource() == KEventProducers.REDWIZ) {
            engineMessageService.serveForRedwiz(kEvent);
        } else if (kEvent.getEventSource() == KEventProducers.DEPWIZ) {

            System.err.println("[DEBUG]: Engine Depwiz Message Service: Payload");
            // for this one, it's won't be common payload rather, the generic kevent itself
            KEventPayloadDepWiz payload = (KEventPayloadDepWiz) kEvent.getPayload();
            CraftUtils.jsonLikePrint(payload);

            if (payload.getCommand() == KEventCommandsDepWiz.FEEDBACK_DEPLOYMENT) {
                engineDepwizMessageService.serveFirstDepFeedback(kEvent, payload);
            } else if (payload.getCommand() == KEventCommandsDepWiz.FEEDBACK_DELETE_DEPLOYMENTS) {
                engineDepwizMessageService.serveDeleteDepsFeedback(kEvent, payload);
            } else if (payload.getCommand() == KEventCommandsDepWiz.FEEDBACK_RE_DEPLOYMENT) {
                engineDepwizMessageService.serveReDeployFeedback(kEvent, payload);
            } else if (payload.getCommand() == KEventCommandsDepWiz.FEEDBACK_UPDATE_DEPLOYMENT) {
                engineDepwizMessageService.serveUpdateDepFeedback(kEvent, payload);
            } else if (payload.getCommand() == KEventCommandsDepWiz.FEEDBACK_ROLLFORWARD) {
                engineDepwizMessageService.serveRollforwardFeedback(kEvent, payload);
            } else if (payload.getCommand() == KEventCommandsDepWiz.FEEDBACK_ROLLBACK) {
                engineDepwizMessageService.serveRollbackFeedback(kEvent, payload);
            } else {
                // invalid command
                System.err.println("[DEBUG]: Invalid command");
            }
        } else {
            // invalid event source
            System.err.println("[DEBUG]: Invalid event source");
        }
    }
}
