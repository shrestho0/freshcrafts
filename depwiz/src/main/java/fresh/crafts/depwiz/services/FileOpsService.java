package fresh.crafts.depwiz.services;

import org.springframework.stereotype.Service;

import fresh.crafts.depwiz.entities.ProjectDeployment;
import fresh.crafts.depwiz.entities.ProjectDeploymentProdFiles;

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
        processBuilder.command("sudo", "rm", "-rf", file.getAbsolutePath());
        try {
            Process process = processBuilder.start();
            process.waitFor();
            if (file.exists()) {
                return false;
            } else {
                return true;
            }
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
            if (fileObj.exists()) {
                if (!deleteForcefully(fileObj)) {
                    // return false;
                    ensured = false;
                }
            }
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
}
