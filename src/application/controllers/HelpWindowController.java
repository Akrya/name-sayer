package application.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.io.IOException;

import javafx.scene.control.*;

public class HelpWindowController {
    @FXML public Button _closeBtn;

    @FXML
    private void closeWindow(ActionEvent event) throws IOException{
        Stage stage = (Stage) _closeBtn.getScene().getWindow();
        stage.close();
    }
}
