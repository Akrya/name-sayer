package application.models;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

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
        String cmd = "rm Single/"+"'"+recording+"'";
        ProcessBuilder deleteFile = new ProcessBuilder("/bin/bash","-c",cmd);
        try {
            Process process = deleteFile.start();
            process.waitFor();
            RecordingModel deletionRecord = null;
            for (RecordingModel record : _records){
                if (record.getFileName().equals(recording)){
                    deletionRecord = record;
                    _recordFiles.remove(recording);
                    break;
                }
            }
            _records.remove(deletionRecord);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /** Search through the list of recording models for the best database recording that will be used for the practice mode
     */
    public RecordingModel getBestRecord(){
        RecordingModel bestRecord = null;
        RecordingModel secondBestRecord = null;
        for (RecordingModel record : _records){
            if (record.getRating().equals("Good ★") && !record.getFileName().contains("personal")){ //search for favourite record
                bestRecord = record;
            } else if(record.getRating().equals("Good") && !record.getFileName().contains("personal")){ //search for alternative good record
                secondBestRecord = record;
            }
        }
        if (bestRecord == null && secondBestRecord == null){ //if there isnt a favourite record and there aren't any good records then default to first recording found
            bestRecord = _records.get(0);
        } else if (bestRecord == null){ //if there isnt a favourite record but there is a good record then default to the good record
            bestRecord = secondBestRecord;
        }
        return bestRecord;
    }

    public List<RecordingModel> getRecords(){
        getNewRecords();
        return _records;
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
        File[] ogFiles = new File("Database").listFiles();
        List<File> files = new ArrayList<>();
        files.addAll(Arrays.asList(ogFiles));
        int ogSize = files.size();
        File[] perFiles = new File("Single").listFiles();
        files.addAll(Arrays.asList(perFiles));
        _records.clear();


        //place each recording into map with value being the identifier for which database it belongs in
        for (int i=0;i<files.size();i++){
            if (files.get(i).isFile()){
                if (files.get(i).getName().substring(files.get(i).getName().lastIndexOf("_") + 1, files.get(i).getName().lastIndexOf('.')).toUpperCase().equals(_name.toUpperCase())) {
                    String recording = files.get(i).getName();
                    _recordFiles.add(recording);
                    if (i < ogSize) {
                        _records.add(new RecordingModel(recording, _name, 0));
                    } else {
                        _records.add(new RecordingModel(recording, _name, 1));
                    }
                }
            }
        }
    }

    private void getNewRecords(){
        File[] ogFiles = new File("Database").listFiles();
        List<File> files = new ArrayList<>();
        files.addAll(Arrays.asList(ogFiles));
        int ogSize = files.size();
        File[] perFiles = new File("Single").listFiles();
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
                        System.out.println(files.get(i).getName());
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
