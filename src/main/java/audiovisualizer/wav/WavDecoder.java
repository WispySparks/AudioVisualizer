package audiovisualizer.wav;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * {@link} http://soundfile.sapp.org/doc/WaveFormat/
 * @author WispySparks
 * @since 2022
 * @version 1.0
 */
public class WavDecoder {
    // Some default settings so audio players don't spasm
    private short audioFormat = 1;
    private short numChannels = 2;
    private int sampleRate = 48000;
    private int byteRate = 96000;
    private short blockAlign = 2;
    private int bitsPerSample= 16;
    private byte[] data = new byte[0];

    public WavFile decode(File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            readWavFile(stream);
            stream.close();
            return new WavFile(audioFormat, numChannels, sampleRate, byteRate, blockAlign, bitsPerSample, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new WavFile(audioFormat, numChannels, sampleRate, byteRate, blockAlign, bitsPerSample, data);
    }

    public void readWavFile(FileInputStream stream) throws IOException {
        readRIFF(stream);
        readFMT(stream);
        readData(stream);
    }

    public void readRIFF(FileInputStream stream) throws IOException { 
        final String id = "RIFF";
        final String format = "WAVE";
        byte[] idToCheck = stream.readNBytes(4);
        for (int i = 0; i < idToCheck.length; i++) {
            if (idToCheck[i] != id.charAt(i)) throw new StreamCorruptedException("Invalid RIFF ID.");
        }
        // int size = ByteBuffer.wrap(stream.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        stream.skipNBytes(4); // Not concerned with size of file
        byte[] formatToCheck = stream.readNBytes(4);
        for (int i = 0; i < formatToCheck.length; i++) {
            if (formatToCheck[i] != format.charAt(i)) throw new StreamCorruptedException("Not WAVE Format.");
        }
    }

    public void readFMT(FileInputStream stream) throws IOException {
        final String id = "fmt ";
        byte[] idToCheck = stream.readNBytes(4);
        for (int i = 0; i < idToCheck.length; i++) {
            if (idToCheck[i] != id.charAt(i)) throw new StreamCorruptedException("Invalid FMT ID.");
        }
        int chunkSize = ByteBuffer.wrap(stream.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        audioFormat = ByteBuffer.wrap(stream.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).getShort();
        numChannels = ByteBuffer.wrap(stream.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).getShort();
        sampleRate = ByteBuffer.wrap(stream.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        byteRate = ByteBuffer.wrap(stream.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        blockAlign = ByteBuffer.wrap(stream.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).getShort();
        bitsPerSample = ByteBuffer.wrap(stream.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).getShort();
        int expectedByteRate = sampleRate * numChannels * bitsPerSample/8; 
        int expectedBlockAlign = numChannels * bitsPerSample/8; 
        if (audioFormat != 1) throw new StreamCorruptedException("Unsupported Audio Format.");
        if (byteRate != expectedByteRate) throw new StreamCorruptedException("ByteRate is incorrect: " + byteRate + " expected: " + expectedByteRate);
        if (blockAlign != expectedBlockAlign) throw new StreamCorruptedException("BlockAlign is incorrect: " + blockAlign + " expected: " + expectedBlockAlign);
        if (chunkSize > 16) stream.skip(chunkSize-16); // sometimes there might be extra params
    }

    public void readData(FileInputStream stream) throws IOException {
        final String id = "data";
        while (true) { // apparently there can be other chunks but we dont care so just look for the data chunk
            byte[] idToCheck = stream.readNBytes(4);
            if (idToCheck[0] == -1) throw new StreamCorruptedException("No Data Chunk Found.");
            stream.skip(-4);
            for (int i = 0; i < idToCheck.length; i++) {
                if (idToCheck[i] != id.charAt(i)) continue;
            }
            break;
        }        
        int chunkSize = ByteBuffer.wrap(stream.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        data = stream.readNBytes(chunkSize);
    }
    
}