package application.models;

import java.io.File;
import java.util.*;

public class NamesListModel {

    private ArrayList<NamesModel> _names; //List of NamesModel objects, each object associated with one or more recording for that name
    private List<String> _uniqueNames;


    public NamesListModel() {
        _names = new ArrayList<>();
        _uniqueNames = new ArrayList<>();
        createDirectory();
        makeNames();
    }

    private void createDirectory(){

        new File( "Personal").mkdir();
        new File("Original").mkdir();

    }

    public List<String> getNames(){
        List<String> names = new ArrayList<>();
        for (NamesModel model : _names){
            names.add(model.toString());
        }
        Collections.sort(names);
        return names;
    }

    public List<NamesModel> getModels(){
        return _names;
    }

    public List<String> getNamesForLetter(char heading, int identifier){ //return list of name strings that start with the heading in its respective database given by the identifier
        List<String> names = new ArrayList<>();
        List<RecordingModel> records;
        boolean contains = false;
        for (NamesModel nameModel : _names){
           records = nameModel.getRecords();
           for (RecordingModel record : records){
               if (record.getIdentifier() == identifier){
                   if (nameModel.toString().toUpperCase().charAt(0) == heading){
                       contains = true;
                   }
               }
           }

           if (contains){
               names.add(nameModel.toString());
           }
           contains = false;
        }
        return names;
    }

    public NamesModel getName(String name){
        name = name.substring(0,1).toUpperCase()+name.substring(1);
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
        File[] ogFiles = new File("Original").listFiles();
        List<File> files = new ArrayList<>();
        files.addAll(Arrays.asList(ogFiles));
        File[] perFiles = new File("Personal").listFiles();
        files.addAll(Arrays.asList(perFiles));

        for (int i=0;i<files.size();i++){
            if (files.get(i).isFile()){
                String name = files.get(i).getName().substring(files.get(i).getName().lastIndexOf("_") + 1, files.get(i).getName().lastIndexOf('.'));
                if (_uniqueNames.indexOf(name.toUpperCase()) == -1){ //unique name means index of -1
                    _uniqueNames.add(name.toUpperCase());
                    if (!Character.isUpperCase(name.charAt(0))){
                        name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    }
                    NamesModel namesModel = new NamesModel(name);
                    _names.add(namesModel);
                }
            }
        }
    }
}
