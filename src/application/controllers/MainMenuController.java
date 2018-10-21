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

    private ControllerManager _manager;

    private NamesListModel _model;

    /** Called when practice mode button is pressed on main menu, we direct the user to the name selection screen after
     */
    @FXML
    private void openSelectMode(ActionEvent event) throws IOException {

        _manager = ControllerManager.getInstance();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/NamesSelector.fxml"));
        Parent root = loader.load();
        NamesSelectorController controller = loader.getController();
        _manager.setController(controller); //pass the manager a reference to the name selector controller, which will be used in the practice mode screen
        controller.initialise(_model);
        Scene scene = new Scene(root);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    /** Called when the Manage mode button is pressed, it directs the user to the manage mode screen after
     */
    @FXML
    private void openManageMode(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/ManageMode.fxml"));
        Parent root = loader.load();
        MangeModeController controller = loader.getController();
        controller.initialise(_model);
        Scene scene = new Scene(root);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    /** Called when the help button is pressed, it opens up the help window which contains our user manual
     */
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

    /** Called when Main menu controller is constructed it gets passed a reference to the name list model
     */
    public void initialise(NamesListModel model){
        _model = model;
    }

}
