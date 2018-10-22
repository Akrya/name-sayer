package application.models;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.List;
import java.util.Optional;

/**Class is responsible for creating pop up messages when user wants to favourite a record
 */
public class RecordingBookmarker {

    private RecordingModel _selection;

    /**Called when user presses the favourite button
     * @param selection Recording model that user wants to favourite
     */
    public RecordingBookmarker(RecordingModel selection){
        _selection = selection;
    }

    /**If the name already has a favourite recording then it asks if they want to overwrite their
     * preferred recording
     * @return true if user wants to overwrite, false otherwise
     */
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

    /**If the name does not have a favourite recording then it asks the user for confirmation
     * if they want to favourite the recording
     * @return true if user wants to continue, false otherwise
     */
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

    /**Warning message is sent if the user attempts to favourite a personal recording or
     * if user attempts to favourite a bad recording.
     */
    public void sendInvalidMessage(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invalid recording");
        alert.setHeaderText(null);
        alert.setContentText("Please select a good database recording to set as your preferred recording!");
        alert.showAndWait();
    }

}
