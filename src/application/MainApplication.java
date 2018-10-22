package application;

import application.controllers.MainMenuController;
import application.models.CSSManager;
import application.models.NameModelManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**Entry point of our program, it loads the main menu screen and creates a name manager
 * which is passed to the main menu controller
 *
 * Authors: Casey Chun-Cheung Wong UPI: cwon880
 *          Aditya Krishnan        UPI: akri095
 */
public class MainApplication extends Application {

    /**Method called when application starts,
     * it loads the main menu controller and passes it a name manager
     * a css manager is also passed which stores the information about the current stylesheet in use
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/MainMenu.fxml")); //open up the menu on start up
        Parent root = loader.load();
        MainMenuController controller = loader.getController();
        NameModelManager model = new NameModelManager(); //construct the list of name models1
        CSSManager cssManager = new CSSManager();
        controller.initialise(model, cssManager);
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
