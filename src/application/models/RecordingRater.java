package application.models;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;

import java.io.*;
import java.util.Optional;
import java.util.Scanner;

public class RecordingRater {

    //class that handles File I/O operations related to the txt file

    private String _selection;

    private RecordingModel _recordingModel;

    public RecordingRater(String selection, RecordingModel recordingModel){
        _recordingModel = recordingModel;
        _selection = selection;
    }

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


    public void overWriteRating(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Overwrite?");
        alert.setHeaderText("A bad rating has been recorded for '"+_selection+"'");
        alert.setContentText("Do you want to remove this rating?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            deleteRating();
            _recordingModel.setRating(true);
        }
    }

    public void makeRating(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Rating");
        alert.setHeaderText("You are rating '"+ _selection+"'");
        alert.setContentText("Is this recording bad quality?");

        ButtonType badButton = new ButtonType("Bad");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(badButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        String rating;
        if (result.get() == badButton) {
            rating = "Bad";
            _recordingModel.setRating(false);
        } else {
            return;
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("Ratings.txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter writer = new PrintWriter(bw);
        writer.println("Rating for " + _selection+ " : '"+rating+"'");
        writer.close();
    }

    private void deleteRating(){
        File ratingFile = new File("Ratings.txt");
        File tempFile = new File("temp.txt");

        try {
            tempFile.createNewFile(); //read in current ratings file and then check each line for the recording to be removed
            BufferedReader br = new BufferedReader(new FileReader(ratingFile)); //copy each line that is not the search recording into temp file then rename temp at the end
            BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
            PrintWriter writer = new PrintWriter(bw);
            String line;

            while((line = br.readLine()) != null) {
                if(!line.contains(_selection)) {
                    writer.println(line);
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
