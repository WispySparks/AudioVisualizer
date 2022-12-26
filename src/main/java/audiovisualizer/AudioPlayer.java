package audiovisualizer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer {

    // Currently Blocking
    public void playPCMData(byte[] PCM, int sampleRate, int bitsPerSample, int numChannels, int loopAmount) {
        try {
            if (loopAmount < 1) loopAmount = 1;
            AudioFormat audioFormat = new AudioFormat(sampleRate, bitsPerSample, numChannels, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(null);

            line.open(audioFormat);
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

    public List<List<Long>> parsePCMData(byte[] PCM, int bitsPerSample, int numChannels) {
        int bytesPerSample = bitsPerSample / 8;
        int samples = PCM.length / numChannels / bytesPerSample;
        List<List<Long>> channels = new ArrayList<>();
        int pos = 0;
        for (int i = 0; i < numChannels; i++) {
            channels.add(new ArrayList<>());
        }
        for (int i = 0; i < samples; i++) {
            for (int j = 0; j < numChannels; j++) {
                ByteBuffer buffer = ByteBuffer.wrap(getNBytes(PCM, pos, bytesPerSample)).order(ByteOrder.LITTLE_ENDIAN);
                long sample = switch(bytesPerSample) {
                    case 1 -> buffer.get();
                    case 2 -> buffer.getShort();
                    case 4 -> buffer.getInt();
                    case 8 -> buffer.getLong();
                    default -> throw new IllegalArgumentException("Unsupported audio format (bitsPerSample) of " + bitsPerSample);
                };
                channels.get(j).add(sample);
                pos += bytesPerSample;
            }
        }
        return channels;
    }

    private byte[] getNBytes(byte[] arr, int start, int len) {
        byte[] nArr = new byte[len];
        for (int i = 0; i < len; i++) {
            nArr[i] = arr[start+i];
        }
        return nArr;
    }

}
