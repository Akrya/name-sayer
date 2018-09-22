package application;

import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class RecordingRater {

    private String _selection;

    public RecordingRater(String selection){
        _selection = selection;
    }

    public boolean checkFile(){
        boolean found = false;
        try {
            Scanner scanner = new Scanner(new File("Names/Ratings.txt"));
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

    }

    public void makeRating(){

    }
}
