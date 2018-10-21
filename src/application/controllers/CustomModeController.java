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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private Label recordingsListLabel;

    private boolean inAction;

    private Task copyWorker;

    private AudioVisualizerModel audioVM;

    private RecordingPlayer _player;

    private CustomPlayer _customPlayer;

    @FXML
    private ImageView recordImage;

    private Recorder _recorder;

    @FXML
    private Text recordBtnText;

    @FXML
    private Text listenDBText;

    @FXML
    private Text listenUserText;

    @FXML
    private ImageView listenUserImage;

    @FXML
    private ImageView listenDBImage;

    @FXML
    private Text compareText;

    @FXML
    private ImageView compareImage;
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
        if (inAction){
            listenUserImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/musical-note.png")));
            listenUserText.setText("Listen Personal");
            stopAudio();
            inAction = false;
        } else {
            if (customRecordings.getSelectionModel().getSelectedItem() != null) {
                if (selectedName.getText().contains("-") || selectedName.getText().contains(" ")) {
                    String filePath = "Concatenated/" + customRecordings.getSelectionModel().getSelectedItem();
                    _player = new RecordingPlayer(filePath);
                } else {
                    String filePath = "Single/" + customRecordings.getSelectionModel().getSelectedItem();
                    _player = new RecordingPlayer(filePath); //make new player and bind progress bar to player
                }
                progressBar.progressProperty().unbind();
                progressBar.progressProperty().bind(_player.progressProperty());
                listenUserImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/stop.png")));
                listenUserText.setText("Stop");
                _player.setOnSucceeded(e -> {
                    changePlayStatus();
                    listenUserImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/musical-note.png")));
                    listenUserText.setText("Listen Personal");
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
                listenPerBtn.setDisable(false);
            }
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
        if (inAction){
            compareImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/compare.png")));
            compareText.setText("Compare");
            stopAudio();
            inAction = false;
        } else {
            if (customRecordings.getSelectionModel().getSelectedItem() == null) {
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
                dialog.setHeaderText("You are comparing recordings for '" + playRecording.getText() + "'");
                dialog.setContentText("Please choose how many times you want to compare: ");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String databaseSelection = selectedName.getText();
                    String userSelection;
                    if (databaseSelection.contains("-") || databaseSelection.contains(" ")) {
                        userSelection = "Concatenated/" + customRecordings.getSelectionModel().getSelectedItem();
                    } else {
                        userSelection = "Single/" + customRecordings.getSelectionModel().getSelectedItem();
                    }
                    playStatus.setText("Comparision " + result.get() + ": user and database versions of:");
                    inAction = true;
                    switchButtonStates(true);
                    compareBtn.setDisable(false);
                    compareImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/stop.png")));
                    compareText.setText("Stop");
                    playRecording.setText("'" + databaseSelection + "'");
                    compare(databaseSelection, userSelection, Integer.valueOf(result.get()));
                }
            }
        }
    }

    /**Recursive function that repeats the comparison for however many times the user specified,
     * it first plays the concatenated recording and then plays the user's own version
     */
    private void compare(String ogSelection, String perSelection, int repeat){
        if (repeat == 0){
            inAction = false;
            compareImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/compare.png")));
            compareText.setText("Compare");
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
            _records.clear();
        }
        checkButtons();
    }

    //plays the currently selected recording for the list view for original recordings
    @FXML
    private void listenOriginal(){
        if (inAction){
            listenDBImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/musical-note.png")));
            listenDBText.setText("Listen Original");
            stopAudio();
            inAction = false;
        } else {
            String selection = selectedName.getText();
            if (selection != null) {
                playStatus.setText("Now playing database version of: ");
                playRecording.setText(selection); //same as other listen function except we use a customplayer instead to play concatenated names
                inAction = true;
                switchButtonStates(true);
                listenOgBtn.setDisable(false);
                listenDBImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/stop.png")));
                listenDBText.setText("Stop");
                _customPlayer = new CustomPlayer(selection, _namesListModel);
                progressBar.progressProperty().unbind();
                progressBar.progressProperty().bind(_customPlayer.progressProperty());
                _customPlayer.setOnSucceeded(e -> {
                    inAction = false;
                    changePlayStatus();
                    listenDBImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/musical-note.png")));
                    listenDBText.setText("Listen Original");
                    switchButtonStates(false);
                    checkButtons();
                    _customPlayer = null;
                });
                new Thread(_customPlayer).start();
            }
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
        if (inAction){ //this means button is pressed when recording so we want to stop recording
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            String newFile = _recorder.stopRecording();
            recordImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/microphone.png")));
            recordBtnText.setText("Record");
            inAction = false;
            switchButtonStates(false);
            _records.add(newFile);
            _recorder = null;
        }else {
            String selection = selectedName.getText();
            if (selection != null && !selection.isEmpty()) {
                if (selection.contains(" ") || selection.contains("-")) {
                    _recorder = new Recorder(selection);  //call constructor for custom name recorder if there are spaces or hyphens in name
                } else {
                    NamesModel name = _namesListModel.getName(selection);
                    _recorder = new Recorder(name);
                }
                inAction = true;
                switchButtonStates(true);
                recordBtn.setDisable(false);
                recordImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/stop.png")));
                recordBtnText.setText("Stop");
                _recorder.setOnSucceeded(e -> {
                    playStatus.setText("Finished recording!");
                    inAction = false;
                    switchButtonStates(false);
                    recordBtnText.setText("Record");
                    _records.add(_recorder.getValue());
                    recordImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/microphone.png")));
                });
                progressBar.progressProperty().unbind();
                progressBar.progressProperty().bind(_recorder.progressProperty());
                new Thread(_recorder).start();
            }
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
        _player = null;
        _customPlayer = null;

        inAction = false;
        switchButtonStates(true);

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
