package application.models;

import javafx.concurrent.Task;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**Class is responsible for making a new recording when the user presses record button
 */
public class Recorder extends Task<String> {

    private NameModel _name;
    private int _versionNum;
    private String _fileName;
    private String _concatName;
    private Process _recordingProcess;

    /**Called when user presses record button for single name
     * it calls getRecordingVersion() to find the number of times user has recorded for the name
     * @param name model of name which user wants to record for
     */
    public Recorder(NameModel name){
        _name = name;
        getRecordingVersion();
    }

    /**Called when user presses record button for concatenated name
     * it replaces all space characters in name with hyphens which will be used in the file name
     * @param model concatenated name model which user wants to record for
     */
    public Recorder(ConcatenatedNameModel model){
        String name = model.toString();
        _concatName = name.replace(' ','-');
        _versionNum = model.getVersionNum();
        model.incrementVersionNum();
    }

    /**Method runs the bash command for recording on a separate process, this method is called when recording for
     * single names
     */
    private void makeAudio() { //run the bash command to record for 5 seconds
        String currentTime = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(Calendar.getInstance().getTime());
        _fileName = "personal_"+currentTime+"_"+"ver_"+"("+_versionNum+")"+"_"+_name.toString()+".wav";

        String cmd = "ffmpeg -loglevel panic -f alsa -t 5 -i default Single/"+"'"+_fileName+"'";

        ProcessBuilder audioBuilder = new ProcessBuilder("/bin/bash","-c", cmd);
        try {
            _recordingProcess = audioBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**Method is similar to above, but is called when recording for concatenated name
     */
    private void makeConcatenatedAudio(){
        String currentTime = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(Calendar.getInstance().getTime());
        _fileName = "custom_"+currentTime+"_"+"ver_("+_versionNum+")_"+ _concatName +".wav";

        String cmd = "ffmpeg -loglevel panic -f alsa -t 5 -i default Concatenated/"+"'"+_fileName+"'";
        ProcessBuilder audioBuilder = new ProcessBuilder("/bin/bash","-c",cmd);
        try {
            _recordingProcess = audioBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**Method called to retrieve recording version number for a single name
     */
    private void getRecordingVersion(){
        if (_name != null) {
            List<RecordingModel> records = _name.getRecords();
            _versionNum = 1;
            for (RecordingModel record : records) { //loops through and increments for each recording that is associated with name
                if (record.getIdentifier() == 1) {
                    _versionNum++;
                }
            }
        }
    }

    /**Called when user wants to stop recording, it kills the recording process prematurely
     * @return file name of the recording that was cut short
     */
    public String stopRecording(){
        _recordingProcess.destroy();
        this.cancel();
        return _fileName;
    }

    /**Method runs on separate thread and makes calls to update the binded progress bar for the
     * duration of the recording (5 seconds)
     * @throws InterruptedException
     */
    private void waitForRecord() throws InterruptedException {
        for (int i=0;i<5000;i++){
            Thread.sleep(1);
            updateProgress(i+1,5000); //update binded progress bar periodically for duration of recording
        }
    }

    /**Method called when task is started, it starts the bash command on a separate process before
     * calling waitForRecord() to update the progress bar.
     * @return
     * @throws Exception
     */
    @Override
    protected String call() throws Exception {
        if (_name != null){
            makeAudio();
        } else {
            makeConcatenatedAudio();
        }
        waitForRecord(); //sleep thread while recording
        return _fileName;
    }
}
