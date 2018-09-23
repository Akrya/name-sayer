package application;

import com.sun.prism.impl.Disposer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import sun.reflect.generics.tree.Tree;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
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

    private NamesListModel _namesListModel = new NamesListModel();

    private ObservableList<String> _practiceRecordings;

    private ObservableList<String> _ogRecordings;

    private ObservableList<String> _ogNames;

    @FXML
    private ProgressBar audioVisualizer;

    private Task copyWorker;

    private TargetDataLine line = null;

    @FXML
    private void goToListenMode(ActionEvent event) throws IOException {
        Parent listenScene = FXMLLoader.load(getClass().getResource("ListenMode.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);

        //Closing TargetDataLine when switching back to listen mode
        line.close();
    }

    @FXML
    private void enableSelectBtn(MouseEvent mouseEvent){
        if (!ogNames.getSelectionModel().isEmpty()) {
            selectBtn.setDisable(false);
        } else {
            selectBtn.setDisable(true);
        }
        if(mouseEvent.getClickCount() == 2){
            addToOgRecordings();
        }
    }

    @FXML
    private void enableListenOg(MouseEvent mouseEvent){
        if (ogRecordings.getSelectionModel().getSelectedItem() != null && personalRecordings.getSelectionModel().getSelectedItem() != null){
            compBtn.setDisable(false);
        }
        if (!ogRecordings.getSelectionModel().isEmpty()){
            listenOgBtn.setDisable(false);
        } else{
            listenOgBtn.setDisable(true);
        }
        if (mouseEvent.getClickCount() == 2) {
            playOgRecording();
        }
    }

    @FXML
    private void enableListenPer(MouseEvent mouseEvent){
        if (ogRecordings.getSelectionModel().getSelectedItem() != null && personalRecordings.getSelectionModel().getSelectedItem() != null){
            compBtn.setDisable(false);
        }
        if (!personalRecordings.getSelectionModel().isEmpty()){
            listenPerBtn.setDisable(false);
        } else{
            listenPerBtn.setDisable(true);
        }
        if (mouseEvent.getClickCount() == 2) {
            playPerRecording();
        }
    }

    @FXML
    private void addToOgRecordings(){
        selectedStatus.setText("Currently selected:");
        selectedName.setText(ogNames.getSelectionModel().getSelectedItem());
        NamesModel model = _namesListModel.getName(ogNames.getSelectionModel().getSelectedItem());
        List<String> recordings = model.getOgRecordings();
        _ogRecordings = FXCollections.observableArrayList(recordings);
        ogRecordings.setItems(_ogRecordings);
        recordings = model.getPerRecordings();
        _practiceRecordings = FXCollections.observableArrayList(recordings);
        personalRecordings.setItems(_practiceRecordings);
        recordBtn.setDisable(false);
        listenOgBtn.setDisable(true);
        listenPerBtn.setDisable(true);
        compBtn.setDisable(true);
    }

    @FXML
    private void compareRecordings(){
        if (personalRecordings.getSelectionModel().getSelectedItem() != null && ogRecordings.getSelectionModel().getSelectedItem() != null){
            ogPlayStatus.setText("Now comparing ");
            selectedRecording.setText("'"+ogRecordings.getSelectionModel().getSelectedItem()+"' with '"+personalRecordings.getSelectionModel().getSelectedItem()+"'");
            listenModeBtn.setDisable(true);
            listenOgBtn.setDisable(true);
            listenPerBtn.setDisable(true);
            recordBtn.setDisable(true);
            compBtn.setDisable(true);
            String ogfile = "";
            String perfile = "";
            String ogSelection = ogRecordings.getSelectionModel().getSelectedItem();
            String perSelection = personalRecordings.getSelectionModel().getSelectedItem();
            String queueName = ogSelection.substring(ogSelection.lastIndexOf('_')+1,ogSelection.lastIndexOf('.'));
            NamesModel model = _namesListModel.getName(queueName);
            Map<String, Integer> recordingsMap = model.getRecordings();
            for (Map.Entry<String, Integer> entry : recordingsMap.entrySet()){
                if (entry.getKey().contains(ogSelection)) {
                    ogfile = entry.getKey();
                }
                if (entry.getKey().contains(perSelection)){
                    perfile = entry.getKey();
                }
            }
            final String ogPath = "Names/Original/"+ogfile;
            final String perPath = "Names/Personal/"+perfile;
            ogProgressBar.setProgress(0);
            AudioInputStream audioInputStream = null;
            try {
                audioInputStream = AudioSystem.getAudioInputStream(new File(ogPath));
                AudioFormat format = audioInputStream.getFormat();
                long frames = audioInputStream.getFrameLength();
                final double ogLength =  ((frames+0.0) / format.getFrameRate()); //length of recording in seconds
                audioInputStream = AudioSystem.getAudioInputStream(new File(perPath));
                format = audioInputStream.getFormat();
                frames = audioInputStream.getFrameLength();
                final double perLength =  ((frames+0.0) / format.getFrameRate());
                setUpProgressBar(ogLength+perLength);
                System.out.println(ogPath + " "+ ogLength);
                RecordingPlayer player = new RecordingPlayer(ogPath,ogLength);
                player.setOnSucceeded(e ->{
                    System.out.println(perPath + " "+ perLength);
                    RecordingPlayer player2 = new RecordingPlayer(perPath,perLength); //play second video when first ends
                    player2.setOnSucceeded(b ->{
                        listenModeBtn.setDisable(false); //re-enable buttons after both videos play
                        listenOgBtn.setDisable(false);
                        listenPerBtn.setDisable(false);
                        recordBtn.setDisable(false);
                        compBtn.setDisable(false);
                        ogPlayStatus.setText("Comparison Over");
                        selectedRecording.setText("");
                    });
                    new Thread(player2).start();
                });
                new Thread(player).start();
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void playOgRecording(){
        playRecording(0);
    }

    @FXML
    private void playPerRecording(){
        playRecording(1);
    }

    private void playRecording(int identifier){

        String selection = (identifier == 0) ? ogRecordings.getSelectionModel().getSelectedItem() : personalRecordings.getSelectionModel().getSelectedItem();
        if (selection != null){
            listenPerBtn.setDisable(true);
            listenOgBtn.setDisable(true);
            listenModeBtn.setDisable(true);
            recordBtn.setDisable(true);
            compBtn.setDisable(true);
            ogPlayStatus.setText("Currently playing");
            selectedRecording.setText("'"+selection+"'");
            String filePath = "";
            String queueName = selection.substring(selection.lastIndexOf('_')+1,selection.lastIndexOf('.'));
            NamesModel queueNameModel = _namesListModel.getName(queueName);
            Map<String, Integer> recordingsMap = queueNameModel.getRecordings();
            for (Map.Entry<String, Integer> entry : recordingsMap.entrySet()){
                if (entry.getKey().contains(selection)) {
                    filePath = entry.getKey();
                }
            }
            filePath = (identifier == 0) ? "Names/Original/"+filePath : "Names/Personal/" + filePath;
            ogProgressBar.setProgress(0);
            try {
                //get length of the recording
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
                AudioFormat format = audioInputStream.getFormat();
                long frames = audioInputStream.getFrameLength();
                final double length =  ((frames+0.0) / format.getFrameRate()); //length of recording in seconds
                setUpProgressBar(length);
                //play the selected recording
                RecordingPlayer player = new RecordingPlayer(filePath,length);
                player.setOnSucceeded(e ->{
                    System.out.println(length);
                    listenOgBtn.setDisable(false);
                    listenPerBtn.setDisable(false);
                    recordBtn.setDisable(false);
                    compBtn.setDisable(true);
                    ogPlayStatus.setText("No recording currently playing");
                    selectedRecording.setText("");
                    listenModeBtn.setDisable(false);
                });
                new Thread(player).start();
            } catch (IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpProgressBar(double length){
        ogProgressBar.setProgress(0);
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (ogProgressBar.getProgress() >= 1) {
                    timer.cancel();
                    ogProgressBar.setProgress(0);
                } else {
                    Platform.runLater(() -> {
                        ogProgressBar.setProgress(ogProgressBar.getProgress() + 0.01);
                    });
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, (int)(length*10));
    }

    @FXML
    private void recordNewName(){
        String selection = selectedName.getText();
        NamesModel selectedName = _namesListModel.getName(selection);
        Recorder recorder = new Recorder(selectedName);
        recorder.setOnSucceeded(e -> {
            listenOgBtn.setDisable(false);
            listenPerBtn.setDisable(false);
            recordBtn.setDisable(false);
            ogPlayStatus.setText("Finished recording!");
            _practiceRecordings.add(recorder.getValue());
            System.out.println("yes");
        });
        new Thread(recorder).start();
        recordBtn.setDisable(true);
        listenPerBtn.setDisable(true);
        listenOgBtn.setDisable(true);
        ogProgressBar.setProgress(0);
        ogPlayStatus.setText("Now recording for 5 seconds for the name");
        selectedRecording.setText("'"+selectedName.toString()+"'");
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (ogProgressBar.getProgress() >= 1) {
                    timer.cancel();
                } else {
                    Platform.runLater(() -> { //update progress bar display, but doing it on gui thread to ensure thread safety
                        ogProgressBar.setProgress(ogProgressBar.getProgress() + 0.01);
                    });
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 50); // 5 seconds for recording

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

        audioVisualizer.setProgress(0.0);
        copyWorker = createWorker();
        audioVisualizer.progressProperty().unbind();
        audioVisualizer.progressProperty().bind(copyWorker.progressProperty());
        new Thread(copyWorker).start(); //run mic testing code on separate thread so GUI is responsive
    }

    public Task createWorker() {
        return new Task() {
            @Override
            protected Object call() throws Exception {

                // Open a TargetDataLine for getting microphone input & sound level

                AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 4400, 16, 2, 4, 1000, true);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); //     format is an AudioFormat object
                //System.out.println(info);
                if (!AudioSystem.isLineSupported(info)) {
                    System.out.println("The line is not supported.");
                }
                // Obtain and open the line.
                try {
                    line = (TargetDataLine) AudioSystem.getLine(info);
                    line.open(format);
                    line.start();
                } catch (LineUnavailableException ex) {
                    System.out.println("The TargetDataLine is Unavailable.");
                }


                while (1 > 0) {
                    byte[] audioData = new byte[line.getBufferSize() / 10];
                    line.read(audioData, 0, audioData.length);

                    long lSum = 0;
                    for (int i = 0; i < audioData.length; i++)
                        lSum = lSum + audioData[i];

                    double dAvg = lSum / audioData.length;

                    double sumMeanSquare = 0d;
                    for (int j = 0; j < audioData.length; j++)
                        sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);

                    double averageMeanSquare = sumMeanSquare / audioData.length;
                    int x = (int) (Math.pow(averageMeanSquare, 0.5d) + 0.5);

                    double num = x;
                    updateProgress(num, 100);
                }

            }
        };


    }

}