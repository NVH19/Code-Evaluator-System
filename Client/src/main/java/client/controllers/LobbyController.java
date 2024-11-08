package client.controllers;

import client.connection.ConnectionManager;
import client.models.Exam;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LobbyController extends BaseController {

    @FXML
    private Button startButton;

    @FXML
    private Text startTimeText;

    @FXML
    private Text durationText;

    private Timeline timeline;
    private int countdownTime;
    private Exam exam;

    public void initialize() {
        try {
            // Receive exam start time and duration from the server
            BufferedReader in = ConnectionManager.getInstance().getBufferedReader();

            // Get start time from the server
            String startTimeString = in.readLine();
            LocalTime startTime = LocalTime.parse(startTimeString, DateTimeFormatter.ISO_LOCAL_TIME);

            // Get duration from the server
            int examDuration = Integer.parseInt(in.readLine()); // Duration in seconds

            exam = new Exam(startTime, examDuration);
            System.out.println("Start time: " + exam.getStartTime());
            System.out.println("Duration: " + exam.getDurationInSeconds() + " seconds");
            int durationInMinutes = examDuration / 60;

            // Update the FXML Text components with start time and duration
            startTimeText.setText(startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            durationText.setText(durationInMinutes + " phút");
            System.out.println("Start time: " + startTime);
            System.out.println("Duration: " + durationInMinutes + " minutes");

            // Calculate the countdown time
            LocalTime currentTime = LocalTime.now();
            countdownTime = (int) java.time.temporal.ChronoUnit.SECONDS.between(currentTime, startTime);

            if (countdownTime <= 0 && countdownTime >= -examDuration) {
                startButton.setText("Làm bài");
                startButton.setDisable(false);
            } else {
                if (countdownTime < -examDuration) {
                    countdownTime += 24 * 60 * 60; // Adjust for past time
                }
                updateButtonText();
                startCountdown(exam.getDurationInSeconds()); // Pass the duration to countdown
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi kết nối", "Không thể kết nối đến server.");
        }
    }


    private void startCountdown(int examDuration) {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            countdownTime--;
            updateButtonText();

            if (countdownTime <= 0) {
                // When countdown ends, enable the button and show message
                startButton.setText("Làm bài");
                startButton.setDisable(false);
                timeline.stop();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        startButton.setDisable(true);
    }

    private void updateButtonText() {
        int minutes = countdownTime / 60;
        int seconds = countdownTime % 60;
        startButton.setText(String.format("%02d:%02d", minutes, seconds));
    }

    @FXML
    private void onStartButtonClicked() {
        System.out.println("Bắt đầu làm bài!");
        sendStartExamRequest();
    }

    private void sendStartExamRequest() {
        try {
            PrintWriter out = ConnectionManager.getInstance().getPrintWriter();
            BufferedReader in = ConnectionManager.getInstance().getBufferedReader();

            out.println("START_EXAM");
            String response = in.readLine();
            System.out.println("Server response: " + response);

            if ("EXAM_STARTED".equals(response)) {
                // Load the home page after starting the exam
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
                Parent homeRoot = loader.load();

//                HomeController homeController = loader.getController();
//                homeController.setExam(exam);  // Truyền đối tượng Exam đã tạo

                Stage stage = (Stage) startButton.getScene().getWindow();
                stage.setScene(new Scene(homeRoot));
                stage.setTitle("Trang Chính");
            } else {
                System.out.println("Could not start exam: " + response);
                showAlert("Lỗi", "Không thể bắt đầu thi: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi kết nối", "Không thể kết nối đến server.");
        }
    }
}
