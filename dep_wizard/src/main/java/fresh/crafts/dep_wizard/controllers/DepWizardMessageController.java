package fresh.crafts.dep_wizard.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fresh.crafts.dep_wizard.entities.KEventCommandsDepWizard;
import fresh.crafts.dep_wizard.entities.KEventDepWizardPayload;
import fresh.crafts.dep_wizard.entities.KEventFeedbackPayload;
import fresh.crafts.dep_wizard.models.KEvent;
import fresh.crafts.dep_wizard.services.DepWizardMessageService;
import fresh.crafts.dep_wizard.utils.enums.KEventProducers;

@Controller
public class DepWizardMessageController {

    @Autowired
    private DepWizardMessageService service;

    public void handleStuff(KEvent kEvent) {
        System.err.println("[DEBUG] DepWizardMessageController: Handling stuff");
        System.err.println("[DEBUG] Handling event: " + kEvent);

        KEvent feedbackKEvent = this.generateFeedbackKEvent(kEvent);
        if (service == null) {
            System.err.println("[DEBUG] Error: DepWizardMessageService is null");
            return;
        }

        if (kEvent.getPayload() == null) {
            System.err.println("[DEBUG] Error: Payload is null");
            return;
        }

        KEventDepWizardPayload kEventPayload = (KEventDepWizardPayload) kEvent.getPayload();

        service.sayHello(kEvent, feedbackKEvent);

    }

    public KEvent generateFeedbackKEvent(KEvent event) {
        KEvent feedbackKEvent = new KEvent();

        feedbackKEvent.setEventDestination(KEventProducers.ENGINE);
        feedbackKEvent.setEventSource(KEventProducers.DEP_WIZARD);

        KEventFeedbackPayload feedbackPayload = new KEventFeedbackPayload();
        // setting request<-event id
        feedbackPayload.setRequestEventId(event.getId());

        feedbackKEvent.setPayload(feedbackPayload);

        return feedbackKEvent;
    }
}
