package fresh.crafts.engine.v1.entities;

import fresh.crafts.engine.v1.utils.enums.KEventCommandsRedWiz;
import lombok.Data;

@Data
public class KEventPayloadRedWiz implements KEventPayloadInterface {

    private String dbModelId; // primary db saved id
    private KEventCommandsRedWiz command;

    private String username;
    private String password;
    private String dbPrefix;

}
