package application.models;

import javafx.concurrent.Task;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RecordingPlayer extends Task<Void> {

    private String _filePath;
    private double _length;

    public RecordingPlayer(String filePath){
        _filePath =filePath;
        calcLength();
    }

    @Override
    protected Void call() throws Exception {
        playAudio();
        waitForPlay();
        return null;
    }

    private void playAudio(){
        String cmd = "ffplay -loglevel panic -autoexit -nodisp -i "+"'"+_filePath+"'";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c",cmd);
        try {
            builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void waitForPlay() throws InterruptedException {
        int approxLength = (int) (_length*1000);
        for (int i=0;i<approxLength;i++){
            Thread.sleep(1);
            updateProgress(i+1,approxLength); //update binded progress bar periodically for duration of audio
        }
    }
    private void calcLength() {
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(new File(_filePath));
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            _length =  ((frames+0.0) / format.getFrameRate());
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
