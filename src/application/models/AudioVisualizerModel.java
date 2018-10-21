package application.models;

import javafx.concurrent.Task;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

/**Class sets up the logic for reading in mic-input levels, it then binds the mic-levels to an associated progress bar
 * The logic for calculating each mic-level is handles by a task object which runs on a separate thread from the event dispatch thread
 */
public class AudioVisualizerModel {

    private TargetDataLine line = null;

    /** Method creates a Task object which calaculates the RMS values of the system input, and then binds this value to a progress bar
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
