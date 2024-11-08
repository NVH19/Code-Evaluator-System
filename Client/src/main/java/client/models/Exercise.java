package client.models;

public class Exercise {
    private final String exerciseId;
    private final String name;
    private final String description;
    private String status;

    public Exercise(String exerciseId, String name, String description) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.description = description;
        this.status = null;
    }

    public String getDescription() {
        return description;
    }

    public String getExerciseId() {
        return exerciseId;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
