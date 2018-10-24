package application.controllers;

import application.models.CSSManager;
import application.models.ControllerManager;
import application.models.NameModelManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;


import java.io.IOException;

public class MainMenuController implements Controller{


    @FXML private RadioButton themeSwitch;

    private CSSManager _cssManager;

    private ControllerManager _manager;

    private NameModelManager _nameModelManager;

    /**Called when practice mode button is pressed on main menu, we direct the user to the name selection screen after
     * @param event
     * @throws IOException
     */
    @FXML
    private void openSelectMode(ActionEvent event) throws IOException {

        _manager = ControllerManager.getInstance();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/NamesSelector.fxml"));
        Parent root = loader.load();
        root.getStylesheets().clear();
        root.getStylesheets().add(_cssManager.cssTheme);
        NamesSelectorController controller = loader.getController();
        _manager.setController(controller); //pass the manager a reference to the name selector controller, which will be used in the practice mode screen
        controller.initialise(_nameModelManager, _cssManager);
        Scene scene = new Scene(root);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    /**Called when the Manage mode button is pressed, it directs the user to the manage mode screen after
     * @param event
     * @throws IOException
     */
    @FXML
    private void openManageMode(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/ManageMode.fxml"));
        Parent root = loader.load();
        root.getStylesheets().clear();
        root.getStylesheets().add(_cssManager.cssTheme);
        MangeModeController controller = loader.getController();
        controller.initialise(_nameModelManager, _cssManager);
        Scene scene = new Scene(root);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }


    /**Called when the help button is pressed, it opens up the help window which contains our user manual
     * @param event
     */
    @FXML
    private void openHelpWindow(ActionEvent event){
        try {
            String cmd1 = " xdg-open NameSayer_Manual.pdf";
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd1);
            builder.start();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Called when the radioButton "Light Theme" is called. It toggles the CSS file currently being used and
     * reloads the MainMenu window to show the css change.
     * @param event
     * @throws IOException
     */

    @FXML
    private void changeTheme(ActionEvent event) throws IOException{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/MainMenu.fxml"));
        Parent root = loader.load();

        if(themeSwitch.isSelected()){
            _cssManager.switchLight();
            _cssManager.isLight = true;
        }
        else{
            _cssManager.switchDark();
            _cssManager.isLight = false;

        }
        root.getStylesheets().clear();
        root.getStylesheets().add(_cssManager.cssTheme);

        MainMenuController controller = loader.getController();
        controller.initialise(_nameModelManager, _cssManager);
        Scene scene = new Scene(root);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);

    }


    /**Called when Main menu controller is constructed it gets passed a reference to the name model manager
     * radiobutton is toggled if css theme is light theme
     * @param manager contains all the name models the program finds
     */
    public void initialise(NameModelManager manager, CSSManager cssManager){
        _nameModelManager = manager;
        _cssManager = cssManager;

        if(_cssManager.isLight){
            themeSwitch.setSelected(true);
        }

    }

}
