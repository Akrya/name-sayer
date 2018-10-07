package application.models;

import javafx.concurrent.Task;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CustomPlayer extends Task<Void> {

    private String[] _splitNames;
    private List<NamesModel> _nameModels;
    private List<RecordingModel> _recordings;
    private List<String> _trimmedFiles;
    private List<String> _playListFiles;
    private int queueNum = 1;
    private double _length;


    public CustomPlayer(String customName) {
        _splitNames = customName.split("[-\\s]");
        getModels(new NamesListModel());
        getRecordings();
        _playListFiles = new ArrayList<>();
        _trimmedFiles = new ArrayList<>();
        for (RecordingModel record : _recordings){
            String trimFile = null;
            try {
                trimFile = trimAudio("Original/"+record.getFileName());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            calcLength(trimFile);
            if (_length == 0){
                _playListFiles.add("Original/"+record.getFileName());
            } else {
                _playListFiles.add(trimFile);
            }
        }
    }

    private void getModels(NamesListModel namesListModel){
        _nameModels = new ArrayList<>();
        for (String name : _splitNames){
            NamesModel model = namesListModel.getName(name);
            if (model != null) {
                _nameModels.add(model);
            }
        }
    }

    private void getRecordings(){
        _recordings = new ArrayList<>();
        for (NamesModel model : _nameModels){
            RecordingModel record = model.getBestRecord();
            _recordings.add(record);
        }
    }

    private void playAudio(String filePath){
        String cmd = "ffplay -loglevel panic -autoexit -nodisp -i '"+filePath+"'";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c",cmd);
        try {
            Process process = builder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String trimAudio(String filePath) throws IOException, InterruptedException {
        String cmd = "ffmpeg -i '"+filePath+"' -af silenceremove=1:0:-30dB silenced"+queueNum+".wav";
        _trimmedFiles.add("silenced"+queueNum+".wav");
        queueNum++;
        ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c",cmd);
        Process process = builder.start();
        process.waitFor();
        return "silenced"+(queueNum-1)+".wav";

    }

    private void cleanUpFiles(){
        for (String filePath : _trimmedFiles){
            new File(filePath).delete();
            System.out.println(filePath+" deleted");
        }
    }

    private void calcLength(String filePath) {
        //reference to calculate wav file length https://stackoverflow.com/questions/3009908/how-do-i-get-a-sound-files-total-time-in-java
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            _length =  ((frames+0.0) / format.getFrameRate());
            System.out.println(_length);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected Void call() throws Exception {
        for (String filePath : _playListFiles){
            playAudio(filePath);
        }
        cleanUpFiles();
        return null;
    }
}
