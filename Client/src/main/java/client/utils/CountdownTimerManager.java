package client.utils;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.time.Duration;
import java.time.LocalTime;

public class CountdownTimerManager {
    private final Label countdownLabel;
    private final LocalTime startTime;
    private final int durationInSeconds;
    private AnimationTimer timer;
    private long lastUpdate = 0;
    private long countdownTimeInSeconds;

    public CountdownTimerManager(Label countdownLabel, LocalTime startTime, int durationInSeconds) {
        this.countdownLabel = countdownLabel;
        this.startTime = startTime;
        this.durationInSeconds = durationInSeconds;

        LocalTime now = LocalTime.now();
        countdownTimeInSeconds = Duration.between(now, startTime).getSeconds();
    }

    public void start() {
        if (timer == null) {
            timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (now - lastUpdate >= 1_000_000_000) {
                        lastUpdate = now;
                        if (countdownTimeInSeconds <= 0) {
                            countdownLabel.setText("00:00:00");
                            stop();
                            showTimeUpAlert();
                        } else {
                            long hours = countdownTimeInSeconds / 3600;
                            long minutes = (countdownTimeInSeconds % 3600) / 60;
                            long seconds = countdownTimeInSeconds % 60;
                            countdownLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                            countdownTimeInSeconds--;
                        }
                    }
                }
            };
            timer.start();
        }
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void showTimeUpAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText("Hết giờ làm bài!");
            alert.setContentText("Bạn đã hết thời gian làm bài.");
            alert.showAndWait();
        });
    }
}
