package application.models;

/**Class is contains information about a recording, it contains information about its rating status
 */
public class RecordingModel {

    private int _identifier; //identifier for recording, 0 if it is a database recording and 1 if it is personal

    private String _fileName;

    private String _name;

    private int _versionNum;

    private boolean _goodRating; //by default each recording has good rating unless stating otherwise by rating.txt

    private boolean _favourite;

    /**Constructor called in the name model class when retrieving all associated recordings of the name
     * @param fileName name of file of recording
     * @param name name of associated name model
     * @param identifier 0 if recording is a database recording, 1 if its a user recording
     */
    public RecordingModel(String fileName, String name, int identifier){
        _fileName = fileName;
        _name = name;
        _identifier = identifier;
        _goodRating = true;
        _favourite = false;
    }

    /**Getter method for the file name of recording
     * @return file name
     */
    public String getFileName(){
        return _fileName;
    }

    /**Getter method for the name associated with the recording
     * @return name representation of recording
     */
    public String getName(){ return _name; }

    /**Method returns what directory the recording belongs to
     * @return identifier of recording
     */
    public int getIdentifier(){
        return _identifier;
    }

    /**Called when user wants to give a bad or good rating to recording
     * @param rating true if recording is good, false otherwise
     */
    public void setRating(boolean rating){
        _goodRating = rating;
    }

    /**Called if user wants to favourite the recording
     * @param favourite true if recording is user's favourite, false otherwise
     */
    public void setFavourite(boolean favourite){
        _favourite = favourite;
    }

    /**Method called when controller wants to know what rating a recording has
     * @return string representation of the rating of the recording
     */
    public String getRating(){
        if (_favourite){
            return "Good ★";
        } else if (_goodRating){
            return "Good";
        } else {
            return "Bad";
        }
    }

}
