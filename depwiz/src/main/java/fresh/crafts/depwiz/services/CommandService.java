
package fresh.crafts.depwiz.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

import fresh.crafts.depwiz.entities.CommandServiceResult;
import fresh.crafts.depwiz.entities.ProjectDeployment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

// Note: Refactor required
@Service
public class CommandService {

    @Autowired
    private Environment env;

    public CommandServiceResult lsDir(String dir) {
        String command = "ls " + dir;
        CommandServiceResult result = _runBashCommand(command, null);
        return result;
    }

    /// Internal
    private CommandServiceResult _runBashCommand(String command, String dir) {
        String newCommand = "bash -c " + command;
        CommandServiceResult result = _runCommandRaw(newCommand, dir);
        return result;
    }

    private CommandServiceResult _runCommandRaw(String command, String dir) {
        // StringBuilder output = new StringBuilder();
        CommandServiceResult result = new CommandServiceResult();
        StringBuilder res_string = new StringBuilder();

        result.setCommand(command);

        int exitCode = -1;

        try {
            // Using ProcessBuilder to run the command
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(command);

            // set pwd
            if (dir != null) {
                processBuilder.directory(new File(dir));
            }

            // exec
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                res_string.append(line).append("\n");
            }

            exitCode = process.waitFor();
            res_string.append("Exited with code: ").append(exitCode);

            StringBuilder error_string = new StringBuilder();
            if (exitCode != 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    error_string.append(errorLine).append("\n");

                }
                result.setError(error_string.toString());

            } else {
                result.setOutput(res_string.toString());
            }

        } catch (Exception e) {
            // for unhanded exceptions
            res_string.append("Exception: ").append(e.getMessage());
            result.setError(res_string.toString());
        }

        result.setExitCode(exitCode);

        return result;
    }

    ///// Project Specific

    public CommandServiceResult freshBuildProject(ProjectDeployment deployment) {
        // build project and return result
        // build command should be in deployment
        // build will be done in project rootDirAbsPath
        // build dir should be in deployment, we ignore this
        // to check (done):
        // check if build command exists, this should be done in validations
        // check if project rootDirAbsPath exists, this should be done in validations
        // command
        String rootDir = deployment.getSrc().getRootDirAbsPath();
        // String command = "/usr/bin/" + deployment.getDepCommands().getInstall();

        // System.out.println("Install Deps Command: " + command);
        // CommandServiceResult result = _runCommandRaw(command, rootDir);

        // run command here
        ProcessBuilder processBuilder = new ProcessBuilder();
        File wd = new File(rootDir);
        processBuilder.directory(wd);
        processBuilder.command("npm", "run", "build");
        CommandServiceResult r = runIt(processBuilder);
        return r;
    }

    public CommandServiceResult freshInstallDeps(ProjectDeployment deployment) {

        // command
        String rootDir = deployment.getSrc().getRootDirAbsPath();
        // String command = "/usr/bin/" + deployment.getDepCommands().getInstall();

        // System.out.println("Install Deps Command: " + command);
        // CommandServiceResult result = _runCommandRaw(command, rootDir);

        // run command here
        ProcessBuilder processBuilder = new ProcessBuilder();
        File wd = new File(rootDir);
        processBuilder.directory(wd);
        processBuilder.command("npm", "install", "--force");
        CommandServiceResult r = runIt(processBuilder);
        return r;

    }

    public CommandServiceResult postInstallCommands(ProjectDeployment pd) {
        // post install command is multi line and multiple commands
        // run one by one
        // fail on fail
        // single success result

        String rootDir = pd.getSrc().getRootDirAbsPath();

        String[] commands = pd.getDepCommands().getPostInstall().split("\n");

        CommandServiceResult[] results = new CommandServiceResult[commands.length];

        ProcessBuilder processBuilder = new ProcessBuilder();
        File wd = new File(rootDir);

        for (int i = 0; i < commands.length; i++) {
            processBuilder.directory(wd);
            if (commands[i].trim().length() == 0) {
                continue;
            }
            processBuilder.command("bash", "-c", commands[i]);
            results[i] = runIt(processBuilder);
        }

        // if any fails, return the first failure
        // for (CommandServiceResult r : results) {
        // if (r.getExitCode() != 0) {
        // return r;

        // }
        // }
        for (int i = 0; i < results.length; i++) {
            if (results[i] == null) {
                continue;
            }
            if (results[i].getExitCode() != 0) {
                results[i].setShortError("Post install command `" + commands[i] + "` failed with exit code: "
                        + results[i].getExitCode());
                return results[i];
            }
        }

        CommandServiceResult result = new CommandServiceResult();
        result.setExitCode(0);
        result.setOutput("All commands ran successfully");

        return result;

    }

    private CommandServiceResult runIt(ProcessBuilder processBuilder) {

        String commandOutput = "";
        String commandError = "";

        int exitCode = -1;
        try {
            // start
            Process process = processBuilder.start();
            exitCode = process.waitFor();

            // output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                commandOutput += line + "\n";
            }

            // error
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                commandError += errorLine + "\n";
            }

            commandError += "Exited with code: " + exitCode + "\n";

            System.out.println("Exit Code: " + exitCode);
            // System.out.println("\nCommand Output: " + commandOutput + "\n");
            // System.out.println("\nCommand Error: " + commandError + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        CommandServiceResult result = new CommandServiceResult();
        result.setExitCode(exitCode);
        result.setOutput(commandOutput);
        result.setError(commandError);
        result.setCommand(commandError);

        return result;
    }

    public CommandServiceResult startPM2(String ecoSystemFileAbsPath) {
        // pm2 start ecosystem.config.js
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("pm2", "start", ecoSystemFileAbsPath);
        CommandServiceResult result = runIt(processBuilder);
        return result;
    }

    public CommandServiceResult savePM2() {
        // pm2 save
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("pm2", "save");
        CommandServiceResult result = runIt(processBuilder);
        return result;
    }

    public CommandServiceResult enableNginxSite(String nginxConfFileAbsPath) {
        // move nginx conf file to /etc/nginx/sites-available
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("sudo", "mv", nginxConfFileAbsPath, "/etc/nginx/sites-available");
        CommandServiceResult result = runIt(processBuilder);
        if (result.getExitCode() != 0) {
            result.setShortError("Failed to move nginx conf file");
            return result;
        }

        // create symlink
        String fileName = new File(nginxConfFileAbsPath).getName();
        processBuilder.command("sudo", "ln", "-s", "/etc/nginx/sites-available/" + fileName,
                "/etc/nginx/sites-enabled/" + fileName);
        CommandServiceResult result2 = runIt(processBuilder);

        if (result2.getExitCode() != 0) {
            result2.setShortError("Failed to create symlink");
            return result2;
        }

        // reload nginx
        processBuilder.command("sudo", "systemctl", "reload", "nginx");
        CommandServiceResult result3 = runIt(processBuilder);

        if (result3.getExitCode() != 0) {
            result3.setShortError("Failed to reload nginx");
            return result3;
        }

        CommandServiceResult result4 = new CommandServiceResult();
        result4.setExitCode(0);
        result4.setOutput("Nginx site enabled successfully");
        return result4;

    }

    public List<CommandServiceResult> disableNginxSite(String id) {

        List<CommandServiceResult> results = new ArrayList<CommandServiceResult>();

        // delete enabled symlink
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("sudo", "rm", "/etc/nginx/sites-enabled/" + id + ".conf");

        CommandServiceResult result = runIt(processBuilder);
        if (result.getExitCode() != 0) {
            result.setShortError("Failed to remove symlink");
            // return result;
            results.add(result);
        }

        // remove conf file
        processBuilder.command("sudo", "rm", "/etc/nginx/sites-available/" + id + ".conf");
        CommandServiceResult result1 = runIt(processBuilder);
        if (result1.getExitCode() != 0) {
            result1.setShortError("Failed to remove conf file");
            // return result1;
            results.add(result1);
        }

        // reload nginx
        processBuilder.command("sudo", "systemctl", "reload", "nginx");
        CommandServiceResult result2 = runIt(processBuilder);

        if (result2.getExitCode() != 0) {
            result2.setShortError("Failed to reload nginx");
            // return result2;
            results.add(result2);
        }

        CommandServiceResult result3 = new CommandServiceResult();
        result3.setExitCode(0);
        result3.setOutput("Nginx site disabled successfully");
        // return result3;
        results.add(result3);

        return results;
    }

    public List<CommandServiceResult> stopAndDeletePM2(String ecoSystemFileAbsPath) {
        List<CommandServiceResult> results = new ArrayList<CommandServiceResult>();

        // stop pm2
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("pm2", "stop", ecoSystemFileAbsPath);
        CommandServiceResult result = runIt(processBuilder);
        if (result.getExitCode() != 0) {
            result.setShortError("Failed to stop pm2");
            results.add(result);
        }

        // delete pm2
        processBuilder.command("pm2", "delete", ecoSystemFileAbsPath);
        CommandServiceResult result1 = runIt(processBuilder);
        if (result1.getExitCode() != 0) {
            result1.setShortError("Failed to delete pm2");
            results.add(result1);
        }

        // pm2 save
        processBuilder.command("pm2", "save");
        CommandServiceResult result2 = runIt(processBuilder);
        if (result2.getExitCode() != 0) {
            result2.setShortError("Failed to save pm2");
            results.add(result2);
        }

        CommandServiceResult result3 = new CommandServiceResult();
        result3.setExitCode(0);
        result3.setOutput("PM2 stopped and deleted successfully");
        results.add(result3);

        return results;
    }

}
