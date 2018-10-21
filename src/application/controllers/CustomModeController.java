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

import java.io.*;
import java.util.*;

public class CustomModeController {


    private ControllerManager _singleton;

    private NamesSelectorController _controller;

    private ObservableList<String> _selectedNames;

    private ObservableList<String> _records;

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

    @FXML
    private Label recordingsListLabel;

    private boolean inAction;

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

    private void checkButtons(){
        if (selectedNames.getSelectionModel().isEmpty()){
            listenOgBtn.setDisable(true);
            shuffleBtn.setDisable(true);
            removeBtn.setDisable(true);
            compareBtn.setDisable(true);
            recordBtn.setDisable(true);
        }
    }

    //Enables the personalListen button if there's a double click
    @FXML
    private void enablePersonalListen(MouseEvent mouseEvent){
        if (mouseEvent.getClickCount() == 2 && !inAction){
            listenPersonal();
        }
        if (!customRecordings.getSelectionModel().isEmpty()){
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
            if (selectedName.getText().contains("-")|| selectedName.getText().contains(" ")){
                String filePath = "Concatenated/"+customRecordings.getSelectionModel().getSelectedItem();
                _player = new RecordingPlayer(filePath);
            } else {
                String filePath = "Single/"+customRecordings.getSelectionModel().getSelectedItem();
                _player = new RecordingPlayer(filePath); //make new player and bind progress bar to player
            }
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(_player.progressProperty());
            _player.setOnSucceeded(e ->{
                changePlayStatus();
                inAction = false;
                switchButtonStates(false);
                checkButtons();
                _player = null;
            });
            new Thread(_player).start();
            playStatus.setText("Now playing: ");
            playRecording.setText(customRecordings.getSelectionModel().getSelectedItem());
            inAction = true;
            switchButtonStates(true);
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
        switchButtonStates(inAction);
        checkButtons();
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
                String databaseSelection = selectedName.getText();
                String userSelection;
                if (databaseSelection.contains("-") || databaseSelection.contains(" ")){
                    userSelection = "Concatenated/"+customRecordings.getSelectionModel().getSelectedItem();
                } else {
                    userSelection = "Single/"+customRecordings.getSelectionModel().getSelectedItem();
                }
                playStatus.setText("Comparision "+result.get()+ ": user and database versions of:");
                inAction = true;
                switchButtonStates(true);
                playRecording.setText("'"+databaseSelection+"'");
                compare(databaseSelection,userSelection,Integer.valueOf(result.get()));
            }
        }
    }

    /**Recursive function that repeats the comparison for however many times the user specified,
     * it first plays the concatenated recording and then plays the user's own version
     */
    private void compare(String ogSelection, String perSelection, int repeat){
        if (repeat == 0){
            inAction = false;
            switchButtonStates(false);
            checkButtons();
            changePlayStatus();
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

                    playStatus.setText("Comparision "+(repeat-1)+ ": user and database versions of:");
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
            if (!inAction){
                recordingsListLabel.setText("User recordings for: "+selection);
                selectStatus.setText("Currently selected:");
                selectedName.setText(selection);
                if (selection.contains(" ") || selection.contains("-")){ //concatentated name
                    CustomNameModel model = new CustomNameModel(selection);
                    _records.clear();
                    _records.addAll(model.getRecordings());
                } else { //single name
                    NamesModel model = _namesListModel.getName(selection);
                    _records.clear();
                    _records.addAll(model.getPerRecordings());
                }
                inAction = false;
                switchButtonStates(false);
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
            recordingsListLabel.setText("Please select a name to see user recordings");
            switchButtonStates(true);
            stopBtn.setDisable(true);
        }
        checkButtons();
    }

    //plays the currently selected recording for the list view for original recordings
    @FXML
    private void listenOriginal(){
        String selection =  selectedName.getText();
        if (selection != null){
            playStatus.setText("Now playing database version of: ");
            playRecording.setText(selection); //same as other listen function except we use a customplayer instead to play concatenated names
            inAction = true;
            switchButtonStates(true);
            _customPlayer = new CustomPlayer(selection,_namesListModel);
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(_customPlayer.progressProperty());
            _customPlayer.setOnSucceeded(e ->{
                inAction = false;
                changePlayStatus();
                switchButtonStates(false);
                checkButtons();
                _customPlayer = null;
            });
            new Thread(_customPlayer).start();
        }
    }

    private void switchButtonStates(boolean flip){
        listenOgBtn.setDisable(flip);
        listenPerBtn.setDisable(flip);
        recordBtn.setDisable(flip);
        deleteBtn.setDisable(flip);
        shuffleBtn.setDisable(flip);
        removeBtn.setDisable(flip);
        compareBtn.setDisable(flip);
        stopBtn.setDisable(!flip);
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
                new File("Concatenated/" + selection).delete();
                _records.remove(selection);
            }
        }

    }


    //Method for making a recording for the currently selected CustomName
    @FXML
    private void recordCustom(){

        String selection = selectedName.getText();
        if (selection != null && !selection.isEmpty()){
            Recorder recorder;
            if (selection.contains(" ") || selection.contains("-")){
                recorder = new Recorder(selection);  //call constructor for custom name recorder if there are spaces or hyphens in name
            } else {
                NamesModel name = _namesListModel.getName(selection);
                recorder = new Recorder(name);
            }
            inAction = true;
            switchButtonStates(true);
            recorder.setOnSucceeded(e -> {
                playStatus.setText("Finished recording!");
                inAction = false;
                switchButtonStates(false);
                _records.add(recorder.getValue());
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
        _records = FXCollections.observableArrayList();
        customRecordings.setItems(_records);

        inAction = false;
        switchButtonStates(true);
        stopBtn.setDisable(true);

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
}
