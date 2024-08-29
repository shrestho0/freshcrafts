package fresh.crafts.engine.v1.entities;

import java.util.HashMap;

import lombok.Data;

@Data
public class KEventFeedbackPayload implements KEventPayloadInterface {
    // that's not even important
    private String command = "FEEDBACK";

    private String requestEventId;
    private Boolean success;
    private Boolean isPartial;

    private String message;
    private HashMap<String, Object> data;

}
