package application.uiControllers;

import application.models.NameSelectorSingleton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

public class CustomModeController implements Initializable {


    private NameSelectorSingleton _singleton;

    private NamesSelectorController _controller;

    private ObservableList<String> _selectedNames;

    @FXML
    private ListView<String> selectedNames;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _singleton = NameSelectorSingleton.getInstance();
        _controller = _singleton.getController();
        _selectedNames = FXCollections.observableArrayList(_controller.getSelectedNames());
        for (String s : _selectedNames){
            System.out.println(s);
        }
        selectedNames.setItems(_selectedNames);

    }
}
