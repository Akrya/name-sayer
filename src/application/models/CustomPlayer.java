package application.models;

import javafx.concurrent.Task;
import sun.util.resources.pl.CalendarData_pl;

import javax.naming.Name;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CustomPlayer extends Task<Void> {

    private String[] _splitNames;
    List<NamesModel> _nameModels;
    List<RecordingModel> _recordings;
    List<String> _trimmedFiles;
    int queueNum = 1;


    public CustomPlayer(String customName){
        _splitNames = customName.split("[-\\s]");
        getModels(new NamesListModel());
        getRecordings();
        _trimmedFiles = new ArrayList<>();
        for (RecordingModel record : _recordings){
            trimAudio("Original/"+record.getFileName());
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

    private void concatFiles(){
        try {
            File concatFile = new File("concat.txt");
            if (concatFile.exists()){
                concatFile.delete();
            }
            concatFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(concatFile, true));
            PrintWriter writer = new PrintWriter(bw);
            for (RecordingModel record : _recordings){
                writer.println("file 'Original/"+record.getFileName()+"'");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//
//    private void makeoOutputFile(){
//        String cmd = "ffmpeg -f concat -safe 0 -i concat.txt -c copy output.wav";
//        ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c",cmd);
//        try {
//            builder.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void playOutputFile(){
//        String cmd = "ffplay -loglevel panic -autoexit -nodisp -i output.wav";
//        ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c",cmd);
//        try {
//            builder.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

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

    private void trimAudio(String filePath){
        String cmd = "ffmpeg -i '"+filePath+"' -af silenceremove=1:0:-30dB silenced"+queueNum+".wav";
        _trimmedFiles.add("silenced"+queueNum+".wav");
        queueNum++;
        ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c",cmd);
        try {
            Process process = builder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void cleanUpFiles(){
        for (String filePath : _trimmedFiles){
            new File(filePath).delete();
        }
    }

    @Override
    protected Void call() throws Exception {
        for (String filePath : _trimmedFiles){
            playAudio(filePath);
        }
        cleanUpFiles();
        return null;
    }
}
