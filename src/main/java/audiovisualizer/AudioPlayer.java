package audiovisualizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer {

    public void playPCMData(byte[] PCM, float sampleRate, int bitsPerSample, int numChannels, int loopAmount) { 
        try {
            if (loopAmount < 1) loopAmount = 1;
            AudioFormat audioFormat = new AudioFormat(sampleRate, bitsPerSample, numChannels, true, false);
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

            line.open(audioFormat, PCM.length);
            line.start();

            for (int i = 0; i < loopAmount; i++) {
                line.write(PCM, 0, PCM.length);

            }            

            line.drain();
            line.stop();
            line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

}
