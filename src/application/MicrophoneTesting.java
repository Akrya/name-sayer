/*package application;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

////NEED TO REFERENCE STACK OVERFLOW

public class TestmicroPhone {

    public TestmicroPhone() {

    }

    protected static int calculateRMSLevel(byte[] audioData)
    { // audioData might be buffered data read from a data line
        long lSum = 0;
        for(int i=0; i<audioData.length; i++)
            lSum = lSum + audioData[i];

        double dAvg = lSum / audioData.length;

        double sumMeanSquare = 0d;
        for(int j=0; j<audioData.length; j++)
            sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);

        double averageMeanSquare = sumMeanSquare / audioData.length;
        return (int)(Math.pow(averageMeanSquare,0.5d) + 0.5);
    }

    public static void main(String[] args){

        // Open a TargetDataLine for getting microphone input & sound level
        TargetDataLine line = null;
        //Different parameters for AudioFormat, can mess around with them. DOesnt work dependin on some numbers
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 30000, 16, 2, 4, 100, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); //     format is an AudioFormat object
        System.out.println(info);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("The line is not supported.");
        }
        // Obtain and open the line.
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        } catch (LineUnavailableException ex) {
            System.out.println("The TargetDataLine is Unavailable.");
        }

        // Can use a timer class to only read for a certain period of time
        while(1>0){
            byte[] bytes = new byte[line.getBufferSize() / 10];
            line.read(bytes, 0, bytes.length);
            System.out.println("RMS Level: " + calculateRMSLevel(bytes));
        }
    }
}
*/