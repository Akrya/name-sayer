package application.controllers;

import application.models.*;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class MangeModeController{

    @FXML
    private Button rateBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button listenBtn;

    @FXML
    private TextField searchBox;

    @FXML
    private ProgressBar playProgressBar;

    @FXML
    private Label playStatus;

    @FXML
    private Label playRecording;

    @FXML
    private ListView<String> namesList;

    @FXML
    private TableView<RecordingModel> recordingsTable;

    @FXML
    private Label recordingLabel;

    @FXML
    private Label recordingStatus;

    @FXML
    private Button bookMarkBtn;

    private ObservableList<RecordingModel> _recordingModels = FXCollections.observableArrayList();

    @FXML
    private TableColumn<RecordingModel, String> fileCol;

    @FXML
    private TableColumn<RecordingModel, String> ratingCol;

    private NamesListModel _namesListModel;

    private FilteredList<String> _filteredNames;

    private boolean _inAction;

    @FXML
    private Slider volumeSlider;


    @FXML
    private void deleteRecording(){

        RecordingModel selection = recordingsTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            if (!selection.getFileName().substring(0,8).equals("personal")){
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
                    NamesModel selectionModel = _namesListModel.getName(selection.getName());
                    selectionModel.delete(selection.getFileName());
                    List<RecordingModel> records = selectionModel.getRecords();
                    for (RecordingModel record : records) {
                        _recordingModels.add(record);
                    }
                    recordingsTable.getItems().setAll(_recordingModels);
                }
            }
        }
    }

    @FXML
    private void rateRecording(){
        RecordingModel selection = recordingsTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            String name = selection.getName();
            RecordingRater rater = new RecordingRater(selection.getFileName(), selection); //make new rater object
            boolean exists = rater.checkFile(); //if rating exists ask if they want to overwrite
            if (exists) {
                rater.overWriteRating();
            } else if (selection.getRating().equals("Good ★")) {
                boolean overwritten = rater.overWriteFavRating();
                if (overwritten){
                    NamesModel namesModel = _namesListModel.getName(name);
                    namesModel.setFavourite(false);
                }
            } else {
                rater.makeRating();
            }
            recordingsTable.getItems().clear(); //update table with new ratings by resetting the recordings list
            _recordingModels.clear();
            NamesModel model = _namesListModel.getName(name);
            List<RecordingModel> records = model.getRecords();
            for (RecordingModel record : records) {
                _recordingModels.add(record);
            }
            recordingsTable.getItems().setAll(_recordingModels);
        }
    }


    @FXML
    private void playRecording(){
        RecordingModel selection = recordingsTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            deleteBtn.setDisable(true);
            rateBtn.setDisable(true);
            bookMarkBtn.setDisable(true);
            listenBtn.setDisable(true);
            playStatus.setText("Now playing: ");
            playRecording.setText(selection.getFileName());
            _inAction = true;
            String filePath;
            if (selection.getFileName().substring(0, 8).equals("personal")) {
                filePath = "Single/" + selection.getFileName();
            }else {
                filePath = "Database/" + selection.getFileName(); //get file path to the recording and pass it into player
            }
            RecordingPlayer player = new RecordingPlayer(filePath);
            playProgressBar.progressProperty().unbind();
            playProgressBar.progressProperty().bind(player.progressProperty());
            player.setOnSucceeded(e -> {
                _inAction = false;
                rateBtn.setDisable(false);
                listenBtn.setDisable(false);
                deleteBtn.setDisable(false);
                bookMarkBtn.setDisable(false);
                playStatus.setText("No recording currently playing");
                playRecording.setText("");
            });
            new Thread(player).start();
        }
    }

    @FXML
    private void clearSearch(){
        searchBox.setText("");
    }

    @FXML
    private void enableListen(MouseEvent mouseEvent){
        if (recordingsTable.getSelectionModel().getSelectedItem() != null){
            rateBtn.setDisable(false);
            bookMarkBtn.setDisable(false);
            listenBtn.setDisable(false);
            deleteBtn.setDisable(false);
        }
        if (mouseEvent.getClickCount() == 2 && !_inAction){
            playRecording();
        }
    }

    @FXML
    private void getRecordings(){
        String selection = namesList.getSelectionModel().getSelectedItem();
        if (selection == null){
            selection = _filteredNames.get(0);
        }
        if (!selection.equals("Name not found")){
            _recordingModels.clear();
            NamesModel model = _namesListModel.getName(selection);
            List<RecordingModel> records = model.getRecords();
            for (RecordingModel record : records){
                _recordingModels.add(record);
            }
            recordingsTable.getItems().setAll(_recordingModels);
            recordingStatus.setText("Recordings for: ");
            recordingLabel.setText(selection);
        }
    }

    /**this method takes the currently selected recording in the table and asks user
     *if they would like to set it as their preferred recording which is used in practice mode
     */
    @FXML
    private void bookMarkRecording(){
        RecordingModel selection = recordingsTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            RecordingBookmarker bookmarker = new RecordingBookmarker(selection);
            NamesModel model = _namesListModel.getName(selection.getName());
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
                    recordingsTable.getItems().setAll(_recordingModels);
                }
            } else {
                if(bookmarker.setAsFavourite()) { //if user wants to bookmark a recording for the name then update the table
                    _recordingModels.clear();
                    model.setFavourite(true);
                    List<RecordingModel> records = model.getRecords();
                    for (RecordingModel record : records) { //repopulate the tableview with recordings
                        _recordingModels.add(record);
                    }
                    recordingsTable.getItems().setAll(_recordingModels);
                }
            }
        }
    }

    //takes you to the main menu
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


    /** method is called immediately after the controller is constructed, it sets up the button configurations for the scene and sets up the dynamic searching feature
     */
    public void initialise(NamesListModel model) {
        _namesListModel = model;

        //disable all buttons on start up except for testing mic and creating recording
        deleteBtn.setDisable(true);
        rateBtn.setDisable(true);
        bookMarkBtn.setDisable(true);
        listenBtn.setDisable(true);
        searchBox.setPromptText("Search...");

        makeRatingFile();
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName")); //bind two columns to RecordingModel class
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        recordingsTable.getItems().setAll(_recordingModels);


        //reference for search box https://stackoverflow.com/questions/44735486/javafx-scenebuilder-search-listview
        ObservableList<String> names = FXCollections.observableArrayList(_namesListModel.getNames());
        _filteredNames = new FilteredList<>(names, e -> true);
        namesList.setItems(_filteredNames);
        searchBox.textProperty().addListener((observable,oldValue, newValue) ->{
            _filteredNames.setPredicate(element ->{
                if (newValue == null || newValue.isEmpty()){
                    return true;
                }
                if (element.length() >= newValue.length()){
                    if (element.toUpperCase().substring(0,newValue.length()).equals(newValue.toUpperCase())){ //filter for names that start with search string
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
            namesList.setItems(_filteredNames);
        });
        startVolumeSlider();
    }


    private void startVolumeSlider(){

        //running command to get current volume https://unix.stackexchange.com/questions/89571/how-to-get-volume-level-from-the-command-line/89581
        String cmd1 = "amixer get Master | awk '$0~/%/{print $4}' | tr -d '[]%'";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd1);

        //setting the slider to the current volume
        try{
            Process volumeInitializer = builder.start();
            InputStream inputStream = volumeInitializer.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String volumeLevel = br.readLine();
            double vlevel = Double.parseDouble(volumeLevel);
            volumeSlider.setValue(vlevel);

        } catch (IOException e){
            e.printStackTrace();
        }


        //A listener gets the value from slider and runs a bash command with that changes the volume based on the value

        //https://www.youtube.com/watch?v=X9mEBGXX3dA reference
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                double volume = volumeSlider.getValue();
                //System.out.println(volume);
                String cmd2 = "amixer set 'Master' " + volume + "%";
                ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd2);
                try {
                    builder.start();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void makeRatingFile(){
        File rateFile = new File("Ratings.txt"); //make file if it doesnt exist ie on first start of program
        if(rateFile.exists()) {
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

    private void getRatings(){ //can be improved greatly right now O(n^2), function loops through rating.txt and finds the model associated with the recording then assigns a bad rating
        List<NamesModel> models = _namesListModel.getModels();
        Map<String, Integer> fileMap = new HashMap<>();
        List<String> fileNames = new ArrayList<>();
        List<RecordingModel> recordings = new ArrayList<>();
        for (NamesModel model : models){
            List<RecordingModel> records = model.getRecords();
            for (RecordingModel record : records){
                recordings.add(record);
                fileMap.put(record.getFileName(), 0);
                fileNames.add(record.getFileName());
            }
        }
        try {
            Scanner scanner = new Scanner(new File("Ratings.txt"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                int index =  fileNames.indexOf(line);
                if (index != -1){
                    fileMap.put(fileNames.get(index),1);
                }
            }
            for (Map.Entry<String,Integer> entry : fileMap.entrySet()){
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
