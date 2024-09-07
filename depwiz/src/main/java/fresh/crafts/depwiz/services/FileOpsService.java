package fresh.crafts.depwiz.services;

import org.springframework.stereotype.Service;

import fresh.crafts.depwiz.entities.Project;
import fresh.crafts.depwiz.entities.ProjectDeployment;
import fresh.crafts.depwiz.entities.ProjectDeploymentProdFiles;
import fresh.crafts.depwiz.utils.CraftUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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

    private boolean deleteForcefully(File file) {
        // delete file forcefully
        // delete using sudo
        // String sudoCommand = "sudo rm -rf " + file.getAbsolutePath();
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (!file.exists()) {
            return true;
        }
        String absPath = file.getAbsolutePath();
        processBuilder.command("sudo", "rm", "-rf", absPath);
        try {
            Process process = processBuilder.start();
            process.waitFor();

            // check if file exists

            File fileObj = new File(absPath);
            return !fileObj.exists();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public Boolean ensurePreviousBuildFilesRemoved(ProjectDeployment projectDeployment) {
        // delete package-lock.json, node_modules, buildDir, etc.
        String[] files = { "package-lock.json", "node_modules", projectDeployment.getSrc().getBuildDirPath() };

        String projectRoot = projectDeployment.getSrc().getRootDirAbsPath();

        Boolean ensured = true;
        for (String file : files) {
            File fileObj = new File(projectRoot + "/" + file);
            fileObj.delete();

            // if (fileObj.exists()) {
            //     fileObj.delete();
            // }

            // // if (!deleteForcefully(fileObj)) {
            // // ensured = false;
            // // return ensured;
            // // } else {
            // // ensured = true;
            // // }

            // if (fileObj.exists()) {
            // deleteForcefully(fileObj);
            // }
        }

        return ensured;
    }

    public Boolean setPortToProdFiles(ProjectDeploymentProdFiles pfs, int port) {

        String ecoSystemFile = pfs.getEcoSystemFileAbsPath();
        String nginxFile = pfs.getNginxConfFileAbsPath();

        Boolean ecoSystemFileUpdated = updateFileSpecificContent(ecoSystemFile, port);
        Boolean nginxFileUpdated = updateFileSpecificContent(nginxFile, port);

        if (ecoSystemFileUpdated && nginxFileUpdated) {
            return true;
        }

        return false;

    }

    public Boolean updateFileSpecificContent(String filePath, int port) {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        StringBuilder fileContent = new StringBuilder();
        boolean success = false;

        try {
            // Open the file for reading
            reader = new BufferedReader(new FileReader(filePath));
            String line;

            // Read the file content
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append(System.lineSeparator());
            }

            // Replace {PORT} with the specified port
            String updatedContent = fileContent.toString().replace("{PORT}", String.valueOf(port));

            // Open the file for writing
            writer = new BufferedWriter(new FileWriter(filePath));

            // Save the updated content to the file
            writer.write(updatedContent);

            success = true; // Operation was successful

        } catch (IOException e) {
            e.printStackTrace();
            success = false; // Operation failed

        } finally {
            // Close the file reader and writer
            try {
                if (reader != null)
                    reader.close();
                if (writer != null)
                    writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return success;
    }

    public Boolean deleteProjectDir(String projectId, String absPath) {
        // last part of absPath must match projectId
        // delete project directory
        String[] absPathParts = absPath.split("/");
        String lastPart = absPathParts[absPathParts.length - 1];

        System.out.println("\n\nlastPart: ");
        CraftUtils.jsonLikePrint(lastPart);
        CraftUtils.jsonLikePrint(absPath);
        System.out.println("<---\n\n ");

        if (lastPart.equals(projectId)) {
            File projectDir = new File(absPath);
            if (projectDir.exists()) {
                return deleteForcefully(projectDir);
            }
        }

        return false;
    }

}
