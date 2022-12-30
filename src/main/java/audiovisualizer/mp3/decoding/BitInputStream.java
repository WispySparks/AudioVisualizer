package audiovisualizer.mp3.decoding;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitInputStream extends FilterInputStream {

    private long bitBuffer = 0L;
    /**Bit Buffer Length in Bits*/
    private int bufferLength = 0;
    /**Current Position in Stream*/
    private int pos = 0;

    public BitInputStream(InputStream in) {
        super(in);
    }

    /**
     * Brings you to a byte boundary.
     * @return Whether or not you were already on a byte boundary.
     * @throws IOException
     */
    public boolean assertByteBoundary() throws IOException {
        readNBits(bufferLength%8);
        return bufferLength % 8 == 0;
    }

    public int getStreamPosition() {
        return pos;
    }

    private void clearBuffer() {
        bitBuffer = 0L;
    }
    
    public int readNBits(int len) throws IOException {
        if (len > 64) throw new IllegalArgumentException("whaddya think this is, a charity?");
        if (len <= 0) return 0;
        int bytesNeeded = len/8;
        if (len % 8 != 0) bytesNeeded++;
        if (len > bufferLength) { // If we need more bits than are currently in buffer load some more.
            int readAmount = bytesNeeded - bufferLength/8; // Figure out how many bytes we already have in the buffer to load minimum
            for (int i = 0; i < readAmount; i++) {
                int num = in.read();
                if (num == -1) return -1;
                pos++;
                bitBuffer = bitBuffer << 8; // make space for new byte
                bitBuffer = bitBuffer | num; // set new byte
            }
            bufferLength += bytesNeeded*8; // add the bytes we read to length
        }
        int val = (int) (bitBuffer >>> bufferLength-len); // grab the return value 
        // Fix the buffer to get rid of all the bits we read
        int shiftValue = 64 - (bufferLength-len);
        bitBuffer = bitBuffer << shiftValue;
        bitBuffer = bitBuffer >>> shiftValue;
        if (shiftValue == 64) bitBuffer = 0; // java doesn't let you shift 64 bits but this means it's zero
        bufferLength -= len; // subtract the bits we used in this call from buffer length
        return val;
    }

    /**
     * Reads the next byte of data from this input stream. The value
     * byte is returned as an {@code int} in the range
     * {@code 0} to {@code 255}. If no byte is available
     * because the end of the stream has been reached, the value
     * {@code -1} is returned. This method blocks until input data
     * is available, the end of the stream is detected, or an exception
     * is thrown.
     * <p>
     *
     * @return     the next byte of data, or {@code -1} if the end of the
     *             stream is reached.
     * @throws     IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    @Override
    public int read() throws IOException {
        return readNBits(8);
    }

    /**
     * Skips over and discards {@code n} bytes of data from the
     * input stream. The {@code skip} method may, for a variety of
     * reasons, end up skipping over some smaller number of bytes,
     * possibly {@code 0}. The actual number of bytes skipped is
     * returned.
     * <p>
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @throws     IOException  if {@code in.skip(n)} throws an IOException.
     */
    @Override
    public long skip(long n) throws IOException {
        long l = super.skip(n);
        clearBuffer();
        pos += n;
        return l;
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        byte[] arr = new byte[len];
        for (int i = 0; i < len; i++) {
            arr[i] = (byte) read();
        }
        return arr;
    }

}
