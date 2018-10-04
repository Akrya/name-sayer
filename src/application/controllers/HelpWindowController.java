package application.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.io.IOException;

import javafx.scene.control.*;




public class HelpWindowController {

    ///Feels like theres a better way to do this.

    @FXML
    public Button closeButton;

    @FXML
    private void closeWindow(ActionEvent event) throws IOException{

        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
