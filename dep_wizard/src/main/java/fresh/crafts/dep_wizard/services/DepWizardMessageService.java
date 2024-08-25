package fresh.crafts.dep_wizard.services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.dep_wizard.config.MessageProducer;
import fresh.crafts.dep_wizard.entities.CommandServiceResult;
import fresh.crafts.dep_wizard.entities.KEventFeedbackPayload;
import fresh.crafts.dep_wizard.models.KEvent;
import fresh.crafts.dep_wizard.utils.enums.KEventProducers;

@Service
public class DepWizardMessageService {

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private CommandService commandService;

    public String sayHello(KEvent kEvent, KEvent feedbacKEvent) {

        CommandServiceResult cr = commandService.lsDir("/home/");
        System.err.println("[DEBUG] CommandServiceResult: " + cr);

        feedbacKEvent.setEventSource(KEventProducers.DEP_WIZARD);
        feedbacKEvent.setEventDestination(KEventProducers.ENGINE);
        KEventFeedbackPayload payload = (KEventFeedbackPayload) feedbacKEvent.getPayload();
        HashMap<String, Object> payloadData = new HashMap<>();
        payloadData.put("message", "Hello, World!");
        payload.setData(payloadData);

        feedbacKEvent.setPayload(payload);
        messageProducer.sendEvent(feedbacKEvent);

        return "Hello, World!";
    }

}
