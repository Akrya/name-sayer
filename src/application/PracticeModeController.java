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
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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