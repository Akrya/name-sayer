package application.models;

import application.models.NamesModel;
import javafx.concurrent.Task;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class Recorder extends Task<String> {

    private NamesModel _name;
    private int _versionNum;
    private String _fileName;

    public Recorder(NamesModel name){
        _name = name;
        getRecordingVersion();
    }

    private void createAudio() { //run the bash command to record for 5 seconds
        String currentTime = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(Calendar.getInstance().getTime());
        _fileName = "se206_"+currentTime+"_"+"ver_"+"("+_versionNum+")"+"_"+_name.toString()+".wav";

        String cmd = "ffmpeg -loglevel panic -f alsa -i default -t 5 Personal/"+"'"+_fileName+"'";
        ProcessBuilder audioBuilder = new ProcessBuilder("/bin/bash","-c", cmd);
        try {
            audioBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void trimAudio(){

    }

    private void getRecordingVersion(){
        List<RecordingModel> records = _name.getRecords();
        _versionNum = 1;
        for (RecordingModel record : records){
            if (record.getIdentifier() == 1){
                _versionNum++;
            }
        }
    }

    @Override
    protected String call() throws Exception {
        createAudio();
        Thread.sleep(5000); //sleep thread while recording
        trimAudio();
        return _fileName;
    }
}
