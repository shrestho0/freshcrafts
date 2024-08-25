package fresh.crafts.engine.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.services.EngineMessageService;
import fresh.crafts.engine.v1.utils.enums.KEventProducers;

@Controller
public class EngineMessageController {

    @Autowired
    private EngineMessageService engineMessageService;

    public void handleStuff(KEvent kEvent) {
        System.err.println("[DEBUG]: Engine Message Controller");
        System.err.println("[DEBUG]: engineMessageService: " + engineMessageService);
        System.err.println("[DEBUG]: KEvent: " + kEvent);

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
        } else if (kEvent.getEventSource() == KEventProducers.DEP_WIZARD) {
            //
        } else {
            // invalid event source
            System.err.println("[DEBUG]: Invalid event source");
        }
    }
}
