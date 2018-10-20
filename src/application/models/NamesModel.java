package application.models;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NamesModel {

    //model associated with each name, one name can have multiple recordings

    private List<RecordingModel> _records;

    private List<String> _recordFiles;

    private String _name;

    private boolean _favourite;

    public NamesModel(String name){
        _records = new ArrayList<>();
        _recordFiles = new ArrayList<>();
        _name = name;
        makeRecordings();
        _favourite = false;
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

    public RecordingModel getBestRecord(){
        RecordingModel goodRecord = null;
        for (RecordingModel record : _records){
            if (record.getRating().equals("Good")){
                goodRecord = record;
                break;
            }
        }
        if (goodRecord == null){
            goodRecord = _records.get(0);
        }
        return goodRecord;
    }

    public List<RecordingModel> getRecords(){
        getNewRecords();
        return _records;
    }

    public List<String> getOgRecordings(){
        getNewRecords();
        List<String> ogRecordings = new ArrayList<>();
        for (RecordingModel record : _records){
            if (record.getIdentifier() == 0){
                ogRecordings.add(record.getFileName());
            }
        }
        return ogRecordings;
    }

    public List<String> getPerRecordings(){
        getNewRecords();
        List<String> perRecordings = new ArrayList<>();
        for (RecordingModel record : _records){
            if (record.getIdentifier() == 1){
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
                    _recordFiles.add(recording);
                    if (i < ogSize){
                        _records.add(new RecordingModel(recording, _name, 0));
                    } else {
                        _records.add(new RecordingModel(recording, _name, 1));
                    }
                }
            }
        }
    }

    private void getNewRecords(){
        File[] ogFiles = new File("Original").listFiles();
        List<File> files = new ArrayList<>();
        files.addAll(Arrays.asList(ogFiles));
        int ogSize = files.size();
        File[] perFiles = new File("Personal").listFiles();
        files.addAll(Arrays.asList(perFiles));

        for (int i=0;i<files.size();i++){
            if (files.get(i).isFile()){
                if (files.get(i).getName().substring(files.get(i).getName().lastIndexOf("_") + 1, files.get(i).getName().lastIndexOf('.')).toUpperCase().equals(_name.toUpperCase())) {
                    if (_recordFiles.indexOf(files.get(i).getName()) == -1) { //unique name means index of -1
                        if (i < ogSize) {
                            _records.add(new RecordingModel(files.get(i).getName(), _name, 0));
                        } else {
                            _records.add(new RecordingModel(files.get(i).getName(), _name, 1));
                        }
                        _recordFiles.add(files.get(i).getName());
                    }
                }
            }
        }
    }

    public void setFavourite(boolean favourite){
        _favourite = favourite;
    }

    public boolean hasFavourite(){
        return _favourite;
    }
}
