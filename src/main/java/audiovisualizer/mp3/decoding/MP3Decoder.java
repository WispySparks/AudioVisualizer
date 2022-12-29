package audiovisualizer.mp3.decoding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import audiovisualizer.mp3.MP3;
import audiovisualizer.mp3.MP3Frame;
import audiovisualizer.mp3.MP3Header;
import audiovisualizer.mp3.MP3Header.Layer;
import audiovisualizer.mp3.MP3Header.Mode;
import audiovisualizer.util.BitBuffer;

public class MP3Decoder { 

    private HeaderDecoder headerDecoder = new HeaderDecoder();
    List<MP3Frame> frames = new ArrayList<>();
    MP3Header currentHeader;
    short crc = -1;

    public MP3 decode(File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            if (checkForMetadata(stream)) {
                readMetadata(stream);
            }
            while (containsSyncWord(stream)) {
                readFrame(stream);
            }
            stream.close();
            return new MP3(frames);
        } catch (IOException e) {
            System.out.println("MP3 decoding error with file " + file.getAbsolutePath()); 
            e.printStackTrace();
        }
        return new MP3(frames);
    }

    private void readFrame(FileInputStream stream) throws IOException {
        try {
            currentHeader = headerDecoder.readFrameHeader(stream);
        } catch (StringIndexOutOfBoundsException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("MP3 Header Invalid")) {
                // return; // If this frame has a messed up header ideally we should skip it.
            }
        }
        if (currentHeader.errorProtection()) {
            crc = readCRC(stream);
        }
        int frameLength = getFrameLength();
        // System.out.println(currentHeader);
        // System.out.println(stream.getChannel().position());
        // System.out.println(frameLength);
        switch(currentHeader.layer()) {
            case LAYER1 -> readAudioDataLayer1(stream, frameLength);
            case LAYER2 -> readAudioDataLayer2(stream, frameLength);
            case LAYER3 -> readAudioDataLayer3(stream);
        }
        frames.add(new MP3Frame(currentHeader, crc, true, null, null));
    }

    /**
     * Looks for a sync word until it hits EOF. 
     * If it does find a syncword then it goes back 1 byte so that it can be used in the header.
     * @param stream
     * @return the second byte used for the syncword since it contains header information
     * @throws IOException
     */
    private boolean containsSyncWord(FileInputStream stream) throws IOException {
        while (true) {
            int syncword1 = stream.read();
            int syncword2 = stream.read();
            if (syncword1 == -1 && syncword2 == -1) return false;
            // Confirm that the sync word is 11 '1' bits and that the version and or layer is not reserved.
            stream.skip(-1);
            if (syncword1 != 0xFF || (syncword2 >> 5) != 7 || (syncword2 & 0b00011000 >> 3) == 1 || (syncword2 & 0b00000110 >> 1) == 0) {
                continue;
            }
            return true;
        }        
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

    /**
     * {@link} http://www.diva-portal.org/smash/get/diva2:830195/FULLTEXT01.pdf 
     * @return Frame Length in Bytes.
     */
    private int getFrameLength() {
        double frameLengthBytes = 0;
        if (currentHeader.layer() == Layer.LAYER1) {
            frameLengthBytes = (12 * currentHeader.bitrate()/currentHeader.samplingFrequency());
            if (currentHeader.padded()) frameLengthBytes += 4; // Add an extra slot cause padded bit is set in header.
        } else {
            frameLengthBytes = (144 * currentHeader.bitrate()/currentHeader.samplingFrequency());
            if (currentHeader.padded()) frameLengthBytes++; // Add an extra slot cause padded bit is set in header.
        }
        return (int) frameLengthBytes;
    }

    /**
     * {@link} https://www.iso.org/standard/22412.html Section 2.4.1.5
     * @param stream
     * @throws IOException
     */
    private void readAudioDataLayer1(FileInputStream stream, int length) throws IOException {
        BitBuffer buffer = new BitBuffer(stream.readNBytes(length), false);
        int maxChannels = currentHeader.mode() == Mode.SINGLE_CHANNEL ? 1 : 2;
        int[][] bitAllocation = new int[maxChannels][32]; // Channel, Sub-band
        int[][] scaleFactor = new int[maxChannels][32]; // Channel, Sub-band
        int[][][] sample = new int[maxChannels][32][12]; // Channel, Sub-band, uh idk 
        int bound = currentHeader.mode() == Mode.JOINT_STEREO ? (currentHeader.modeExtensionNumber()+1)*4 : 32;
        for (int subBand = 0; subBand < bound; subBand++) {
            for (int channel = 0; channel < maxChannels; channel++) {
                bitAllocation[channel][subBand] = buffer.readNBits(4);
            }
        }
        for (int subBand = bound; subBand < 32; subBand++) {
            bitAllocation[0][subBand] = buffer.readNBits(4);
            if (maxChannels > 1) {
                bitAllocation[1][subBand] = bitAllocation[0][subBand];
            }
        }
        for (int subBand = 0; subBand < 32; subBand++) {
            for (int channel = 0; channel < maxChannels; channel++) {
                if (bitAllocation[channel][subBand] != 0) {
                    scaleFactor[channel][subBand] = buffer.readNBits(6);
                }
            }
        }
        for (int s = 0; s < 12; s++) {
            for (int subBand = 0; subBand < bound; subBand++) {
                for (int channel = 0; channel < maxChannels; channel++) {
                    if (bitAllocation[channel][subBand] != 0) {
                        sample[channel][subBand][s] = buffer.readNBits(bitAllocation[channel][subBand]+1);
                    }
                }
            }
            for (int subBand = bound; subBand < 32; subBand++) {
                if (bitAllocation[0][subBand] != 0) {
                    sample[0][subBand][s] =  buffer.readNBits(bitAllocation[0][subBand]+1);
                }
            }
        }
    }

    private void readAudioDataLayer2(FileInputStream stream, int length) throws IOException {
    }

    private void readAudioDataLayer3(FileInputStream stream) throws IOException {
    }

    /**
     * Checks for ID3v2 metadata. <p>
     * {@link} https://id3.org/id3v2.4.0-structure
     */
    private boolean checkForMetadata(FileInputStream stream) throws IOException {
        String id3 = "ID3";
        byte[] bytes = stream.readNBytes(3);
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != id3.charAt(i)) {
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
    
}
