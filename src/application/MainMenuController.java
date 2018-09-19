package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {

    @FXML
    private Button recordBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private TreeView<String> originalTreeView;

    @FXML
    private TreeView<String> personalTreeView;

    private NamesListModel _namesListModel = new NamesListModel();

    @FXML
    private void openRecordScene(ActionEvent event) throws IOException {
        Parent createScene = FXMLLoader.load(getClass().getResource("RecordScene.fxml"));
        Scene scene = new Scene(createScene);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void deleteRecording(){
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Delete?");
//        alert.setHeaderText("You are about to delete "+ "'"+ creationList.getSelectionModel().getSelectedItem()+"'");
//        alert.setContentText("Hit Ok to confirm or Cancel to return to menu");
//
//        Optional<ButtonType> result = alert.showAndWait();
//        if (result.get() == ButtonType.OK){
//            int selectedItemIndex = creationList.getSelectionModel().getSelectedIndex();
//            model.deleteCreation(creations.get(selectedItemIndex));
//            creations.remove(selectedItemIndex);
//            if (creations.isEmpty()){ //lock view and delete button if there are no creations stored
//                deleteBtn.setDisable(true);
//                viewBtn.setDisable(true);
//            }
//        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        populateTree(originalTreeView);
        populateTree(personalTreeView);
        _namesListModel.createDirectory();
    }

    private void populateTree(TreeView<String> tree){
        TreeItem<String> root = new TreeItem<>("Names");
        char[] alphabetHeadings = "abcdefghijklmnopqrstuvwxyz".toUpperCase().toCharArray();
        for (char c: alphabetHeadings){
            TreeItem<String> heading = new TreeItem<>(Character.toString(c));
            heading.setExpanded(true);
            root.getChildren().add(heading);
        }
        TreeItem<String> specialHeading = new TreeItem<>("Other");
        specialHeading.setExpanded(true);
        root.getChildren().add(specialHeading);
        root.setExpanded(true);
        tree.setRoot(root);

    }
}
