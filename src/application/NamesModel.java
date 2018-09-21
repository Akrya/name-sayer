package application;

import javafx.scene.control.TreeView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NamesModel {

    //model associated with each name, one name can have multiple recordings

    private List<String> _originalRecordings;

    private List<String> _personalRecordings;

    private String _name;

    public NamesModel(String name){
        _originalRecordings = new ArrayList<>();
        _personalRecordings = new ArrayList<>();
        _name = name;
        getRecordings();
    }

    public void delete(String recording){
        String cmd = "rm Names/Personal/"+"'"+recording+"'";
        ProcessBuilder deleteFile = new ProcessBuilder("/bin/bash","-c",cmd);
        try {
            deleteFile.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getOriginalRecordings(){
        return _originalRecordings;
    }

    public List<String> getPersonalRecordings(){
        return _personalRecordings;
    }

    public String toString(){
        return _name;
    }

    private void getRecordings(){
        File[] ogFiles = new File("Names/Original").listFiles();
        for (File file: ogFiles){
            if (file.isFile()){
                if (file.getName().substring(file.getName().lastIndexOf("_") + 1, file.getName().lastIndexOf('.')).equals(_name)) {
                    String recording = file.getName();
                    _originalRecordings.add(recording);
                }
            }
        }
        File[] perFiles = new File("Names/Personal").listFiles();
        for (File file: perFiles){
            if (file.isFile()){
                if (file.getName().substring(file.getName().lastIndexOf("_") + 1, file.getName().lastIndexOf('.')).equals(_name)) {
                    String recording = file.getName();
                    _personalRecordings.add(recording);
                }
            }
        }

    }
}
