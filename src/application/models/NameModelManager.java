package application.models;

import java.io.File;
import java.util.*;

/**Class is instantiated when the program first begins, it is responsible for keeping track of all changes to
 * Name models, and is used by controller classes to get information about name models
 */
public class NameModelManager {

    private ArrayList<NameModel> _names; //List of NameModel objects, each object associated with one or more recording for that name
    private List<String> _uniqueNames; //List of unique names found in the database

    /**Called on start up of program it instantiates all name models
     * in the makeNames() method
     */
    public NameModelManager() {
        _names = new ArrayList<>();
        _uniqueNames = new ArrayList<>();
        createDirectory();
        makeNames();
    }

    /**If its the first time running the program then program will create the
     * following directories
     */
    private void createDirectory(){

        new File( "Single").mkdir();
        new File("Database").mkdir();
        new File("Playlists").mkdir();
        new File("Concatenated").mkdir();

    }

    /**Method called by controllers when they a list of all the names in string form
     * in alphabetical order
     * @return list of names stored in database
     */
    public List<String> getNames(){
        List<String> names = new ArrayList<>();
        for (NameModel model : _names){
            names.add(model.toString());
        }
        Collections.sort(names);
        return names;
    }

    /**Method called when controllers want a copy of the list name models stored in the class
     * @return list of name models stored in class
     */
    public List<NameModel> getModels(){
        return _names;
    }

    /**Caleld when a specific NameModel is needed for retrieval
     * it searches for this model by its string name
     * @param name name of model as a string
     * @return reference to the model
     */
    public NameModel getName(String name){
        name = name.substring(0,1).toUpperCase()+name.substring(1);
        NameModel targetName = null;
        for (NameModel candidateName : _names){
            if (candidateName.toString().equals(name)){
                targetName = candidateName;
                break;
            }
        }
        return targetName;
    }

    /**Called in constructor it loops through the Database and Single directories and finds
     * all unique names, and creates a name model for the name
     */
    private void makeNames(){
        File[] ogFiles = new File("Database").listFiles(); //loop through the two directories and get all unique names
        List<File> files = new ArrayList<>();
        files.addAll(Arrays.asList(ogFiles));
        File[] perFiles = new File("Single").listFiles();
        files.addAll(Arrays.asList(perFiles));

        for (int i=0;i<files.size();i++){
            if (files.get(i).isFile()){
                String name = files.get(i).getName().substring(files.get(i).getName().lastIndexOf("_") + 1, files.get(i).getName().lastIndexOf('.'));
                if (_uniqueNames.indexOf(name.toUpperCase()) == -1){ //unique name means index of -1
                    _uniqueNames.add(name.toUpperCase());
                    if (!Character.isUpperCase(name.charAt(0))){
                        name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    }
                    NameModel nameModel = new NameModel(name);
                    _names.add(nameModel);
                }
            }
        }
    }
}
