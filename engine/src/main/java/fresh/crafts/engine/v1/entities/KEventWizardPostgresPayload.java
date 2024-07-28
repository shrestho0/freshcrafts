package fresh.crafts.engine.v1.entities;

import lombok.Data;
import fresh.crafts.engine.v1.utils.enums.KEventCommandsWizardPostgres;

@Data
public class KEventWizardPostgresPayload implements KEventPayloadInterface {

    private String dbModelId; // primary db saved id
    private KEventCommandsWizardPostgres command;
    private String dbName;
    private String dbUser;
    private String dbPassword;

    private String newDBName; // for update
    private String newDBUser; // for update
    private String newUserPassword; // for update
}
