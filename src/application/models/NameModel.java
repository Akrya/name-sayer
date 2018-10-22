package application.models;

import java.io.File;
import java.io.IOException;
import java.util.*;

/** Class is an abstraction of a single name object, it contains a list of recordings associated with the name,
 *  and contains information about the name, (its ratings and favourite recordings)
 */
public class NameModel {

    private List<RecordingModel> _records; //list of recording model objects associated with name

    private List<String> _recordFiles;  //list of file names of recordings associated with name

    private String _name;

    private boolean _favourite; //true if name has a favourite record, false if it doesn't

    /** Constructor called in the NameModelManager class, it finds all the associated recordings of the name
     *  with the method findRecordings()
     * @param name name we want to make a model for
     */
    public NameModel(String name){
        _records = new ArrayList<>();
        _recordFiles = new ArrayList<>();
        _name = name;
        findRecordings();
        _favourite = false;
    }

    /**Called when a user wants to delete one of their recordings
     * It uses bash to delete the file
     * @param recording file name of recording we want to delete
     */
    public void delete(String recording){
        String cmd = "rm Single/"+"'"+recording+"'";
        ProcessBuilder deleteFile = new ProcessBuilder("/bin/bash","-c",cmd);
        try {
            Process process = deleteFile.start();
            process.waitFor();
            RecordingModel deletionRecord = null;
            for (RecordingModel record : _records){ //update the RecordingModel list by removing the deleted record
                if (record.getFileName().equals(recording)){
                    deletionRecord = record;
                    _recordFiles.remove(recording);
                    break;
                }
            }
            _records.remove(deletionRecord);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /** Search through the list of recording models for the best database recording that will be used for the practice mode
     */
    public RecordingModel getBestRecord(){
        RecordingModel bestRecord = null;
        RecordingModel secondBestRecord = null;
        for (RecordingModel record : _records){
            if (record.getRating().equals("Good ★") && !record.getFileName().contains("personal")){ //search for favourite record
                bestRecord = record;
            } else if(record.getRating().equals("Good") && !record.getFileName().contains("personal")){ //search for alternative good record
                secondBestRecord = record;
            }
        }
        if (bestRecord == null && secondBestRecord == null){ //if there isnt a favourite record and there aren't any good records then default to first recording found
            bestRecord = _records.get(0);
        } else if (bestRecord == null){ //if there isnt a favourite record but there is a good record then default to the good record
            bestRecord = secondBestRecord;
        }
        return bestRecord;
    }

    /**Called when controller wants all the recordings for a specific name
     * it first calls getNewRecords() to update its recording list
     * @return list of recording model objects associated with the name
     */
    public List<RecordingModel> getRecords(){
        getNewRecords();
        return _records;
    }

    /** Method called in practice mode when controller only wants user recordings of the name
     * @return list of user recordings as a file name
     */
    public List<String> getUserRecordings(){
        getNewRecords();
        List<String> perRecordings = new ArrayList<>();
        for (RecordingModel record : _records){
            if (record.getIdentifier() == 1){
                perRecordings.add(record.getFileName());
            }
        }
        return perRecordings;
    }

    /** Returns the string representation of the name
     * @return string form of name
     */
    public String toString(){
        return _name;
    }

    /**Called in constructor, it searches through the two directories for recordings associated with the name
     * if the recording is a user recording (in Single) then its given an identifier of 1 otherwise it gets an identifier of 0 (in Database)
     */
    private void findRecordings(){
        File[] databaseFiles = new File("Database").listFiles();
        List<File> files = new ArrayList<>();
        files.addAll(Arrays.asList(databaseFiles));
        int databaseSize = files.size();
        File[] userFiles = new File("Single").listFiles();
        files.addAll(Arrays.asList(userFiles));
        _records.clear();


        //place each recording into map with value being the identifier for which database it belongs in
        for (int i=0;i<files.size();i++){
            if (files.get(i).isFile()){
                if (files.get(i).getName().substring(files.get(i).getName().lastIndexOf("_") + 1, files.get(i).getName().lastIndexOf('.')).toUpperCase().equals(_name.toUpperCase())) {
                    String recording = files.get(i).getName();
                    _recordFiles.add(recording);
                    if (i < databaseSize) {
                        _records.add(new RecordingModel(recording, _name, 0));
                    } else {
                        _records.add(new RecordingModel(recording, _name, 1));
                    }
                }
            }
        }
    }

    /**Called when controller wants to get recording models of the name, it checks for any new additions to the directories
     * and adds them to the _records field if any are found
     */
    private void getNewRecords(){
        File[] ogFiles = new File("Database").listFiles();
        List<File> files = new ArrayList<>();
        files.addAll(Arrays.asList(ogFiles));
        int ogSize = files.size();
        File[] perFiles = new File("Single").listFiles();
        files.addAll(Arrays.asList(perFiles));

        for (int i=0;i<files.size();i++){
            if (files.get(i).isFile()){
                if (files.get(i).getName().substring(files.get(i).getName().lastIndexOf("_") + 1, files.get(i).getName().lastIndexOf('.')).toUpperCase().equals(_name.toUpperCase())) {
                    if (_recordFiles.indexOf(files.get(i).getName()) == -1) { //unique recording means it has an index of -1, so it will be added to _records
                        if (i < ogSize) {
                            _records.add(new RecordingModel(files.get(i).getName(), _name, 0));
                        } else {
                            _records.add(new RecordingModel(files.get(i).getName(), _name, 1));
                        }
                        _recordFiles.add(files.get(i).getName());
                        System.out.println(files.get(i).getName());
                    }
                }
            }
        }
    }

    /**Called in manage mode when there a recording has or has not been favourited
     * @param favourite true if name has a favourite recording, false if not
     */
    public void setFavourite(boolean favourite){
        _favourite = favourite;
    }

    /**Called in manage mode when controller wants to know if the name model has a favourite
     * recording
     * @return true if model has favourite recording, false if not
     */
    public boolean hasFavourite(){
        return _favourite;
    }
}
