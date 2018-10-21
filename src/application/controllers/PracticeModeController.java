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

/**Controller class for practice mode, it handles all the business logic associated with the functionality found in practice mode
 * and updates the views of the GUI when applicable
 * It also calls on model classes when appropriate and allows them to handle background processes
 */
public class PracticeModeController {

    private ControllerManager _manager;

    private NamesSelectorController _nameSelectorController;

    private ObservableList<String> _selectedNames; //this list is read by the names list view component

    private ObservableList<String> _records; //this list is read by the recordings list view component

    private NamesListModel _namesListModel;

    private boolean _inAction = false;

    private Task _micWorker; //Task object which reads in mic levels of the user's input

    private AudioVisualizerModel _audioVisualModel; //Model which binds mic levels to a progress bar

    private RecordingPlayer _player = null; //player responsible for playing user recordings

    private ConcatenatedPlayer _concatenatedPlayer = null; //player responsible for playing concatenated recordings

    private Recorder _recorder = null; //recorder which runs the bash command to record a new name

    @FXML private ListView<String> _recordingListView;

    @FXML private ListView<String> _namesListView;

    @FXML private ProgressBar _audioProgressBar; //generic progress bar for all audio features (playback and recording)

    @FXML private Text _selectStatus;

    @FXML private Text _selectedName;

    @FXML private Label _playStatus;

    @FXML private Label _recordingInPlay;

    @FXML private Button _userListenBtn;

    @FXML private Button _dbListenBtn;

    @FXML private Button _recordBtn;

    @FXML private Button _deleteBtn;

    @FXML private Slider _volumeSlider;

    @FXML private ProgressBar _micLevelBar;

    @FXML private Button _compareBtn;

    @FXML private Button _removeBtn;

    @FXML private Button _shuffleBtn;

    @FXML private Label _recordingListLabel;

    @FXML private Text _recordBtnText; //the following buttons change to a stop icon after being press so they each have an associated text and imageview component

    @FXML private ImageView _recordBtnImage;

    @FXML private Text _dbListenBtnText;

    @FXML private ImageView _dbListenBtnImage;

    @FXML private Text _userListenBtnText;

    @FXML private ImageView _userListenBtnImage;

    @FXML private Text _compareBtnText;

    @FXML private ImageView _compareBtnImage;

    /**Method is called when the scene is loaded and the controller is instantiated, a reference to the NamesListModel is
     * passed into the controller and stored as a field, method also sets up the mic levels bar and disables buttons on startup
     * as well as populate the listviews with names and recordings.
     */
    public void initialise(NamesListModel model){
        _namesListModel = model; //name list model holds information about all the name models stored in the program
        _manager = ControllerManager.getInstance();
        _nameSelectorController = _manager.getController();
        _selectedNames = FXCollections.observableArrayList(_nameSelectorController.getSelectedNames());
        _namesListView.setItems(_selectedNames); //populate name list view with selected names which we get from the manager
        _records = FXCollections.observableArrayList();
        _recordingListView.setItems(_records); //set up recording list view (empty on start up)
        switchButtonStates(true); //set all buttons as disable initially

        //initialising mic level bar
        _micLevelBar.setProgress(0.0);
        _audioVisualModel = new AudioVisualizerModel();
        _micWorker = _audioVisualModel.createWorker();
        _micLevelBar.progressProperty().unbind();
        _micLevelBar.progressProperty().bind(_micWorker.progressProperty());
        new Thread(_micWorker).start(); //run mic testing code on separate thread so GUI is responsive

        //initialise the volume slider bar
        startVolumeSlider();
    }


    /** Returns the user to the main menu and passes the name list model to the main menu, so its state is saved.
     */
    @FXML
    private void goToMain(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/MainMenu.fxml"));
        Parent root = loader.load();
        MainMenuController controller = loader.getController();
        controller.initialise(_namesListModel);
        Scene scene = new Scene(root);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);

        _audioVisualModel.endTask(); //stop the mic level bar when changing scenes
    }


    /**Returns the user to the name selection screen, the manager saves a reference to the NameSelectorController so its state can be saved.
     * A copy of the selected names list in the current screen is also passed back to the name selection screen.
     */
    @FXML
    private void goToSelect(ActionEvent event) throws IOException {
        _manager = ControllerManager.getInstance();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/NamesSelector.fxml"));
        Parent root = loader.load();
        NamesSelectorController controller = loader.getController();
        controller.initialise(_namesListModel);
        controller.setSelectedNames(_selectedNames); //pass in a copy of the current selected names list back to the name selector controller
        _manager.setController(controller);
        Scene scene = new Scene(root);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);

        _audioVisualModel.endTask();
    }


    /**Method is called when user selects a name in the practice list,
     *it finds the recordings associated with the selected name and displays the recordings in the recordings table
     */
    @FXML
    private void selectName(){
        String selection = _namesListView.getSelectionModel().getSelectedItem();
        if (selection != null){
            if (!_inAction){
                _recordingListLabel.setText("User recordings for: "+selection); //change labels to show new names
                _selectStatus.setText("Currently selected:");
                _selectedName.setText(selection);
                if (selection.contains(" ") || selection.contains("-")){ //concatentated name
                    CustomNameModel model = new CustomNameModel(selection);
                    _records.clear();
                    _records.addAll(model.getRecordings());
                } else { //single name
                    NamesModel model = _namesListModel.getName(selection);
                    _records.clear();
                    _records.addAll(model.getPerRecordings());
                }
                _inAction = false;
                switchButtonStates(false);
            }
        }
    }

    /**Called when there is a mouse event fired on the recordings table, if a double click
     * is registered on a recording then it calls listenUserRecording() which plays the recording
     * method also checks if the recording table is empty, if it is then it disables the buttons associated to the recording table
     */
    @FXML
    private void enableUserListen(MouseEvent mouseEvent){
        if (mouseEvent.getClickCount() == 2 && !_inAction){
            listenUserRecording();
        }
        if (!_recordingListView.getSelectionModel().isEmpty()){
            _userListenBtn.setDisable(false);
            _deleteBtn.setDisable(false);
        }

    }

    /**Called when the listen personal button is hit
     * On the first click, it plays the selected recording and turns into a stop button
     * If the user clicks it again while it is a stop button, then the audio is stopped
     */
    @FXML
    private void listenUserRecording(){
        if (_inAction){ //i.e. user clicked when it is a stop button
            _userListenBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/musical-note.png"))); //change back to its normal state
            _userListenBtnText.setText("Listen Personal");
            stopAudio();
            _inAction = false;
        } else { //i.e. nothing is playing at the moment
            if (_recordingListView.getSelectionModel().getSelectedItem() != null) {
                if (_selectedName.getText().contains("-") || _selectedName.getText().contains(" ")) { //check if its a concatenated name or if its a single name
                    String filePath = "Concatenated/" + _recordingListView.getSelectionModel().getSelectedItem();
                    _player = new RecordingPlayer(filePath);
                } else {
                    String filePath = "Single/" + _recordingListView.getSelectionModel().getSelectedItem();
                    _player = new RecordingPlayer(filePath); //make new player and bind progress bar to player
                }
                _audioProgressBar.progressProperty().unbind();
                _audioProgressBar.progressProperty().bind(_player.progressProperty());
                _userListenBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/stop.png"))); //change icon of the button to a stop sign
                _userListenBtnText.setText("Stop");
                _player.setOnSucceeded(e -> {
                    changePlayStatus();
                    _userListenBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/musical-note.png"))); //when the audio finishes change the icon back
                    _userListenBtnText.setText("Listen Personal");
                    _inAction = false;
                    switchButtonStates(false); //re-enable buttons
                    _player = null;
                });
                new Thread(_player).start();
                _playStatus.setText("Now playing: ");
                _recordingInPlay.setText(_recordingListView.getSelectionModel().getSelectedItem());
                _inAction = true;
                switchButtonStates(true); //disable buttons
                _userListenBtn.setDisable(false);
            }
        }
    }

    /**Called when the listen original button is hit, it plays the database version of the selected name
     * On the first click, it plays the selected recording and turns into a stop button
     * If the user clicks it again while it is a stop button, then the audio is stopped
     */
    @FXML
    private void listenDBRecording(){
        if (_inAction){ //i.e. it is currently a stop button
            _dbListenBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/musical-note.png"))); //change back to a normal listen button
            _dbListenBtnText.setText("Listen Original");
            stopAudio();
            _inAction = false;
        } else { //i.e. when no name is being played
            String selection = _selectedName.getText();
            if (selection != null) {
                _playStatus.setText("Now playing database version of: ");
                _recordingInPlay.setText(selection);
                _inAction = true;
                switchButtonStates(true);
                _dbListenBtn.setDisable(false);
                _dbListenBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/stop.png"))); //change to a stop button
                _dbListenBtnText.setText("Stop");
                _concatenatedPlayer = new ConcatenatedPlayer(selection, _namesListModel); //make a concatenated name player
                _audioProgressBar.progressProperty().unbind();
                _audioProgressBar.progressProperty().bind(_concatenatedPlayer.progressProperty());
                _concatenatedPlayer.setOnSucceeded(e -> {
                    _inAction = false;
                    changePlayStatus();
                    _dbListenBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/musical-note.png"))); //change back to a listen button after audio stops
                    _dbListenBtnText.setText("Listen Original");
                    switchButtonStates(false);
                    checkEmptyNameList();
                    _concatenatedPlayer = null;
                });
                new Thread(_concatenatedPlayer).start();
            }
        }
    }

    /**Method called when record is pressed, if its the first press then it starts recording audio for 5 seconds, and turns into a stop button
     * If user presses record while its a stop button then recording is cut short added to the screen
     */
    @FXML
    private void makeRecording(){
        if (_inAction){ //this means button is pressed when currently recording so we want to stop recording
            _audioProgressBar.progressProperty().unbind();
            _audioProgressBar.setProgress(0);
            String newFile = _recorder.stopRecording();
            _recordBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/microphone.png"))); //change back to a record button
            _recordBtnText.setText("Record");
            _inAction = false;
            switchButtonStates(false);
            _records.add(newFile);
            _recorder = null;
            _playStatus.setText("Finished recording!");
            _recordingInPlay.setText("");
        }else { //not recording yet
            String selection = _selectedName.getText();
            if (selection != null && !selection.isEmpty()) {
                _playStatus.setText("Currently Recording For:");
                _recordingInPlay.setText(selection);
                if (selection.contains(" ") || selection.contains("-")) { //check what type of name the selection is
                    _recorder = new Recorder(selection);  //call concatenated name constructor recorder if there are spaces or hyphens in name
                } else {
                    NamesModel name = _namesListModel.getName(selection);
                    _recorder = new Recorder(name); //call single name constructor for the recorder object
                }
                _inAction = true;
                switchButtonStates(true);
                _recordBtn.setDisable(false);
                _recordBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/stop.png"))); //change to a stop button
                _recordBtnText.setText("Stop");
                _recorder.setOnSucceeded(e -> {
                    _playStatus.setText("Finished recording!");
                    _inAction = false;
                    switchButtonStates(false);
                    _recordBtnText.setText("Record");
                    _records.add(_recorder.getValue());
                    _recordBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/microphone.png"))); //change back to a record button
                });
                _audioProgressBar.progressProperty().unbind();
                _audioProgressBar.progressProperty().bind(_recorder.progressProperty());
                new Thread(_recorder).start();
            }
        }
    }

    /**Called when there is no name selected
     * it updates the labels to show this status
     */
    private void changePlayStatus(){
        _playStatus.setText("No recording currently playing");
        _recordingInPlay.setText("");
    }

    /**Called when stop button is pressed
     * it stops the audio coming from the players and re enables the buttons
     */
    @FXML
    private void stopAudio(){
        if (_concatenatedPlayer != null){
            _concatenatedPlayer.stopAudio();
            _audioProgressBar.progressProperty().unbind();
            _audioProgressBar.setProgress(0);
            _concatenatedPlayer.cleanUpFiles();
            _inAction = false;
            _concatenatedPlayer = null;
        }
        if (_player != null){
            boolean yes = _player.cancel();
            System.out.println(yes);
            _player.stopAudio();
            _audioProgressBar.progressProperty().unbind();
            _audioProgressBar.setProgress(0);
            _player.cleanUpFiles();
            _inAction = false;
            _player = null;
        }
        changePlayStatus();
        switchButtonStates(_inAction);
    }

    /**Called when compare button is pressed
     * on the first click it prompts the user for the number of times they want to compare
     * and the plays the two audio clips the specified number of times
     * Once the audio starts, the compare button turns into a stop button, if the user clicks it in this state then the comparison is stopped
     */
    @FXML
    private void compareRecords(){
        if (_inAction){ //i.e comparison is ongoing, it is currently a stop button
            _compareBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/compare.png"))); //turn icon back to comparison icon
            _compareBtnText.setText("Compare");
            stopAudio();
            _inAction = false;
        } else { //i.e comparison has not happened yet
            if (_recordingListView.getSelectionModel().getSelectedItem() == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION); //pop up which prompts user with options for comparison
                alert.setTitle("No selection !");
                alert.setHeaderText(null);
                alert.setContentText("Please select a personal recording to do a comparision with!");
                alert.showAndWait();
            } else {
                List<String> choices = new ArrayList<>(Arrays.asList(new String[]{"1","2","3","4","5"}));
                ChoiceDialog<String> dialog = new ChoiceDialog<>("1", choices);
                dialog.setTitle("Comparison");
                dialog.setHeaderText("You are comparing recordings for '" + _recordingInPlay.getText() + "'");
                dialog.setContentText("Please choose how many times you want to compare: ");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) { //check if user clicks cancel
                    String databaseSelection = _selectedName.getText();
                    String userSelection;
                    if (databaseSelection.contains("-") || databaseSelection.contains(" ")) { //get the filepath of the user recording, depending if its a single or concatenated name
                        userSelection = "Concatenated/" + _recordingListView.getSelectionModel().getSelectedItem();
                    } else {
                        userSelection = "Single/" + _recordingListView.getSelectionModel().getSelectedItem();
                    }
                    _playStatus.setText("Comparision " + result.get() + ": user and database versions of:");
                    _inAction = true;
                    switchButtonStates(true);
                    _compareBtn.setDisable(false);
                    _compareBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/stop.png"))); //change the button icon to a stop sign
                    _compareBtnText.setText("Stop");
                    _recordingInPlay.setText("'" + databaseSelection + "'");
                    compare(databaseSelection, userSelection, Integer.valueOf(result.get())); //call the recursive function which loops until specificed number of comparisons is over
                }
            }
        }
    }

    /**Recursive function that repeats the comparison for however many times the user specified, it is called in compareRecords()
     * it first plays the concatenated recording and then plays the user's own version, before making a recursive call on itself
     */
    private void compare(String ogSelection, String perSelection, int repeat){
        if (repeat == 0){ //i.e comparison is over then revert stop button back to a compare button
            _inAction = false;
            _compareBtnImage.setImage(new Image(getClass().getResourceAsStream("/application/Images/compare.png")));
            _compareBtnText.setText("Compare");
            switchButtonStates(false);
            changePlayStatus();
            return;
        } else { //comparison ongoing
            _concatenatedPlayer = new ConcatenatedPlayer(ogSelection,_namesListModel); //play the concatenated version first
            _audioProgressBar.progressProperty().unbind();
            _audioProgressBar.progressProperty().bind(_concatenatedPlayer.progressProperty());
            _concatenatedPlayer.setOnSucceeded(e -> { //when concat version finishes play user's recording before calling compare again
                _player = new RecordingPlayer(perSelection);
                _audioProgressBar.progressProperty().unbind();
                _audioProgressBar.progressProperty().bind(_player.progressProperty());
                _player.setOnSucceeded(m -> {
                    _playStatus.setText("Comparision "+(repeat-1)+ ": user and database versions of:");
                    compare(ogSelection, perSelection, repeat - 1); //recursive call
                });
                new Thread(_player).start();
            });
            new Thread(_concatenatedPlayer).start();
        }
    }

    /**Shuffle the names list and reselect a new name;
     */
    @FXML
    private void shuffleNames(){
        Collections.shuffle(_selectedNames);
        selectName();
    }

    /**Remove the current selection from the name list
     * modifies the recording table and its labels to reflect this removal
     */
    @FXML
    private void removeName(){
        String selection = _namesListView.getSelectionModel().getSelectedItem();
        if (selection != null){
            _selectedNames.remove(selection);
            _selectedName.setText("");
            _selectStatus.setText("No name selected");
            _recordingListLabel.setText("Please select a name to see user recordings");
            switchButtonStates(true);
            _records.clear();
            checkEmptyNameList();
        }
    }

    /** Method is called when the delete button is pressed, it prompts the user
     * for confirmation if they want to continue deleting the user's recording
     */
    @FXML
    private void deleteRecording(){
        String selection = _recordingListView.getSelectionModel().getSelectedItem();
        if (selection != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete?");
            alert.setHeaderText("You are about to delete '" + selection + "'");
            alert.setContentText("Hit Ok to confirm or Cancel to return to menu");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                if (_selectedName.getText().contains("-")|| _selectedName.getText().contains(" ")){ //checks which directory the recording is stored in Single or Concatenated
                    new File("Concatenated/" + selection).delete();
                    _records.remove(selection);
                } else {
                    NamesModel model = _namesListModel.getName(_selectedName.getText());
                    model.delete(selection);
                    _records.remove(selection);
                }

            }
        }

    }

    /** Method is called when an audio action has occcurred (either playback or recording)
     * it turns on and off the buttons depending on what boolean is passed in
     */
    private void switchButtonStates(boolean flip){
        _dbListenBtn.setDisable(flip);
        _userListenBtn.setDisable(flip);
        _recordBtn.setDisable(flip);
        _deleteBtn.setDisable(flip);
        _shuffleBtn.setDisable(flip);
        _removeBtn.setDisable(flip);
        _compareBtn.setDisable(flip);
    }

    /**Method called when there a name has been removed
     * it checks if the list is empty, and if it is then it disables all buttons
     */
    private void checkEmptyNameList(){
        if (_namesListView.getSelectionModel().isEmpty()){
            _dbListenBtn.setDisable(true);
            _shuffleBtn.setDisable(true);
            _removeBtn.setDisable(true);
            _compareBtn.setDisable(true);
            _recordBtn.setDisable(true);
        }
    }

    /** Method sets up the volume adjustment bar by binding a volume slider to the volume level;
     */
    private void startVolumeSlider(){

        //running command to get current volume
        String cmd1 = "amixer get Master | awk '$0~/%/{print $4}' | tr -d '[]%'";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd1);

        try{
            Process volumeInitializer = builder.start();
            InputStream inputStream = volumeInitializer.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)); //reading current volume level
            String volumeLevel = br.readLine();
            double vlevel = Double.parseDouble(volumeLevel);
            _volumeSlider.setValue(vlevel); //bind volume level to the slider
        } catch (IOException e){
            e.printStackTrace();
        }

        //attach a listener to the volume bar so when it slides it changes the system volume
        //https://www.youtube.com/watch?v=X9mEBGXX3dA reference
        _volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                double volume = _volumeSlider.getValue();
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
