package fresh.crafts.engine.v1.entities;

import fresh.crafts.engine.v1.utils.enums.KEventCommandsWizardMySQL;
import lombok.Data;

@Data
public class KEventWizardMySQLPayload implements KEventPayloadInterface {

    private String dbModelId; // primary db saved id
    private KEventCommandsWizardMySQL command;
    private String dbName;
    private String dbUser;
    private String dbPassword;

    private String newDBName; // for update
    private String newDBUser; // for update
    private String newUserPassword; // for update
}
