package fresh.crafts.engine.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fresh.crafts.engine.v1.entities.WizardMySQLKEventPayload;
import fresh.crafts.engine.v1.entities.WizardMySQLKEventPayloadFeedback;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.services.WizardMySQLMessageService;
import fresh.crafts.engine.v1.utils.enums.KEventProducers;
import fresh.crafts.engine.v1.utils.enums.WizardMySQLEventCommands;

@Controller
public class WizardMySQLMessageController {

    @Autowired
    private WizardMySQLMessageService service;

    public void handleStuff(KEvent kEvent) {
        System.out.println("[DEBUG]: WizardMySQLMessageConsumerServic handleStuff");
        System.out.println("[DEBUG]: service: " + service);

        KEvent feedbackKEvent = this.generateFeedbackKEvent(kEvent);
        // System.out.println("[DEBUG]: feedbackKEvent" + feedbackKEvent);

        if (service == null) {
            System.err.println("[DEBUG]: Service null");
            return;
        }

        if (kEvent.getPayload() == null) {
            System.err.println("[DEBUG]: Payload null");
            return;
        }

        WizardMySQLKEventPayload kEventPayload = (WizardMySQLKEventPayload) kEvent.getPayload();

        if (kEventPayload.getCommand().equals(WizardMySQLEventCommands.CREATE_USER_AND_DB)) {
            service.createUserAndDB(kEvent, feedbackKEvent);
        } else if (kEventPayload.getCommand().equals(WizardMySQLEventCommands.UPDATE_DATABASE_NAME)) {
            service.updateDBName(kEvent, feedbackKEvent);
        } else if (kEventPayload.getCommand().equals(WizardMySQLEventCommands.UPDATE_USER_PASSWORD)) {
            service.updateUserPassword(kEvent, feedbackKEvent);
        }

        // ((WizardMySQLKEventPayloadFeedback)
        // feedbackKEvent.getPayload()).setMessage("the msg");

        // messageProducer.sendMessage("ENGINE", "hello banchot");
        // messageProducer.sendEvent(feedbackKEvent);
    }

    public KEvent generateFeedbackKEvent(KEvent event) {
        KEvent feedbackKEvent = new KEvent();
        feedbackKEvent.setEventDestination(KEventProducers.ENGINE);
        feedbackKEvent.setEventSource(KEventProducers.WIZARD_MYSQL);

        WizardMySQLKEventPayloadFeedback feedbackPayload = new WizardMySQLKEventPayloadFeedback();
        // setting request<-event id
        feedbackPayload.setRequestEventId(event.getId());

        feedbackKEvent.setPayload(feedbackPayload);

        return feedbackKEvent;
    }
}
