package fresh.crafts.engine.v1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import fresh.crafts.engine.v1.entities.ProjectDomain;
import fresh.crafts.engine.v1.entities.ProjectGithubRepo;
import fresh.crafts.engine.v1.utils.UlidGenerator;
import fresh.crafts.engine.v1.utils.enums.ProjectStatus;
import fresh.crafts.engine.v1.utils.enums.ProjectType;
import lombok.Data;

@Data
@Document(collection = "Projects")
public class Project {

    @Id
    String id;

    @Indexed(unique = true)
    String uniqueName;

    ProjectType type;
    ProjectStatus status;

    Integer totalVersions;

    String activeDeploymentId;

    Integer portAssigned;

    ProjectGithubRepo githubRepo;

    ProjectDomain domain;

    public Project() {
        this.id = UlidGenerator.generate();
        this.totalVersions = 0;
    }

}
