package fresh.crafts.wiz_mongo.entities;

import fresh.crafts.wiz_mongo.utils.enums.KEventCommandsWizardMongo;
import lombok.Data;

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
