package fresh.crafts.wiz_mysql.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import fresh.crafts.wiz_mysql.entities.CommandServiceResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;

@Service
public class CommandService {

    @Autowired
    private Environment env;

    public void getDBCreds() {
        System.out.println("===================================");
        System.out.println(env.getProperty("freshCrafts.SPRING_DATASOURCE_USERNAME", "root"));
        System.out.println(env.getProperty("freshCrafts.SPRING_DATASOURCE_PASSWORD", "default_password"));
        System.out.println("===================================");
    }

    public CommandServiceResult dumpDB(String dbName, String dumpFileName) {
        String dbRoot = env.getProperty("freshCrafts.SPRING_DATASOURCE_USERNAME", "root");
        String dbPass = env.getProperty("freshCrafts.SPRING_DATASOURCE_PASSWORD", "default_password");
        String dbUrl = env.getProperty("freshCrafts.SPRING_DATASOURCE_URL", "localhost");
        // host and port from dbUrl (jdbc:mysql://localhost:3306/dbname)
        String[] dbUrlParts = dbUrl.split(":");
        String dbHost = dbUrlParts[2].replace("//", "");
        String dbPort = dbUrlParts[3].split("/")[0];

        // String command = "/usr/bin/mysqldump -u " + dbRoot + " -p" + dbPass + " " +
        // dbName + " > " + dumpFileName;
        String command = "/usr/bin/mysqldump -u " + dbRoot + " -p" + dbPass + " -h " + dbHost + " -P " + dbPort + " "
                + dbName + " > " + dumpFileName;

        // do something
        CommandServiceResult result = runCommandRaw(command);
        System.out.println("\n" + result.getOutput() + "\n");
        return result;
    }

    public CommandServiceResult deleteDumppedFile(String dumpFileName) {
        String command = "/usr/bin/rm ./" + dumpFileName;
        CommandServiceResult result = runCommandRaw(command);
        System.out.println("\n" + result.getOutput() + "\n");
        return result;
    }

    public CommandServiceResult restoreDB(String dbName, String dumpFileName) {
        String dbRoot = env.getProperty("freshCrafts.SPRING_DATASOURCE_USERNAME", "root");
        String dbPass = env.getProperty("freshCrafts.SPRING_DATASOURCE_PASSWORD", "default_password");
        String dbUrl = env.getProperty("freshCrafts.SPRING_DATASOURCE_URL", "localhost");
        // host and port from dbUrl (jdbc:mysql://localhost:3306/dbname)
        String[] dbUrlParts = dbUrl.split(":");
        String dbHost = dbUrlParts[2].replace("//", "");
        String dbPort = dbUrlParts[3].split("/")[0];

        String command = "/usr/bin/mysql -u " + dbRoot + " -p" + dbPass + " -h " + dbHost + " -P " + dbPort + " "
                + dbName
                + " < " + dumpFileName;
        // do something
        CommandServiceResult result = runCommandRaw(command);
        System.out.println("\n" + result.getOutput() + "\n");

        return result;
    }

    public CommandServiceResult runBashCommand(String command) {
        String newCommand = "bash -c " + command;
        CommandServiceResult result = runCommandRaw(newCommand);
        return result;
    }

    public CommandServiceResult runCommandRaw(String command) {
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
