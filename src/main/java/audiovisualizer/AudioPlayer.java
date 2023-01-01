package audiovisualizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer {

    /**
     * Plays PCM audio in a new thread.
     * @param PCM data
     * @param sampleRate number of samples per second
     * @param bitsPerSample bits for each value of a sample
     * @param numChannels number of channels pcm data is broken up into. 1 = mono, ect.
     * @param loopAmount number of times you want audio to play
     */
    public void playPCMData(byte[] PCM, int sampleRate, int bitsPerSample, int numChannels, Runnable callback) {
        try {
            AudioFormat audioFormat = new AudioFormat(sampleRate, bitsPerSample, numChannels, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(null);
            line.open(audioFormat);
            Thread t = new Thread(() -> {
                int writeAmount = 500;
                line.start();
                for (int j = 0; j < PCM.length; j+= writeAmount) {
                    line.write(PCM, j, writeAmount);
                    callback.run();
                }
                // line.write(PCM, 0, PCM.length);
                line.drain();
                line.stop();
                line.close();
            });
            t.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    

}
