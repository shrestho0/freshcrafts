package fresh.crafts.redwiz.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fresh.crafts.redwiz.entities.KEvent;
import fresh.crafts.redwiz.entities.KEventFeedbackPayload;
import fresh.crafts.redwiz.entities.KEventRedWizPayload;
import fresh.crafts.redwiz.enums.KEventCommandsRedWiz;
import fresh.crafts.redwiz.enums.KEventProducers;
import fresh.crafts.redwiz.services.RedWizMessageService;

@Controller
public class RedWizMessageController {

    @Autowired
    private RedWizMessageService service;

    public void handleStuff(KEvent kEvent) {
        System.err.println("[DEBUG]: WizardMySQLMessageConsumerServic handleStuff");
        System.err.println("[DEBUG]: service: " + service);

        KEvent feedbackKEvent = this.generateFeedbackKEvent(kEvent);
        // System.err.println("[DEBUG]: feedbackKEvent" + feedbackKEvent);

        if (service == null) {
            System.err.println("[DEBUG]: Service null");
            return;
        }

        if (kEvent.getPayload() == null) {
            System.err.println("[DEBUG]: Payload null");
            return;
        }

        KEventRedWizPayload kEventPayload = (KEventRedWizPayload) kEvent.getPayload();
        KEventCommandsRedWiz command = kEventPayload.getCommand();

        if (command.equals(KEventCommandsRedWiz.ALLOW_PREFIX_TO_USER)
                || command.equals(KEventCommandsRedWiz.REVOKE_ACCESS_FROM_USER)
                || command.equals(KEventCommandsRedWiz.UPDATE_USER_PASSWORD)) {
            System.err.println("[DEBUG]: Assign DB to user");
            service.serveStuff(kEvent, feedbackKEvent);
        } else {
            throw new UnsupportedOperationException("Invalid command: " + command);
        }
    }

    public KEvent generateFeedbackKEvent(KEvent event) {
        KEvent feedbackKEvent = new KEvent();

        feedbackKEvent.setEventDestination(KEventProducers.ENGINE);
        feedbackKEvent.setEventSource(KEventProducers.REDWIZ);

        KEventFeedbackPayload feedbackPayload = new KEventFeedbackPayload();

        feedbackPayload.setRequestEventId(event.getId());

        feedbackKEvent.setPayload(feedbackPayload);

        return feedbackKEvent;
    }
}
