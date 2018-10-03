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
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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
    private ListView<String> recordingsList;

    private NamesListModel _namesListModel = new NamesListModel();

    private ObservableList<String> _names;

    private ObservableList<String> _recordings;

    private ObservableList<String> _queuedRecordings;


    @FXML
    private void addToQueue(){
        String selection = recordingsList.getSelectionModel().getSelectedItem();
        if (selection != null){
            if (_queuedRecordings.indexOf(selection) == -1){
                _queuedRecordings.add(selection);
            }
        }
    }

    @FXML
    private void enableRecordingListBtns(MouseEvent mouseEvent){
        if (mouseEvent.getClickCount() == 2){
            addToQueue();
        }
        if (recordingsList.getSelectionModel().getSelectedItem() != null){
            addBtn.setDisable(false);
            rateBtn.setDisable(false);
            if (recordingsList.getSelectionModel().getSelectedItem().substring(0,8).equals("personal")){
                deleteBtn.setDisable(false);
            } else {
                deleteBtn.setDisable(true);
            }
        }
    }

    @FXML
    private void deleteRecording(){
        String selection = recordingsList.getSelectionModel().getSelectedItem();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete?");
        alert.setHeaderText("You are about to delete "+ "'se206_"+ selection+"'");
        alert.setContentText("Hit Ok to confirm or Cancel to return to menu");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            _recordings.remove(selection);
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
        String selection = recordingsList.getSelectionModel().getSelectedItem();
        if (selection != null) {
            String name = selection.substring(selection.lastIndexOf('_') + 1, selection.lastIndexOf('.'));
            NamesModel model = _namesListModel.getName(name);
            List<RecordingModel> records = model.getRecords();
            for (RecordingModel record : records) {
                if (record.getFileName().equals(selection)) {
                    RecordingRater rater = new RecordingRater(selection, record);
                    boolean exists = rater.checkFile();
                    if (exists) {
                        rater.overWriteRating();
                    } else {
                        rater.makeRating();
                    }
                    break;
                }
            }
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
            _recordings.clear();
            NamesModel model = _namesListModel.getName(selection);
            List<RecordingModel> records = model.getRecords();
            for (RecordingModel record : records){
                _recordings.add(record.getFileName());
            }
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
        _recordings = FXCollections.observableArrayList();
        _queuedRecordings = FXCollections.observableArrayList();
        playList.setItems(_queuedRecordings);
        recordingsList.setItems(_recordings);
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
