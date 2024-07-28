package fresh.crafts.wiz_mysql.entities;

import java.util.HashMap;

import lombok.Data;

@Data
public class KEventFeedbackPayload implements KEventPayloadInterface {
    // that's not even important
    private String command = "FEEDBACK";

    private String requestEventId;
    private Boolean success;
    private String message;
    private HashMap<String, Object> data;

}
