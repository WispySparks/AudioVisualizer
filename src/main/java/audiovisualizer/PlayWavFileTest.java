package audiovisualizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class PlayWavFileTest {

    public static void main(String[] args) {
        File file = new File("src\\main\\resources\\chromemusicsong.wav");
        playWaveFile(file);
    }

    public static void playWaveFile(File file) { // wav files are ez mode 
        try {
            FileInputStream stream = new FileInputStream(file);
            stream.skip(22);
            int numChannels = ByteBuffer.wrap(stream.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).getShort();
            int sampleRate = ByteBuffer.wrap(stream.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            stream.skip(6);
            int sampleSizeInBits = ByteBuffer.wrap(stream.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).getShort();
            stream.skip(4);
            int dataSizeBytes = ByteBuffer.wrap(stream.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            playPCMData(stream.readNBytes(dataSizeBytes) /* raw pcm data */, sampleRate, sampleSizeInBits, numChannels, 1);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void playPCMData(byte[] PCM, float sampleRate, int sampleSizeInBits, int channels, int loopAmount) { 
        try {
            AudioFormat af = new AudioFormat(sampleRate, sampleSizeInBits, channels, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

            line.open(af, PCM.length);
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