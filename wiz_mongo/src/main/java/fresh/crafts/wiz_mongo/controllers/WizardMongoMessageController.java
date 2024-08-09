package fresh.crafts.wiz_mongo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fresh.crafts.wiz_mongo.entities.KEventFeedbackPayload;
import fresh.crafts.wiz_mongo.entities.KEventWizardMongoPayload;
import fresh.crafts.wiz_mongo.models.KEvent;
import fresh.crafts.wiz_mongo.services.WizardMongoMessageService;
import fresh.crafts.wiz_mongo.utils.enums.KEventCommandsWizardMongo;
import fresh.crafts.wiz_mongo.utils.enums.KEventProducers;

@Controller
public class WizardMongoMessageController {

    @Autowired
    private WizardMongoMessageService service;

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

        KEventWizardMongoPayload kEventPayload = (KEventWizardMongoPayload) kEvent.getPayload();

        if (kEventPayload.getCommand().equals(KEventCommandsWizardMongo.CREATE_USER_AND_DB)) {
            service.createUserAndDB(kEvent, feedbackKEvent);
        } else if (kEventPayload.getCommand().equals(KEventCommandsWizardMongo.DELETE_USER_AND_DB)) {
            service.deleteUserAndDB(kEvent, feedbackKEvent);
        } else if (kEventPayload.getCommand().equals(KEventCommandsWizardMongo.UPDATE_USER_AND_DB)) {
            service.updateUserAndDB(kEvent, feedbackKEvent);
        } else {
            throw new UnsupportedOperationException("Unimplemented method 'handleStuff'");
        }
    }

    public KEvent generateFeedbackKEvent(KEvent event) {
        KEvent feedbackKEvent = new KEvent();

        feedbackKEvent.setEventDestination(KEventProducers.ENGINE);
        feedbackKEvent.setEventSource(KEventProducers.WIZARD_MONGO);

        KEventFeedbackPayload feedbackPayload = new KEventFeedbackPayload();
        // setting request<-event id
        feedbackPayload.setRequestEventId(event.getId());

        feedbackKEvent.setPayload(feedbackPayload);

        return feedbackKEvent;
    }
}
