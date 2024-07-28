package fresh.crafts.wiz_mysql.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fresh.crafts.wiz_mysql.entities.KEventFeedbackPayload;
import fresh.crafts.wiz_mysql.entities.KEventWizardMySQLPayload;
import fresh.crafts.wiz_mysql.models.KEvent;
import fresh.crafts.wiz_mysql.services.WizardMySQLMessageService;
import fresh.crafts.wiz_mysql.utils.enums.KEventCommandsWizardMySQL;
import fresh.crafts.wiz_mysql.utils.enums.KEventProducers;

@Controller
public class WizardMySQLMessageController {

    @Autowired
    private WizardMySQLMessageService service;

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

        KEventWizardMySQLPayload kEventPayload = (KEventWizardMySQLPayload) kEvent.getPayload();

        if (kEventPayload.getCommand().equals(KEventCommandsWizardMySQL.CREATE_USER_AND_DB)) {
            service.createUserAndDB(kEvent, feedbackKEvent);
        } else if (kEventPayload.getCommand().equals(KEventCommandsWizardMySQL.DELETE_USER_AND_DB)) {
            service.deleteUserAndDB(kEvent, feedbackKEvent);
        } else if (kEventPayload.getCommand().equals(KEventCommandsWizardMySQL.UPDATE_USER_AND_DB)) {
            service.updateUserAndDB(kEvent, feedbackKEvent);
        } else {
            throw new UnsupportedOperationException("Unimplemented method 'handleStuff'");
        }
    }

    public KEvent generateFeedbackKEvent(KEvent event) {
        KEvent feedbackKEvent = new KEvent();

        feedbackKEvent.setEventDestination(KEventProducers.ENGINE);
        feedbackKEvent.setEventSource(KEventProducers.WIZARD_MYSQL);

        KEventFeedbackPayload feedbackPayload = new KEventFeedbackPayload();
        // setting request<-event id
        feedbackPayload.setRequestEventId(event.getId());

        feedbackKEvent.setPayload(feedbackPayload);

        return feedbackKEvent;
    }
}
