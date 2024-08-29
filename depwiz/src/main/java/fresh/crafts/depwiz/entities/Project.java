package fresh.crafts.depwiz.entities;

import fresh.crafts.depwiz.enums.ProjectStatus;
import fresh.crafts.depwiz.enums.ProjectType;
import lombok.Data;

@Data
public class Project {

    String id;

    String uniqueName;

    ProjectType type;
    ProjectStatus status;

    Integer totalVersions;
    Integer activeVersion;

    // as long as deployment version is active
    String activeDeploymentId;
    // mainly for while deployment is in progress
    String currentDeploymentId;

    Integer portAssigned;

    ProjectGithubRepo githubRepo;

    String domain;

}
