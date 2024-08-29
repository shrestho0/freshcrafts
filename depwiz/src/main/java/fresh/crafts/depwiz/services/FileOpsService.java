package fresh.crafts.depwiz.services;

import org.springframework.stereotype.Service;

import fresh.crafts.depwiz.entities.ProjectDeployment;

import java.io.File;

@Service
public class FileOpsService {

    public Boolean checkDirectoryExists(String dirAbsPath) {
        // check if dirAbsPath exists

        File directory = new File(dirAbsPath);
        if (directory.exists() && directory.isDirectory()) {
            return true;
        }

        return false;
    }

    public Boolean ensurePreviousBuildFilesRemoved(ProjectDeployment projectDeployment) {
        // delete package-lock.json, node_modules, buildDir, etc.
        String[] files = { "package-lock.json", "node_modules", projectDeployment.getSrc().getBuildDirPath() };

        String projectRoot = projectDeployment.getSrc().getRootDirAbsPath();
        try {
            for (String file : files) {
                File f = new File(projectRoot + "/" + file);
                if (f.exists()) {
                    System.out.println("[INFO] Deleting file: " + f.getAbsolutePath());
                    f.delete();
                    System.out.println("[INFO] Deleted file: " + f.getAbsolutePath());
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
