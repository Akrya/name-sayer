package application.uiControllers;

import application.models.NamesListModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class NamesSelectorController implements Initializable {

    @FXML
    private Button practiceBtn;

    @FXML
    private TextField searchBox;

    @FXML
    private ListView<String> namesList;

    @FXML
    private ListView<String> selectedList;

    @FXML
    private Button clearBtn;

    @FXML
    private Button addBtn;

    @FXML
    private Button removeBtn;

    private ObservableList<String> _selectedNames;

    private FilteredList<String> _filteredNames;

    private NamesListModel _namesListModel = new NamesListModel();

    @FXML
    private void goToMain(ActionEvent event) throws IOException {
        Parent listenScene = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void goToPracticeMode(ActionEvent event) throws IOException {
        Parent listenScene = FXMLLoader.load(getClass().getResource("PracticeMode.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }
    @FXML
    private void addSelection(){
        if (namesList.getSelectionModel().getSelectedItem() != null){
            if (_selectedNames.indexOf(namesList.getSelectionModel().getSelectedItem()) == -1){
                if (!namesList.getSelectionModel().getSelectedItem().equals("Name not found")) {
                    _selectedNames.add(namesList.getSelectionModel().getSelectedItem());
                    clearBtn.setDisable(false);
                }
            }
        }
    }

    @FXML
    private void enableAddBtn(MouseEvent mouseEvent){
        if (!_filteredNames.isEmpty() && _filteredNames.size() ==1){
            if (_filteredNames.get(0).equals("Name not found")){
                addBtn.setDisable(true);
            } else {
                addBtn.setDisable(false);
            }
        }
        if (mouseEvent.getClickCount() == 2){
            addSelection();
        }
    }

    @FXML
    private void addAllSelections(){
        List<String> currentNames = namesList.getItems();
        for (String name : currentNames){
            if (_selectedNames.indexOf(name) == -1 && !name.equals("Name not found")){
                _selectedNames.add(name);
            }
        }
        clearBtn.setDisable(false);
    }

    @FXML
    private void clearSelections(){
        _selectedNames.clear();
        clearBtn.setDisable(true);
        removeBtn.setDisable(true);
    }

    @FXML
    private void enableSelectBtns(){
        if (!selectedList.getSelectionModel().isEmpty()){
            removeBtn.setDisable(false);
        }
    }

    @FXML
    private void removeSelection(){
        _selectedNames.remove(selectedList.getSelectionModel().getSelectedItem());
        if (_selectedNames.isEmpty()){
            clearBtn.setDisable(true);
            removeBtn.setDisable(true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _selectedNames = FXCollections.observableArrayList();
        selectedList.setItems(_selectedNames);
        clearBtn.setDisable(true);
        removeBtn.setDisable(true);
        //populate listview with names in database
        ObservableList<String> names = FXCollections.observableArrayList(_namesListModel.getNames());
        _filteredNames = new FilteredList<>(names, e -> true);
        namesList.setItems(_filteredNames);
        //same code as other search box reference: https://stackoverflow.com/questions/44735486/javafx-scenebuilder-search-listview
        searchBox.textProperty().addListener((observable,oldValue, newValue) ->{
            _filteredNames.setPredicate(element ->{
                if (element.length() >= newValue.length()){
                    if (element.toUpperCase().substring(0,newValue.length()).equals(newValue.toUpperCase())){ //filter for names that start with search string
                        return true;
                    }
                }
                if (element.contains("Name not found")){
                    return true;
                }
                if (newValue == null || newValue.isEmpty()){
                    return true;
                }
                return false;
            });
            if (_filteredNames.isEmpty()){
                names.add("Name not found");
            } else if (!_filteredNames.isEmpty() && names.indexOf("Name not found") != -1 && _filteredNames.size() !=1){
                names.remove("Name not found");
            }
            namesList.setItems(_filteredNames);
        });

    }
}
