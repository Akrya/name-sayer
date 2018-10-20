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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.util.*;

public class CustomModeController {


    private ControllerManager _singleton;

    private NamesSelectorController _controller;

    private ObservableList<String> _selectedNames;

    private ObservableList<String> _customRecords;

    @FXML
    private ListView<String> customRecordings;

    private NamesListModel _namesListModel;
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

    @FXML
    private Button compareBtn;

    @FXML
    private Button removeBtn;

    @FXML
    private Button shuffleBtn;

    @FXML
    private Button stopBtn;

    private boolean inAction = false;

    private Task copyWorker;

    private AudioVisualizerModel audioVM;

    private RecordingPlayer _player;

    private CustomPlayer _customPlayer;


    //takes you back to the main menu


    @FXML
    private void goToMain(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/MainMenu.fxml"));
        Parent root = loader.load();
        MainMenuController controller = loader.getController();
        controller.initialise(_namesListModel);
        Scene scene = new Scene(root);

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
        controller.initialise(_namesListModel);
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
            deleteBtn.setDisable(false);
        }

    }

    private void changePlayStatus(){
        playStatus.setText("No recording currently playing");
        playRecording.setText("");
    }

    //Plays the currently selected recording in the list view for personal recordings
    @FXML
    private void listenPersonal(){
        if (customRecordings.getSelectionModel().getSelectedItem() != null){
            String filePath = "CustomRecords/"+customRecordings.getSelectionModel().getSelectedItem();
            _player = new RecordingPlayer(filePath); //make new player and bind progress bar to player
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(_player.progressProperty());
            playStatus.setText("Now playing: ");
            playRecording.setText(customRecordings.getSelectionModel().getSelectedItem());
            inAction = true;
            switchButtonStates();
            _player.setOnSucceeded(e ->{
                changePlayStatus();
                inAction = false;
                switchButtonStates();
                _player = null;
            });
            new Thread(_player).start();
        }
    }

    @FXML
    private void stopAudio(){
        if (_customPlayer != null){
            _customPlayer.stopAudio();
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            _customPlayer.cleanUpFiles();
            inAction = false;
            _customPlayer = null;
        }
        if (_player != null){
            boolean yes = _player.cancel();
            System.out.println(yes);
            _player.stopAudio();
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            _player.cleanUpFiles();
            inAction = false;
            _player = null;
        }
        changePlayStatus();
        switchButtonStates();
    }

    @FXML
    private void compareRecords(){
        if (customRecordings.getSelectionModel().getSelectedItem() == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No selection !");
            alert.setHeaderText(null);
            alert.setContentText("Please select a personal recording to do a comparision with!");
            alert.showAndWait();
        } else {
            List<String> choices = new ArrayList<>();
            choices.add("1");
            choices.add("2");
            choices.add("3");
            choices.add("4");
            choices.add("5");

            ChoiceDialog<String> dialog = new ChoiceDialog<>("1", choices);
            dialog.setTitle("Comparison");
            dialog.setHeaderText("You are comparing recordings for '"+playRecording.getText()+"'");
            dialog.setContentText("Please choose how many times you want to compare: ");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                String ogSelection = selectedName.getText();
                playStatus.setText("Currently comparing user and database version of");
                inAction = true;
                switchButtonStates();
                String perSelection = "CustomRecords/"+customRecordings.getSelectionModel().getSelectedItem();
                playRecording.setText("'"+ogSelection+"'");
                compare(ogSelection,perSelection,Integer.valueOf(result.get()));
            }
        }
    }

    /**Recursive function that repeats the comparison for however many times the user specified,
     * it first plays the concatenated recording and then plays the user's own version
     */
    private void compare(String ogSelection, String perSelection, int repeat){
        if (repeat == 0){
            inAction = false;
            switchButtonStates();
            return;
        } else {
            _customPlayer = new CustomPlayer(ogSelection,_namesListModel);
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(_customPlayer.progressProperty());
            _customPlayer.setOnSucceeded(e -> {
                _player = new RecordingPlayer(perSelection);
                progressBar.progressProperty().unbind();
                progressBar.progressProperty().bind(_player.progressProperty());
                _player.setOnSucceeded(m -> {
                    compare(ogSelection, perSelection, repeat - 1); //recursive call
                });
                new Thread(_player).start(); //when concat version finishes play user's recording before calling compare again
            });
            new Thread(_customPlayer).start(); //play the concatenated version first
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
                removeBtn.setDisable(false);
                shuffleBtn.setDisable(false);
                recordBtn.setDisable(false);
                compareBtn.setDisable(false);
            }
        }

    }

    @FXML
    private void shuffleList(){
        Collections.shuffle(_selectedNames);
    }

    @FXML
    private void removeSelection(){
        String selection = selectedNames.getSelectionModel().getSelectedItem();
        if (selection != null){
            _selectedNames.remove(selection);
            selectedName.setText("");
            selectStatus.setText("No name selected");
        }
    }

    //plays the currently selected recording for the list view for original recordings
    @FXML
    private void listenOriginal(){
        String selection =  selectedName.getText();
        if (selection != null){
            playStatus.setText("Now playing database version of: ");
            playRecording.setText(selection); //same as other listen function except we use a customplayer instead to play concatenated names
            inAction = true;
            switchButtonStates();
            _customPlayer = new CustomPlayer(selection,_namesListModel);
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(_customPlayer.progressProperty());
            _customPlayer.setOnSucceeded(e ->{
                inAction = false;
                changePlayStatus();
                switchButtonStates();
                _customPlayer = null;
            });
            new Thread(_customPlayer).start();
        }
    }

    private void switchButtonStates(){
        listenOgBtn.setDisable(inAction);
        listenPerBtn.setDisable(inAction);
        recordBtn.setDisable(inAction);
        deleteBtn.setDisable(inAction);
        shuffleBtn.setDisable(inAction);
        removeBtn.setDisable(inAction);
        compareBtn.setDisable(inAction);
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

    /**Method is called when the scene is loaded and the controller is instantiated, a reference to the NamesListModel is
     * passed into the controller and stored as a field, method also sets up the mic levels bar and disables buttons on startup
     * as well as populate the listviews with names and recordings.
     */
    public void initialise(NamesListModel model){
        _namesListModel = model;
        _singleton = ControllerManager.getInstance();
        _controller = _singleton.getController();
        _selectedNames = FXCollections.observableArrayList(_controller.getSelectedNames());
        selectedNames.setItems(_selectedNames);
        _customRecords = FXCollections.observableArrayList();
        getCustomRecordings();
        customRecordings.setItems(_customRecords);

        listenPerBtn.setDisable(true);
        recordBtn.setDisable(true);
        compareBtn.setDisable(true);
        listenOgBtn.setDisable(true);
        shuffleBtn.setDisable(true);
        deleteBtn.setDisable(true);
        removeBtn.setDisable(true);

        //initializing mic
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
