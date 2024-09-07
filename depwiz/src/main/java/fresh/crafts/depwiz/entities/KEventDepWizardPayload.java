package fresh.crafts.depwiz.entities;

import java.util.HashMap;
import java.util.List;

import fresh.crafts.depwiz.enums.DepWizKEventCommands;
import lombok.Data;

@Data
public class KEventDepWizardPayload implements KEventPayloadInterface {

    private DepWizKEventCommands command;

    private Project project;
    private ProjectDeployment deployment;
    private ProjectDeployment deployment2; // maybe for update, check later
    private List<ProjectDeployment> deploymentList;

    // For feedback
    private String message;
    private Boolean success;
    private Boolean isPartial;
    private String requestEventId;
    HashMap<String, Object> data;

}
