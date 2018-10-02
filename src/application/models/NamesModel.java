package application.models;

import javafx.scene.control.TreeView;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NamesModel {

    //model associated with each name, one name can have multiple recordings

    private Map<String, Integer> _recordings;

    private String _name;

    public NamesModel(String name){
        _recordings = new HashMap<>();
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

    public Map<String, Integer> getRecordings(){
        makeRecordings();
        return _recordings;
    }

    public List<String> getOgRecordings(){
        makeRecordings();
        List<String> ogRecordings = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : _recordings.entrySet()){
            if (entry.getValue() == 0){
                ogRecordings.add(entry.getKey());
            }
        }
        return ogRecordings;
    }

    public List<String> getPerRecordings(){
        makeRecordings();
        List<String> perRecordings = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : _recordings.entrySet()){
            if (entry.getValue() == 1){
                perRecordings.add(entry.getKey());
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


        //place each recording into map with value being the identifier for which database it belongs in
        for (int i=0;i<files.size();i++){
            if (files.get(i).isFile()){
                if (files.get(i).getName().substring(files.get(i).getName().lastIndexOf("_") + 1, files.get(i).getName().lastIndexOf('.')).toUpperCase().equals(_name.toUpperCase())) {
                    String recording = files.get(i).getName();
                    if (i < ogSize){
                        _recordings.put(recording,0);
                    } else {
                        _recordings.put(recording,1);
                    }
                }
            }
        }
    }
}
