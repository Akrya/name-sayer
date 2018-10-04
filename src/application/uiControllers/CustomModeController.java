package application.uiControllers;

import application.models.NameSelectorSingleton;
import application.models.NamesListModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CustomModeController implements Initializable {


    private NameSelectorSingleton _singleton;

    private NamesSelectorController _controller;

    private ObservableList<String> _selectedNames;

    private ObservableList<String> _customRecords;

    @FXML
    private ListView<String> customRecordings;

    private NamesListModel _namesListModel = new NamesListModel();

    @FXML
    private ListView<String> selectedNames;

    @FXML
    private Text selectStatus;

    @FXML
    private Text selectedName;

    @FXML
    private void goToMain(ActionEvent event) throws IOException {
        Parent listenScene = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void goToSelect(ActionEvent event) throws IOException {
        _singleton = NameSelectorSingleton.getInstance();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("NamesSelector.fxml"));
        Parent root = loader.load();
        NamesSelectorController controller = loader.getController();
        _singleton.setController(controller);
        Scene scene = new Scene(root);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    @FXML
    private void enableSelect(MouseEvent mouseEvent){
        if (mouseEvent.getClickCount() == 2){
            selectName();
        }
    }

    @FXML
    private void selectName(){
        String selection = selectedNames.getSelectionModel().getSelectedItem();
        if (selection != null){
            if (!selection.contains("invalid")){
                selectStatus.setText("Currently selected:");
                selectedName.setText(selection);
            }
        }

    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _singleton = NameSelectorSingleton.getInstance();
        _controller = _singleton.getController();
        _selectedNames = FXCollections.observableArrayList(_controller.getSelectedNames());
        selectedNames.setItems(_selectedNames);
        getCustomRecordings();
        customRecordings.setItems(_customRecords);

    }

    private void getCustomRecordings(){
        File[] files = new File("CustomRecords").listFiles();
        List<String> customRecords = new ArrayList<>();
        for (File file : files){
            if (file.isFile()){
                customRecords.add(file.getName());
            }
        }
        _customRecords = FXCollections.observableArrayList(customRecords);
    }
}
