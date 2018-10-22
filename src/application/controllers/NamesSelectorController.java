package application.controllers;

import application.models.NameModelManager;
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

/**Controller class for name selection, it handles all the business logic associated with the functionality found in selection mode
 * and updates the views of the GUI when applicable
 * It also calls on model classes when appropriate and allows them to handle background processes
 */
public class NamesSelectorController {

    //reference for pop-up boxes https://code.makery.ch/blog/javafx-dialogs-official/

    @FXML private TextField _searchBox;

    @FXML private ComboBox<String> __practiceListFiles;

    @FXML private ListView<String> _namesList;

    @FXML private ListView<String> _practiceNamesList;

    @FXML private Button _clearBtn;

    @FXML private Button _saveBtn;

    @FXML private Button _removeBtn;

    @FXML private Button _uploadBtn;

    @FXML private Text _uploadStatus;

    private ObservableList<String> _practiceNames;

    private FilteredList<String> _filteredNames;

    private NameModelManager _nameModelManager;

    private ObservableList<String> _practiceFiles;


    /**Called immediately after controller is constructed, it sets up the button, listview configurations and also sets up the
     * dynamic searching feature
     * @param manager contains all the name models the program finds
     */
    public void initialise(NameModelManager manager){
        _nameModelManager = manager;
        _practiceNames = FXCollections.observableArrayList();
        _practiceNamesList.setItems(_practiceNames);
        _clearBtn.setDisable(true);
        _removeBtn.setDisable(true);
        _saveBtn.setDisable(true);
        _searchBox.setPromptText("Search for names here, use space key to autofill suggested result");
        getPlayListFiles();
        __practiceListFiles.setItems(_practiceFiles);

        //populate listview with names in database
        ObservableList<String> names = FXCollections.observableArrayList(_nameModelManager.getNames());
        _filteredNames = new FilteredList<>(names, e -> true);
        _namesList.setItems(_filteredNames);

        //same code as other search box reference: https://stackoverflow.com/questions/44735486/javafx-scenebuilder-search-listview
        _searchBox.textProperty().addListener((observable, oldValue, newValue) ->{
            _filteredNames.setPredicate(element ->{ //set the rules for how the search box filters out names
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
                if (element.contains("Name not found")){ //if the item in the filtered list is the "Name not found" message then display it
                    return true;
                }
                if (newValue == null || newValue.isEmpty()){
                    return true;
                }
                return false;
            });
            if (_filteredNames.isEmpty()){ //when filtered list is empty, i.e. user enters a name not in the database then add a name not found message
                names.add("Name not found");
            } else if (!_filteredNames.isEmpty() && names.indexOf("Name not found") != -1 && _filteredNames.size() !=1){ //if list is no longer empty then remove message
                names.remove("Name not found");
            }
            _namesList.setItems(_filteredNames);
        });
    }

    /**Returns the user to the main menu and passes the name list model to the main menu, so its state is saved.
     * @param event
     * @throws IOException
     */
    @FXML
    private void goToMain(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/MainMenu.fxml"));
        Parent root = loader.load();
        MainMenuController controller = loader.getController();
        controller.initialise(_nameModelManager);
        Scene scene = new Scene(root);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }


    /**Method called when user presses practice mode button, it checks if the practice list is empty or if it contains invalid names
     *if there is an invalid name or its empty then we display a warning message otherwise we change screens to practice mode
     * @param event
     * @throws IOException
     */
    @FXML
    private void goToCustomMode(ActionEvent event) throws IOException {
        boolean invalidName = false;
        for (String name : _practiceNames){
            if (name.contains("(invalid name)")){ //check if any names in practice list is invalid
                invalidName = true;
            }
        }
        if (invalidName){
            Alert alert = new Alert(Alert.AlertType.INFORMATION); //display warning for invalid name
            alert.setTitle("Invalid name !");
            alert.setHeaderText(null);
            alert.setContentText("There seems to be a name not found in our database in your practice list!");
            alert.showAndWait();
        } else if (_practiceNames.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION); //display warning for empty list
            alert.setTitle("Empty List!");
            alert.setHeaderText(null);
            alert.setContentText("Your practice list is empty! Please select names before continuing");
            alert.showAndWait();
        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/CustomPlayMode.fxml"));//switch scenes
            Parent root = loader.load();
            PracticeModeController controller = loader.getController();
            controller.initialise(_nameModelManager);
            Scene scene = new Scene(root);

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
        }
    }


    /**Method called when add is pressed or user hits enter on keyboard to add the name to practice list
     * it calls addLine() which validates the user's entry
     */
    @FXML
    private void addSelection(){ //add whatever input is in textfield to the custom names list
        if (!_searchBox.getText().isEmpty()){
            addLine(_searchBox.getText());
        }
    }

    /**Method called when user clicks into the names list view, their selection is added into the searchbox
     * @param mouseEvent
     */
    @FXML
    private void addToSearch(MouseEvent mouseEvent){ //add selection to the search box when clicked
        if (mouseEvent.getClickCount() == 1){
            String selection = _namesList.getSelectionModel().getSelectedItem();
            if (selection != null && !selection.equals("Name not found")){ //don't add selection if its "Name not found"
                String currentText = _searchBox.getText();
                currentText = currentText +selection+ " ";
                _searchBox.setText(currentText);
                _searchBox.end();
            }
        }
    }

    /** Called when user presses save button, it prompts user to enter a new file name for their
     * practice list, before saving the new file in the Playlists directory
     */
    @FXML
    private void makeFile(){ //ask the user to make a new practice list file
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Make a new practice list!");
        dialog.setHeaderText("You are about to make a new practice list with these names");
        dialog.setContentText("Please enter a new file name (don't include .txt extension):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            File playList = new File("Playlists/"+result.get()+".txt"); //check if text file user inputs is already taken
            if (playList.exists()){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Overwrite file?");
                alert.setHeaderText("A file already exists with this name");
                alert.setContentText("Overwrite?");

                Optional<ButtonType> entry = alert.showAndWait();
                if (entry.get() == ButtonType.OK){
                    playList.delete();
                    writePlayListFile(playList); // writePlayListFile() creates the file and appends practice list names to the file
                }
            } else {
                writePlayListFile(playList);
            }
        }
    }

    /** Method called by makeFile(), it creates the file passed in as a parameter and appends each name in the current practice list
     *to the text file
     * @param playList a text file containing names
     */
    private void writePlayListFile(File playList){ //write to text file by looping through selected names list and adding it to a new line of txt file
        try {
            playList.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(playList, true));
            PrintWriter writer = new PrintWriter(bw);
            List<String> customNames = _practiceNamesList.getItems();
            for (String name : customNames){
                writer.println(name); //add the name to the file
            }
            writer.close();
            _practiceFiles.clear(); //update the practice list combo-box with the new text file
            __practiceListFiles.getItems().clear();
            getPlayListFiles();
            __practiceListFiles.getItems().addAll(_practiceFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**Method called when a key is pressed in the search box, if the key pressed is a space bar then it triggers the auto-complete
     * code, auto-complete works by removing what the user entered after the latest space or hyphen character and replaces it
     * with the top entry found in the filtered list. If the key press is Enter then add the searchbox name to the practice list
     * @param event key press event
     */
    @FXML
    private void addToSelection(KeyEvent event){
        if (event.getCode().equals(KeyCode.SPACE)){ //autocomplete by taking top result from _namesList and replacing it with user entry
            String topResult = _filteredNames.get(0);
            String currentText = _searchBox.getText();
            if (!topResult.equals("Name not found")) { //do not auto-complete if the top result is "Name not found"
                if (currentText.contains(" ") && currentText.contains("-")) { //ternary operator checks which character out of '-' and ' ' appears last, and appends the top result accordingly
                    currentText = (currentText.lastIndexOf(' ') >= currentText.lastIndexOf('-')) ? currentText.substring(0, currentText.lastIndexOf(' '))+" "+topResult : currentText.substring(0, currentText.lastIndexOf('-'))+"-"+topResult;
                }else if (currentText.contains(" ")) { //only contains space character
                    currentText = currentText.substring(0, currentText.lastIndexOf(' '));
                    currentText = currentText + " " + topResult;
                } else if (currentText.contains("-")) { //only contains hyphens
                    currentText = currentText.substring(0, currentText.lastIndexOf('-'));
                    currentText = currentText + "-" + topResult;
                } else { //does not contain hyphens or spaces i.e. only one name in search box
                    currentText = topResult;
                }
                _searchBox.setText(currentText);
                _searchBox.end();
            }
        }

        if (event.getCode().equals(KeyCode.ENTER)){ //if key press is Enter then add the selection to practice list
            if (!_searchBox.getText().isEmpty()) {
                addLine(_searchBox.getText());
            }
        }
    }

    /**Method called when load button is pressed, it gets the text file that is selected in the
     * combo-box and loads its contents into the practice List View
     */
    @FXML
    private void loadPlayList(){ //load the current txt file in combo box onto the practice list
        String selectedFile = __practiceListFiles.getSelectionModel().getSelectedItem();
        if (selectedFile != null){
            File fileName = new File("Playlists/"+selectedFile);
            loadFile(fileName); //method loads the contents of the file onto the ListView
        }
    }


    /**Method validates the searchbox entry by checking if each individual name in the entry is valid,
     * if there is an invalid name in the string then a warning message is displayed, otherwise the string is added into the practice list
     * @param line current string in the search box
     */
    private void addLine(String line){
        String[] candidateNames = line.split("\\s+"); //split by space characters
        boolean invalid = false; //check if every entry in the string is a valid name in our namesListModel
        String entryName = ""; //this is the string that will be added into the practice list
        for (String name : candidateNames){
            if (name.contains("-")){ //for the split names that contain a hyphen, apply another split on the sub-name
                String[] hyphenatedName = name.split("-");
                for (String hyphenName : hyphenatedName){
                    if (hyphenName.length() != 0) { //check if each name in the sub-name is valid
                        hyphenName = hyphenName.substring(0, 1).toUpperCase() + hyphenName.substring(1);
                        entryName = entryName + hyphenName + "-";
                        if (_nameModelManager.getName(hyphenName) == null) {
                            invalid = true;
                            break;
                        }
                    }
                }
            }else { //sub-name does not contain hyphens
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                if (entryName.contains("-")){
                    entryName = entryName.substring(0, entryName.lastIndexOf('-'))+" "+entryName.substring(entryName.lastIndexOf('-')+1); //replace last hyphen with a space
                }
                entryName = entryName + name + " "; //append sub-name to entry name
                if (_nameModelManager.getName(name) == null) {
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
            entryName = entryName.substring(0,entryName.length()-1); //remove space character at end of string
            _searchBox.clear();
            _practiceNamesList.getItems().add(entryName);
            _clearBtn.setDisable(false);
            _saveBtn.setDisable(false);
        }
    }

    /**Called when clear button is pressed, it wipes the practice list of every name and disables the associated buttons
     */
    @FXML
    private void clearSelections(){
        _practiceNames.clear();
        _clearBtn.setDisable(true);
        _removeBtn.setDisable(true);
        _saveBtn.setDisable(true);
    }

    /**Called when the practice list is clicked into, checks if the list is empty, if it isn't then
     * it enables the buttons associated with the practice list
     */
    @FXML
    private void enablePracticeBtns(){
        if (!_practiceNamesList.getSelectionModel().isEmpty()){
            _removeBtn.setDisable(false);
            _clearBtn.setDisable(false);
            _saveBtn.setDisable(false);
        }
    }

    /**Called when the remove button is pressed, it removes the current name from the practice list
     * and checks if the practice list is empty after the removal
     */
    @FXML
    private void removeSelection(){
        _practiceNames.remove(_practiceNamesList.getSelectionModel().getSelectedItem());
        if (_practiceNames.isEmpty()){
            _clearBtn.setDisable(true);
            _removeBtn.setDisable(true);
            _saveBtn.setDisable(true);
        }
    }

    /**Called by writePlayListFile(), it searches through the Playlists directory and gets
     * every text file stored within
     */
    private void getPlayListFiles(){
        File[] files = new File("Playlists").listFiles();
        List<String> fileNames = new ArrayList<>();
        for (File file : files){
            if (file.isFile()){
                fileNames.add(file.getName());
            }
        }
        _practiceFiles = FXCollections.observableArrayList(fileNames);
    }

    /**Called when upload button is pressed, it opens a file chooser, allowing a user to navigate to anywhere
     * in the computer to choose a txt file if their desired text file is not in our Playlists directory.
     */
    @FXML
    private void uploadFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("text files (*.txt)","*.txt"));
        File file = fileChooser.showOpenDialog(_uploadBtn.getScene().getWindow());
        if (file != null){
            _uploadStatus.setText(file.getName());
            loadFile(file);
        }

    }

    /**Called when loadPlayList() or uploadFile() is called, it loops through the file passed in, and
     * reads each line and if there is a name on the line that is not in the database then the line is
     * given an invalid name tag, after reading each line it outputs the names to the practice list
     */
    private void loadFile(File file){
        Scanner sc = null;
        try {
            sc = new Scanner(file); //read the file
            List<String> names = new ArrayList<>();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] candidateNames = line.split("[-\\s]"); //split line on text file by spaces and hyphen
                boolean invalid = false;
                for (String name: candidateNames) {//check if each name in the line is valid
                    if (!name.isEmpty()) {
                        name = name.substring(0, 1).toUpperCase() + name.substring(1);
                        if (_nameModelManager.getName(name) == null) { //getName() returns null if a name model is not found
                            invalid = true;
                            break;
                        }
                    } else {
                        invalid = true;
                    }
                }
                if (invalid) { //if the line contains a name that is not in the database then add invalid tag
                    names.add(line+" (invalid name)");
                } else {

                    names.add(line);
                }
            }
            _practiceNames.addAll(names);
            _clearBtn.setDisable(false);
            _saveBtn.setDisable(false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**Called when updatePracticeNames is called, it resets the practice list view with the new set of practice names
     */
    private void updateList(){
        _practiceNamesList.getItems().clear();
        _practiceNamesList.setItems(_practiceNames);
    }

    /**Called when PracticeModeController is initialised and wants a copy of the names found in the practice list
     * @return list of names in the practice list
     */
    public List<String> getPracticeNames(){
        return _practiceNames;
    }


    /**Called in the PracticeModeController class when it is going back to selection mode, it updates the practice list
     * with any changes that occurred in the practice mode
     * @param selectedNames list of names in the practice mode before it returned to selection screen
     */
    public void updatePracticeNames(List<String> selectedNames){
        _practiceNames = FXCollections.observableArrayList(selectedNames);
        updateList();
    }

}
