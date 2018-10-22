package application.models;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**Simple model class handles file operations to retrieve the recordings
 * associated with a concatenated name
 */
public class ConcatenatedNameModel {

    private List<String> _records;
    private String _originalName; //unaltered name
    private String _name;
    private int _versionNum;

    /** Constructor called when user makes a recording for a concatenated name
     * @param name concatenated name
     */
    public ConcatenatedNameModel(String name){
        _originalName = name;
        _name = name.replace(' ','-');
        _records = new ArrayList<>();
        findRecordings();
    }

    public String toString(){
        return _originalName;
    }

    /**Method called when a user selects a concatenated name in practice mode,
     * it returns all the associated recordings with that name
     * @return
     */
    public List<String> getRecordings(){
        findRecordings();
        return _records;
    }

    public void incrementVersionNum(){
        _versionNum++;
    }

    public int getVersionNum(){
        return _versionNum;
    }

    /**Method called in constructor to retrieve all the associated recordings
     * with the concatenated name
     */
    private void findRecordings(){
        File[] files = new File("Concatenated/").listFiles();
        _records.clear();
        _versionNum = 1;
        for (File file : files){
            if (file.isFile()){
                String nameOnFile = file.getName().substring(file.getName().lastIndexOf('_')+1,file.getName().lastIndexOf('.')).toUpperCase();
                if (nameOnFile.equals(_name.toUpperCase())){
                    _records.add(file.getName());
                    _versionNum++;
                }
            }
        }
    }

}
