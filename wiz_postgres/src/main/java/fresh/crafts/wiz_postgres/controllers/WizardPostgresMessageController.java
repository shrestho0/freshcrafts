package fresh.crafts.wiz_postgres.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fresh.crafts.wiz_postgres.entities.KEventFeedbackPayload;
import fresh.crafts.wiz_postgres.entities.KEventWizardPostgresPayload;
import fresh.crafts.wiz_postgres.models.KEvent;
import fresh.crafts.wiz_postgres.services.WizardPostgresMessageService;
import fresh.crafts.wiz_postgres.utils.enums.KEventCommandsWizardPostgres;
import fresh.crafts.wiz_postgres.utils.enums.KEventProducers;

@Controller
public class WizardPostgresMessageController {

    @Autowired
    private WizardPostgresMessageService service;

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

        KEventWizardPostgresPayload kEventPayload = (KEventWizardPostgresPayload) kEvent.getPayload();

        if (kEventPayload.getCommand().equals(KEventCommandsWizardPostgres.CREATE_USER_AND_DB)) {
            service.createUserAndDB(kEvent, feedbackKEvent);
        } else if (kEventPayload.getCommand().equals(KEventCommandsWizardPostgres.DELETE_USER_AND_DB)) {
            service.deleteUserAndDB(kEvent, feedbackKEvent);
        } else if (kEventPayload.getCommand().equals(KEventCommandsWizardPostgres.UPDATE_USER_AND_DB)) {
            service.updateUserAndDB(kEvent, feedbackKEvent);
        } else {
            throw new UnsupportedOperationException("Unimplemented method 'handleStuff'");
        }
    }

    public KEvent generateFeedbackKEvent(KEvent event) {
        KEvent feedbackKEvent = new KEvent();

        feedbackKEvent.setEventDestination(KEventProducers.ENGINE);
        feedbackKEvent.setEventSource(KEventProducers.WIZARD_POSTGRES);

        KEventFeedbackPayload feedbackPayload = new KEventFeedbackPayload();
        // setting request<-event id
        feedbackPayload.setRequestEventId(event.getId());

        feedbackKEvent.setPayload(feedbackPayload);

        return feedbackKEvent;
    }
}
