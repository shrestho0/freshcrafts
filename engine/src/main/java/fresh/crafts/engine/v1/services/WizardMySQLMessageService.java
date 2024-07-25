package fresh.crafts.engine.v1.services;

import fresh.crafts.engine.v1.config.MessageProducer;
import fresh.crafts.engine.v1.entities.WizardMySQLKEventPayloadFeedback;
import fresh.crafts.engine.v1.models.KEvent;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WizardMySQLMessageService {

    @Autowired
    private MessageProducer messageProducer;

    public void createUserAndDB(KEvent kEvent, KEvent feedbackKEvent) {

        System.err.println("[DEBUG]: WizardMySQLMessageService");
        System.err.println("[DEBUG]: createUserAndDB; messageProducer" + messageProducer);
        System.err.println("[DEBUG]: \n kEvent::" + kEvent + "\n feedbackKEvent::" + feedbackKEvent + "\n");
        ((WizardMySQLKEventPayloadFeedback) feedbackKEvent.getPayload()).setSuccess(false);
        ((WizardMySQLKEventPayloadFeedback) feedbackKEvent.getPayload()).setMessage("hehe");

        // TODO: Create User And DB from payload data here

        messageProducer.sendEvent(feedbackKEvent);

    }

    public void updateDBName(KEvent kEvent, KEvent feedbackKEvent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateDBName'");
    }

    public void updateUserPassword(KEvent kEvent, KEvent feedbackKEvent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUserPassword'");
    }

}
