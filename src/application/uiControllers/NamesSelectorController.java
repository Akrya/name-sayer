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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    private Button makeFileBtn;

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
    private void addSelection(){ //add whatever input is in textfield to the custom names list
        if (!searchBox.getText().isEmpty()){
            addLine(searchBox.getText());
        }
    }

//    @FXML
//    private void enableAddBtn(MouseEvent mouseEvent){
//        if (!_filteredNames.isEmpty() && _filteredNames.size() ==1){
//            if (_filteredNames.get(0).equals("Name not found")){
//                addBtn.setDisable(true);
//            } else {
//                addBtn.setDisable(false);
//            }
//        }
//        if (mouseEvent.getClickCount() == 2){
//            addSelection();
//        }
//    }

    @FXML
    private void makeFile(){

    }

    @FXML
    private void addToSelection(KeyEvent event){
        if (event.getCode().equals(KeyCode.SPACE)){ //autocomplete by taking top result from namesList and replacing it with user entry
            String topResult = _filteredNames.get(0);
                String currentText = searchBox.getText();
                if (!topResult.equals("Name not found")) {
                    if (currentText.contains(" ")) {
                        currentText = currentText.substring(0, currentText.lastIndexOf(' '));
                        currentText = currentText + " " + topResult;
                        searchBox.setText(currentText);
                        searchBox.end();
                    } else {
                        currentText = topResult;
                        searchBox.setText(currentText);
                        searchBox.endOfNextWord();
                    }
                }
        }

        if (event.getCode().equals(KeyCode.ENTER)){
            addLine(searchBox.getText());
        }
    }

    private void addLine(String line){
        String[] candidateNames = line.split("\\s+");
        boolean invalid = false; //check if every entry in the string is a valid name in our namesListModel
        String entryName = "";
        for (String name : candidateNames){
            name = name.substring(0,1).toUpperCase()+name.substring(1);
            entryName = entryName + name+ " ";
            if (_namesListModel.getName(name) == null){
                invalid = true;
                break;
            }
        }
        if (invalid){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Invalid Name!");
            alert.setHeaderText(null);
            alert.setContentText("There seems to be a name in your selection that is not in our database!");
            alert.showAndWait();
        } else {
            searchBox.clear();
            selectedList.getItems().add(entryName);
            clearBtn.setDisable(false);
            makeFileBtn.setDisable(false);
        }
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
        makeFileBtn.setDisable(true);
        searchBox.setPromptText("Search...");
        //populate listview with names in database
        ObservableList<String> names = FXCollections.observableArrayList(_namesListModel.getNames());
        _filteredNames = new FilteredList<>(names, e -> true);
        namesList.setItems(_filteredNames);
        //same code as other search box reference: https://stackoverflow.com/questions/44735486/javafx-scenebuilder-search-listview
        searchBox.textProperty().addListener((observable,oldValue, newValue) ->{
            _filteredNames.setPredicate(element ->{
                String currentText = newValue;
                if (currentText.contains(" ")){
                    currentText = currentText.substring(currentText.lastIndexOf(' ')+1);
                }
//                System.out.println(newValue);
//                System.out.println(currentText);
                if (element.length() >= currentText.length()){
                    if (element.toUpperCase().substring(0,currentText.length()).equals(currentText.toUpperCase())){ //filter for names that start with search string
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
