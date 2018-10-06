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


    public CustomPlayer(String customName){
        _splitNames = customName.split("[-\\s]");
        getModels(new NamesListModel());
        getRecordings();
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

    private void makeoOutputFile(){
        String cmd = "ffmpeg -f concat -safe 0 -i concat.txt -c copy output.wav";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c",cmd);
        try {
            builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playOutputFile(){
        String cmd = "ffplay -loglevel panic -autoexit -nodisp -i output.wav";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c",cmd);
        try {
            builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeOutputFile(){
        File outputFile = new File("output.wav");
        outputFile.delete();
    }

    @Override
    protected Void call() throws Exception {
        concatFiles();
        makeoOutputFile();
        playOutputFile();
        removeOutputFile();
        return null;
    }
}
