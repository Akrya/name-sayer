package application.controllers;

import application.models.*;
import com.sun.prism.impl.Disposer;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.*;

public class MangeModeController implements Initializable {

    @FXML
    private Button rateBtn;

    @FXML
    private Button randomiseBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button listenBtn;

    @FXML
    private Button addBtn;

    @FXML
    private Button addAllBtn;

    @FXML
    private Button removeBtn;

    @FXML
    private Button clearBtn;

    @FXML
    private TextField searchBox;

    @FXML
    private ProgressBar playProgressBar;

    @FXML
    private Label playStatus;

    @FXML
    private Label playRecording;

    @FXML
    private ListView<String> playList;

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

    private ObservableList<String> _queuedRecordings;

    private boolean isPlaying = false;


    @FXML
    private void addToQueue(){
        RecordingModel selected = recordingsTable.getSelectionModel().getSelectedItem();
        if (selected != null){
            String recording = selected.getFileName();
            if (_queuedRecordings.indexOf(recording) == -1){
                _queuedRecordings.add(recording);
                clearBtn.setDisable(false);
                randomiseBtn.setDisable(false);
            }
        }
    }


    @FXML
    private void addAllToQueue(){
        List<RecordingModel> selected = recordingsTable.getItems();

        for (RecordingModel recording: selected){
            String recordingName = recording.getFileName();
            if (_queuedRecordings.indexOf(recordingName) == -1){
                _queuedRecordings.add(recordingName);
            }

        }
        clearBtn.setDisable(false);
        randomiseBtn.setDisable(false);
    }


    @FXML
    private void enableRecordingListBtns(MouseEvent mouseEvent){
        if (mouseEvent.getClickCount() == 2){
            addToQueue();
        }
        if (recordingsTable.getSelectionModel().getSelectedItem() != null){
            addBtn.setDisable(false);
            addAllBtn.setDisable(false);
            rateBtn.setDisable(false);
            String recording = recordingsTable.getSelectionModel().getSelectedItem().getFileName();
            if (recording.substring(0,8).equals("personal")){
                deleteBtn.setDisable(false);
            } else {
                deleteBtn.setDisable(true);
            }
        }
    }

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
            if (_queuedRecordings.indexOf(selection) != -1){ //remove the recording if its been queued in play list
                _queuedRecordings.remove(selection);
            }
        }
    }

    @FXML
    private void rateRecording(){
        RecordingModel selection = recordingsTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            String name = selection.getFileName().substring(selection.getFileName().lastIndexOf('_') + 1, selection.getFileName().lastIndexOf('.'));
            RecordingRater rater = new RecordingRater(selection.getFileName(), selection);
            boolean exists = rater.checkFile();
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
        removeBtn.setDisable(true);
        clearBtn.setDisable(true);
        deleteBtn.setDisable(true);
        listenBtn.setDisable(true);
        playStatus.setText("Now playing: ");
        playRecording.setText(playList.getSelectionModel().getSelectedItem());
        isPlaying = true;
        String filePath;
        if (playList.getSelectionModel().getSelectedItem().substring(0,8).equals("personal")){
            filePath = "Personal/"+playList.getSelectionModel().getSelectedItem();
        } else {
            filePath = "Original/"+playList.getSelectionModel().getSelectedItem();
        }
        RecordingPlayer player = new RecordingPlayer(filePath);
        playProgressBar.progressProperty().unbind();
        playProgressBar.progressProperty().bind(player.progressProperty());
        player.setOnSucceeded(e ->{
            removeBtn.setDisable(false);
            clearBtn.setDisable(false);
            deleteBtn.setDisable(false);
            listenBtn.setDisable(false);
            playStatus.setText("No recording currently playing");
            playRecording.setText("");
            playList.getSelectionModel().selectNext();
            isPlaying = false;
        });
        new Thread(player).start();
    }

    @FXML
    private void clearSearch(){
        searchBox.setText("");
    }

    @FXML
    private void removeQueue(){
        String selection = playList.getSelectionModel().getSelectedItem();
        _queuedRecordings.remove(selection);
        if (_queuedRecordings.isEmpty()) {
            clearBtn.setDisable(true);
            removeBtn.setDisable(true);
            listenBtn.setDisable(true);
            randomiseBtn.setDisable(true);
        }
    }

    @FXML
    private void clearQueue(){
        _queuedRecordings.clear();
        clearBtn.setDisable(true);
        removeBtn.setDisable(true);
        listenBtn.setDisable(true);
        randomiseBtn.setDisable(true);
    }

    @FXML
    private void randomiseQueue(){
        Collections.shuffle(_queuedRecordings);
    }

    @FXML
    private void enablePlayBtns(MouseEvent mouseEvent){
        if (!playList.getSelectionModel().isEmpty()){
            listenBtn.setDisable(false);
            removeBtn.setDisable(false);
        } else {
            listenBtn.setDisable(true);
        }

        if (mouseEvent.getClickCount() == 2 && !isPlaying){
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

    @FXML
    private void goToMain(ActionEvent event) throws IOException {
        Parent listenScene = FXMLLoader.load(getClass().getResource("../views/MainMenu.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        addBtn.setDisable(true); //disable all buttons on start up except for testing mic and creating recording
        addAllBtn.setDisable(true);
        deleteBtn.setDisable(true);
        removeBtn.setDisable(true);
        clearBtn.setDisable(true);
        listenBtn.setDisable(true);
        randomiseBtn.setDisable(true);
        rateBtn.setDisable(true);
        searchBox.setPromptText("Search...");

        makeRatingFile();
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName")); //bind two columns to RecordingModel class
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        recordingsTable.getItems().setAll(_recordingModels);
        _queuedRecordings = FXCollections.observableArrayList();
        playList.setItems(_queuedRecordings);


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

    }

    private void makeRatingFile(){
        File rateFile = new File("Ratings.txt");
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

    private void getRatings(){ //can be improved greatly right now O(n^2)
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
