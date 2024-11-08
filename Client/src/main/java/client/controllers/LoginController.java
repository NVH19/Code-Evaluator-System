package client.controllers;

import client.connection.ConnectionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class LoginController extends BaseController {

    @FXML
    private TextField usernameTextfield;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    @FXML
    public void initialize() {
        loginButton.setOnAction(event -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameTextfield.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi đăng nhập", "Tên đăng nhập và mật khẩu không được để trống.");
            return;
        }

        if (!username.equals(password)) {
            showAlert("Lỗi đăng nhập", "Tên đăng nhập và mật khẩu không khớp.");
            return;
        }

        try {
            // Kết nối đến server thông qua ConnectionManager
            ConnectionManager.getInstance().connect(SERVER_HOST, SERVER_PORT);
            PrintWriter out = ConnectionManager.getInstance().getPrintWriter();
            BufferedReader in = ConnectionManager.getInstance().getBufferedReader();

            // Gửi yêu cầu đăng nhập với userId
            out.println("LOGIN:" + username);

            // Đọc phản hồi từ server
            String response = in.readLine();
            System.out.println("Response: " + response);
            if (response != null && response.equals("LOGIN_SUCCESS")) {
                // Đăng nhập thành công, chuyển đến giao diện chính
                goToPage("lobby.fxml", loginButton);
            } else {
                showAlert("Lỗi đăng nhập", response != null ? response : "Đã xảy ra lỗi không xác định.");
            }

        } catch (IOException e) {
            showAlert("Lỗi kết nối", "Không thể kết nối đến server.");
            e.printStackTrace();
        }
    }
}
