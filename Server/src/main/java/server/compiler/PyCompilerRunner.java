package server.compiler;

import server.models.TestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

public class PyCompilerRunner {
    public int runPythonScript(String filePath, String exerciseId, List<TestCase> testCases) {
        int successCount = 0;
        try {
            System.out.println("Running Python script for exercise ID: " + exerciseId);

            for (TestCase testCase : testCases) {
                System.out.println("Input: " + testCase.getInput());
                System.out.println("Expected output: " + testCase.getExpectedOutput());

                ProcessBuilder processBuilder = new ProcessBuilder("python", filePath);
                processBuilder.redirectErrorStream(true);

                Process process = processBuilder.start();

                // Write input to the Python program
                try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream())) {
                    writer.write(testCase.getInput());
                    writer.flush();
                }

                // Read program output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    StringBuilder outputBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputBuilder.append(line).append("\n");
                    }
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        String output = outputBuilder.toString().trim();
                        System.out.println("Execution result: " + output);

                        if (compareWithExpectedOutput(testCase.getExpectedOutput(), output)) {
                            successCount++;
                        }
                    } else {
                        System.err.println("Execution failed with exit code: " + exitCode);
                    }
                }
//                System.out.println("Output: " + outputBuilder.toString().trim());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return successCount;
    }

    private boolean compareWithExpectedOutput(String expectedOutput, String actualOutput) {
        boolean isSuccess = expectedOutput.trim().equals(actualOutput.trim());
        if (isSuccess) {
            System.out.println("Test case passed. Actual output: " + actualOutput);
        } else {
            System.err.println("Test case failed. Actual: " + actualOutput + ", Expected: " + expectedOutput);
        }
        return isSuccess;
    }
}