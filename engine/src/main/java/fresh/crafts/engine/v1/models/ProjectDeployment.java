package fresh.crafts.engine.v1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import fresh.crafts.engine.v1.entities.ProjectDeploymentCommands;
import fresh.crafts.engine.v1.entities.ProjectDeploymentFile;
import fresh.crafts.engine.v1.entities.ProjectDeploymentProdFiles;
import fresh.crafts.engine.v1.entities.ProjectDeploymentSource;
import fresh.crafts.engine.v1.utils.UlidGenerator;
import fresh.crafts.engine.v1.utils.enums.ProjectDeploymentStatus;
import lombok.Data;

@Data
@Document(collection = "ProjectDeployments")
public class ProjectDeployment {

    @Id
    String id;

    String projectId;

    Integer version;
    ProjectDeploymentStatus status;

    Boolean isDeployed;

    String errorTraceback;

    // save other necceassary fields
    // for:
    // deployment files might be lost,
    // github commit sha might help
    // String githubCommit;

    ProjectDeploymentFile rawFile;
    ProjectDeploymentFile envFile;
    ProjectDeploymentCommands depCommands;

    ProjectDeploymentSource src;
    ProjectDeploymentProdFiles prodFiles;

    public ProjectDeployment() {
        this.id = UlidGenerator.generate();
    }

}
