package application.models;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.util.Optional;
import java.util.Scanner;

/**Class is responsible for handling the file I/O operations related to the text file
 */
public class RecordingRater {

    private String _selection;

    private RecordingModel _recordingModel;

    /**Constructor called when a user presses the flag button
     * @param recordingModel associated RecordingModel of the recording
     */
    public RecordingRater(RecordingModel recordingModel){
        _recordingModel = recordingModel;
        _selection = recordingModel.getFileName();
    }

    /**Method called when checking if the ratings text file contains
     * the recording user wants to flag (in which case it already has a bad rating)
     * @return
     */
    public boolean checkFile(){
        boolean found = false;
        try {
            Scanner scanner = new Scanner(new File("Ratings.txt"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.contains(_selection)) {
                    found = true;
                    break;
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        if (found){
            return true;
        } else {
            return false;
        }
    }

    /** Method called if user wants to flag a recording for the first time, it adds the recording to the
     * rating text file
     */
    public void makeRating(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Rating");
        alert.setHeaderText("You are rating '"+ _selection+"'");
        alert.setContentText("Is this recording bad quality?");

        ButtonType badButton = new ButtonType("Bad");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(badButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == badButton) { //user confirms recording is bad
            _recordingModel.setRating(false);
        } else {
            return;
        }
        BufferedWriter bw = null; //add the selected recording to the rating text file
        try {
            bw = new BufferedWriter(new FileWriter("Ratings.txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter writer = new PrintWriter(bw);
        writer.println(_selection);
        writer.close();
    }

    /**If a bad rating has already been found for the recording then prompts the user if they want to overwrite
     * the bad rating and return the recording to a good rating status
     * @return true if user wants to overwrite, false otherwise
     */
    public void overWriteRating(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Overwrite?");
        alert.setHeaderText("A bad rating has been recorded for '"+_selection+"'");
        alert.setContentText("Do you want to remove this rating?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){//user wants to overwrite the bad rating
            deleteRating();
            _recordingModel.setRating(true);
        }
    }

    /**Method called if user attempts to flag a recording that they have favourited, it will prompt the user
     * if they want to continue, if they continue then the favourite status of the name and recording is set false and the
     * recording is added to the rating text file
     * @return true if user continued overwriting, false otherwise
     */
    public boolean overWriteFavRating(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Overwrite?");
        alert.setHeaderText("'"+_selection+"'"+" is your preferred recording");
        alert.setContentText("Do you want to give this recording a bad rating?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            _recordingModel.setFavourite(false);
            _recordingModel.setRating(false);
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter("Ratings.txt", true));
            } catch (IOException e) {
                e.printStackTrace();
            }
            PrintWriter writer = new PrintWriter(bw);
            writer.println(_selection);
            writer.close();
            return true;
        } else {
            return false;
        }
    }



    /**Method called when user wants to remove a bad rating associated with a recording
     * it reads in the current rating text files and copies every line to a new text file except for the recording we want to overwrite
     */
    private void deleteRating(){
        File ratingFile = new File("Ratings.txt");
        File tempFile = new File("temp.txt");

        try {
            tempFile.createNewFile(); //read in current ratings file and then check each line for the recording to be removed
            BufferedReader br = new BufferedReader(new FileReader(ratingFile)); //copy each line that is not the selected recording into temp file then rename temp at the end
            BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
            PrintWriter writer = new PrintWriter(bw);
            String line;

            while((line = br.readLine()) != null) {
                if(!line.contains(_selection)) {
                    writer.println(line); //add line if not the selected recording
                } else {
                    continue;
                }
            }
            writer.close();
            br.close();
            tempFile.renameTo(ratingFile);
        } catch (IOException e) {
        }
    }
}
