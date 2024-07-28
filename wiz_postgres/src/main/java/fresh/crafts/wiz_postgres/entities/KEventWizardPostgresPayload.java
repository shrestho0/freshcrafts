package fresh.crafts.wiz_postgres.entities;

import fresh.crafts.wiz_postgres.utils.enums.KEventCommandsWizardPostgres;
import lombok.Data;

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
