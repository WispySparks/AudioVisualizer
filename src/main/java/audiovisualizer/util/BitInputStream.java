package audiovisualizer.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitInputStream extends FilterInputStream {

    private long bitBuffer = 0L;
    /**Bit Buffer Length in Bits*/
    private int bufferLength = 0;

    public BitInputStream(InputStream in) {
        super(in);
    }
    
    public int readNBits(int len) throws IOException {
        if (len > 64) throw new IllegalArgumentException("whaddya think this is, a charity?");
        if (len <= 0) return 0;
        int bytesNeeded = len/8;
        if (len % 8 != 0) bytesNeeded++;
        if (bytesNeeded*8 > bufferLength) { // If we need more bits than are currently in buffer load some more.
            int readAmount = bytesNeeded - bufferLength/8; // Figure out how many bytes we already have in the buffer to load minimum
            for (int i = 0; i < readAmount; i++) {
                System.out.println("call! " + len);
                int num = in.read();
                if (num == -1) return -1;
                bitBuffer = bitBuffer << 8; // make space for new byte
                bitBuffer = bitBuffer | num; // set new byte
            }
        }
        bufferLength += bytesNeeded*8; // add the bytes we read to length
        int val = (int) (bitBuffer >>> bufferLength-len); // grab the return value 
        // Fix the buffer to get rid of all the bits we read
        int shiftValue = 64 - (bufferLength-len);
        bitBuffer = bitBuffer << shiftValue;
        bitBuffer = bitBuffer >>> shiftValue;
        if (shiftValue == 64) bitBuffer = 0;
        bufferLength -= len; // subtract the bits we used in this call from buffer length
        return val;
    }

    @Override
    public int read() throws IOException {
        return readNBits(8);
    }

}
