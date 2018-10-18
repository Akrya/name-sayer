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
    private final int _targetVolume = -15;


    public CustomPlayer(String customName) {
        _splitNames = customName.split("[-\\s]");
        getModels(new NamesListModel());
        getRecordings();
        _playListFiles = new ArrayList<>();
        _trimmedFiles = new ArrayList<>();
        for (RecordingModel record : _recordings){ //normalise and trim the recordings, if the trimmed file is empty then add original recording into the queue
            String trimFile = null;
            try {
                String normalisedFile = normaliseAudio("Original/"+record.getFileName());
                trimFile = trimAudio(normalisedFile);
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
        //trim the normalised file for any silence then add new trimmed file to a list
        String cmd = "ffmpeg -i '"+filePath+"' -af silenceremove=1:0:-30dB silenced"+queueNum+".wav";
        _trimmedFiles.add("silenced"+queueNum+".wav");
        queueNum++;
        ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c",cmd);
        Process process = builder.start();
        process.waitFor();
        new File("normalised.wav").delete();
        return "silenced"+(queueNum-1)+".wav";

    }

    private void cleanUpFiles(){
        for (String filePath : _trimmedFiles){
            new File(filePath).delete();
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
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String normaliseAudio(String filePath) throws IOException, InterruptedException {
        //extract the mean volume from the audio file using ffmpeg
        String cmd1 = "ffmpeg -i '" + filePath + "' -filter:a volumedetect -f null /dev/null 2>&1| grep mean_volume";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd1);
        Process volume = builder.start();
        volume.waitFor();

        //read in mean volume from stdout
        BufferedReader br = new BufferedReader(new InputStreamReader(volume.getInputStream()));
        String output = br.readLine();

        //calculate the difference between target volume and extracted volume
        int originalVolume = Integer.valueOf(output.substring(output.lastIndexOf(':')+2,output.lastIndexOf('.')));
        System.out.println(originalVolume+"");
        int difference = _targetVolume - originalVolume;

        //normalise the audio by increasing or decreasing by the difference
        String cmd2 = "ffmpeg -i '"+filePath+"' -filter:a \"volume="+difference+"dB\" normalised.wav";
        ProcessBuilder builder2 = new ProcessBuilder("/bin/bash","-c",cmd2);
        Process normalise = builder2.start();
        normalise.waitFor();
        return "normalised.wav";
    }

    @Override
    protected Void call() throws Exception {
        for (String filePath : _playListFiles){
            playAudio(filePath); //loop through and play each name in the concatenated name
        }
        cleanUpFiles();
        return null;
    }
}
