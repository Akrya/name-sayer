package application.controllers;

import application.models.NamesListModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class NamesSelectorController {

    //reference for pop-up boxes https://code.makery.ch/blog/javafx-dialogs-official/

    @FXML
    private TextField searchBox;

    @FXML
    private ComboBox<String> customFiles;

    @FXML
    private ListView<String> namesList;

    @FXML
    private ListView<String> selectedList;

    @FXML
    private Button clearBtn;

    @FXML
    private Button makeFileBtn;

    @FXML
    private Button removeBtn;

    @FXML
    private Button uploadBtn;

    @FXML
    private Text fileUpload;

    private ObservableList<String> _selectedNames;

    private FilteredList<String> _filteredNames;

    private NamesListModel _namesListModel;

    private ObservableList<String> _customFiles;

    @FXML
    private void goToMain(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/MainMenu.fxml"));
        Parent root = loader.load();
        MainMenuController controller = loader.getController();
        controller.initialise(_namesListModel);
        Scene scene = new Scene(root);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void goToCustomMode(ActionEvent event) throws IOException {
        boolean invalidName = false;
        for (String name : _selectedNames){
            if (name.contains("(invalid name)")){
                invalidName = true;
            }
        }
        if (invalidName){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Invalid name !");
            alert.setHeaderText(null);
            alert.setContentText("There seems to be a name not found in our database in your practice list!");
            alert.showAndWait();
        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/CustomPlayMode.fxml"));
            Parent root = loader.load();
            CustomModeController controller = loader.getController();
            controller.initialise(_namesListModel);
            Scene scene = new Scene(root);

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
        }
    }
    @FXML
    private void addSelection(){ //add whatever input is in textfield to the custom names list
        if (!searchBox.getText().isEmpty()){
            addLine(searchBox.getText());
        }
    }

    @FXML
    private void addToSearch(MouseEvent mouseEvent){ //add selection to the search box when double clicked
        if (mouseEvent.getClickCount() == 2){
            String selection = namesList.getSelectionModel().getSelectedItem();
            if (selection != null){
                String currentText = searchBox.getText();
                currentText = currentText +selection+ " ";
                searchBox.setText(currentText);
                searchBox.end();
            }
        }
    }

    @FXML
    private void makeFile(){ //ask the user to make a new custom list file
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Make a new practice list!");
        dialog.setHeaderText("You are about to make a new practice list with these names");
        dialog.setContentText("Please enter a new file name (don't include .txt extension):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            File customFile = new File("Custom/"+result.get()+".txt"); //check if text file user inputs is already taken
            if (customFile.exists()){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Overwrite file?");
                alert.setHeaderText("A file already exists with this name");
                alert.setContentText("Overwrite?");

                Optional<ButtonType> entry = alert.showAndWait();
                if (entry.get() == ButtonType.OK){
                    customFile.delete();
                    writeCustomFile(customFile);
                }
            } else {
                writeCustomFile(customFile);
            }
        }
    }

    private void writeCustomFile(File customFile){ //write to text file by looping through selected names list and adding it to a new line of txt file
        try {
            customFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(customFile, true));
            PrintWriter writer = new PrintWriter(bw);
            List<String> customNames = selectedList.getItems();
            for (String name : customNames){
                writer.println(name);
            }
            writer.close();
            _customFiles.clear();
            customFiles.getItems().clear();
            getCustomFiles();
            customFiles.getItems().addAll(_customFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addToSelection(KeyEvent event){
        if (event.getCode().equals(KeyCode.SPACE)){ //autocomplete by taking top result from namesList and replacing it with user entry
            String topResult = _filteredNames.get(0);
            String currentText = searchBox.getText();
            if (!topResult.equals("Name not found")) {
                if (currentText.contains(" ") && currentText.contains("-")) {
                    currentText = (currentText.lastIndexOf(' ') >= currentText.lastIndexOf('-')) ? currentText.substring(0, currentText.lastIndexOf(' '))+" "+topResult : currentText.substring(0, currentText.lastIndexOf('-'))+"-"+topResult;
                }else if (currentText.contains(" ")) {
                    currentText = currentText.substring(0, currentText.lastIndexOf(' '));
                    currentText = currentText + " " + topResult;
                } else if (currentText.contains("-")) {
                    currentText = currentText.substring(0, currentText.lastIndexOf('-'));
                    currentText = currentText + "-" + topResult;
                } else {
                    currentText = topResult;
                }
                searchBox.setText(currentText);
                searchBox.end();
            }
        }

        if (event.getCode().equals(KeyCode.ENTER)){
            if (!searchBox.getText().isEmpty()) {
                addLine(searchBox.getText());
            }
        }
    }

    @FXML
    private void loadCustomFile(){ //load the current txt file in combo box onto the selected names list
        String selectedFile = customFiles.getSelectionModel().getSelectedItem();
        if (selectedFile != null){
            File fileName = new File("Custom/"+selectedFile);
            loadFile(fileName);
        }
    }

    private void addLine(String line){
        String[] candidateNames = line.split("\\s+");
        boolean invalid = false; //check if every entry in the string is a valid name in our namesListModel
        String entryName = "";
        for (String name : candidateNames){
            if (name.contains("-")){
                String[] hyphenatedName = name.split("-");
                for (String hyphenName : hyphenatedName){
                    if (hyphenName.length() != 0) {
                        hyphenName = hyphenName.substring(0, 1).toUpperCase() + hyphenName.substring(1);
                        entryName = entryName + hyphenName + "-";
                        if (_namesListModel.getName(hyphenName) == null) {
                            invalid = true;
                            break;
                        }
                    }
                }
            }else {
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                if (entryName.contains("-")){
                    entryName = entryName.substring(0, entryName.lastIndexOf('-'))+" "+entryName.substring(entryName.lastIndexOf('-')+1);
                }
                entryName = entryName + name + " ";
                if (_namesListModel.getName(name) == null) {
                    invalid = true;
                    break;
                }
            }
        }
        if (invalid){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Invalid Name!");
            alert.setHeaderText(null);
            alert.setContentText("There seems to be a name in your selection that is not in our database!");
            alert.showAndWait();
        } else {
            entryName = entryName.substring(0,entryName.length()-1);
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

    private void getCustomFiles(){
        File[] files = new File("Custom").listFiles();
        List<String> fileNames = new ArrayList<>();
        for (File file : files){
            if (file.isFile()){
                fileNames.add(file.getName());
            }
        }
        _customFiles = FXCollections.observableArrayList(fileNames);
    }

    @FXML
    private void uploadFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("text files (*.txt)","*.txt"));
        File file = fileChooser.showOpenDialog(uploadBtn.getScene().getWindow());
        if (file != null){
            fileUpload.setText(file.getName());
            loadFile(file);
        }

    }


    private void loadFile(File file){
        Scanner sc = null;
        try {
            sc = new Scanner(file);
            List<String> names = new ArrayList<>();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] candidateNames = line.split("[-\\s]"); //split line on text file by spaces and hyphen
                boolean invalid = false;
                for (String name: candidateNames) {
                    if (!name.isEmpty()) {
                        name = name.substring(0, 1).toUpperCase() + name.substring(1);
                        if (_namesListModel.getName(name) == null) {
                            invalid = true;
                            break;
                        }
                    } else {
                        invalid = true;
                    }
                }
                if (invalid) {
                    names.add(line+" (invalid name)");
                } else {

                    names.add(line);
                }
            }
            _selectedNames.clear();
            _selectedNames.addAll(names);
            clearBtn.setDisable(false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initialise(NamesListModel model){
        _namesListModel = model;
        _selectedNames = FXCollections.observableArrayList();
        selectedList.setItems(_selectedNames);
        clearBtn.setDisable(true);
        removeBtn.setDisable(true);
        makeFileBtn.setDisable(true);
        searchBox.setPromptText("Search for names here, use space key to autofill suggested result ...");
        getCustomFiles();
        customFiles.setItems(_customFiles);

        //populate listview with names in database
        ObservableList<String> names = FXCollections.observableArrayList(_namesListModel.getNames());
        _filteredNames = new FilteredList<>(names, e -> true);
        namesList.setItems(_filteredNames);
        //same code as other search box reference: https://stackoverflow.com/questions/44735486/javafx-scenebuilder-search-listview
        searchBox.textProperty().addListener((observable,oldValue, newValue) ->{
            _filteredNames.setPredicate(element ->{
                String currentText = newValue;
                if (currentText.contains(" ") && currentText.contains("-")){
                    currentText = (currentText.lastIndexOf(' ') >= currentText.lastIndexOf('-')) ? currentText.substring(currentText.lastIndexOf(' ')+1) : currentText.substring(currentText.lastIndexOf('-')+1);
                }else if (currentText.contains(" ")){ //checks if there is a space character, if there is then mark current string from last instance of space character
                    currentText = currentText.substring(currentText.lastIndexOf(' ')+1);
                } else if (currentText.contains("-")){
                    currentText = currentText.substring(currentText.lastIndexOf('-')+1);
                }
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

    public List<String> getSelectedNames(){
        return _selectedNames;
    }

}
