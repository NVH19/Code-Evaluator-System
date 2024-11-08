package client.models;

public class Ranking {
    private String idSv;
    private String name;
    private int answerCorrectNumber;
    private int rank;

    public Ranking(String idSv, String name, int answerCorrectNumber, int rank) {
        this.idSv = idSv;
        this.name = name;
        this.answerCorrectNumber = answerCorrectNumber;
        this.rank = rank;
    }

    public String getIdSv() {
        return idSv;
    }

    public String getName() {
        return name;
    }

    public int getAnswerCorrectNumber() {
        return answerCorrectNumber;
    }

    public int getRank() {
        return rank;
    }
    public void setRank(int rank) {
        this.rank = rank; // Set the rank to the provided value
    }
}
