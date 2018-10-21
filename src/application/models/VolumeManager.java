package application.models;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.control.Slider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VolumeManager {

    private Slider _volumeSlider;

    public VolumeManager(Slider volumeSlider){
        _volumeSlider = volumeSlider;
    }

    /** Method sets up the volume adjustment bar by binding a volume slider to the volume level;
     */
    public void startVolumeSlider(){

        //running command to get current volume https://unix.stackexchange.com/questions/89571/how-to-get-volume-level-from-the-command-line/89581
        String cmd1 = "amixer get Master | awk '$0~/%/{print $4}' | tr -d '[]%'";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd1);

        //setting the slider to the current volume
        try{
            Process volumeInitializer = builder.start();
            InputStream inputStream = volumeInitializer.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String volumeLevel = br.readLine();
            double vlevel = Double.parseDouble(volumeLevel);
            _volumeSlider.setValue(vlevel);

        } catch (IOException e){
            e.printStackTrace();
        }

        //attach a listener to the volume bar so when it slides it changes the system volume
        //https://www.youtube.com/watch?v=X9mEBGXX3dA reference
        _volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                double volume = _volumeSlider.getValue();
                //System.out.println(volume);
                String cmd2 = "amixer set 'Master' " + volume + "%";
                ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd2);
                try {
                    builder.start();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }

}
