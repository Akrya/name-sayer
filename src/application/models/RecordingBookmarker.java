package application.models;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.List;
import java.util.Optional;

public class RecordingBookmarker {

    private RecordingModel _selection;


    public RecordingBookmarker(RecordingModel selection){
        _selection = selection;
    }

    public boolean overwriteFavourite(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Overwriting");
        alert.setHeaderText("You have already bookmarked a recording for this name");
        alert.setContentText("Would you like to change your preferred recording to this recording?");

        ButtonType okayButton = new ButtonType("Okay");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(okayButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okayButton) {
            return true;
        } else {
            return false;
        }
    }


    public boolean setAsFavourite(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Bookmarking");
        alert.setHeaderText("You are bookmarking '" + _selection.getFileName() + "'");
        alert.setContentText("Would you like to set this recording as your preferred practice recording?");

        ButtonType okayButton = new ButtonType("Okay");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(okayButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okayButton) {
            _selection.setFavourite(true);
            return true;
        } else {
            return false;
        }
    }

    public void sendInvalidMessage(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bad recording");
        alert.setHeaderText(null);
        alert.setContentText("Please select a good recording to set as your preferred recording!");
        alert.showAndWait();
    }

}
