package me.shrestho.nginx_wrapper.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.stereotype.Service;

@Service
public class BaseService {

    public String sayHello() {
        return "Hello, from SpringBoot";
    }

    public String listDir(String dir) throws IOException, InterruptedException{
        // ProcessBuilder processBuilder = new ProcessBuilder("sudo", "nginx", "-s", "reload");

        String out = "";

        ProcessBuilder processBuilder = new ProcessBuilder("ls", "-alh", dir);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
                // Capture output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Print the command output to the console
                out = out + "\n" + line;
            }
        }


        // Get the exit code
        int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);

        // Handle non-zero exit codes
        if (exitCode != 0) {
            throw new RuntimeException("Command exited with code " + exitCode);
        }

        return out;
    }


    public String readFile(String fileloc) throws IOException, InterruptedException{
        // ProcessBuilder processBuilder = new ProcessBuilder("sudo", "nginx", "-s", "reload");

        String out = "";

        ProcessBuilder processBuilder = new ProcessBuilder("cat", fileloc);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
                // Capture output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Print the command output to the console
                out = out + "\n" + line;
            }
        }


        // Get the exit code
        int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);

        // Handle non-zero exit codes
        if (exitCode != 0) {
            throw new RuntimeException("Command exited with code " + exitCode);
        }

        return out;
    }
 
    public String checkNginxStatus() throws IOException, InterruptedException{
        // ProcessBuilder processBuilder = new ProcessBuilder("sudo", "nginx", "-s", "reload");

        String out = "";

        ProcessBuilder processBuilder = new ProcessBuilder("sudo", "nginx", "-t");
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
                // Capture output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Print the command output to the console
                out = out + "\n" + line;
            }
        }


        // Get the exit code
        int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);

        // Handle non-zero exit codes
        if (exitCode != 0) {
            throw new RuntimeException("Command exited with code " + exitCode);
        }

        return out;
    }

    public String reloadNginx() throws IOException, InterruptedException{
        
        String out = "";

        ProcessBuilder processBuilder = new ProcessBuilder("sudo","/usr/sbin/nginx", "-s", "reload");
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
                // Capture output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Print the command output to the console
                out = out + "\n" + line;
            }
        }


        // Get the exit code
        int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);

        // Handle non-zero exit codes
        if (exitCode != 0) {
            throw new RuntimeException("Command exited with code " + exitCode);
        }

        return out;
    }




    
}
