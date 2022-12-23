package audiovisualizer.decoding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;

import audiovisualizer.decoding.MP3Header.Emphasis;
import audiovisualizer.decoding.MP3Header.Layer;
import audiovisualizer.decoding.MP3Header.MPEGVersion;
import audiovisualizer.decoding.MP3Header.Mode;
import javafx.util.Pair;

@SuppressWarnings("unused")
public class MP3Decoder { // todo add to the bitrate and frequency tables for version 2/2.5, maybe bands stuff for joint stereo

    MP3Header currentHeader;

    public void decode(File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            if (checkForMetadata(stream)) {
                readMetadata(stream);
            }
            readFrame(stream);
            stream.close();
        } catch (IOException e) {
            System.out.println("MP3 decoding error with file " + file.getAbsolutePath()); 
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
        byte[] size = stream.readNBytes(4);
        
        int metadataSize = ByteBuffer.wrap(size).getInt(); // Get size of ID3 tag
        if (footer) metadataSize += 10;
        stream.skip(metadataSize);
    }

    
    private void readFrame(FileInputStream stream) throws IOException {
        currentHeader = readFrameHeader(stream);
    }

    /**
     * {@link} https://en.wikipedia.org/wiki/MP3#/media/File:Mp3filestructure.svg
     * {@link} http://mpgedit.org/mpgedit/mpeg_format/mpeghdr.htm
     */
    private MP3Header readFrameHeader(FileInputStream stream) throws IOException {
        int syncword = stream.read();
        int headerInfo = stream.read();
        int syncword2 = headerInfo >> 4;
        if (syncword != 0xFF || syncword2 != 0xF ) throw new StreamCorruptedException("MP3 Header Invalid Syncword " + syncword + " " + syncword2); // Make sure sync word is FFF
        int versionNumber = headerInfo & 0b00001000 >> 3;
        MPEGVersion version = switch(versionNumber) {
            case 0 -> MPEGVersion.MPEG_2_5;
            case 1 -> MPEGVersion.RESERVED;
            case 2 -> MPEGVersion.MPEG_2;
            case 3 -> MPEGVersion.MPEG_1;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Version " + versionNumber);
        };
        int layerNumber = headerInfo & 0b00000110 >> 1;
        Layer layer = switch(layerNumber) {
            case 0 -> Layer.RESERVED;
            case 1 -> Layer.LAYER3;
            case 2 -> Layer.LAYER2;
            case 3 -> Layer.LAYER1;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Layer " + layerNumber);
        };
        boolean errorProtection = (headerInfo & 1) == 0;
        int headerInfo2 = stream.read();
        int bitrate = bitrateTable(layer, headerInfo2 >> 4);
        double frequency = headerInfo2 & 0b00001100 >> 2;
        boolean padded = (headerInfo2 & 0b00000010 >> 1) == 1;
        int headerInfo3 = stream.read();
        int modeNumber = headerInfo3 >> 6;
        Mode mode = switch(modeNumber) {
            case 0 -> Mode.STEREO;
            case 1 -> Mode.JOINT_STEREO;
            case 2 -> Mode.DUAL_CHANNEL;
            case 3 -> Mode.SINGLE_CHANNEL;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Mode " + modeNumber);
        };
        int modeExtensionNumber = headerInfo3 & 0b00110000 >> 4;
        boolean intensityStereo = false;
        boolean msStereo = false;
        if (mode == Mode.JOINT_STEREO) {
            Pair<Boolean, Boolean> modeExtension = configureModeExtension(modeExtensionNumber, layer);
            intensityStereo = modeExtension.getKey();
            msStereo = modeExtension.getValue();
        }
        boolean copyrighted = (headerInfo3 & 0b00001000 >> 3) == 1;
        boolean original = (headerInfo3 & 0b00000100 >> 2) == 1;
        int emphasisNumber = headerInfo3 & 0b00000011;
        Emphasis emphasis = switch(emphasisNumber) {
            case 0 -> Emphasis.NONE;
            case 1 -> Emphasis.MICROSECONDS_50_15; 
            case 2 -> Emphasis.RESERVED; 
            case 3 -> Emphasis.CCITT_J_17;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Emphasis " + emphasisNumber);
        };
        MP3Header header = new MP3Header();
        return header;
    }

    private int bitrateTable(Layer layer, int index) throws StreamCorruptedException {
        return switch(layer) {
            case RESERVED:
            case LAYER1: yield switch(index) {
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
                default -> throw new StreamCorruptedException("MP3 Header Invalid Bitrate Index " + index);
            };
            case LAYER2: yield switch(index) {
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
                default -> throw new StreamCorruptedException("MP3 Header Invalid Bitrate Index " + index);
            };
            case LAYER3: yield switch(index) {
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
                default -> throw new StreamCorruptedException("MP3 Header Invalid Bitrate Index " + index);
            };
            default: throw new IllegalArgumentException("Illegal Layer");
        };
    }

    private double frequencyTable(int index) throws StreamCorruptedException {
        return switch(index) {
            case 0 -> 44.1;
            case 1 -> 48;
            case 2 -> 32;
            case 3 -> 0;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Frequency Index " + index);
        };
    }

    private Pair<Boolean, Boolean> configureModeExtension(int modeExtension, Layer layer) {
        boolean intensityStereo = false;
        boolean msStereo = false;
        if (layer == Layer.LAYER1 || layer == Layer.LAYER2) {
            intensityStereo = true;
            msStereo = false;
        } 
        else {
            switch(modeExtension) {
                case 0 -> {
                    intensityStereo = false;
                    msStereo = false;
                }
                case 1 -> {
                    intensityStereo = false;
                    msStereo = false;
                } 
                case 2 -> {
                    intensityStereo = false;
                    msStereo = false;
                } 
                case 3 -> {
                    intensityStereo = false;
                    msStereo = false;
                }
            }
        }
        return new Pair<Boolean,Boolean>(intensityStereo, msStereo);
    }    

}
