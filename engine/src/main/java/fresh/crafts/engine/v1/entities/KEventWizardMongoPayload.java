package fresh.crafts.engine.v1.entities;

import lombok.Data;
import fresh.crafts.engine.v1.utils.enums.KEventCommandsWizardMongo;

@Data
public class KEventWizardMongoPayload implements KEventPayloadInterface {

    private String dbModelId; // primary db saved id
    private KEventCommandsWizardMongo command;
    private String dbName;
    private String dbUser;
    private String dbPassword;

    private String newDBName; // for update
    private String newDBUser; // for update
    private String newUserPassword; // for update
}
