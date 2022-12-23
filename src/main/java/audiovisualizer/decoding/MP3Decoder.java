package audiovisualizer.decoding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public class MP3Decoder {

    enum Layer {
        LAYER1,
        LAYER2,
        LAYER3,
        RESERVED
    }

    private Layer currentLayer;
    private boolean errorProtection;
    private int bitrate;
    private double frequency;
    private boolean padded;

    public void decode(File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            if (checkForMetadata(stream)) {
                readMetadata(stream);
            }
            readFrame(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks for ID3v2 metadata. <p>
     * {@link} https://id3.org/id3v2.4.0-structure
     */
    private boolean checkForMetadata(FileInputStream stream) throws IOException {
        String id3 = "ID3";
        byte[] bytes = stream.readNBytes(3);
        for (int i = 0; i < bytes.length; i++) {
            if (AsciiTable.convert(bytes[i]) != id3.charAt(i)) return false;
        }
        return true;
    }
    /**
     * Reads ID3v2 metadata. Subclasses can override this method to read and return metadata. <p>
     * {@link} https://id3.org/id3v2.4.0-structure
     */
    private void readMetadata(FileInputStream stream) throws IOException {
        stream.skip(2); // Skip ID3 version bytes
        boolean footer = false;
        int flags = stream.read(); // Get ID3 flags
        if ((flags & 0b0001000) != 0) footer = true; // If the footer flag is set then it's not zero and there is a footer
        int metadataSize = ByteBuffer.wrap(stream.readNBytes(4)).getInt(); // Get size of ID3 tag
        if (footer) metadataSize += 10;
        stream.skip(metadataSize);
    }

    
    private void readFrame(FileInputStream stream) throws IOException {
        readFrameHeader(stream);
    }

    /**
     * {@link} https://en.wikipedia.org/wiki/MP3#/media/File:Mp3filestructure.svg
     */
    private void readFrameHeader(FileInputStream stream) throws IOException {
        int syncword = stream.read();
        int headerInfo = stream.read();
        if (syncword != 0xFF || headerInfo >> 4 != 0xF ) throw new StreamCorruptedException("MP3 Header Invalid Syncword"); // Make sure sync word is FFF
        int layer = headerInfo & 0b00000110 >> 1;
        currentLayer = switch(layer) {
            case 0 -> Layer.RESERVED;
            case 1 -> Layer.LAYER3;
            case 2 -> Layer.LAYER1;
            case 3 -> Layer.LAYER1;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Layer " + layer);
        };
        errorProtection = (headerInfo & 0x01) == 0;
        int headerInfo2 = stream.read();
        bitrate = bitrateTable(currentLayer, headerInfo2 >> 4);
        frequency = headerInfo2 & 0b00001100 >> 2;
        padded = (headerInfo2 & 0b00000010 >> 1) == 1;
        int headerInfo3 = stream.read();
    }

    private int bitrateTable(Layer layer, int value) {
        return switch(layer) {
            case RESERVED:
            case LAYER1: yield switch(value) {
                case 0 -> 0;
                case 1 -> 32;
                case 2 -> 64;
                case 3 -> 96;
                case 4 -> 128;
                case 5 -> 160;
                case 6 -> 192;
                case 7 -> 224;
                case 8 -> 256;
                case 9 -> 288;
                case 10 -> 320;
                case 11 -> 352;
                case 12 -> 384;
                case 13 -> 416;
                case 14 -> 448;
                default -> throw new IllegalArgumentException("Invalid Bitrate Index");
            };
            case LAYER2: yield switch(value) {
                case 0 -> 0;
                case 1 -> 32;
                case 2 -> 48;
                case 3 -> 56;
                case 4 -> 64;
                case 5 -> 80;
                case 6 -> 96;
                case 7 -> 112;
                case 8 -> 128;
                case 9 -> 160;
                case 10 -> 192;
                case 11 -> 224;
                case 12 -> 256;
                case 13 -> 320;
                case 14 -> 384;
                default -> throw new IllegalArgumentException("Invalid Bitrate Index");
            };
            case LAYER3: yield switch(value) {
                case 0 -> 0;
                case 1 -> 32;
                case 2 -> 40;
                case 3 -> 48;
                case 4 -> 56;
                case 5 -> 64;
                case 6 -> 80;
                case 7 -> 96;
                case 8 -> 112;
                case 9 -> 128;
                case 10 -> 160;
                case 11 -> 192;
                case 12 -> 224;
                case 13 -> 256;
                case 14 -> 320;
                default -> throw new IllegalArgumentException("Invalid Bitrate Index");
            };
            default: throw new IllegalArgumentException("Illegal Layer");
        };
    }

    private double frequencyTable(int value) {
        return switch(value) {
            case 0 -> 44.1;
            case 1 -> 48;
            case 2 -> 32;
            case 3 -> 0;
            default -> throw new IllegalArgumentException("Invalid Frequency Index");
        };
    }

}
