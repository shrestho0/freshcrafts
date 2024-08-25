
package fresh.crafts.dep_wizard.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import fresh.crafts.dep_wizard.entities.CommandServiceResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class CommandService {

    @Autowired
    private Environment env;

    public CommandServiceResult lsDir(String dir) {
        String command = "ls " + dir;
        CommandServiceResult result = _runBashCommand(command);
        return result;
    }

    /// Internal
    private CommandServiceResult _runBashCommand(String command) {
        String newCommand = "bash -c " + command;
        CommandServiceResult result = _runCommandRaw(newCommand);
        return result;
    }

    private CommandServiceResult _runCommandRaw(String command) {
        // StringBuilder output = new StringBuilder();
        CommandServiceResult result = new CommandServiceResult();
        StringBuilder res_string = new StringBuilder();

        result.setCommand(command);

        int exitCode = -1;

        try {
            // Using ProcessBuilder to run the command
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            // processBuilder.command("");

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                res_string.append(line).append("\n");
            }

            exitCode = process.waitFor();
            res_string.append("Exited with code: ").append(exitCode);

            result.setOutput(res_string.toString());

        } catch (Exception e) {
            res_string.append("Exception: ").append(e.getMessage());
            result.setError(res_string.toString());
        }

        result.setExitCode(exitCode);

        return result;
    }
}
