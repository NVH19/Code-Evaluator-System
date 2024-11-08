package client.models;

public class SubmissionHistory {
    private String submissionId;
    private String exerciseName;
    private String status;
    private String submissionTime;

    public SubmissionHistory(String submissionId, String exerciseName, String status, String submissionTime) {
        this.submissionId = submissionId;
        this.exerciseName = exerciseName;
        this.status = status;
        this.submissionTime = submissionTime;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public String getStatus() {
        return status;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }
}

