package application.models;

import javafx.concurrent.Task;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.FileInputStream;
import java.io.InputStream;

public class RecordingPlayer extends Task<Void> {

    private String _filePath;
    private double _duration;

    public RecordingPlayer(String filePath, double duration){
        _filePath =filePath;
        _duration =duration+0.5; //add another half second as record playing is not instantaneous
    }

    @Override
    protected Void call() throws Exception {
        InputStream inputStream = new FileInputStream(_filePath);
        AudioStream audioStream = new AudioStream(inputStream);
        AudioPlayer.player.start(audioStream);
        Thread.sleep((long)_duration*1000);
        audioStream.close();
        return null;
    }
}
