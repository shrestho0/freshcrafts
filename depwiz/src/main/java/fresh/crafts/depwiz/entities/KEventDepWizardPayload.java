package fresh.crafts.depwiz.entities;

import fresh.crafts.depwiz.enums.DepWizKEventCommands;
import lombok.Data;

@Data
public class KEventDepWizardPayload implements KEventPayloadInterface {

    private DepWizKEventCommands command;

    private Project project;
    private ProjectDeployment deployment;
    private ProjectDeployment deployment2;
    // more will be added later
}
