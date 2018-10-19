package application.controllers;

import application.models.ControllerManager;
import application.models.NamesListModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuController {

    private ControllerManager _singleton;

    private NamesListModel _model;

    @FXML
    private void openCustomMode(ActionEvent event) throws IOException {

        _singleton = ControllerManager.getInstance();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/NamesSelector.fxml"));
        Parent root = loader.load();
        NamesSelectorController controller = loader.getController();
        _singleton.setController(controller);
        controller.initialise(_model);
        Scene scene = new Scene(root);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void openListenMode(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/ManageMode.fxml"));
        Parent root = loader.load();
        MangeModeController controller = loader.getController();
        controller.initialise(_model);
        Scene scene = new Scene(root);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void openPracticeMode(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/PracticeMode.fxml"));
        Parent root = loader.load();
        PracticeModeController controller = loader.getController();
        controller.setModel(_model);
        Scene scene = new Scene(root);

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
            stage.setScene(new Scene(root, 960, 1000));
            stage.show();

        }

        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void initialise(NamesListModel model){
        _model = model;
    }

}
