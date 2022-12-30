package audiovisualizer.mp3.decoding;

import java.io.IOException;
import java.io.StreamCorruptedException;

import audiovisualizer.mp3.MP3Header;
import audiovisualizer.mp3.MP3Header.Emphasis;
import audiovisualizer.mp3.MP3Header.Layer;
import audiovisualizer.mp3.MP3Header.MPEGVersion;
import audiovisualizer.mp3.MP3Header.Mode;

public class HeaderDecoder {

    // Bitrate in kbits/s, index 15 is reserved.
    protected static final double[][][] bitrateTable = {
        { // MPEG_1
            {0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, -1}, // LAYER1
            {0, 32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384, -1}, // LAYER2
            {0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, -1} // LAYER3
        }, 
        { // MPEG_2, MPEG_2_5
            {0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256, -1}, // LAYER1
            {0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, -1}, // LAYER2
            {0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, -1} // LAYER3, Same as LAYER2
        } 
    };

    // Sampling frequency in kHz, index 3 is reserved.
    protected static final double[][] frequencyTable = {
        {44.1, 48, 32, -1}, // MPEG_1
        {22.05, 24, 16, -1}, // MPEG_2
        {11.025, 12, 8, -1} // MPEG_2_5
    };

    /**
     * Reads an MP3 Frame header which is 3 bytes long and returns an MP3Header record by parsing the data from the 24 bits that contain the following: 
     * MPEG Version, MPEG Layer, Error Protection (boolean), Bitrate in kbps, Sampling Frequency in kHz, Frame Padded (boolean),
     * Channel Mode, Mode Extension Bands for Layers 1 & 2 or type of joint stereo for Layer 3 - when Channel Mode is JOINT_STEREO,
     * Copyrighted (boolean), Original or Copy (boolean), and the type of de-emphasis to use. <p>
     * {@link} https://en.wikipedia.org/wiki/MP3#/media/File:Mp3filestructure.svg
     * {@link} http://mpgedit.org/mpgedit/mpeg_format/mpeghdr.htm
     * {@link} http://www.mp3-tech.org/programmer/frame_header.html
     * @param stream MP3 file stream to read header from
     * @throws IOException if any corruptions or invalid values are found when trying to parse the header
     */
    public MP3Header readFrameHeader(BitInputStream stream) throws IOException {
        int headerInfo = stream.read();
        int versionNumber = (headerInfo & 0b00011000) >> 3; // MPEG Audio version
        MPEGVersion version = switch(versionNumber) { // MPEG Audio version, 1 is reserved.
            case 0 -> MPEGVersion.MPEG_2_5;
            case 2 -> MPEGVersion.MPEG_2;
            case 3 -> MPEGVersion.MPEG_1;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Version Index " + versionNumber);
        };
        int layerNumber = (headerInfo & 0b00000110) >> 1; // MPEG Layer
        Layer layer = switch(layerNumber) { // MPEG Layer, 0 is reserved.
            case 1 -> Layer.LAYER3;
            case 2 -> Layer.LAYER2;
            case 3 -> Layer.LAYER1;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Layer Index " + layerNumber);
        };
        boolean errorProtection = (headerInfo & 1) == 0; // Protection bit
        int headerInfo2 = stream.read();
        double bitrate = bitrateTable[versionToIndex(version, true)][layerToIndex(layer)][headerInfo2 >> 4]; // Bitrate from index
        double frequency = frequencyTable[versionToIndex(version, false)][(headerInfo2 & 0b00001100) >> 2]; // Sampling rate frequency from index
        boolean padded = (headerInfo2 & 0b00000010) >> 1 == 1; // Padding bit
        int headerInfo3 = stream.read();
        int modeNumber = (headerInfo3 >> 6); // Channel Mode
        Mode mode = switch(modeNumber) { // Channel Mode
            case 0 -> Mode.STEREO;
            case 1 -> Mode.JOINT_STEREO;
            case 2 -> Mode.DUAL_CHANNEL;
            case 3 -> Mode.SINGLE_CHANNEL;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Channel Mode Index " + modeNumber); // Shouldn't happen
        };
        int modeExtensionNumber = (headerInfo3 & 0b00110000) >> 4; // Mode extension (Only if Joint stereo)
        boolean intensityStereo = false;
        boolean msStereo = false;
        if (mode == Mode.JOINT_STEREO) { // Mode Extension if Joint Stereo
            intensityStereo = true;
            if (layer == Layer.LAYER3) {
                intensityStereo = (modeExtensionNumber & 1) == 1;
                msStereo = modeExtensionNumber >> 1 == 1;
            }
        }
        boolean copyrighted = (headerInfo3 & 0b00001000) >> 3 == 1;
        boolean original = (headerInfo3 & 0b00000100) >> 2 == 1; // Original or Copy
        int emphasisNumber = (headerInfo3 & 0b00000011);
        Emphasis emphasis = switch(emphasisNumber) { // Type of de-emphasis that shall be used, index 2 is reserved.
            case 0 -> Emphasis.NONE;
            case 1 -> Emphasis.MICROSECONDS_50_15; 
            case 3 -> Emphasis.CCITT_J_17;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Emphasis Index " + emphasisNumber);
        };
        if (bitrate == -1) throw new StreamCorruptedException("MP3 Header Invalid Bitrate, had an index of 15.");
        if (frequency == -1) throw new StreamCorruptedException("MP3 Header Invalid Sampling Frequency, had an index of 15.");
        return new MP3Header(version, layer, errorProtection, bitrate, frequency, 
        padded, mode, modeExtensionNumber, intensityStereo, msStereo, copyrighted, original, emphasis);
    }

    private int versionToIndex(MPEGVersion version, boolean twoSame) {
        if (!twoSame) {
            return switch(version) {
                case MPEG_1 -> 0;
                case MPEG_2 -> 1;
                case MPEG_2_5 -> 2;
            };
        }
        return switch(version) {
            case MPEG_1 -> 0;
            case MPEG_2 -> 1;
            case MPEG_2_5 -> 1;
        };
    }
    
    private int layerToIndex(Layer layer) {
        return switch(layer) {
            case LAYER1 -> 0;
            case LAYER2 -> 1;
            case LAYER3 -> 2;
        };
    }

}
