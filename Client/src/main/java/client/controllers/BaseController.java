package client.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.util.Objects;

public class BaseController {
    protected static final String SERVER_HOST = "localhost";
    protected static final int SERVER_PORT = 2024;

    protected void goToPage(String fxmlFile, Button sourceButton) {
        try {
            Parent page = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/" + fxmlFile)));
            Scene scene = new Scene(page);
            Stage stage = (Stage) sourceButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showAlert("Lỗi tải trang", "Không thể tải trang: " + fxmlFile);
            e.printStackTrace();
        }
    }

    protected void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
