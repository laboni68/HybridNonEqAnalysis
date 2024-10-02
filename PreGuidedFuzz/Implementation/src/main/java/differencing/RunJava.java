package differencing;

import javax.tools.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RunJava {
    public RunJava() {
    }
    public String runJavaClass(String className, String classpath, List<String> programArgs) throws IOException, InterruptedException {
        // System.out.println("Running Java class: " + className);
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-cp");
        command.add(classpath);
        command.add(className);
        command.addAll(programArgs);
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        // System.out.println("before start"); 
        Process process = processBuilder.start();
        // System.out.println("after start");

        StringBuilder output = new StringBuilder();
        // try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        //     String line;
        //     System.out.println("before while loop");
        //     System.out.println("reader: ");
        //     System.out.println(reader);
        //     System.out.println("reader.readLine(): "+ reader.readLine());
        //     while ((line = reader.readLine()) != null) {
        //         System.out.println("in while loop");
        //         output.append(line).append("\n");
        //     }
        //     System.out.println("after while loop");
        // }
        // process.waitFor();
        // System.out.println("Process completed successfully.");
        long timeout = 3;
        boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
        // System.out.println("Process finished: " + finished);
        if (finished) {
            // Process completed within the timeout
            // System.out.println("Process completed successfully.");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            // System.out.println("before while loop");
            // System.out.println("reader: ");
            // System.out.println(reader);
            // System.out.println("reader.readLine(): "+ reader.readLine());
            while ((line = reader.readLine()) != null) {
                // System.out.println("in while loop");
                output.append(line).append("\n");
            }
            // System.out.println("after while loop");
        }
            return output.toString();
        } else {
            // Process exceeded the timeout, so destroy it
            process.destroy();
            System.out.println("Process was terminated due to timeout.");
            return "ProcessTimeout";
            
        }
        // return output.toString();
    }
}