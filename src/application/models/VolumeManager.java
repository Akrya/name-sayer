package application.models;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.concurrent.Task;
import javafx.scene.control.Slider;

import javax.sound.sampled.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**Class is responsible for setting up the audio related GUI components
 * it sets up the logic for reading in mic-input levels, it then binds the mic-levels to an associated progress bar
 * The logic for calculating each mic-level is handles by a task object which runs on a separate thread from the event dispatch thread
 * It also binds the volume slider to system levels
 */
public class VolumeManager {

    private Slider _volumeSlider;

    public VolumeManager(Slider volumeSlider){
        _volumeSlider = volumeSlider;
    }

    /** Method sets up the volume adjustment bar by binding a volume slider to the volume level;
     */
    public void startVolumeSlider(){

        //running command to get current volume https://unix.stackexchange.com/questions/89571/how-to-get-volume-level-from-the-command-line/89581
        String cmd1 = "amixer get Master | awk '$0~/%/{print $4}' | tr -d '[]%'";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd1);

        //setting the slider to the current volume
        try{
            Process volumeInitializer = builder.start();
            InputStream inputStream = volumeInitializer.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String volumeLevel = br.readLine();
            double vlevel = Double.parseDouble(volumeLevel);
            _volumeSlider.setValue(vlevel);

        } catch (IOException e){
            e.printStackTrace();
        }

        //attach a listener to the volume bar so when it slides it changes the system volume
        //https://www.youtube.com/watch?v=X9mEBGXX3dA reference
        _volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                double volume = _volumeSlider.getValue();
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

    private TargetDataLine line = null;

    /**Method creates a Task object which calaculates the RMS values of the system input, and then binds this value to a progress bar
     * @return Task object which handles calculates system volume levels
     */
    public Task createWorker() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                //Reference for mic-testing: https://stackoverflow.com/questions/15870666/calculating-microphone-volume-trying-to-find-max

                // Open a TargetDataLine for getting microphone input & sound level
                AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 4400, 16, 2, 4, 1000, true);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); //     format is an AudioFormat object
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

                    //update binded progress bar with mic level
                    updateProgress(num, 100);
                }

            }
        };
    }

    /**Called when scene is switching and mic-level bar is no longer needed.
     */
    public void endTask(){
        line.close();
    }


}
