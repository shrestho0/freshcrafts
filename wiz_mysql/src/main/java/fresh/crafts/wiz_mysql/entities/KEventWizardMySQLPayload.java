package fresh.crafts.wiz_mysql.entities;

import fresh.crafts.wiz_mysql.utils.enums.KEventCommandsWizardMySQL;
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
