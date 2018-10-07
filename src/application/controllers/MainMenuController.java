package application.controllers;

import application.models.ControllerManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {

    @FXML
    private Button viewRecordings;

    @FXML
    private Button practiceModeButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button fuseButton;

    private ControllerManager _singleton;

    public void initialize(URL location, ResourceBundle resources) {
        //fill in maybe later

    }

    @FXML
    private void openCustomMode(ActionEvent event) throws IOException {

        _singleton = ControllerManager.getInstance();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/NamesSelector.fxml"));
        Parent root = loader.load();
        NamesSelectorController controller = loader.getController();
        _singleton.setController(controller);
        Scene scene = new Scene(root);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void openListenMode(ActionEvent event) throws IOException {
        Parent listenScene = FXMLLoader.load(getClass().getResource("/application/views/ManageMode.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void openPracticeMode(ActionEvent event) throws IOException {
        Parent listenScene = FXMLLoader.load(getClass().getResource("/application/views/PracticeMode.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void openHelpWindow(ActionEvent event){

        Parent root;
        try{
            root = FXMLLoader.load(getClass().getResource("/application/views/HelpScene.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Help");
            stage.setScene(new Scene(root, 1200, 1000));
            stage.show();

        }

        catch (IOException e){
            e.printStackTrace();
        }
    }


}
