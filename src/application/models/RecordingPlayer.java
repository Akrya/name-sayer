package application.models;

import javafx.concurrent.Task;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RecordingPlayer extends Task<Void> {

    private String _filePath;

    public RecordingPlayer(String filePath){
        _filePath =filePath;
    }

    @Override
    protected Void call() throws Exception {
        playAudio();

        return null;
    }

    private void playAudio(){
        String cmd = "ffplay -loglevel panic -autoexit -nodisp -i "+"'"+_filePath+"'";
        System.out.println(cmd);
        ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c",cmd);
        try {
            Process process = builder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
