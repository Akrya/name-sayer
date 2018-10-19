package application.controllers;

import application.models.*;
import com.sun.prism.impl.Disposer;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.*;

public class MangeModeController implements Initializable {

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

    private ObservableList<RecordingModel> _recordingModels = FXCollections.observableArrayList();

    @FXML
    private TableColumn<RecordingModel, String> fileCol;

    @FXML
    private TableColumn<RecordingModel, String> ratingCol;

    private NamesListModel _namesListModel = new NamesListModel();

    private FilteredList<String> _filteredNames;

    @FXML
    private Slider volumeSlider;

    private boolean isPlaying = false;



    /*
    method for adding selected name into the playlist, a name already in the playlist cannot be added
     */
//    @FXML
//    private void addToQueue(){
//        RecordingModel selected = recordingsTable.getSelectionModel().getSelectedItem(); //get the selected recording and put in playlist
//        if (selected != null){
//            String recording = selected.getFileName();
//            if (_queuedRecordings.indexOf(recording) == -1){
//                _queuedRecordings.add(recording);
//                clearBtn.setDisable(false);           //buttons related to the playlist are enabled
//                randomiseBtn.setDisable(false);
//            }
//        }
//    }

//    //method for adding all selected names into the playlist, names already in the playlist will not be added
//    @FXML
//    private void addAllToQueue(){
//        List<RecordingModel> selected = recordingsTable.getItems(); //get all entries in recordings and add to playlist
//
//        for (RecordingModel recording: selected){
//            String recordingName = recording.getFileName();
//            if (_queuedRecordings.indexOf(recordingName) == -1){
//                _queuedRecordings.add(recordingName);
//            }
//
//        }
//        clearBtn.setDisable(false);       //buttons related to the playlist are enabled
//        randomiseBtn.setDisable(false);
//    }
//
//

//
//    @FXML
//    private void enableRecordingListBtns(MouseEvent mouseEvent){
//        if (mouseEvent.getClickCount() == 2){ //allow double click to also add
//            addToQueue();
//        }
//        if (recordingsTable.getSelectionModel().getSelectedItem() != null){ //enable buttons if recording selected is not null
//            addBtn.setDisable(false);
//            addAllBtn.setDisable(false);
//            rateBtn.setDisable(false);
//            String recording = recordingsTable.getSelectionModel().getSelectedItem().getFileName();
//            if (recording.substring(0,8).equals("personal")){
//                deleteBtn.setDisable(false);
//            } else {
//                deleteBtn.setDisable(true);
//            }
//        }
//    }

    @FXML
    private void deleteRecording(){

        String selection = recordingsTable.getSelectionModel().getSelectedItem().getFileName();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete?");
        alert.setHeaderText("You are about to delete "+ "'se206_"+ selection+"'");
        alert.setContentText("Hit Ok to confirm or Cancel to return to menu");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            _recordingModels.remove(recordingsTable.getSelectionModel().getSelectedItem());
            recordingsTable.getItems().clear();
            recordingsTable.getItems().setAll(_recordingModels);
            String selectionName = selection.substring(selection.lastIndexOf('_')+1,selection.lastIndexOf('.'));
            NamesModel selectionModel = _namesListModel.getName(selectionName);
            List<RecordingModel> records = selectionModel.getRecords();
            for (RecordingModel record : records){
                if (record.getFileName().equals(selection)){
                    selectionModel.delete(record.getFileName());
                    break;
                }
            }
        }
    }

    @FXML
    private void rateRecording(){
        RecordingModel selection = recordingsTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            String name = selection.getFileName().substring(selection.getFileName().lastIndexOf('_') + 1, selection.getFileName().lastIndexOf('.'));
            RecordingRater rater = new RecordingRater(selection.getFileName(), selection); //make new rater object
            boolean exists = rater.checkFile(); //if rating exists ask if they want to overwrite
            if (exists) {
                rater.overWriteRating();
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
        deleteBtn.setDisable(true);
        listenBtn.setDisable(true);
        RecordingModel selection = recordingsTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            playStatus.setText("Now playing: ");
            playRecording.setText(selection.getFileName());
            isPlaying = true;
            String filePath;
            if (selection.getFileName().substring(0, 8).equals("personal")) {
                filePath = "Personal/" + selection.getFileName();
            }else {
                filePath = "Original/" + selection.getFileName(); //get file path to the recording and pass it into player
            }
            RecordingPlayer player = new RecordingPlayer(filePath);
            playProgressBar.progressProperty().unbind();
            playProgressBar.progressProperty().bind(player.progressProperty());
            player.setOnSucceeded(e -> {
                deleteBtn.setDisable(false);
                listenBtn.setDisable(false);
                playStatus.setText("No recording currently playing");
                playRecording.setText("");
                recordingsTable.getSelectionModel().selectNext();
                isPlaying = false;
            });
            new Thread(player).start();
        }
    }

    @FXML
    private void clearSearch(){
        searchBox.setText("");
    }

//    @FXML
//    private void randomiseQueue(){
//        Collections.shuffle(_queuedRecordings);
//    }
//
//    @FXML
//    private void enablePlayBtns(MouseEvent mouseEvent){
//        if (!playList.getSelectionModel().isEmpty()){
//            listenBtn.setDisable(false);
//            removeBtn.setDisable(false);
//        } else {
//            listenBtn.setDisable(true);
//        }
//
//        if (mouseEvent.getClickCount() == 2 && !isPlaying){
//            playRecording();
//        }
//
//    }
    @FXML
    private void enableListen(MouseEvent mouseEvent){
        if (recordingsTable.getSelectionModel().getSelectedItem() != null){
            rateBtn.setDisable(false);
            listenBtn.setDisable(false);
            String recording = recordingsTable.getSelectionModel().getSelectedItem().getFileName();
            if (recording.substring(0,8).equals("personal")){
                deleteBtn.setDisable(false);
            } else {
                deleteBtn.setDisable(true);
            }
        }
        if (mouseEvent.getClickCount() == 2){
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

    //takes you to the main menu
    @FXML
    private void goToMain(ActionEvent event) throws IOException {
        Parent listenScene = FXMLLoader.load(getClass().getResource("/application/views/MainMenu.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //disable all buttons on start up except for testing mic and creating recording
        deleteBtn.setDisable(true);
        listenBtn.setDisable(true);
        rateBtn.setDisable(true);
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
