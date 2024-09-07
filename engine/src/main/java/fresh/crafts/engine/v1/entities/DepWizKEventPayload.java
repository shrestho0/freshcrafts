package fresh.crafts.engine.v1.entities;

import lombok.Data;
import fresh.crafts.engine.v1.models.Project;
import fresh.crafts.engine.v1.models.ProjectDeployment;
import fresh.crafts.engine.v1.utils.enums.DepWizKEventCommands;

import java.util.HashMap;
import java.util.List;

@Data
public class DepWizKEventPayload implements KEventPayloadInterface {

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
