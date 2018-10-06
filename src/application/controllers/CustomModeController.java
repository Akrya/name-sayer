package application.controllers;

import application.models.*;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class CustomModeController implements Initializable {


    private NameSelectorSingleton _singleton;

    private NamesSelectorController _controller;

    private ObservableList<String> _selectedNames;

    private ObservableList<String> _customRecords;

    @FXML
    private ListView<String> customRecordings;

    private NamesListModel _namesListModel = new NamesListModel();

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ListView<String> selectedNames;

    @FXML
    private Text selectStatus;

    @FXML
    private Text selectedName;

    @FXML
    private Label playStatus;

    @FXML
    private Label playRecording;

    @FXML
    private Button listenPerBtn;

    @FXML
    private Button listenOgBtn;

    @FXML
    private Button recordBtn;

    @FXML
    private Button deleteBtn;

    private boolean inAction = false;

    @FXML
    private void goToMain(ActionEvent event) throws IOException {
        Parent listenScene = FXMLLoader.load(getClass().getResource("../views/MainMenu.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void goToSelect(ActionEvent event) throws IOException {
        _singleton = NameSelectorSingleton.getInstance();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/NamesSelector.fxml"));
        Parent root = loader.load();
        NamesSelectorController controller = loader.getController();
        _singleton.setController(controller);
        Scene scene = new Scene(root);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void enableSelect(){
        if (selectedNames.getSelectionModel().getSelectedItem() != null){
            selectName();
        }
    }

    @FXML
    private void enablePersonalListen(MouseEvent mouseEvent){
        if (mouseEvent.getClickCount() == 2 && !inAction){
            listenPersonal();
        }
        if (!customRecordings.getSelectionModel().isEmpty() && !inAction){
            listenPerBtn.setDisable(false);
        }

    }

    @FXML
    private void listenPersonal(){
        if (customRecordings.getSelectionModel().getSelectedItem() != null){
            String filePath = "CustomRecords/"+customRecordings.getSelectionModel().getSelectedItem();
            RecordingPlayer player = new RecordingPlayer(filePath);
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(player.progressProperty());
            playStatus.setText("Now playing: ");
            playRecording.setText(customRecordings.getSelectionModel().getSelectedItem());
            inAction = true;
            recordBtn.setDisable(true);
            listenPerBtn.setDisable(true);
            listenOgBtn.setDisable(true);
            deleteBtn.setDisable(true);
            player.setOnSucceeded(e ->{
                playStatus.setText("No recording currently playing");
                playRecording.setText("");
                deleteBtn.setDisable(false);
                inAction = false;
                listenPerBtn.setDisable(false);
                recordBtn.setDisable(false);
                listenOgBtn.setDisable(false);
            });
            new Thread(player).start();
        }
    }

    @FXML
    private void selectName(){
        String selection = selectedNames.getSelectionModel().getSelectedItem();
        if (selection != null){
            if (!selection.contains("invalid")){
                selectStatus.setText("Currently selected:");
                selectedName.setText(selection);
                listenOgBtn.setDisable(false);
                recordBtn.setDisable(false);
            }
        }

    }

    @FXML
    private void listenOriginal(){
        String selection =  selectedName.getText();
        if (selection != null){
            listenOgBtn.setDisable(true);
            listenPerBtn.setDisable(true);
            recordBtn.setDisable(true);
            inAction = true;
            deleteBtn.setDisable(true);
            CustomPlayer player = new CustomPlayer(selection);
            progressBar.progressProperty().unbind();
            progressBar.setProgress(-1.0f);
            player.setOnSucceeded(e ->{
                progressBar.setProgress(0);
                inAction = false;
                listenOgBtn.setDisable(false);
                listenPerBtn.setDisable(false);
                recordBtn.setDisable(false);
                deleteBtn.setDisable(false);
            });
            new Thread(player).start();
        }
    }

    @FXML
    private void deleteRecording(){

        String selection = customRecordings.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete?");
        alert.setHeaderText("You are about to delete '"+ selection+"'");
        alert.setContentText("Hit Ok to confirm or Cancel to return to menu");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            new File("CustomRecords/"+selection).delete();
            _customRecords.remove(selection);
        }
    }

    @FXML
    private void recordCustom(){

        String selection = selectedName.getText();
        if (selection != null && !selection.isEmpty()){
            Recorder recorder = new Recorder(selection);
            inAction = true;
            listenPerBtn.setDisable(true);
            recordBtn.setDisable(true);
            listenOgBtn.setDisable(true);
            recorder.setOnSucceeded(e -> {
                playStatus.setText("Finished recording!");
                inAction = false;
                _customRecords.add(recorder.getValue());
                listenPerBtn.setDisable(false);
                recordBtn.setDisable(false);
                listenOgBtn.setDisable(false);
            });
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(recorder.progressProperty());
            new Thread(recorder).start();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _singleton = NameSelectorSingleton.getInstance();
        _controller = _singleton.getController();
        _selectedNames = FXCollections.observableArrayList(_controller.getSelectedNames());
        selectedNames.setItems(_selectedNames);
        _customRecords = FXCollections.observableArrayList();
        getCustomRecordings();
        customRecordings.setItems(_customRecords);

        listenPerBtn.setDisable(true);
        recordBtn.setDisable(true);
        listenOgBtn.setDisable(true);

    }

    private void getCustomRecordings(){
        _customRecords.clear();
        File[] files = new File("CustomRecords").listFiles();
        List<String> customRecords = new ArrayList<>();
        for (File file : files){
            if (file.isFile()){
                customRecords.add(file.getName());
            }
        }
        _customRecords.addAll(customRecords);
    }
}
