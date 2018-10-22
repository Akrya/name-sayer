package application.controllers;

import application.models.*;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

/**Controller class for manage mode, it handles all the business logic associated with the functionality found in manage mode
 * and updates the views of the GUI when applicable
 * It also calls on model classes when appropriate and allows them to handle background processes such as audio playback
 */
public class MangeModeController{

    private NameModelManager _nameModelManager;

    private FilteredList<String> _filteredNames; //this list changes depending on what the user searches, it is originally a copy of all names stored

    private boolean _inAction;

    private ObservableList<RecordingModel> _recordingModels = FXCollections.observableArrayList(); //list of recording models which is displayed in the recording table view

    private RecordingPlayer _player = null;

    private VolumeManager _volumeManager;

    @FXML private Button _rateBtn;

    @FXML private Button _deleteBtn;

    @FXML private Button _listenBtn;

    @FXML private Button _favouriteBtn;

    @FXML private TextField _searchBox;

    @FXML private ProgressBar _audioProgressBar;

    @FXML private Label _playBackStatus;

    @FXML private Label _recordingInPlay;

    @FXML private ListView<String> _namesList; //list view which is binded to filterenames, it is dynamically updated as the user searches

    @FXML private TableView<RecordingModel> _recordingsTable;

    @FXML private Label _recordingsTableName;

    @FXML private Label _recordingsTableStatus;

    @FXML private TableColumn<RecordingModel, String> _fileNameColumn;

    @FXML private TableColumn<RecordingModel, String> _ratingColumn;

    @FXML private Slider _volumeSlider;

    @FXML private Text _listenBtnText;

    @FXML private ImageView _listenBtnImage;

    private CSSManager _cssManager;


    /**method is called immediately after the controller is constructed, it sets up the button configurations for the scene and sets up the dynamic searching feature
     * @param model name list model contains all the name models the program finds
     */
    public void initialise(NameModelManager model, CSSManager cssManager) {
        _nameModelManager = model;
        _cssManager = cssManager;

        //disable all buttons on start up except for testing mic and creating recording
        _deleteBtn.setDisable(true);
        _rateBtn.setDisable(true);
        _favouriteBtn.setDisable(true);
        _listenBtn.setDisable(true);
        _searchBox.setPromptText("Search...");

        makeRatingFile();
        _fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName")); //bind two columns to RecordingModel class
        _ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        _recordingsTable.getItems().setAll(_recordingModels);


        //reference for search box https://stackoverflow.com/questions/44735486/javafx-scenebuilder-search-listview
        ObservableList<String> names = FXCollections.observableArrayList(_nameModelManager.getNames());
        _filteredNames = new FilteredList<>(names, e -> true);
        _namesList.setItems(_filteredNames);
        _searchBox.textProperty().addListener((observable, oldValue, newValue) ->{ //add a listener to the search box, so filteredNames changes when user enters a letter
            _filteredNames.setPredicate(element ->{ //setPredicate() defines the rules for what items in the filteredNames list is removed and which ones remain
                if (newValue == null || newValue.isEmpty()){
                    return true;
                }
                if (element.length() >= newValue.length()){
                    if (element.toUpperCase().substring(0,newValue.length()).equals(newValue.toUpperCase())){ //allow names that start with search string to remain
                        return true;
                    }
                }
                if (element.contains("Name not found")){
                    return true;
                }
                return false;
            });
            if (_filteredNames.isEmpty()){
                names.add("Name not found");
            } else if (!_filteredNames.isEmpty() && names.indexOf("Name not found") != -1 && _filteredNames.size() !=1){
                names.remove("Name not found");
            }
            if (_filteredNames.size() == 1) { //automatically update recordings table if only one item in the search results
                getRecordings();
            }
            _namesList.setItems(_filteredNames);
        });

        //initialise the volume by creating a volume manager which sets up the slider
        _volumeManager = new VolumeManager(_volumeSlider);
        _volumeManager.startVolumeSlider();
    }


    /**Called when delete button is pressed, Prompts the user if they want to continue deleting the selected recording
     * if the selected recording cannot be deleted then display a warning message
     */
    @FXML
    private void deleteRecording(){

        RecordingModel selection = _recordingsTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            if (!selection.getFileName().substring(0,8).equals("personal")){ //i.e trying to delete a database recording
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Invalid deletion");
                alert.setHeaderText(null);
                alert.setContentText("Please select a personal recording to delete, database recordings cannot be deleted!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete?");
                alert.setHeaderText("You are about to delete '" + selection.getFileName() + "'");
                alert.setContentText("Hit Ok to confirm or Cancel to return to menu");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    _recordingModels.clear();
                    NameModel selectionModel = _nameModelManager.getName(selection.getName()); //find name model of the recording and delete the recording from its list
                    selectionModel.delete(selection.getFileName());
                    List<RecordingModel> records = selectionModel.getRecords();
                    for (RecordingModel record : records) { //re-populate the table view with recordings
                        _recordingModels.add(record);
                    }
                    _recordingsTable.getItems().setAll(_recordingModels);
                }
            }
        }
    }

    /**Called when user presses the flag button, prompts the useer if they want to give the selected recording a bad rating
     * if the recording already has a bad rating or is favourited then prompts the user if they want to overwrite the rating
     */
    @FXML
    private void rateRecording(){
        RecordingModel selection = _recordingsTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            String name = selection.getName();
            RecordingRater rater = new RecordingRater(selection); //make new rater object
            boolean exists = rater.checkFile(); //if rating exists ask if they want to overwrite
            if (exists) {
                rater.overWriteRating();
            } else if (selection.getRating().equals("Good ★")) { //if record is favourited then ask if they want to overwrite
                boolean overwritten = rater.overWriteFavRating();
                if (overwritten){
                    NameModel nameModel = _nameModelManager.getName(name);
                    nameModel.setFavourite(false);
                }
            } else {
                rater.makeRating();
            }
            _recordingsTable.getItems().clear(); //update table with new ratings by resetting the recordings table
            _recordingModels.clear();
            NameModel model = _nameModelManager.getName(name);
            List<RecordingModel> records = model.getRecords();
            for (RecordingModel record : records) {
                _recordingModels.add(record);
            }
            _recordingsTable.getItems().setAll(_recordingModels);
        }
    }

    /** Method is called when an audio action has occcurred (either playback or recording)
     * it turns on and off the buttons depending on what boolean is passed in
     */
    private void switchButtonStates(boolean flip){
        _deleteBtn.setDisable(flip);
        _rateBtn.setDisable(flip);
        _favouriteBtn.setDisable(flip);
        _listenBtn.setDisable(flip);
    }

    /**Called when stop button is pressed
     * it stops the audio coming from the players and re enables the buttons
     */
    private void stopAudio(){
        if (_player != null){
            _audioProgressBar.progressProperty().unbind();
            _audioProgressBar.setProgress(0);
            _player.stopAudio();
            _player.cleanUpFiles();
            _player.cancel();
            _inAction = false;
            _player = null;
        }
        switchButtonStates(_inAction);
    }

    /**Called when the user presses the listen button, it plays the selected recording then changes to a stop button,
     * if user presses stop button while audio is playing then audio is stopped.
     */
    @FXML
    private void playRecording(){
        if (_inAction){ //i.e a recording is playing at the moment
            stopAudio();
            switchButtonStates(false);
            _inAction = false;
            _listenBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/images/musical-note.png"))); //change back to a normal listen button
            _listenBtnText.setText("Listen");
        } else { //i.e the player is not in action
            RecordingModel selection = _recordingsTable.getSelectionModel().getSelectedItem();
            if (selection != null) {
                switchButtonStates(true);
                _listenBtn.setDisable(false);
                _playBackStatus.setText("Now playing: ");
                _recordingInPlay.setText(selection.getFileName());
                _inAction = true;
                String filePath;
                if (selection.getFileName().substring(0, 8).equals("personal")) {
                    filePath = "Single/" + selection.getFileName();
                } else {
                    filePath = "Database/" + selection.getFileName(); //get file path to the recording and pass it into player
                }
                _player = new RecordingPlayer(filePath);
                _audioProgressBar.progressProperty().unbind();
                _audioProgressBar.progressProperty().bind(_player.progressProperty());
                _listenBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/images/stop.png"))); //change back to a normal listen button
                _listenBtnText.setText("Stop");
                _player.setOnSucceeded(e -> {
                    _inAction = false;
                    switchButtonStates(false);
                    _playBackStatus.setText("No recording currently playing");
                    _recordingInPlay.setText("");
                    _listenBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/images/musical-note.png"))); //change back to a normal listen button
                    _listenBtnText.setText("Listen");
                    _player = null;
                });
                new Thread(_player).start();
            }
        }
    }

    /**Method is called when the clear search button is pressed, it resets the search box making it empty
     */
    @FXML
    private void clearSearch(){
        _searchBox.setText("");
    }

    /**Called when user mouse clicks into the recording table, if a double click is registered then it auto plays the recording
     * if no item is selected then it disables the buttons
     */
    @FXML
    private void enableListen(MouseEvent mouseEvent){
        if (_recordingsTable.getSelectionModel().getSelectedItem() != null){
            switchButtonStates(false);
        }
        if (mouseEvent.getClickCount() == 2 && !_inAction){
            playRecording();
        }
    }

    /**Called when user selects a name in the list, it fetches the associated recordings of the name
     * and populates the recording table with the recordings
     */
    @FXML
    private void getRecordings(){
        String selection = _namesList.getSelectionModel().getSelectedItem();
        if (selection == null){
            selection = _filteredNames.get(0);
        }
        if (!selection.equals("Name not found")){ //populate recordings table by retrieving the name model of the name and getting its records
            _recordingModels.clear();
            NameModel model = _nameModelManager.getName(selection);
            List<RecordingModel> records = model.getRecords();
            for (RecordingModel record : records){
                _recordingModels.add(record);
            }
            _recordingsTable.getItems().setAll(_recordingModels);
            _recordingsTableStatus.setText("Recordings for: ");
            _recordingsTableName.setText(selection);
        }
    }

    /**this method takes the currently selected recording in the table and asks user
     *if they would like to set it as their preferred recording which is used in practice mode
     */
    @FXML
    private void bookMarkRecording(){
        RecordingModel selection = _recordingsTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            RecordingBookmarker bookmarker = new RecordingBookmarker(selection);
            NameModel model = _nameModelManager.getName(selection.getName());
            if (selection.getRating().equals("Bad") || selection.getFileName().contains("personal")) {
                bookmarker.sendInvalidMessage();//if the recording has a bad rating then send a warning message telling user you can't bookmark a bad recording
            }else if (model.hasFavourite()){
                if(bookmarker.overwriteFavourite()){  //if the name already has a bookmarked recording then ask the user if they want to change their preferred recording
                    _recordingModels.clear();
                    selection.setFavourite(true);
                    List<RecordingModel> records = model.getRecords();
                    for (RecordingModel record: records){
                        if (record.getRating().equals("Good ★") && !record.equals(selection)){ //search for old bookmarked recording and remove its favourite status
                            record.setFavourite(false);
                        }
                        _recordingModels.add(record);
                    }
                    _recordingsTable.getItems().setAll(_recordingModels);
                }
            } else {
                if(bookmarker.setAsFavourite()) { //if user wants to bookmark a recording for the name then update the table
                    _recordingModels.clear();
                    model.setFavourite(true);
                    List<RecordingModel> records = model.getRecords();
                    for (RecordingModel record : records) { //repopulate the tableview with recordings
                        _recordingModels.add(record);
                    }
                    _recordingsTable.getItems().setAll(_recordingModels);
                }
            }
        }
    }

    /** Method is called when home button is pressed, it returns the user back to the main menu and passes the name list model
     * to the new controller
     */
    @FXML
    private void goToMain(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/MainMenu.fxml"));
        Parent root = loader.load();
        root.getStylesheets().clear();
        root.getStylesheets().add(_cssManager.cssTheme);
        MainMenuController controller = loader.getController();
        Scene scene = new Scene(root);
        controller.initialise(_nameModelManager, _cssManager);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    /**Called when this controller is instantiated, it creates the rating.txt file if it doesn't exist (i.e. first time launching)
     */
    private void makeRatingFile(){
        File rateFile = new File("Ratings.txt");
        if(rateFile.exists()) { //if file exists then retrieve the ratings of all the names
            getRatings();
            return;
        } else {
            try {
                rateFile.createNewFile(); //make file if first time using program
                BufferedWriter bw = new BufferedWriter(new FileWriter("Ratings.txt", true));
                PrintWriter writer = new PrintWriter(bw);
                writer.println("This is the ratings for the recordings stored in the Original and Personal databases");
                writer.println("Each recording stored in this file has a'Bad'rating");
                writer.println("");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**Called if the rating file exists, it loops through the whole txt file,
     * and grabs each name on each line, it finds the associated recording model and assigns it a bad rating
     */
    private void getRatings(){
        List<NameModel> models = _nameModelManager.getModels();
        Map<String, Integer> fileMap = new HashMap<>(); //map of files, filename is the key and its value is an integer, 0 means the file has good rating, 1 means file has bad rating
        List<String> fileNames = new ArrayList<>();
        List<RecordingModel> recordings = new ArrayList<>();
        for (NameModel model : models){ //loop through every model and get all its recordings
            List<RecordingModel> records = model.getRecords();
            for (RecordingModel record : records){
                recordings.add(record);
                fileMap.put(record.getFileName(), 0); //by default each file has good rating
                fileNames.add(record.getFileName());
            }
        }
        try {
            Scanner scanner = new Scanner(new File("Ratings.txt"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                int index =  fileNames.indexOf(line);
                if (index != -1){
                    fileMap.put(fileNames.get(index),1); //if name is found in the file then it has a bad rating, so it is assigned value of 1
                }
            }
            for (Map.Entry<String,Integer> entry : fileMap.entrySet()){ //for every file in the map with a value of 1, set a bad rating on its recording model
                if (entry.getValue() == 1){
                    for (RecordingModel record : recordings){
                        if (record.getFileName().equals(entry.getKey())){
                            record.setRating(false);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
}
