package application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class NamesListModel {
    private ArrayList<String> _names;


    public void createDirectory(){

        new File("Names").mkdir();
        new File( "Names/Personal").mkdir();
        new File("Names/Original").mkdir();
        String cmd = "unzip names.zip -d Names/Original";
        ProcessBuilder makeOriginal = new ProcessBuilder("/bin/bash","-c", cmd);
        try {
            makeOriginal.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteCreation(String name){ //delete selected personal name file
        String cmd = "rm Names/Personal"+"'"+name+"'"+".wav";
        ProcessBuilder deleteFile = new ProcessBuilder("/bin/bash","-c",cmd);
        try {
            deleteFile.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
