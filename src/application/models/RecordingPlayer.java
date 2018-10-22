package application.models;

import javafx.concurrent.Task;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.security.Provider;

/**Class is responsible for playing user recordings and also single database recordings
 */
public class RecordingPlayer extends Task<Void> {

    private String _filePath;
    private double _length;
    private boolean _playOriginal;
    private Process _audioProcess;

    /**Constructor called when user presses listen for a single or user recording
     * @param filePath relative file path of the audio
     */
    public RecordingPlayer(String filePath) {
        _filePath = filePath;
        _playOriginal = false;
        trimAudio();
        calcLength("silenced.wav"); //"silenced.wav" is generated at the end of trimAudio();
        if (_length == 0) { //if trimmed file is empty then play original file
            _playOriginal = true;
            calcLength(filePath);
        }
    }

    /**Method called when task is started, it plays the audio on a separate thread,
     * and then waits for the audio to stop while updating the binded progress bar
     * @return null if task successful
     * @throws Exception
     */
    @Override
    protected Void call() throws Exception {
        new Thread(()->{
            playAudio();
        }).start();
        waitForPlay();
        cleanUpFiles();
        return null;
    }

    /**Called after playback of audio is complete, or is stopped prematurely
     * it deletes the temporary files used for playback, so the player can be reused again in the future
     */
    public void cleanUpFiles() {
        File temp = new File("silenced.wav");
        temp.delete();
    }

    /**Method calls a bash command to trim out any silence detected in the recording
     */
    private void trimAudio() {
        String cmd = "ffmpeg -i '" + _filePath + "' -af silenceremove=1:0:-30dB silenced.wav";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd);
        try {
            Process process = builder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**Method is called in call() to play the audio, it uses a bash command to play the audio file
     */
    private void playAudio() {
        String cmd;
        if (_playOriginal) {
            cmd = "ffplay -loglevel panic -autoexit -nodisp -i '" + _filePath + "'";
        } else {
            cmd = "ffplay -loglevel panic -autoexit -nodisp -i silenced.wav";
        }
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd);
        try {
            _audioProcess = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**Method called by controller if the stop button is pressed, it prematurely ends the
     * play back process
     */
    public void stopAudio(){
        _audioProcess.destroy();
    }

    /** Method periodically updates the progress bar while the audio is playing on a separate thread
     */
    private void waitForPlay() throws InterruptedException {
        int approxLength = (int) (_length * 1000);
        for (int i = 0; i < approxLength; i++) {
            Thread.sleep(1);
            updateProgress(i + 1, approxLength); //update binded progress bar periodically for duration of audio
        }
    }

    /**Method calculates the length of audio of given file and returns this length in seconds
     * @param filePath relative path of file we want to find a length for
     */
    private void calcLength(String filePath) {
        //reference to calculate wav file length https://stackoverflow.com/questions/3009908/how-do-i-get-a-sound-files-total-time-in-java
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            _length = ((frames + 0.0) / format.getFrameRate());
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
