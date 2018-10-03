package application.uiControllers;

import application.models.NamesListModel;
import application.models.NamesModel;
import application.models.RecordingModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
        }
    }

    @FXML
    private void deleteRecording(){

    }

    @FXML
    private void rateRecording(){

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

        _recordings = FXCollections.observableArrayList();
        _queuedRecordings = FXCollections.observableArrayList();
        playList.setItems(_queuedRecordings);
        recordingsList.setItems(_recordings);
        _names = FXCollections.observableArrayList(_namesListModel.getNames());
        namesList.setItems(_names);

    }
}
