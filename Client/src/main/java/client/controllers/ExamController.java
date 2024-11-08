package client.controllers;

import client.connection.ConnectionManager;
import client.models.Exercise;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ExamController extends BaseController {
    private Exercise exercise;
    private File selectedFile;

    @FXML
    private Button exerciseButton, historyButton, rankingButton, logoutButton, selectFileButton, sendFileButton;
    @FXML
    private ChoiceBox<String> languageChoiceBox;
    @FXML
    private Label exerciseNameLabel, exerciseDescriptionLabel;


    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
        exerciseNameLabel.setText(exercise.getName());
        exerciseDescriptionLabel.setText(exercise.getDescription());
    }

    public void initialize() {
        setupNavigation();
        languageChoiceBox.getItems().addAll("Java", "Python", "C++");
        languageChoiceBox.setValue("Java");
        selectFileButton.setOnAction(event -> handleSelectFile());
        sendFileButton.setOnAction(event -> handleSendFile());
    }

    private void setupNavigation() {
        exerciseButton.setOnAction(event -> goToPage("home.fxml", exerciseButton));
        historyButton.setOnAction(event -> goToPage("history.fxml", historyButton));
        rankingButton.setOnAction(event -> goToPage("ranking.fxml", rankingButton));
        logoutButton.setOnAction(event -> goToPage("login.fxml", logoutButton));
    }

    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Java Files", "*.java"),
                new FileChooser.ExtensionFilter("Python Files", "*.py"),
                new FileChooser.ExtensionFilter("C++ Files", "*.cpp")
        );
        Stage stage = (Stage) selectFileButton.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            selectFileButton.setText(selectedFile.getName());
        }
    }

    @FXML
    private void handleSendFile() {
        if (selectedFile == null) {
            showAlert("Error", "Please select a file before sending.");
            return;
        }

        try (
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(selectedFile));
        ) {

            Socket socket = ConnectionManager.getInstance().getSocket();
            OutputStream os = socket.getOutputStream();
            PrintWriter writer = ConnectionManager.getInstance().getPrintWriter();
            BufferedReader in = ConnectionManager.getInstance().getBufferedReader();

            writer.println("SEND_FILE");
            writer.println(exercise.getExerciseId());
            writer.println(selectedFile.getName());

//            goToPage("history.fxml", sendFileButton);
            String response = in.readLine();
            if ("READY_TO_RECEIVE_FILE".equals(response)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
                writer.println("\nEND_OF_FILE");
//                socket.shutdownOutput();
            }

            String resultMessage = in.readLine();
            System.out.println(resultMessage);
            exercise.setStatus(resultMessage.contains("Success") ? "correct" : "incorrect");
            showAlert("Result", resultMessage);

//            String response2 = in.readLine();
//            System.out.println(response2);

            goToPage("history.fxml", sendFileButton);
            System.out.println("Socket closed status after receiving file: " + socket.isClosed());



        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to send file. Please try again.");
        }
    }
}