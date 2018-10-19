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
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.*;

public class CustomModeController implements Initializable {


    private ControllerManager _singleton;

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

    @FXML
    private Slider volumeSlider;

    @FXML
    private ProgressBar audioVisualizer;



    private boolean inAction = false;
    private Task copyWorker;

    private AudioVisualizerModel audioVM;



    //takes you back to the main menu
    @FXML
    private void goToMain(ActionEvent event) throws IOException {
        Parent listenScene = FXMLLoader.load(getClass().getResource("/application/views/MainMenu.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);

        audioVM.endTask();
    }


    //takes you back to the select screen
    @FXML
    private void goToSelect(ActionEvent event) throws IOException {
        _singleton = ControllerManager.getInstance();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/NamesSelector.fxml"));
        Parent root = loader.load();
        NamesSelectorController controller = loader.getController();
        _singleton.setController(controller);
        Scene scene = new Scene(root);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);

        audioVM.endTask();
    }


    //Selects a Name if an entry exists in the row
    @FXML
    private void enableSelect(){
        if (selectedNames.getSelectionModel().getSelectedItem() != null){
            selectName();  //selectName is called which enables other Buttons for use
        }
    }


    //Enables the personalListen button if there's a double click
    @FXML
    private void enablePersonalListen(MouseEvent mouseEvent){
        if (mouseEvent.getClickCount() == 2 && !inAction){
            listenPersonal();
        }
        if (!customRecordings.getSelectionModel().isEmpty() && !inAction){
            listenPerBtn.setDisable(false);
        }

    }

    //Plays the currently selected recording in the list view for personal recordings
    @FXML
    private void listenPersonal(){
        if (customRecordings.getSelectionModel().getSelectedItem() != null){
            String filePath = "CustomRecords/"+customRecordings.getSelectionModel().getSelectedItem();
            RecordingPlayer player = new RecordingPlayer(filePath); //make new player and bind progress bar to player
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(player.progressProperty());
            playStatus.setText("Now playing: ");
            playRecording.setText(customRecordings.getSelectionModel().getSelectedItem());
            inAction = true;
            recordBtn.setDisable(true); //disable buttons while playing
            listenPerBtn.setDisable(true);
            listenOgBtn.setDisable(true);
            deleteBtn.setDisable(true);
            player.setOnSucceeded(e ->{
                playStatus.setText("No recording currently playing");
                playRecording.setText("");
                deleteBtn.setDisable(false);
                inAction = false;
                listenPerBtn.setDisable(false); //renable buttons
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


    //plays the currently selected recording for the list view for original recordings
    @FXML
    private void listenOriginal(){
        String selection =  selectedName.getText();
        if (selection != null){
            listenOgBtn.setDisable(true); //same as other listen function except we use a customplayer instead
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
        if (selection != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete?");
            alert.setHeaderText("You are about to delete '" + selection + "'");
            alert.setContentText("Hit Ok to confirm or Cancel to return to menu");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                new File("CustomRecords/" + selection).delete();
                _customRecords.remove(selection);
            }
        }

    }


    //Method for making a recording for the currently selected CustomName
    @FXML
    private void recordCustom(){

        String selection = selectedName.getText();
        if (selection != null && !selection.isEmpty()){
            Recorder recorder = new Recorder(selection); //make a new Recorder model that runs the bash commands for recording
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
                new File("temp.wav").delete();
            });
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(recorder.progressProperty());
            new Thread(recorder).start();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _singleton = ControllerManager.getInstance();
        _controller = _singleton.getController();
        _selectedNames = FXCollections.observableArrayList(_controller.getSelectedNames());
        selectedNames.setItems(_selectedNames);
        _customRecords = FXCollections.observableArrayList();
        getCustomRecordings();
        customRecordings.setItems(_customRecords);

        listenPerBtn.setDisable(true);
        recordBtn.setDisable(true);
        listenOgBtn.setDisable(true);

        //initializin mic
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

        try{
            //reading current volume level
            Process volumeInitializer = builder.start();
            InputStream inputStream = volumeInitializer.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String volumeLevel = br.readLine();
            System.out.println(volumeLevel);

            double vlevel = Double.parseDouble(volumeLevel);
            volumeSlider.setValue(vlevel);

        } catch (IOException e){
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
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
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
