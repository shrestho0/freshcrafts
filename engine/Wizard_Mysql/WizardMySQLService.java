package fresh.crafts.engine.Wizard_Mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.config.MessageProducer;
import fresh.crafts.engine.v1.entities.WizardMySQLKEventPayloadFeedback;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.utils.enums.KEventProducers;

/**
 * Service for WizardMySQLMessageConsumer
 * This is will be in WizardMySQL Service of FreshCrafts Microservice
 * 
 */
@Service
public class WizardMySQLService {

    // @Autowired has some issue
    // private MessageProducer messageProducer = new MessageProducer();

    // @Autowired
    // private MessageProducer messageProducer;
    private MessageProducer messageProducer = MessageProducer.getInstance();

    public void createUserAndDB(KEvent kEventReceived) {

        System.out.println("createUserAndDB; recieved" + kEventReceived);

        KEvent kEventToSend = defaultKEventToSend(kEventReceived);
        // feedback has the pointer
        WizardMySQLKEventPayloadFeedback feedback = (WizardMySQLKEventPayloadFeedback) kEventToSend.getPayload();
        feedback.setSuccess(true);
        feedback.setMessage("kaj hobe, relax");
        System.out.println("\ncreateUserAndDB; toSend" + kEventToSend + "\n");
        System.out.println("\ncreateUserAndDB; toSend json: " + kEventToSend.toJson() + "class: "
                + kEventToSend.toJson().getClass() + "\n");
        // kEventToSend.setPayload(feedback);
        // System.out.println("createUserAndDB; toSend (no auto ptr)" + kEventToSend);

        // kEventToSend.getPayload().setSuccess(true);
        // kEventToSend.setMessage("kaj hobe, pera nei");
        System.out.println(
                "messageProducer from createUserAndDB: " + messageProducer + " :type: " + messageProducer.getClass());

        messageProducer.sendMessage(kEventToSend.getEventDestination().toString(), "from wizwiz");

        // messageProducer.sendEvent(kEventToSend);

    }

    public void updateDB(KEvent kEventReceived) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateDB'");
    }

    public void updateUserPassword(KEvent kEventReceived) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUserPassword'");
    }

    private KEvent defaultKEventToSend(KEvent kEventReceived) {
        // Event to send
        KEvent kEventToSend = new KEvent();
        kEventToSend.setEventSource(KEventProducers.WIZARD_MYSQL);
        kEventToSend.setEventDestination(KEventProducers.ENGINE);

        // Common Stuff
        WizardMySQLKEventPayloadFeedback feedback = new WizardMySQLKEventPayloadFeedback();
        feedback.setRequestEventId(kEventReceived.getId());
        kEventToSend.setPayload(feedback);

        return kEventToSend;
    }

}
