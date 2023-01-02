package audiovisualizer.mp3.decoding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import audiovisualizer.mp3.MP3;
import audiovisualizer.mp3.MP3Frame;
import audiovisualizer.mp3.MP3Header;
import audiovisualizer.mp3.MP3Header.Mode;

public class MP3Decoder { 

    private HeaderDecoder headerDecoder = new HeaderDecoder();
    private List<MP3Frame> frames = new ArrayList<>();
    private MP3Header currentHeader;
    private short crc = -1;

    public MP3 decode(File file) {
        try {
            BitInputStream stream = new BitInputStream(new FileInputStream(file));
            if (checkForMetadata(stream)) {
                readMetadata(stream);
            }
            while (containsSyncWord(stream)) {
                readFrame(stream);
            }
            stream.close();
            // System.out.println(count + " frames were read");
            // System.out.println(frames.size() + " frames were stored");
            return new MP3(file, frames);
        } catch (IOException e) {
            System.out.println("MP3 decoding error with file " + file.getAbsolutePath()); 
            e.printStackTrace();
        }
        return new MP3(file, frames);
    }
    public static int count = 0;
    private void readFrame(BitInputStream stream) throws IOException {
        try {
            currentHeader = headerDecoder.readFrameHeader(stream);
        } catch (StreamCorruptedException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("MP3 Header Invalid")) {
                // count++;
                return; // If this frame has a messed up header ideally we should skip it.
            }
        }
        if (currentHeader.errorProtection()) crc = readCRC(stream);
        // System.out.println(currentHeader);
        int[][][] pcm = switch(currentHeader.layer()) {
            case LAYER1 -> readAudioDataLayer1(stream);
            case LAYER2 -> readAudioDataLayer2(stream);
            case LAYER3 -> readAudioDataLayer3(stream);
        };
        frames.add(new MP3Frame(currentHeader, crc, true, pcm, null));
    }

    /**
     * Looks for a sync word until it hits EOF at which point it returns false. 
     * If it does find a syncword then it goes back 1 byte so that it can be used in the header.
     * @param stream
     * @return the second byte used for the syncword since it contains header information
     * @throws IOException
     */
    private boolean containsSyncWord(BitInputStream stream) throws IOException {
        stream.assertByteBoundary(); // All sync words should be on byte boundaries...
        while (true) {
            int syncword1 = stream.read();
            int syncword2 = stream.read();
            if (syncword1 == -1 || syncword2 == -1) return false;
            // Confirm that the sync word is 11 '1' bits and that the version and or layer is not reserved.
            stream.skip(-1);
            if (syncword1 != 0xFF || (syncword2 >> 5) != 7 || ((syncword2 & 0b00011000) >> 3 == 1) || ((syncword2 & 0b00000110) >> 1 == 0)) {
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
    private short readCRC(BitInputStream stream) throws IOException {
        return ByteBuffer.wrap(stream.readNBytes(2)).getShort();
    }

    /**
     * {@link} https://www.iso.org/standard/22412.html Section 2.4.1.5 and Section 2.4.3.2
     * @param stream
     * @throws IOException
     */
    private int[][][] readAudioDataLayer1(BitInputStream stream) throws IOException {
        int maxChannels = currentHeader.mode() == Mode.SINGLE_CHANNEL ? 1 : 2;
        int[][] bitAllocation = new int[maxChannels][32]; // Channel, Sub-band
        int[][] scaleFactor = new int[maxChannels][32]; // Channel, Sub-band
        int[][][] sample = new int[maxChannels][32][12]; // Channel, Sub-band, Sample
        int[][][] fractionalNumbers = new int[maxChannels][32][12]; // Channel, Sub-band, Sample 
        int[][][]  requantizedSamples = new int[maxChannels][32][12]; // Channel, Sub-band, Sample 
        int bound = currentHeader.mode() == Mode.JOINT_STEREO ? (currentHeader.modeExtensionNumber()+1)*4 : 32;
        for (int subBand = 0; subBand < bound; subBand++) {
            for (int channel = 0; channel < maxChannels; channel++) {
                bitAllocation[channel][subBand] = stream.readNBits(4);
            }
        }
        for (int subBand = bound; subBand < 32; subBand++) {
            bitAllocation[0][subBand] = stream.readNBits(4);
            if (maxChannels > 1) {
                bitAllocation[1][subBand] = bitAllocation[0][subBand];
            }
        }
        for (int subBand = 0; subBand < 32; subBand++) {
            for (int channel = 0; channel < maxChannels; channel++) {
                if (bitAllocation[channel][subBand] != 0) {
                    scaleFactor[channel][subBand] = stream.readNBits(6);
                }
            }
        }
        for (int s = 0; s < 12; s++) {
            for (int subBand = 0; subBand < bound; subBand++) {
                for (int channel = 0; channel < maxChannels; channel++) {
                    if (bitAllocation[channel][subBand] != 0) {
                        sample[channel][subBand][s] = stream.readNBits(bitAllocation[channel][subBand]+1);
                    }
                }
            }
            for (int subBand = bound; subBand < 32; subBand++) {
                if (bitAllocation[0][subBand] != 0) {
                    sample[0][subBand][s] = stream.readNBits(bitAllocation[0][subBand]+1);
                }
            }
            // Requantization of subband samples 
            for (int subBand = 0; subBand < bound; subBand++) {
                for (int channel = 0; channel < maxChannels; channel++) {
                    if (bitAllocation[channel][subBand] != 0) {
                        int shiftAmount = (numBits(sample[channel][subBand][s])-1);
                        // Invert first bit
                        fractionalNumbers[channel][subBand][s] = sample[channel][subBand][s] ^ (1 << shiftAmount); 
                        // Apply formula
                        requantizedSamples[channel][subBand][s] = 
                        requantizationFormula(fractionalNumbers[channel][subBand][s], bitAllocation[channel][subBand]+1);
                        // Apply scale factor
                        requantizedSamples[channel][subBand][s] = requantizedSamples[channel][subBand][s] * scaleFactor[channel][subBand];
                    }
                }
            }
        }
        return requantizedSamples;
    }

    private int requantizationFormula(int fractionalNumber, int bitsAllocated) {
        double requantizedValue = 
        (Math.pow(2, bitsAllocated) / (Math.pow(2, bitsAllocated)-1)) * (fractionalNumber + Math.pow(2, (-1*bitsAllocated+1)));
        return (int) Math.round(requantizedValue);
    } 

    private int[][][] readAudioDataLayer2(BitInputStream stream) throws IOException {
        return null;
    }

    private int[][][] readAudioDataLayer3(BitInputStream stream) throws IOException {
        return null;
    }

    private int numBits(int i) {
        return Integer.toBinaryString(i).length();
    }

    /**
     * Checks for ID3v2 metadata. <p>
     * {@link} https://id3.org/id3v2.4.0-structure
     */
    private boolean checkForMetadata(BitInputStream stream) throws IOException {
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
    private void readMetadata(BitInputStream stream) throws IOException {
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
