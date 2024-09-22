package fresh.crafts.depwiz.consumers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fresh.crafts.depwiz.controllers.DepwizMessageController;
import fresh.crafts.depwiz.entities.KEvent;
import fresh.crafts.depwiz.entities.KEventDepWizardPayload;

@Component
public class DepwizMessageConsumer {

    @Autowired
    DepwizMessageController controller;

    @KafkaListener(topics = "DEPWIZ", groupId = "freshCrafts")
    public void listen(String message) {
        // System.err.println("[DEBUG] Received message from DEP_WIZ: " + message);
        // System.err.println("[DEBUG] Controller autowired: " + controller);

        try {

            KEvent kEvent = KEvent.fromJson(message, KEventDepWizardPayload.class);

            // System.err.println("[DEBUG] Parsed KEvent: " + kEvent);

            Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create();
            // System.err.println("[DEBUG] Parsed KEvent: " + gson.toJson(kEvent));
            System.out.println("[DEBUG] Event received: from" + kEvent.getEventSource());

            if (kEvent == null || kEvent.getId() == null) {
                System.err.println("[DEBUG] Error: KEvent is null or id invalid. kEvent: " +
                        kEvent);
                return;
            }

            if (controller == null) {
                System.err.println("[DEBUG] Error: DEP_WIZ MessageController is null[\0m]");
                return;
            }

            // Handle event if all okay
            controller.handleStuff(kEvent);
        } catch (Exception e) {
            System.err.println("[DEBUG] Error: Exception in parsing event: " + e.getMessage());
            // e.printStackTrace();
        }

    }

}