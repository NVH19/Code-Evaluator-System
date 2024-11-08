package client.controllers;

import client.connection.ConnectionManager;
import client.models.Ranking; // Assuming you have a Ranking model class
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;

public class RankingController extends BaseController {
    @FXML
    private Button exerciseButton, historyButton, rankingButton, logoutButton;
    @FXML
    private TableView<Ranking> rankingTable;
    @FXML
    private TableColumn<Ranking, String> idSvColumn;
    @FXML
    private TableColumn<Ranking, String> nameColumn;
    @FXML
    private TableColumn<Ranking, Integer> answerCorrectNumberColumn;
    @FXML
    private TableColumn<Ranking, Integer> rankColumn;

    private final ObservableList<Ranking> rankings = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupNavigation();
        setupTableColumns();
        loadRankingData(fetchRankingFromServer());
    }

    private void setupNavigation() {
        exerciseButton.setOnAction(event -> goToPage("home.fxml", exerciseButton));
        historyButton.setOnAction(event -> goToPage("history.fxml", historyButton));
        rankingButton.setOnAction(event -> goToPage("ranking.fxml", rankingButton));
        logoutButton.setOnAction(event -> goToPage("login.fxml", logoutButton));
    }

    private void setupTableColumns() {
        idSvColumn.setCellValueFactory(new PropertyValueFactory<>("idSv"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        answerCorrectNumberColumn.setCellValueFactory(new PropertyValueFactory<>("answerCorrectNumber"));
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
    }


    private ObservableList<Ranking> fetchRankingFromServer() {
        ObservableList<Ranking> rankingList = FXCollections.observableArrayList();
        try {
            PrintWriter out = ConnectionManager.getInstance().getPrintWriter();
            BufferedReader in = ConnectionManager.getInstance().getBufferedReader();

            out.println("GET_RANKING");
            String jsonResponse = in.readLine();
            System.out.println("Server response: " + jsonResponse);

            // Ensure the server's response is not null
            if (jsonResponse != null && !jsonResponse.isEmpty()) {
                JSONParser parser = new JSONParser();
                JSONArray rankingArray = (JSONArray) parser.parse(jsonResponse);
                System.out.println("Ranking Array: " + rankingArray.toJSONString());

                for (Object obj : rankingArray) {
                    JSONObject rankingJson = (JSONObject) obj;
                    Ranking ranking = new Ranking(
                            (String) rankingJson.get("idSv"), // ID of the student
                            (String) rankingJson.get("name"),  // Name of the student
                            ((Number) rankingJson.get("answerCorrectNumber")).intValue(), // Correct answers
                            ((Number) rankingJson.get("rank")).intValue() // Rank
                    );
                    rankingList.add(ranking);
                }
            }
        } catch (IOException | ParseException e) {
            showAlert("Connection Error", "Unable to retrieve ranking data.");
            e.printStackTrace();
        }
        return rankingList;
    }

    private void loadRankingData(ObservableList<Ranking> rankingData) {
        System.out.println("Loading ranking data..." + rankingData);
        // Sort the ranking data according to the specified criteria
        rankings.setAll(rankingData.sorted(Comparator.comparingInt(Ranking::getAnswerCorrectNumber)
                .reversed() // Sort by number of correct answers (descending)
                .thenComparing(Ranking::getName) // Then by first name (ascending)
        ));


        // Assign ranks from 1 to 100
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1); // Set the rank
        }

        rankingTable.setItems(rankings);
    }
}
