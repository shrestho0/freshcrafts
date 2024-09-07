package fresh.crafts.depwiz.entities;

import fresh.crafts.depwiz.enums.ProjectDeploymentStatus;
import lombok.Data;

@Data
public class ProjectDeployment {

    String id;

    String projectId;

    Integer version;
    ProjectDeploymentStatus status;

    Boolean isDeployed;

    // String partialDeploymentMsg;
    String errorTraceback;

    // save other necceassary fields
    // for:
    // deployment files might be lost,
    // github commit sha might help
    // String githubCommit;

    ProjectDeploymentFile rawFile;
    ProjectDeploymentFile envFile;
    ProjectDeploymentCommands depCommands;
    ProjectDeploymentProdFiles prodFiles;

    ProjectDeploymentSource src;

}
