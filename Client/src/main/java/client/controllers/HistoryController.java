package client.controllers;

import client.connection.ConnectionManager;
import client.models.SubmissionHistory;
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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HistoryController extends BaseController {
    @FXML
    private Button exerciseButton, historyButton, rankingButton, logoutButton;
    @FXML
    private TableView<SubmissionHistory> historyTable;
    @FXML
    private TableColumn<SubmissionHistory, Integer> submissionIdColumn;
    @FXML
    private TableColumn<SubmissionHistory, String> exerciseNameColumn, statusColumn, submissionTimeColumn;
    private final ObservableList<SubmissionHistory> histories = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupNavigation();
        setupTableColumns();
        loadHistoryData(fetchHistoryFromServer());
    }

    private void setupNavigation() {
        exerciseButton.setOnAction(event -> goToPage("home.fxml", exerciseButton));
        historyButton.setOnAction(event -> goToPage("history.fxml", historyButton));
        rankingButton.setOnAction(event -> goToPage("ranking.fxml", rankingButton));
        logoutButton.setOnAction(event -> goToPage("login.fxml", logoutButton));
    }

    private void setupTableColumns() {
        submissionIdColumn.setCellValueFactory(new PropertyValueFactory<>("submissionId"));
        exerciseNameColumn.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        submissionTimeColumn.setCellValueFactory(new PropertyValueFactory<>("submissionTime"));

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                    setTextFill("AC".equals(item) ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
                }
            }
        });

        submissionTimeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-alignment: CENTER;");
            }
        });
    }

    private ObservableList<SubmissionHistory> fetchHistoryFromServer() {
        ObservableList<SubmissionHistory> historyList = FXCollections.observableArrayList();
        try  {

            PrintWriter out = ConnectionManager.getInstance().getPrintWriter();
            BufferedReader in = ConnectionManager.getInstance().getBufferedReader();

            out.println("GET_HISTORY");
            String jsonResponse = in.readLine();
            System.out.println("Server response: " + jsonResponse);

            // Ensure the server's response is not null
            if (jsonResponse != null && !jsonResponse.isEmpty()) {
                JSONParser parser = new JSONParser();
                JSONArray historyArray = (JSONArray) parser.parse(jsonResponse);

                for (Object obj : historyArray) {
                    JSONObject historyJson = (JSONObject) obj;
                    SubmissionHistory history = new SubmissionHistory(
                            (String) historyJson.get("idSv"),         // Assuming server response has the correct keys
                            (String) historyJson.get("idExercises"),   // Update to match your server JSON keys
                            (String) historyJson.get("status"),
                            (String) historyJson.get("dateTime")
                    );
                    historyList.addFirst(history);
                }
            }
        } catch (IOException | ParseException e) {
            showAlert("Connection Error", "Unable to retrieve history data.");
            e.printStackTrace();
        }
        return historyList;
    }


    private void loadHistoryData(ObservableList<SubmissionHistory> historyData) {
        historyTable.setItems(historyData);
    }
}
