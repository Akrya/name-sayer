package application;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Map;

public class Recorder extends Thread{

    private NamesModel _name;
    private int _versionNum;

    public Recorder(NamesModel name){
        _name = name;
        getRecordingVersion();
    }

    @Override
    public void run(){

        try {
            createAudio();
            Thread.sleep(5000); //sleep thread while recording
            trimAudio();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createAudio() { //run the bash command to record for 5 seconds
        String currentTime = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(Calendar.getInstance().getTime());
        String fileName = "se206_"+currentTime+"_"+"ver_"+_versionNum+"_"+_name.toString()+".wav";

        String cmd = "ffmpeg -loglevel panic -f alsa -i default -t 5 Names/Personal/"+"'"+fileName+"'";
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
        Map<String, Integer> recordingsMap = _name.getRecordings();
        _versionNum = 1;
        for (Map.Entry<String, Integer> entry : recordingsMap.entrySet()){
            if (entry.getValue() == 1){
                _versionNum++;
            }
        }
    }

}
