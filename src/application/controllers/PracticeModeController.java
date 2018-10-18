package application.controllers;

import application.models.*;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.*;

public class PracticeModeController implements Initializable {

    @FXML
    private ListView<String> ogNames;

    @FXML
    private ListView<String> ogRecordings;

    @FXML
    private ListView<String> personalRecordings;

    @FXML
    private Text selectedName;

    @FXML
    private Button listenOgBtn;

    @FXML
    private Button recordBtn;

    @FXML
    private Button compBtn;

    @FXML
    private Button listenModeBtn;

    @FXML
    private Button listenPerBtn;

    @FXML
    private Text ogPlayStatus;

    @FXML
    private Text selectedRecording;

    @FXML
    private Text selectedStatus;

    @FXML
    private Button selectBtn;

    @FXML
    private ProgressBar ogProgressBar;

    @FXML
    private Slider volumeSlider;

    private NamesListModel _namesListModel = new NamesListModel();

    private ObservableList<String> _practiceRecordings;

    private ObservableList<String> _ogRecordings;

    private ObservableList<String> _ogNames;

    private boolean isRecording = false;

    private AudioVisualizerModel audioVM;

    @FXML
    private ProgressBar audioVisualizer;

    private Task copyWorker;

    //private TargetDataLine line = null;

    //takes you to home window
    @FXML
    private void goToListenMode(ActionEvent event) throws IOException {
        Parent listenScene = FXMLLoader.load(getClass().getResource("/application/views/MainMenu.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);

        //Closing TargetDataLine when switching back to listen mode
        audioVM.endTask();
    }

    @FXML
    private void enableSelectBtn(MouseEvent mouseEvent) {
        if (!isRecording) {
            if (!ogNames.getSelectionModel().isEmpty()) {
                selectBtn.setDisable(false);
            } else {
                selectBtn.setDisable(true);
            }
            if (mouseEvent.getClickCount() == 2) {
                addToOgRecordings();
            }
        }
    }

    @FXML
    private void enableListenOg(MouseEvent mouseEvent) {
        if (ogRecordings.getSelectionModel().getSelectedItem() != null && personalRecordings.getSelectionModel().getSelectedItem() != null) {
            compBtn.setDisable(false);
        }
        if (!ogRecordings.getSelectionModel().isEmpty()) {
            listenOgBtn.setDisable(false);
        } else {
            listenOgBtn.setDisable(true);
        }
        if (mouseEvent.getClickCount() == 2) {
            playOgRecording();
        }
    }

    @FXML
    private void enableListenPer(MouseEvent mouseEvent) {
        if (ogRecordings.getSelectionModel().getSelectedItem() != null && personalRecordings.getSelectionModel().getSelectedItem() != null) {
            compBtn.setDisable(false);
        }
        if (!personalRecordings.getSelectionModel().isEmpty()) {
            listenPerBtn.setDisable(false);
        } else {
            listenPerBtn.setDisable(true);
        }
        if (mouseEvent.getClickCount() == 2) {
            playPerRecording();
        }
    }

    @FXML
    private void addToOgRecordings() {
        selectedStatus.setText("Currently selected:"); //get recordings for currently selected names and display them in original records list
        selectedName.setText(ogNames.getSelectionModel().getSelectedItem());
        NamesModel model = _namesListModel.getName(ogNames.getSelectionModel().getSelectedItem());
        List<String> recordings = model.getOgRecordings();
        _ogRecordings.clear();
        _ogRecordings.addAll(recordings);
        recordings = model.getPerRecordings();
        _practiceRecordings.clear();
        _practiceRecordings.addAll(recordings);
        recordBtn.setDisable(false);
        listenOgBtn.setDisable(true);
        listenPerBtn.setDisable(true);
        compBtn.setDisable(true);
    }

    @FXML
    private void compareRecordings() { //play the first recording then play the second one after it has finished.
        if (personalRecordings.getSelectionModel().getSelectedItem() != null && ogRecordings.getSelectionModel().getSelectedItem() != null) {
            ogPlayStatus.setText("Now comparing ");
            selectedRecording.setText("'" + ogRecordings.getSelectionModel().getSelectedItem() + "' with '" + personalRecordings.getSelectionModel().getSelectedItem() + "'");
            listenModeBtn.setDisable(true);
            listenOgBtn.setDisable(true);
            listenPerBtn.setDisable(true);
            recordBtn.setDisable(true);
            compBtn.setDisable(true);
            String ogfile = "";
            String perfile = "";
            String ogSelection = ogRecordings.getSelectionModel().getSelectedItem();
            String perSelection = personalRecordings.getSelectionModel().getSelectedItem();
            String queueName = ogSelection.substring(ogSelection.lastIndexOf('_') + 1, ogSelection.lastIndexOf('.'));
            NamesModel model = _namesListModel.getName(queueName);
            List<RecordingModel> records = model.getRecords();
            for (RecordingModel record : records) {
                if (record.getFileName().contains(ogSelection)) {
                    ogfile = record.getFileName();
                }
                if (record.getFileName().contains(perSelection)) {
                    perfile = record.getFileName();
                }
            }
            final String ogPath = "Original/" + ogfile;
            final String perPath = "Personal/" + perfile;
            RecordingPlayer player1 = new RecordingPlayer(ogPath);
            player1.setOnSucceeded(e -> {
                RecordingPlayer player2 = new RecordingPlayer(perPath); //play second video when first ends
                player2.setOnSucceeded(b -> {
                    listenModeBtn.setDisable(false); //re-enable buttons after both videos play
                    listenOgBtn.setDisable(false);
                    listenPerBtn.setDisable(false);
                    recordBtn.setDisable(false);
                    compBtn.setDisable(false);
                    ogPlayStatus.setText("Comparison Over");
                    selectedRecording.setText("");
                });
                ogProgressBar.progressProperty().unbind();
                ogProgressBar.progressProperty().bind(player2.progressProperty());
                new Thread(player2).start();
            });
            ogProgressBar.progressProperty().unbind();
            ogProgressBar.progressProperty().bind(player1.progressProperty());

            new Thread(player1).start();
        }
    }

    @FXML
    private void playOgRecording() {
        playRecording(0);
    }

    @FXML
    private void playPerRecording() {
        playRecording(1);
    }

    private void playRecording(int identifier) {

        String selection = (identifier == 0) ? ogRecordings.getSelectionModel().getSelectedItem() : personalRecordings.getSelectionModel().getSelectedItem();
        if (selection != null) {
            listenPerBtn.setDisable(true); //play recording, depending on which listen button was hit, it will get the appropriate file path and pass it to player
            listenOgBtn.setDisable(true);
            listenModeBtn.setDisable(true);
            recordBtn.setDisable(true);
            compBtn.setDisable(true);
            ogPlayStatus.setText("Currently playing");
            selectedRecording.setText("'" + selection + "'");
            String filePath = "";
            String queueName = selection.substring(selection.lastIndexOf('_') + 1, selection.lastIndexOf('.'));
            NamesModel queueNameModel = _namesListModel.getName(queueName);
            List<RecordingModel> records = queueNameModel.getRecords();
            for (RecordingModel record : records) {
                if (record.getFileName().contains(selection)) {
                    filePath = record.getFileName();
                }
            }
            filePath = (identifier == 0) ? "Original/" + filePath : "Personal/" + filePath;
            RecordingPlayer player = new RecordingPlayer(filePath);
            player.setOnSucceeded(e -> {
                listenOgBtn.setDisable(false);
                listenPerBtn.setDisable(false);
                recordBtn.setDisable(false);
                compBtn.setDisable(true);
                if (identifier == 0) {
                    ogRecordings.getSelectionModel().selectNext();
                } else {
                    personalRecordings.getSelectionModel().selectNext();
                }
                ogPlayStatus.setText("No recording currently playing");
                selectedRecording.setText("");
                listenModeBtn.setDisable(false);
            });
            ogProgressBar.progressProperty().unbind();
            ogProgressBar.progressProperty().bind(player.progressProperty());
            new Thread(player).start();
        }
    }

    @FXML
    private void recordNewName() {
        String selection = selectedName.getText();
        NamesModel selectedName = _namesListModel.getName(selection);
        Recorder recorder = new Recorder(selectedName);  //make recorder object that runs bash commands for recording
        recorder.setOnSucceeded(e -> {
            listenOgBtn.setDisable(false);
            listenPerBtn.setDisable(false);
            recordBtn.setDisable(false);
            selectBtn.setDisable(false);
            isRecording = false;
            ogPlayStatus.setText("Finished recording!");
            _practiceRecordings.add(recorder.getValue());
        });

        ogProgressBar.progressProperty().unbind();
        ogProgressBar.progressProperty().bind(recorder.progressProperty());
        new Thread(recorder).start();
        recordBtn.setDisable(true);
        selectBtn.setDisable(true);
        isRecording = true;
        listenPerBtn.setDisable(true);
        listenOgBtn.setDisable(true);
        ogPlayStatus.setText("Now recording for 5 seconds for the name");
        selectedRecording.setText("'" + selectedName.toString() + "'");

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listenOgBtn.setDisable(true);
        listenPerBtn.setDisable(true);
        recordBtn.setDisable(true);
        compBtn.setDisable(true);
        selectBtn.setDisable(true);


        _practiceRecordings = FXCollections.observableArrayList();

        personalRecordings.setItems(_practiceRecordings);
        _ogRecordings = FXCollections.observableArrayList();
        ogRecordings.setItems(_ogRecordings);
        _ogNames = FXCollections.observableArrayList(_namesListModel.getNames());
        ogNames.setItems(_ogNames);

        //initializing mic level bar
        audioVisualizer.setProgress(0.0);
        audioVM = new AudioVisualizerModel();
        copyWorker = audioVM.createWorker();
        audioVisualizer.progressProperty().unbind();
        audioVisualizer.progressProperty().bind(copyWorker.progressProperty());
        new Thread(copyWorker).start(); //run mic testing code on separate thread so GUI is responsive



        startVolumeSlider();


    }

    private void startVolumeSlider(){
        //initiliazing volume slider

        //running command to get current volume
        String cmd1 = "amixer get Master | awk '$0~/%/{print $4}' | tr -d '[]%'";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd1);

        try {
            //reading current volume level
            Process volumeInitializer = builder.start();
            InputStream inputStream = volumeInitializer.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String volumeLevel = br.readLine();
            double vlevel = Double.parseDouble(volumeLevel);
            volumeSlider.setValue(vlevel);

        } catch (IOException e) {
            e.printStackTrace();
        }


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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
