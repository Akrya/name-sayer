package application.models;

import application.models.NamesModel;
import javafx.concurrent.Task;
import org.omg.CORBA.VM_CUSTOM;
import sun.util.resources.ca.CalendarData_ca;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class Recorder extends Task<String> {

    private NamesModel _name;
    private int _versionNum;
    private String _fileName;
    private String _customName;

    public Recorder(NamesModel name){
        _name = name;
        getRecordingVersion();
    }

    public Recorder(String customName){
        _customName = customName.substring(0,customName.lastIndexOf(' ')).replace(' ','-');
    }

    private void createAudio() { //run the bash command to record for 5 seconds
        String currentTime = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(Calendar.getInstance().getTime());
        _fileName = "personal_"+currentTime+"_"+"ver_"+"("+_versionNum+")"+"_"+_name.toString()+".wav";

        String cmd = "ffmpeg -loglevel panic -f alsa -i default -t 5 Personal/"+"'"+_fileName+"'";
        ProcessBuilder audioBuilder = new ProcessBuilder("/bin/bash","-c", cmd);
        try {
            audioBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCustomAudio(){
        String currentTime = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(Calendar.getInstance().getTime());
        _fileName = "custom_"+currentTime+"_"+_customName+".wav";
        String cmd = "ffmpeg -loglevel panic -f alsa -i default -t 5 CustomRecords/"+"'"+_fileName+"'";
        ProcessBuilder audioBuilder = new ProcessBuilder("/bin/bash","-c",cmd);
        try {
            audioBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getRecordingVersion(){
        if (_name != null) {
            List<RecordingModel> records = _name.getRecords();
            _versionNum = 1;
            for (RecordingModel record : records) {
                if (record.getIdentifier() == 1) {
                    _versionNum++;
                }
            }
        }
    }

    private void waitForRecord() throws InterruptedException {
        for (int i=0;i<5000;i++){
            Thread.sleep(1);
            updateProgress(i+1,5000); //update binded progress bar periodically for duration of recording
        }
    }
    @Override
    protected String call() throws Exception {
        if (_name != null){
            createAudio();
        } else {
            createCustomAudio();
        }
        waitForRecord(); //sleep thread while recording
        return _fileName;
    }
}
