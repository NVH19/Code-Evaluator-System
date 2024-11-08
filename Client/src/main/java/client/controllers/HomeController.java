package client.controllers;

import client.connection.ConnectionManager;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import client.models.Exercise;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.io.FileDescriptor.in;
import static java.lang.System.out;

public class HomeController {

    @FXML
    private TableView<Exercise> tableView;
    @FXML
    private TableColumn<Exercise, String> idColumn;
    @FXML
    private TableColumn<Exercise, String> nameColumn;
    @FXML
    private TableColumn<Exercise, String> difficultyColumn;
    @FXML
    private TableColumn<Exercise, String> scoreColumn;
    @FXML
    private TextField searchField;
    private ObservableList<Exercise> exerciseList;

    @FXML
    private Button exerciseButton;
    @FXML
    private Button historyButton;
    @FXML
    private Button rankingButton;
    @FXML
    private Button logoutButton;
    ObservableList<Exercise> exercises = fetchExercisesFromServer();
    @FXML
    private Label countdownLabel;

    private long startTime;
    private long countdownTime;
    @FXML
    private Button outButton;
    @FXML
    public void initialize() {

        // Thiết lập các cột của TableView
        idColumn.setCellValueFactory(new PropertyValueFactory<>("exerciseId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        exerciseButton.setOnAction(event -> goToPage("home.fxml", exerciseButton));
//        exerciseButton.setOnAction(event -> openHome());
        historyButton.setOnAction(event -> goToPage("history.fxml", historyButton));
        rankingButton.setOnAction(event -> goToPage("ranking.fxml", rankingButton));
        logoutButton.setOnAction(event -> handleLogout());

        // Thiết lập sự kiện cho TextField tìm kiếm
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterExercises(newValue);
        });
        // Load exercises into the table
        updateSubmissionsOnHome(fetchHistoryFromServer());
        tableView.setItems(exercises);
        // Set style for table rows based on exercise status
        tableView.setRowFactory(tv -> new TableRow<Exercise>() {
            @Override
            protected void updateItem(Exercise item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if ("AC".equals(item.getStatus())) {
                        setStyle("-fx-background-color: #28a745;"); // Màu xanh lá
                    } else if ("WA".equals(item.getStatus())) {
                        setStyle("-fx-background-color: #dc3545;"); // Màu đỏ
                    } else {
                        setStyle(""); // Không có màu sắc nếu không thuộc trạng thái nào
                    }
                }
            }
        });
        tableView.setOnMouseClicked(this::handleRowClick);


        //Set Timeout cho làm bài thi
        String endTimeString = fetchServerExamEndTime();
        if (!endTimeString.isEmpty()) {
            setupCountdownTimer(endTimeString);
        }
    }

    public void updateSubmissionsOnHome(Map<String, String> uniqueSubmissions) {
        for (Exercise exercise : exercises) {
            String exerciseId = exercise.getExerciseId(); // Giả sử có phương thức getId() trong model Exercise

            // Kiểm tra xem bài tập có trong uniqueSubmissions không
            if (uniqueSubmissions.containsKey(exerciseId)) {
                String status = uniqueSubmissions.get(exerciseId);
                exercise.setStatus(status); // Giả sử có phương thức setStatus() trong model Exercise
            }
        }
    }

    private void setupCountdownTimer(String endTimeString) {
        System.out.println("Setting up countdown timer...");
        System.out.println("End time: " + endTimeString);
//        LocalTime start = LocalTime.of(23,59, 00 ); // Thời gian kết thúc

        // Method 2: Using LocalTime.of()
        String[] timeParts = endTimeString.split(":");
        int hours = Integer.parseInt(timeParts[0]);
        int minutes = Integer.parseInt(timeParts[1]);
        int seconds = Integer.parseInt(timeParts[2]);
        LocalTime start = LocalTime.of(hours, minutes, seconds);

        LocalTime now = LocalTime.now();
        if (now.isBefore(start)) {
            countdownTime = Duration.between(now, start).toMillis();
        } else {
            countdownTime = 0;
        }

        startTime = System.currentTimeMillis() + countdownTime;
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long remainingTime = startTime - System.currentTimeMillis();
                if (remainingTime <= 0) {
                    countdownLabel.setText("00:00");
                    stop();
                    showTimeUpAlert(outButton);
                } else {
                    long seconds = (remainingTime / 1000) % 60;
                    long minutes = (remainingTime / 1000) / 60;
                    countdownLabel.setText(String.format("%02d:%02d", minutes, seconds));
                }
            }
        };
        timer.start();
    }

    private String fetchServerExamEndTime() {
        String endTimeResponse = "";
        try {
            PrintWriter out = ConnectionManager.getInstance().getPrintWriter();
            BufferedReader in = ConnectionManager.getInstance().getBufferedReader();
            out.println("FETCH_EXAM_END_TIME");

            endTimeResponse = in.readLine();

            System.out.println("Received exam end time: " + endTimeResponse);
        } catch (IOException e) {
            System.err.println("Error while fetching exam end time: " + e.getMessage());
        }
        return endTimeResponse;
    }

    private ObservableList<Exercise> fetchExercisesFromServer() {
        ObservableList<Exercise> exerciseList = FXCollections.observableArrayList();
        try {
            PrintWriter out = ConnectionManager.getInstance().getPrintWriter();
            BufferedReader in = ConnectionManager.getInstance().getBufferedReader();

            // Send request to get exercises
            out.println("GET_EXERCISES");

            // Read JSON response
            String jsonResponse = in.readLine();
            System.out.println("Server response: " + jsonResponse);
            JSONParser parser = new JSONParser();
            JSONArray exercisesArray = (JSONArray) parser.parse(jsonResponse);

            for (Object obj : exercisesArray) {
                JSONObject exerciseJson = (JSONObject) obj;
                String exerciseId = (String) exerciseJson.get("exerciseId");
                String name = (String) exerciseJson.get("name");
                String description = (String) exerciseJson.get("description");
                Exercise exercise = new Exercise(exerciseId, name, description);

                exerciseList.add(exercise);
            }
        } catch (IOException | ParseException e) {
            showAlert("Lỗi kết nối", "Không thể kết nối tới server để nhận danh sách bài tập.");
            e.printStackTrace();
        }
        return exerciseList;
    }

    private Map<String, String> fetchHistoryFromServer() {
        Map<String, String> exerciseStatusMap = new HashMap<>();

        try {
            PrintWriter out = ConnectionManager.getInstance().getPrintWriter();
            BufferedReader in = ConnectionManager.getInstance().getBufferedReader();

            out.println("GET_HISTORY");
            String jsonResponse = in.readLine();
            System.out.println("Server response: " + jsonResponse);

            // Đảm bảo rằng phản hồi từ server không null
            if (jsonResponse != null && !jsonResponse.isEmpty()) {
                JSONParser parser = new JSONParser();
                JSONArray historyArray = (JSONArray) parser.parse(jsonResponse);

                for (Object obj : historyArray) {
                    JSONObject historyJson = (JSONObject) obj;
                    String exerciseId = (String) historyJson.get("idExercises");
                    String status = (String) historyJson.get("status");

                    // Cập nhật status cho idExercises nếu id đó đã tồn tại
                    exerciseStatusMap.put(exerciseId, status);
                }
            }
        } catch (IOException | ParseException e) {
            showAlert("Connection Error", "Unable to retrieve history data.");
            e.printStackTrace();
        }
        return exerciseStatusMap;
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void filterExercises(String searchText) {
        ObservableList<Exercise> filteredList = FXCollections.observableArrayList();
        for (Exercise exercise : exercises) {
            if (exercise.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                    exercise.getExerciseId().contains(searchText)) {
                filteredList.add(exercise);
            }
        }
        tableView.setItems(filteredList);
    }

    private void goToPage(String fxmlFile, Button sourceButton) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlFile));
            Scene scene = new Scene(page);
            Stage stage = (Stage) (sourceButton != null ? sourceButton.getScene().getWindow() : Stage.getWindows().get(0)); // Lấy cửa sổ từ sourceButton hoặc cửa sổ đầu tiên
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleRowClick(MouseEvent event) {
        if (event.getClickCount() == 1) {
            Exercise selectedExercise = tableView.getSelectionModel().getSelectedItem();
            if (selectedExercise != null) {
                loadExamPage(selectedExercise);
            }
        }
    }

    private void loadExamPage(Exercise exercise) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercise.fxml"));
            Parent root = loader.load();


            ExamController examController = loader.getController();
            examController.setExercise(exercise);

            Stage stage = (Stage) tableView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showTimeUpAlert(Button outButton) {
        // Chạy trên luồng giao diện người dùng
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText("Hết giờ làm bài!");
            alert.setContentText("Bạn đã hết thời gian làm bài. Bạn có muốn đăng xuất?");

            ButtonType okButton = new ButtonType("OK");
            ButtonType cancelButton = new ButtonType("Cancel");
            alert.getButtonTypes().setAll(okButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();
            result.ifPresent(response -> {
                if (response == okButton) {
                    // Tạo một luồng mới để xử lý xóa lịch sử và chờ phản hồi
                    new Thread(() -> {
                        try {
                            PrintWriter out = ConnectionManager.getInstance().getPrintWriter();
                            BufferedReader in = ConnectionManager.getInstance().getBufferedReader();

                            // Gửi yêu cầu xóa lịch sử
                            out.println("CLEAR_HISTORY");

                            // Đọc phản hồi từ server để xác nhận
                            String serverResponse = in.readLine();
                            if ("HISTORY_CLEARED".equals(serverResponse)) {
                                // Chuyển trang sau khi nhận được xác nhận từ server
                                Platform.runLater(() -> goToPage("login.fxml", null));
                            } else {
                                // Hiển thị thông báo nếu xóa không thành công
                                Platform.runLater(() -> showAlert("Error", "Không thể xóa lịch sử. Vui lòng thử lại."));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Platform.runLater(() -> showAlert("Connection Error", "Không thể kết nối tới server."));
                        }
                    }).start();
                }
            });
        });
    }

    private void handleLogout() {
        goToPage("login.fxml", logoutButton);
        ConnectionManager.getInstance().close();
    }
}
