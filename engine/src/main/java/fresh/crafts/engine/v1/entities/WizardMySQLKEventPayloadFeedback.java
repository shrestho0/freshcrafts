package fresh.crafts.engine.v1.entities;

import java.util.HashMap;
import java.util.LinkedHashMap;

import fresh.crafts.engine.v1.utils.enums.WizardMySQLEventCommands;
import lombok.Data;

@Data
public class WizardMySQLKEventPayloadFeedback implements KEventPayloadInterface {
    private WizardMySQLEventCommands command = WizardMySQLEventCommands.FEEDBACK;

    private String requestEventId;
    private Boolean success;
    private String message;
    // private HashMap<String, Object> data;

    @Override
    public String toJson() {
        String json = "{";
        json += "\"requestEventId\"" + ":\"" + this.getRequestEventId() + "\",";
        json += "\"success\"" + ":" + this.getSuccess() + ",";
        json += "\"message\"" + ":" + "\"" + this.getMessage() + "\"";
        json += "}";
        return json;
    }

    public static WizardMySQLKEventPayloadFeedback fromHashMap(LinkedHashMap<String, Object> objPayloadMap) {
        WizardMySQLKEventPayloadFeedback feedback = new WizardMySQLKEventPayloadFeedback();
        if (objPayloadMap.get("requestEventId") != null) {
            feedback.setRequestEventId((String) objPayloadMap.get("requestEventId"));
        }

        if (objPayloadMap.get("success") != null) {
            feedback.setSuccess(((Boolean) objPayloadMap.get("success")));
        }

        if (objPayloadMap.get("message") != null) {
            feedback.setMessage((String) objPayloadMap.get("message"));
            // feedback.setSuccess((String) objPayloadMap.get("message"));
        }

        return feedback;

    }

}
