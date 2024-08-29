package fresh.crafts.depwiz.entities;

import lombok.Data;

@Data
public class ProjectDeploymentSource {
    String filesDirPath;
    String filesDirAbsPath;

    String rootDirPath;
    String rootDirAbsPath;

    String buildDirPath;
    String buildDirAbsPath;
}
