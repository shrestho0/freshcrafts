package fresh.crafts.depwiz.utils;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fresh.crafts.depwiz.entities.KEvent;
import fresh.crafts.depwiz.entities.KEventDepWizardPayload;
import fresh.crafts.depwiz.enums.DepWizKEventCommands;
import fresh.crafts.depwiz.enums.KEventProducers;

public class CraftUtils {

    // singleton
    private CraftUtils() {
    }

    public static void throwIfRequiredValuesAreNull(HashMap<String, Object> notNullables) throws Exception {
        for (String key : notNullables.keySet()) {
            if (notNullables.get(key) == null) {
                throw new Exception("Error: " + key + " must not be null");
            }
        }
    }

    public static void throwIfFalseOrNull(Boolean condition, String message) throws Exception {
        if (condition == null || !condition) {
            throw new Exception(message);
        }
    }

    public static void jsonLikePrint(Object obj) {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().setPrettyPrinting()
                .create();
        System.err.println("\n" + gson.toJson(obj) + "\n");
    }

    public static KEvent generateFeedbackKEvent(KEvent event, DepWizKEventCommands command) {
        KEvent feedbackKEvent = new KEvent();

        feedbackKEvent.setEventDestination(KEventProducers.ENGINE);
        feedbackKEvent.setEventSource(KEventProducers.DEPWIZ);

        // KEventFeedbackPayload feedbackPayload = new KEventFeedbackPayload();
        KEventDepWizardPayload feedbackPayload = new KEventDepWizardPayload();
        feedbackPayload.setCommand(command);
        // setting request<-event id
        feedbackPayload.setRequestEventId(event.getId());

        feedbackKEvent.setPayload(feedbackPayload);

        return feedbackKEvent;
    }

}
