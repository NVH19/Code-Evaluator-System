package client.models;

import java.io.Serializable;
import java.time.LocalTime;

public class Exam implements Serializable {
    private LocalTime startTime;
    private int durationInSeconds;

    public Exam(LocalTime startTime, int durationInSeconds) {
        this.startTime = startTime;
        this.durationInSeconds = durationInSeconds;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public int getDurationInMinutes() {
        return durationInSeconds / 60;
    }

    @Override
    public String toString() {
        return "Exam{" +
                "startTime=" + startTime +
                ", durationInSeconds=" + durationInSeconds +
                '}';
    }
}
