package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;


/*
NOT COMPLETE. PROBABLY DOESNT WORK RIgHT NOW.
Will test properly on linux computers
 */
public class CheckMicController{

    @FXML
    //private Button testmicBtn;

    //@FXML
    private void goToListenMode(ActionEvent event) throws IOException {
        Parent listenScene = FXMLLoader.load(getClass().getResource("CheckMic.fxml"));
        Scene scene = new Scene(listenScene);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
    }

    private void micTest(){

//        String dir = System.getProperty("user.dir");
//
//        String cmd1 = "ffmpeg -f alsa -i default -t 5 test.wav &> /dev/null";
//        String cmd2 = "sox test.wav -n stat";
//
//        String cmd = cmd1 + cmd2;
//
//        Service<Void> micThread = new Service<Void>(){
//
//            @Override
//            protected Task<Void> createTask(){
//                return new Task<Void>(){
//                    @Override
//                    protected Void call() throws Exception{
//                        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd);
//                        Process proc = builder.start();
//                        proc.waitFor();
//
//                        InputStream stdout = proc.getInputStream();
//                        InputStream stderr = proc.getErrorStream();
//
//                        BufferedReader stderrBuffered = new BufferedReader(new InputStreamReader(stderr));
//
//                        String rms = null;
//                        List<String> stringList = new ArrayList<String>();
//                        while((rms=stderrBuffered.readLine()) != null) {
//                            stringList.add(rms);
//                        }
//
//                        rms = stringList.get(8);
//                        rms = rms.replaceAll("\\D+", "");
//
//                    }
//                };
//            }
//        };
//
//        micThread.start();
//
//        micThread.setOnSucceeded(Event ->{
//
//            ///WILL DISPLAY FAIL OR PASS MESSAge if depending on RMS
//            //STill needs to be implemented
//        });
    }


}
