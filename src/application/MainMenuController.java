package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainMenuController implements Initializable {


    @FXML
    private Button recordBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button addBtn;

    @FXML
    private ListView<String> playQueue;

    @FXML
    private TreeView<String> originalTreeView;

    @FXML
    private TabPane tabPane;

    private ObservableList<String> _queuedNames;


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

        TreeItem<String> selection = personalTreeView.getSelectionModel().getSelectedItem();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete?");
        alert.setHeaderText("You are about to delete "+ "'se206_"+ selection.getValue() +"'");
        alert.setContentText("Hit Ok to confirm or Cancel to return to menu");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            selection.getParent().getChildren().remove(selection); //remove file from treeview
            String selectionName = selection.getValue().substring(selection.getValue().lastIndexOf('_')+1,selection.getValue().lastIndexOf('.'));
            NamesModel selectionModel = _namesListModel.getName(selectionName);
            selectionModel.delete("se206_"+selection.getValue()); //delete file
            if (_queuedNames.indexOf(selection.getValue()) != -1){ //remove the recording if its been queued in play list
                _queuedNames.remove(selection.getValue());
            }
        }

    }

    @FXML
    public void addToQueue(){

        TreeItem<String> selection;
        List<String> recordings;
        if (tabPane.getSelectionModel().getSelectedIndex() == 0){
            selection = originalTreeView.getSelectionModel().getSelectedItem();
        } else {
            selection = personalTreeView.getSelectionModel().getSelectedItem();
        }

        if (selection != null){
            if (selection.isLeaf() && calcHeight(selection) == 4){
               String queueName = selection.getValue().substring(selection.getValue().lastIndexOf('_')+1,selection.getValue().lastIndexOf('.'));
               NamesModel queueNameModel = _namesListModel.getName(queueName);
                recordings = (tabPane.getSelectionModel().getSelectedIndex() == 0) ? queueNameModel.getOriginalRecordings() : queueNameModel.getPersonalRecordings();
                for (String s : recordings){
                    if (s.indexOf(selection.getValue()) != -1){
                        queueName = s;
                        break;
                    }
                }
                if (_queuedNames.indexOf(queueName) == -1){
                   _queuedNames.add(queueName);
               }
            }

        }

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        addBtn.setDisable(true);
        deleteBtn.setDisable(true);
        populateTree(originalTreeView, "original");
        populateTree(personalTreeView, "personal");
        checkDoubleClick();
        _namesListModel.createDirectory();
        _queuedNames = FXCollections.observableArrayList();
        playQueue.setItems(_queuedNames);

    }

    private void populateTree(TreeView<String> tree, String identifier){
        TreeItem<String> root = new TreeItem<>("Names");
        char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toUpperCase().toCharArray();
        TreeItem<String>[] alphabetHeadings = new TreeItem[27];
        for (int i=0;i<26;i++){
            alphabetHeadings[i]= makeBranch(root, Character.toString(alphabet[i]));
        }
        alphabetHeadings[26]=makeBranch(root,"Other");

        //make branch for recordings
        for (int i=0;i<26;i++){
            ArrayList<String> names = new ArrayList<>();
            names.addAll(_namesListModel.getNames(alphabet[i], identifier));
            for (String s : names){
                TreeItem<String> heading = makeBranch(alphabetHeadings[i], s);
                NamesModel nameModel = _namesListModel.getName(s);
                List<String> recordings = (identifier.equals("original")) ? nameModel.getOriginalRecordings() : nameModel.getPersonalRecordings();
                for (String recording : recordings){
                    makeBranch(heading, recording);
                }
            }
        }

        root.setExpanded(true);
        tree.setRoot(root);

    }

    private TreeItem<String> makeBranch(TreeItem<String> parent, String title){
        TreeItem<String> branch = new TreeItem<>(title);
        parent.getChildren().add(branch);
        if (calcHeight(branch) != 3){
            branch.setExpanded(true);
        } else {
            branch.setExpanded(false);
        }
        return branch;
    }

    private int calcHeight(TreeItem<String> selection){ //recursive function to calculate height of the selected item in tree
        if (selection.getParent() == null){
            return 1;
        } else {
            return calcHeight(selection.getParent())+1;
        }

    }

    private void checkDoubleClick(){
        originalTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2){
                    addToQueue();
                }
                deleteBtn.setDisable(true);
                addBtn.setDisable(false);
            }
        });

        personalTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2){
                    addToQueue();
                }
                TreeItem<String> selection = personalTreeView.getSelectionModel().getSelectedItem();

                if (selection != null) {
                    if (selection.isLeaf() && calcHeight(selection) == 4) {
                        deleteBtn.setDisable(false);
                    } else {
                        deleteBtn.setDisable(true);
                    }
                } else {
                    deleteBtn.setDisable(true);
                }
                addBtn.setDisable(false);
            }
        });

    }
}
