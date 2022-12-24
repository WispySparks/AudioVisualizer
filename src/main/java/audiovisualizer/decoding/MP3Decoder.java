package audiovisualizer.decoding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;

import audiovisualizer.decoding.MP3Header.Emphasis;
import audiovisualizer.decoding.MP3Header.JointStereoBands;
import audiovisualizer.decoding.MP3Header.Layer;
import audiovisualizer.decoding.MP3Header.MPEGVersion;
import audiovisualizer.decoding.MP3Header.Mode;
import audiovisualizer.util.Triple;

public class MP3Decoder { 

    MP3Header currentHeader;
    short crc;

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
            if (AsciiTable.convert(bytes[i]) != id3.charAt(i)) {
                stream.skip(-3);
                return false;
            }
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
        byte[] b = stream.readNBytes(4);
        int[] bytes = new int[b.length];
        for (int i = 0; i < b.length; i++) {
            bytes[i] = b[i];
        }
        for (int i = 0; i < bytes.length-1; i++) { // Synchsafe integer bullshit
            int lastBit = bytes[i] & 1;
            bytes[i] = bytes[i] >> 1;
            bytes[i+1] = bytes[i+1] | (lastBit << 7);
        }
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) bytes[i];
        }
        int metadataSize = ByteBuffer.wrap(b).getInt(); // Get size of ID3 tag
        if (footer) metadataSize += 10;
        stream.skip(metadataSize);
    }

    
    private void readFrame(FileInputStream stream) throws IOException {
        currentHeader = readFrameHeader(stream);
        if (currentHeader.errorProtection()) {
            crc = readCRC(stream);
        }
        int frameLength = 0;
        if (currentHeader.layer() == Layer.LAYER1) {
            frameLength = (int) (12 * (currentHeader.bitrate()/currentHeader.samplingFrequency()));
        } else {
            frameLength = (int) (144 * (currentHeader.bitrate()/currentHeader.samplingFrequency()));
        }
        System.out.println(frameLength);
    }

    /**
     * Reads an MP3 Frame header which is 4 bytes long and returns an MP3Header object by parsing the data from the 32 bits that contains the following: 
     * MPEG Version, MPEG Layer, Error Protection (boolean), Bitrate in kbps, Sampling Frequency in kHz, Frame Padded (boolean),
     * Channel Mode, Mode Extension Bands for Layers 1 & 2 or type of joint stereo for Layer 3 - when Channel Mode is JOINT_STEREO,
     * Copyrighted (boolean), Original or Copy (boolean), and the type of de-emphasis to use. <p>
     * {@link} https://en.wikipedia.org/wiki/MP3#/media/File:Mp3filestructure.svg
     * {@link} http://mpgedit.org/mpgedit/mpeg_format/mpeghdr.htm
     * @param stream MP3 file stream to read header from
     * @throws IOException if any corruptions or invalid values are found when trying to parse the header
     */
    private MP3Header readFrameHeader(FileInputStream stream) throws IOException {
        int syncword = stream.read(); // Frame sync
        int headerInfo = stream.read();
        int syncword2 = headerInfo >> 4; // Frame sync
        if (syncword != 0xFF || syncword2 != 0xF ) throw new StreamCorruptedException("MP3 Header Invalid Syncword " + syncword + " " + syncword2); // Make sure sync word is FFF
        int versionNumber = headerInfo & 0b00001000 >> 3; // MPEG Audio version
        MPEGVersion version = switch(versionNumber) { // MPEG Audio version 
            case 0 -> MPEGVersion.MPEG_2_5;
            case 1 -> MPEGVersion.MPEG_1; // Reserved
            case 2 -> MPEGVersion.MPEG_2;
            case 3 -> MPEGVersion.MPEG_1;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Version " + versionNumber);
        };
        int layerNumber = headerInfo & 0b00000110 >> 1; // MPEG Layer
        Layer layer = switch(layerNumber) { // MPEG Layer
            case 0 -> Layer.LAYER1; // Reserved
            case 1 -> Layer.LAYER3;
            case 2 -> Layer.LAYER2;
            case 3 -> Layer.LAYER1;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Layer " + layerNumber);
        };
        boolean errorProtection = (headerInfo & 1) == 0; // Protection bit
        int headerInfo2 = stream.read();
        int bitrate = bitrateTable(version, layer, headerInfo2 >> 4); // Bitrate from index
        double frequency = frequencyTable(version, headerInfo2 & 0b00001100 >> 2); // Sampling rate frequency from index
        boolean padded = (headerInfo2 & 0b00000010 >> 1) == 1; // Padding bit
        int headerInfo3 = stream.read();
        int modeNumber = headerInfo3 >> 6; // Channel Mode
        Mode mode = switch(modeNumber) { // Channel Mode
            case 0 -> Mode.STEREO;
            case 1 -> Mode.JOINT_STEREO;
            case 2 -> Mode.DUAL_CHANNEL;
            case 3 -> Mode.SINGLE_CHANNEL;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Mode " + modeNumber);
        };
        int modeExtensionNumber = headerInfo3 & 0b00110000 >> 4; // Mode extension (Only if Joint stereo)
        boolean intensityStereo = false;
        boolean msStereo = false;
        JointStereoBands bands = JointStereoBands.BAND_4_31;
        if (mode == Mode.JOINT_STEREO) { // Mode extension (Only if Joint stereo)
            Triple<Boolean, Boolean, JointStereoBands> modeExtension = configureModeExtension(layer, modeExtensionNumber);
            intensityStereo = modeExtension.getFirst();
            msStereo = modeExtension.getSecond();
            bands = modeExtension.getThird();
        }
        boolean copyrighted = (headerInfo3 & 0b00001000 >> 3) == 1;
        boolean original = (headerInfo3 & 0b00000100 >> 2) == 1; // Original or Copy
        int emphasisNumber = headerInfo3 & 0b00000011;
        Emphasis emphasis = switch(emphasisNumber) { // Type of de-emphasis that shall be used
            case 0 -> Emphasis.NONE;
            case 1 -> Emphasis.MICROSECONDS_50_15; 
            case 2 -> Emphasis.RESERVED; 
            case 3 -> Emphasis.CCITT_J_17;
            default -> throw new StreamCorruptedException("MP3 Header Invalid Emphasis " + emphasisNumber);
        };
        MP3Header header = new MP3Header(version, layer, errorProtection, bitrate, frequency, 
        padded, mode, intensityStereo, msStereo, bands, copyrighted, original, emphasis);
        return header;
    }

    /**
     * Takes an input index number and returns the corresponding value in the bitrate table based on MPEG version and layer.
     * @param version MPEG Version
     * @param layer MPEG Layer
     * @param index 4 bit index from MP3 Header
     * @return bitrate
     * @throws StreamCorruptedException
     */
    private int bitrateTable(MPEGVersion version, Layer layer, int index) throws StreamCorruptedException {
        return switch(version) {
            case MPEG_1: yield switch(layer) {
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
            };
            case MPEG_2:
            case MPEG_2_5: yield switch(layer) {
                case LAYER1: yield switch(index) {
                    case 0 -> 0;
                    case 1 -> 32;
                    case 2 -> 48;
                    case 3 -> 56;
                    case 4 -> 64;
                    case 5 -> 80;
                    case 6 -> 96;
                    case 7 -> 112;
                    case 8 -> 128;
                    case 9 -> 144;
                    case 10 -> 160;
                    case 11 -> 176;
                    case 12 -> 192;
                    case 13 -> 224;
                    case 14 -> 256;
                    default -> throw new StreamCorruptedException("MP3 Header Invalid Bitrate Index " + index);
                };
                case LAYER2:
                case LAYER3: yield switch(index) {
                    case 0 -> 0;
                    case 1 -> 8;
                    case 2 -> 16;
                    case 3 -> 24;
                    case 4 -> 32;
                    case 5 -> 40;
                    case 6 -> 48;
                    case 7 -> 56;
                    case 8 -> 64;
                    case 9 -> 80;
                    case 10 -> 96;
                    case 11 -> 112;
                    case 12 -> 128;
                    case 13 -> 144;
                    case 14 -> 160;
                    default -> throw new StreamCorruptedException("MP3 Header Invalid Bitrate Index " + index);
                };
            };
        };
        
    }

    /**
     * Takes an input index number and returns the corresponding value in the frequency table based on MPEG version.
     * @param version MPEG Version
     * @param index 2 bit index from MP3 Header
     * @return Sampling frequency
     * @throws StreamCorruptedException
     */
    private double frequencyTable(MPEGVersion version, int index) throws StreamCorruptedException {
        return switch(version) {
            case MPEG_1: yield switch(index) {
                case 0 -> 44.1;
                case 1 -> 48;
                case 2 -> 32;
                case 3 -> 0;
                default -> throw new StreamCorruptedException("MP3 Header Invalid Frequency Index " + index);
            };
            case MPEG_2: yield switch(index) {
                case 0 -> 22.05;
                case 1 -> 24;
                case 2 -> 16;
                case 3 -> 0;
                default -> throw new StreamCorruptedException("MP3 Header Invalid Frequency Index " + index);
            };
            case MPEG_2_5: yield switch(index) {
                case 0 -> 11.025;
                case 1 -> 12;
                case 2 -> 8;
                case 3 -> 0;
                default -> throw new StreamCorruptedException("MP3 Header Invalid Frequency Index " + index);
            };
        };
    }

    /**
     * Used when the channel mode is JOINT_STEREO to find out what the bands to use for Layer 1 & 2 or what combination of 
     * intensity/ms stereo to use for Layer 3.
     * @param layer MPEG Layer
     * @param index 2 bit index from MP3 Header
     * @return A datatype that holds the whether or not to use intensity stereo, ms stereo and the bands to use for Layer 1 or 2
     * @throws StreamCorruptedException
     */
    private Triple<Boolean, Boolean, JointStereoBands> configureModeExtension(Layer layer, int index) throws StreamCorruptedException {
        boolean intensityStereo = false;
        boolean msStereo = false;
        JointStereoBands bands = JointStereoBands.BAND_4_31;
        if (layer == Layer.LAYER1 || layer == Layer.LAYER2) {
            intensityStereo = true;
            msStereo = false;
            bands = switch(index) {
                case 0 -> JointStereoBands.BAND_4_31;
                case 1 -> JointStereoBands.BAND_8_31;
                case 2 -> JointStereoBands.BAND_12_31;
                case 3 -> JointStereoBands.BAND_16_31;
                default -> throw new StreamCorruptedException("MP3 Header Invalid Mode Extension " + index);
            };
        } 
        else {
            switch(index) {
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
        return new Triple<Boolean,Boolean, JointStereoBands>(intensityStereo, msStereo, bands);
    }    

    /**
     * Used to grab the CRC after the MP3 Header if error protection is set.
     * @param stream
     * @return The crc that was read
     * @throws IOException
     */
    private short readCRC(FileInputStream stream) throws IOException {
        return ByteBuffer.wrap(stream.readNBytes(2)).getShort();
    }
    
}
