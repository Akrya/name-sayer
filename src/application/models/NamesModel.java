package application.models;

import javafx.scene.control.TreeView;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NamesModel {

    //model associated with each name, one name can have multiple recordings

    private List<RecordingModel> _records;

    private String _name;

    public NamesModel(String name){
        _records = new ArrayList<>();
        _name = name;
        makeRecordings();
    }

    public void delete(String recording){
        String cmd = "rm Personal/"+"'"+recording+"'";
        ProcessBuilder deleteFile = new ProcessBuilder("/bin/bash","-c",cmd);
        try {
            Process process = deleteFile.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<RecordingModel> getRecords(){
        makeRecordings();
        return _records;
    }

    public List<String> getOgRecordings(){
        makeRecordings();
        List<String> ogRecordings = new ArrayList<>();
        for (RecordingModel record : _records){
            if (record.getIdentifier() == 0){
                ogRecordings.add(record.getFileName());
            }
        }
        return ogRecordings;
    }

    public List<String> getPerRecordings(){
        makeRecordings();
        List<String> perRecordings = new ArrayList<>();
        List<String> ogRecordings = new ArrayList<>();
        for (RecordingModel record : _records){
            if (record.getIdentifier() == 0){
                perRecordings.add(record.getFileName());
            }
        }
        return perRecordings;
    }

    public String toString(){
        return _name;
    }

    private void makeRecordings(){
        File[] ogFiles = new File("Original").listFiles();
        List<File> files = new ArrayList<>();
        files.addAll(Arrays.asList(ogFiles));
        int ogSize = files.size();
        File[] perFiles = new File("Personal").listFiles();
        files.addAll(Arrays.asList(perFiles));
        _records.clear();


        //place each recording into map with value being the identifier for which database it belongs in
        for (int i=0;i<files.size();i++){
            if (files.get(i).isFile()){
                if (files.get(i).getName().substring(files.get(i).getName().lastIndexOf("_") + 1, files.get(i).getName().lastIndexOf('.')).toUpperCase().equals(_name.toUpperCase())) {
                    String recording = files.get(i).getName();
                    if (i < ogSize){
                        _records.add(new RecordingModel(recording, _name, 0));
                    } else {
                        _records.add(new RecordingModel(recording, _name, 1));
                    }
                }
            }
        }
    }
}
