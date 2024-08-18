package fresh.crafts.engine.v1.dtos;

import fresh.crafts.engine.v1.entities.GithubRepoDetailed;
import fresh.crafts.engine.v1.entities.ProjectDeploymentFile;
import fresh.crafts.engine.v1.utils.enums.ProjectType;
import lombok.Data;

@Data
public class CreateProjectRequestDto {

    ProjectType type;
    ProjectDeploymentFile file;

    GithubRepoDetailed github_repo;
    String github_tar_download_url;

}
