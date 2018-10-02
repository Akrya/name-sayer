package application.uiControllers;

import application.models.*;
import javafx.application.Platform;
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
import javafx.scene.text.Text;
import javafx.stage.Stage;


import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.util.*;

public class ListenModeController implements Initializable {


    @FXML
    private Button practiceBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button addBtn;

    @FXML
    private ListView<String> playQueue;

    @FXML
    private TreeView<String> originalTreeView;

    @FXML
    private TabPane tabPane;

    @FXML
    private Button clearBtn;

    @FXML
    private Button removeBtn;

    @FXML
    private Button randomiseBtn;

    @FXML
    private Button listenBtn;

    @FXML
    private Button rateBtn;

    private ObservableList<String> _queuedNames;

    @FXML
    private Text playingText;

    @FXML
    private Text selectedRecording;

    @FXML
    private ProgressBar playingBar;

    @FXML
    private TreeView<String> personalTreeView;

    private NamesListModel _namesListModel = new NamesListModel();

    private TreeViewModel _treeModel = new TreeViewModel();

    //takes you to home window
    @FXML
    private void openRecordScene(ActionEvent event) throws IOException {
        Parent createScene = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        Scene scene = new Scene(createScene);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void deleteRecording(){

        TreeItem<String> selection = personalTreeView.getSelectionModel().getSelectedItem();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete?");
        alert.setHeaderText("You are about to delete "+ "'se206_"+ selection.getValue() +"'");
        alert.setContentText("Hit Ok to confirm or Cancel to return to menu");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            selection.getParent().getChildren().remove(selection); //remove file from treeview
            String selectionName = selection.getValue().substring(selection.getValue().lastIndexOf('_')+1,selection.getValue().lastIndexOf('.'));
            NamesModel selectionModel = _namesListModel.getName(selectionName);
            String fileName ="";
            List<RecordingModel> records = selectionModel.getRecords();
            for (RecordingModel record : records){
                if (record.getFileName().contains(selection.getValue())){
                    selectionModel.delete(record.getFileName());
                    fileName = record.getFileName();
                    break;
                }
            }
            if (_queuedNames.indexOf(fileName) != -1){ //remove the recording if its been queued in play list
                _queuedNames.remove(fileName);
            }
        }

    }

    @FXML
    public void addToQueue(){

        TreeItem<String> selection;
        List<String> recordings = new ArrayList<>();
        int key;
        if (tabPane.getSelectionModel().getSelectedIndex() == 0){
            selection = originalTreeView.getSelectionModel().getSelectedItem();
        } else {
            selection = personalTreeView.getSelectionModel().getSelectedItem();
        }

        if (selection != null){
            if (selection.isLeaf() && _treeModel.calcHeight(selection) == 4){
               String queueName = selection.getValue().substring(selection.getValue().lastIndexOf('_')+1,selection.getValue().lastIndexOf('.'));
               NamesModel queueNameModel = _namesListModel.getName(queueName);
               List<RecordingModel> records = queueNameModel.getRecords();
               for (RecordingModel record : records){
                   if (record.getIdentifier() == tabPane.getSelectionModel().getSelectedIndex()){
                       recordings.add(record.getFileName());
                   }
               }
               for (String s : recordings){
                   if (s.indexOf(selection.getValue()) != -1){
                       queueName = s;
                       break;
                   }
               }
               if (_queuedNames.indexOf(queueName) == -1){
                   _queuedNames.add(queueName);
                   clearBtn.setDisable(false);
                   randomiseBtn.setDisable(false);
               }

            }

        }

    }

    @FXML
    private void playRecording(){
        //update playingText to display whats currently playing
        String selection = playQueue.getSelectionModel().getSelectedItem();
        removeBtn.setDisable(true);
        listenBtn.setDisable(true);
        practiceBtn.setDisable(true);
        playingText.setText("Currently playing");
        selectedRecording.setText("'"+selection+"'");
        String filePath;
        boolean original = false;
        String queueName = selection.substring(selection.lastIndexOf('_')+1,selection.lastIndexOf('.'));
        NamesModel queueNameModel = _namesListModel.getName(queueName);
        List<RecordingModel> records = queueNameModel.getRecords();
        for (RecordingModel record : records){
            if (record.getFileName().equals(selection)){
                if (record.getIdentifier() == 0){
                    original = true;
                    break;
                }
            }
        }
        if (original){
            filePath = "Original/"+selection;
        } else {
            filePath = "Personal/"+selection;
        }
        playingBar.setProgress(0);
        try {
            //get length of the recording
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            double length =  ((frames+0.0) / format.getFrameRate()); //length of recording in seconds
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (playingBar.getProgress() >= 1) {
                        timer.cancel();
                        removeBtn.setDisable(false);
                        listenBtn.setDisable(false);
                        playingText.setText("No recording currently playing");
                        selectedRecording.setText("");
                        practiceBtn.setDisable(false);
                        playingBar.setProgress(0);
                        playQueue.getSelectionModel().selectNext();
                    } else {
                        Platform.runLater(() -> {
                            playingBar.setProgress(playingBar.getProgress() + 0.01);
                        });
                    }
                }
            };
            timer.scheduleAtFixedRate(timerTask, 0, (int)(length*10));
            //play the selected recording
            RecordingPlayer player = new RecordingPlayer(filePath, length);
            new Thread(player).start();
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clearQueue(){
        _queuedNames.clear(); //need prevent clear button from being pressed during an audio file being played
        clearBtn.setDisable(true);
        removeBtn.setDisable(true);
        listenBtn.setDisable(true);
        randomiseBtn.setDisable(true);
        playingText.setText("No recording currently playing");
        selectedRecording.setText("");
    }

    @FXML
    private void removeQueue(){
        String selection = playQueue.getSelectionModel().getSelectedItem();
        _queuedNames.remove(selection);
        if (_queuedNames.isEmpty()) {
            clearBtn.setDisable(true);
            removeBtn.setDisable(true);
            listenBtn.setDisable(true);
            randomiseBtn.setDisable(true);
            playingText.setText("No recording currently playing");
            selectedRecording.setText("");
        }
    }

    @FXML
    private void enableListBtns(MouseEvent mouseEvent){
        if (!playQueue.getSelectionModel().isEmpty()) {
            removeBtn.setDisable(false);
            listenBtn.setDisable(false);
        } else {
            removeBtn.setDisable(true);
            listenBtn.setDisable(true);
        }
        if(mouseEvent.getClickCount() == 2){
            playRecording();
        }
    }

    @FXML
    private void enableOgBtns(MouseEvent mouseEvent){
        if(mouseEvent.getClickCount() == 2){
            addToQueue();
        }
        deleteBtn.setDisable(true);
        TreeItem<String> selection = originalTreeView.getSelectionModel().getSelectedItem();
        if (selection != null) {
            if (selection.isLeaf() && _treeModel.calcHeight(selection) == 4) {
                addBtn.setDisable(false);
                rateBtn.setDisable(false);
            }
        }
    }

    @FXML
    private void enablePerBtns(MouseEvent mouseEvent){
        if(mouseEvent.getClickCount() == 2){
            addToQueue();
        }
        TreeItem<String> selection = personalTreeView.getSelectionModel().getSelectedItem();

        if (selection != null) {
            if (selection.isLeaf() && _treeModel.calcHeight(selection) == 4) {
                deleteBtn.setDisable(false);
            } else {
                deleteBtn.setDisable(true);
            }
        } else {
            deleteBtn.setDisable(true);
        }
        addBtn.setDisable(false);
        rateBtn.setDisable(false);
    }

    @FXML
    private void rateRecording(){
        TreeItem<String> selection = (tabPane.getSelectionModel().getSelectedIndex() == 0) ? originalTreeView.getSelectionModel().getSelectedItem() : personalTreeView.getSelectionModel().getSelectedItem();
        RecordingRater rater = new RecordingRater(selection.getValue());

        boolean exists = rater.checkFile();
        if (exists){
            boolean overwrite = rater.overWriteRating();
            if (overwrite){
                rater.makeRating();
            } else {
                return;
            }
        } else {
            rater.makeRating();
        }

    }

    @FXML
    private void randomiseQueue(){
        Collections.shuffle(_queuedNames);
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
        _treeModel.populateTree(originalTreeView, 0,_namesListModel);
        _treeModel.populateTree(personalTreeView, 1,_namesListModel);
        _queuedNames = FXCollections.observableArrayList();
        playQueue.setItems(_queuedNames);

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
                writer.println("Each recording stored in this file has a rating of 'Good' or 'Bad'");
                writer.println("");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
