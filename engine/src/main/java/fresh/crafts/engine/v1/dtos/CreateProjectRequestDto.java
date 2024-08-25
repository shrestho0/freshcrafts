package fresh.crafts.engine.v1.dtos;

import fresh.crafts.engine.v1.entities.GithubRepoDetailed;
import fresh.crafts.engine.v1.entities.ProjectDeploymentFile;
import fresh.crafts.engine.v1.entities.ProjectDeploymentSource;
import fresh.crafts.engine.v1.utils.enums.ProjectType;
import lombok.Data;

@Data
public class CreateProjectRequestDto {

    ProjectType type;
    ProjectDeploymentFile file;
    ProjectDeploymentSource src;

    // id will be generated from cockpit
    public String newProjectId;

    GithubRepoDetailed github_repo;
    String github_tar_download_url;

}
