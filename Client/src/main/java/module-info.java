module org.example.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires json.simple;
    requires java.desktop;

    opens client.models to javafx.base;
    opens fxml to javafx.fxml;
    exports client;
    exports client.controllers;
    opens client.controllers to javafx.fxml;
}