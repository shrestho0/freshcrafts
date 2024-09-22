package fresh.crafts.redwiz.entities;

import fresh.crafts.redwiz.enums.KEventCommandsRedWiz;
import lombok.Data;

@Data
public class KEventRedWizPayload implements KEventPayloadInterface {

    private String dbModelId; // primary db saved id
    private KEventCommandsRedWiz command;

    private String username;
    private String password;
    private String dbPrefix;

}
