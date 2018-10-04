package application.uiControllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.lang.Object;
public class MainMenuController implements Initializable {

    @FXML
    private Button viewRecordings;

    @FXML
    private Button practiceModeButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button fuseButton;

    public void initialize(URL location, ResourceBundle resources) {
        //fill in maybe later

    }

    @FXML
    private void openPracticeMode(ActionEvent event) throws IOException {
        Parent createScene = FXMLLoader.load(getClass().getResource("PracticeMode.fxml"));
        Scene scene = new Scene(createScene);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void openListenMode(ActionEvent event) throws IOException {
        //Parent listenScene = FXMLLoader.load(getClass().getResource("ListenMode.fxml"));
        Parent listenScene = FXMLLoader.load(getClass().getResource("NewListenMode.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);


    }


}
