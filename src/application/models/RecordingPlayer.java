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

public class RecordingPlayer extends Task<Void> {

    private String _filePath;
    private double _length;
    private boolean _playOriginal;
    private int _adjustedVolume;
    private final int _targetVolume = -10;

    public RecordingPlayer(String filePath) {
        _filePath = filePath;
        _playOriginal = false;

//        trimAudio();
//        calcLength("silenced.wav");
//        if (_length == 0) {
//            _playOriginal = true;
//            calcLength(filePath);
//        }
    }

    @Override
    protected Void call() throws Exception {
        normaliseAudio();
        playNormal();
//        playAudio();
//        waitForPlay();
//        removeTemp();
        return null;
    }


    private void removeTemp() {
        File temp = new File("silenced.wav");
        temp.delete();
    }

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


    private void playAudio() {
        String cmd;
        if (_playOriginal) {
            cmd = "ffplay -loglevel panic -autoexit -nodisp -i '" + _filePath + "'";
        } else {
            cmd = "ffplay -loglevel panic -autoexit -nodisp -i silenced.wav";
        }
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd);
        try {
            builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void waitForPlay() throws InterruptedException {
        int approxLength = (int) (_length * 1000);
        for (int i = 0; i < approxLength; i++) {
            Thread.sleep(1);
            updateProgress(i + 1, approxLength); //update binded progress bar periodically for duration of audio
        }
    }

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

    private void normaliseAudio() {

        //extract the mean volume from the audio file using ffmpeg
        String cmd = "ffmpeg -i '" + _filePath + "' -filter:a volumedetect -f null /dev/null 2>&1| grep mean_volume";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd);
        Process volume;
        try {
            volume = builder.start();
            volume.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(volume.getInputStream()));
            String output = br.readLine();
            System.out.println(output);
            int originalVolume = Integer.valueOf(output.substring(output.lastIndexOf(':')+2,output.lastIndexOf('.')));
            _adjustedVolume = _targetVolume - originalVolume;
            String cmd2 = "ffmpeg -i '"+_filePath+"' -filter:a \"volume="+_adjustedVolume+"dB\" normal1.wav";
            System.out.println(cmd2);
            ProcessBuilder builder2 = new ProcessBuilder("/bin/bash","-c",cmd2);
            Process normalise = builder2.start();
            normalise.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void playNormal() {
        String cmd = "ffplay -loglevel panic -autoexit -nodisp -i normal1.wav";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c",cmd);
        Process play = null;
        try {
            play = builder.start();
            play.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
