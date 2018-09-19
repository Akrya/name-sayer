package application;

import javafx.scene.control.TreeView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NamesModel {

    public List<String> getNames(char heading, String identifier){
        List<String> names = new ArrayList<>();
        File[] files = (identifier.equals("original")) ? new File("Names/Original").listFiles() : new File("Names/Personal").listFiles();
        for (File file: files){
            if (file.isFile()){
                if (Character.toUpperCase(file.getName().charAt(file.getName().lastIndexOf('_')+1)) == heading) {
                    String name = file.getName().substring(file.getName().lastIndexOf("_") + 1, file.getName().lastIndexOf('.'));  //remove file extension
                    names.add(name);
                }
            }
        }
        return names;
    }
}
