package application;

import application.controllers.MainMenuController;
import application.models.NamesListModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/MainMenu.fxml")); //open up the menu on start up
        Parent root = loader.load();
        MainMenuController controller = loader.getController();
        NamesListModel model = new NamesListModel(); //construct the list of name models
        controller.initialise(model);
        primaryStage.setTitle("Name Sayer");
        primaryStage.setScene(new Scene(root, 1200, 700));
        primaryStage.show();
        primaryStage.setResizable(false);

        primaryStage.setOnCloseRequest(e ->{ //confirmation box for exiting program
            e.consume();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Quit?");
            alert.setHeaderText("Are you sure you want to exit?");
            alert.setContentText("Hit OK to quit");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                primaryStage.close();
                System.exit(0);
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
