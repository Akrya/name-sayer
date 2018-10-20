package application.models;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomNameModel {

    private List<String> _records;
    private String _name;

    public CustomNameModel(String name){
        _name = name.replace(' ','-');
        _records = new ArrayList<>();
        findRecordings();
    }

    public List<String> getRecordings(){
        return _records;
    }

    private void findRecordings(){
        File[] files = new File("CustomRecords/").listFiles();

        for (File file : files){
            if (file.isFile()){
                String nameOnFile = file.getName().substring(file.getName().lastIndexOf('_')+1,file.getName().lastIndexOf('.')).toUpperCase();
                if (nameOnFile.equals(_name.toUpperCase())){
                    _records.add(file.getName());
                }
            }
        }
    }

}
