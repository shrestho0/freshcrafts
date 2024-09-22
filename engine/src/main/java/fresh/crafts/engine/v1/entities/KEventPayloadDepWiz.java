package fresh.crafts.engine.v1.entities;

import lombok.Data;
import fresh.crafts.engine.v1.models.Project;
import fresh.crafts.engine.v1.models.ProjectDeployment;
import fresh.crafts.engine.v1.utils.enums.KEventCommandsDepWiz;

import java.util.HashMap;
import java.util.List;

@Data
public class KEventPayloadDepWiz implements KEventPayloadInterface {

    private KEventCommandsDepWiz command;

    private Project project;
    // private ProjectDeployment deployment;
    // private ProjectDeployment deployment2; // maybe for update, check later
    private ProjectDeployment currentDeployment;
    private ProjectDeployment activeDeployment; // maybe for update, check later
    private List<ProjectDeployment> deploymentList;

    // For feedback
    private String message;
    private Boolean success;
    private Boolean isPartial;
    private String requestEventId;
    HashMap<String, Object> data;

}
