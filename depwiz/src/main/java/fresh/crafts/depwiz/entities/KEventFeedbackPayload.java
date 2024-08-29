package fresh.crafts.depwiz.entities;

import java.util.HashMap;

import lombok.Data;

@Data
public class KEventFeedbackPayload implements KEventPayloadInterface {
    // that's not even important
    private String command = "FEEDBACK";

    private String requestEventId;

    private Boolean isPartial = false;

    private Boolean success;
    private String message;
    private HashMap<String, Object> data;

}
