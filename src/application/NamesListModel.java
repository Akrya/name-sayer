package application;

import javax.naming.Name;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NamesListModel {

    private ArrayList<NamesModel> _names; //List of NamesModel objects, each object associated with one or more recording for that name

    private List<String> _ogNameStrings; //list of unique names in original database

    private List<String> _perNameStrings; //list of unique names in personal database


    public NamesListModel() {
        _names = new ArrayList<>();
        _ogNameStrings = new ArrayList<>();
        _perNameStrings = new ArrayList<>();
        makeNames();
    }

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

   // public void deleteCreation(String name){ //delete selected personal name file
    //    String cmd = "rm Names/Personal"+"'"+name+"'"+".wav";
   //     ProcessBuilder deleteFile = new ProcessBuilder("/bin/bash","-c",cmd);
  //      try {
 //           deleteFile.start();
 //       } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    public List<String> getNames(char heading, String identifier){ //return list of name strings that start with the heading in its respective database given by the identifier
        List<String> names = new ArrayList<>();
        if (identifier.equals("original")){
            for (String name : _ogNameStrings){
                if (name.toUpperCase().charAt(0)== heading){
                    names.add(name);
                }
            }
        } else {
            for (String name : _perNameStrings){
                if (name.toUpperCase().charAt(0)== heading){
                    names.add(name);
                }
            }
        }
        return names;
    }

    public NamesModel getName(String name){
        NamesModel targetName = null;
        for (NamesModel candidateName : _names){
            if (candidateName.toString().equals(name)){
                targetName = candidateName;
                break;
            }
        }
        return targetName;
    }

    private void makeNames(){
        File[] ogFiles = new File("Names/Original").listFiles();
        for (File file: ogFiles){
            if (file.isFile()){
                String name = file.getName().substring(file.getName().lastIndexOf("_") + 1, file.getName().lastIndexOf('.'));  //remove file extension
                if (_ogNameStrings.indexOf(name) == -1){
                    _ogNameStrings.add(name);

                    NamesModel nameModel = new NamesModel(name);
                    _names.add(nameModel);
                }
            }
        }
        File[] perFiles = new File("Names/Personal").listFiles();
        for (File file: perFiles){
            if (file.isFile()){
                String name = file.getName().substring(file.getName().lastIndexOf("_") + 1, file.getName().lastIndexOf('.'));  //remove file extension
                if (_perNameStrings.indexOf(name) == -1){
                    _perNameStrings.add(name);

                    NamesModel nameModel = new NamesModel(name);
                    _names.add(nameModel);
                }
            }
        }
    }
}
