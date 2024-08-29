package fresh.crafts.engine.v1.entities;

import lombok.Data;

@Data
public class ProjectDeploymentProdFiles {
    String ecoSystemFilePath;
    String ecoSystemFileAbsPath;
    String logsDir;
    String outLogFileAbsPath;
    String errorLogFileAbsPath;
    String nginxConfFilePath;
    String nginxConfFileAbsPath;
}
