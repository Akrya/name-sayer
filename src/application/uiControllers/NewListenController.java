package application.uiControllers;

import application.models.NamesListModel;
import application.models.NamesModel;
import application.models.RecordingModel;
import application.models.RecordingRater;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class NewListenController implements Initializable {

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
    private Button removeBtn;

    @FXML
    private Button clearBtn;

    @FXML
    private ListView<String> playList;

    @FXML
    private ListView<String> namesList;

    @FXML
    private TableView<RecordingModel> recordingsTable;

    private ObservableList<RecordingModel> testRec = FXCollections.observableArrayList();

    @FXML
    private TableColumn<RecordingModel, String> fileCol;

    @FXML
    private TableColumn<RecordingModel, String> ratingCol;

    private NamesListModel _namesListModel = new NamesListModel();

    private ObservableList<String> _names;

    private ObservableList<String> _queuedRecordings;


    @FXML
    private void addToQueue(){
        RecordingModel selected = recordingsTable.getSelectionModel().getSelectedItem();
        if (selected != null){
            String recording = selected.getFileName();
            if (_queuedRecordings.indexOf(recording) == -1){
                _queuedRecordings.add(recording);
            }
        }
    }

    @FXML
    private void enableRecordingListBtns(MouseEvent mouseEvent){
        if (mouseEvent.getClickCount() == 2){
            addToQueue();
        }
        if (recordingsTable.getSelectionModel().getSelectedItem() != null){
            addBtn.setDisable(false);
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
            testRec.remove(recordingsTable.getSelectionModel().getSelectedItem());
            recordingsTable.getItems().clear();
            recordingsTable.getItems().setAll(testRec);
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
            testRec.clear();
            NamesModel model = _namesListModel.getName(name);
            List<RecordingModel> records = model.getRecords();
            for (RecordingModel record : records) {
                testRec.add(record);
            }
            recordingsTable.getItems().setAll(testRec);
        }
    }

    @FXML
    private void playRecording(){

    }

    @FXML
    private void removeQueue(){

    }

    @FXML
    private void clearQueue(){

    }

    @FXML
    private void randomiseQueue(){

    }

    @FXML
    private void getRecordings(){

        String selection = namesList.getSelectionModel().getSelectedItem();
        if (selection != null){
            testRec.clear();
            NamesModel model = _namesListModel.getName(selection);
            List<RecordingModel> records = model.getRecords();
            for (RecordingModel record : records){
                testRec.add(record);
            }
            recordingsTable.getItems().setAll(testRec);
        }
    }

    @FXML
    private void goToMain(ActionEvent event) throws IOException {
        Parent listenScene = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        addBtn.setDisable(true); //disable all buttons on start up except for testing mic and creating recording
        deleteBtn.setDisable(true);
        removeBtn.setDisable(true);
        clearBtn.setDisable(true);
        listenBtn.setDisable(true);
        randomiseBtn.setDisable(true);
        rateBtn.setDisable(true);

        makeRatingFile();
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName")); //bind two columns to RecordingModel class
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));

        recordingsTable.getItems().setAll(testRec);
        _queuedRecordings = FXCollections.observableArrayList();
        playList.setItems(_queuedRecordings);
        _names = FXCollections.observableArrayList(_namesListModel.getNames());
        namesList.setItems(_names);

    }

    private void makeRatingFile(){
        File rateFile = new File("Ratings.txt");
        if(rateFile.exists()) {
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
}
