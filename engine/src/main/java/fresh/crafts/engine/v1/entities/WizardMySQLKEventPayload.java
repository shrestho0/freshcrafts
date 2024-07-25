package fresh.crafts.engine.v1.entities;

import java.util.LinkedHashMap;

import fresh.crafts.engine.v1.utils.enums.WizardMySQLEventCommands;
import lombok.Data;

@Data
public class WizardMySQLKEventPayload implements KEventPayloadInterface {

    private String dbModelId; // primary db saved id
    private WizardMySQLEventCommands command;
    private String dbName;
    private String dbUser;
    private String dbPassword;

    @Override
    public String toJson() {
        String json = "{";
        json += "\"dbModelId\":\"" + this.dbModelId + "\"" + ",";
        json += "\"command\":\"" + this.command.toString() + "\"" + ",";
        json += "\"dbName\":\"" + this.dbName + "\"" + ",";
        json += "\"dbUser\":\"" + this.dbUser + "\"" + ",";
        json += "\"dbPassword\":\"" + this.dbPassword + "\"";

        json += "}";

        return json;
    }

    // @Override
    // public HashMap<String, Object> toHashMap() {
    // HashMap<String, Object> hashMap = new HashMap<>();
    // // hashMap.computeIfAbsent(dbModelId, null)
    // hashMap.put("dbModelId", this.dbModelId);
    // hashMap.put("command", this.command);
    // hashMap.put("dbName", dbName);
    // hashMap.put("dbUser", dbUser);
    // hashMap.put("dbPassword", dbPassword);

    // return hashMap;
    // }

    public static WizardMySQLKEventPayload fromHashMap(Object object) {
        if (object == null)
            return null;
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> objMap = (LinkedHashMap<String, Object>) object;

        WizardMySQLKEventPayload payload = new WizardMySQLKEventPayload();

        if (objMap.get("dbModelId") != null) {
            payload.setDbModelId((String) objMap.get("dbModelId"));
        }
        if (objMap.get("command") != null) {
            payload.setCommand(WizardMySQLEventCommands.valueOf((String) objMap.get("command")));
        }

        if (objMap.get("dbName") != null) {
            payload.setDbName((String) objMap.get("dbName"));
        }
        if (objMap.get("dbUser") != null) {
            payload.setDbUser((String) objMap.get("dbUser"));
        }

        if (objMap.get("dbPassword") != null) {
            payload.setDbPassword((String) objMap.get("dbPassword"));
        }

        return payload;
    }

}
