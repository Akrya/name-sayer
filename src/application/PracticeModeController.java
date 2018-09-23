package application;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class PracticeModeController implements Initializable {

    @FXML
    private TreeView<String> ogTreeView;

    @FXML
    private ListView<String> personalRecordings;
import java.net.URL;
import java.util.ResourceBundle;

    @FXML
    private Button listenBtn;

    @FXML
    private Button recordBtn;

    @FXML
    private Button compBtn;

    @FXML
    private Button listenModeBtn;

    @FXML
    private Text ogPlayStatus;

    @FXML
    private Text selectedRecording;

    @FXML
    private ProgressBar ogProgressBar;

    private NamesListModel _namesListModel = new NamesListModel();

    private TreeViewModel _treeViewModel = new TreeViewModel();



    private ObservableList<String> _practiceNames;
public class PracticeModeController implements Initializable {

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
    private void enableBtn(MouseEvent mouseEvent){
        TreeItem<String> selection = ogTreeView.getSelectionModel().getSelectedItem();
        if (selection != null) {
            if (selection.isLeaf() && _treeViewModel.calcHeight(selection) == 4) {
                listenBtn.setDisable(false);
                recordBtn.setDisable(false);
            }
        }
        if(mouseEvent.getClickCount() == 2){
            playRecording();
        }
    }

    @FXML
    private void playRecording(){
        //update playingText to display whats currently playing
        String selection = ogTreeView.getSelectionModel().getSelectedItem().getValue();
        if (ogTreeView.getSelectionModel().getSelectedItem().isLeaf() && _treeViewModel.calcHeight(ogTreeView.getSelectionModel().getSelectedItem()) == 4 ){
            listenBtn.setDisable(true);
            listenModeBtn.setDisable(true);
            recordBtn.setDisable(true);
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
            filePath = "Names/Original/"+filePath;
            ogProgressBar.setProgress(0);
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
                        if (ogProgressBar.getProgress() >= 1) {
                            timer.cancel();
                            listenBtn.setDisable(false);
                            recordBtn.setDisable(false);
                            ogPlayStatus.setText("No recording currently playing");
                            selectedRecording.setText("");
                            listenModeBtn.setDisable(false);
                            ogProgressBar.setProgress(0);
                        } else {
                            Platform.runLater(() -> {
                                ogProgressBar.setProgress(ogProgressBar.getProgress() + 0.01);
                            });
                        }
                    }
                };
                timer.scheduleAtFixedRate(timerTask, 0, (int)(length*10));
                //play the selected recording
                InputStream inputStream = new FileInputStream(filePath);
                AudioStream audioStream = new AudioStream(inputStream);
                AudioPlayer.player.start(audioStream);
            } catch (IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        } else{
            return;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listenBtn.setDisable(true);
        recordBtn.setDisable(true);
        compBtn.setDisable(true);


        _treeViewModel.populateTree(ogTreeView,0,_namesListModel);
        _practiceNames = FXCollections.observableArrayList();
        personalRecordings.setItems(_practiceNames);
    }



}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        audioVisualizer.setProgress(0.0);
        copyWorker = createWorker();
        audioVisualizer.progressProperty().unbind();
        audioVisualizer.progressProperty().bind(copyWorker.progressProperty());
        new Thread(copyWorker).start();
        // TODO
    }


    public Task createWorker() {
        return new Task() {
            @Override
            protected Object call() throws Exception {

                // Open a TargetDataLine for getting microphone input & sound level

                AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44000, 16, 2, 4, 1000, true);
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