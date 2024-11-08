package client.models;

public class ExamSession {
    private static ExamSession instance;
    private Exam exam;
    private int remainingTime;  // Thời gian còn lại trong giây

    private ExamSession() {}

    public static ExamSession getInstance() {
        if (instance == null) {
            instance = new ExamSession();
        }
        return instance;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void updateRemainingTime(int time) {
        this.remainingTime = time;
    }

    public void decrementRemainingTime() {
        if (remainingTime > 0) {
            this.remainingTime--;
        }
    }

    public boolean isTimeUp() {
        return remainingTime <= 0;
    }
}
