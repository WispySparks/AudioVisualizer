package audiovisualizer.util;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class BitInputStream extends BufferedInputStream {
    
    public BitInputStream(InputStream stream) {
        super(stream);
    }

    public BitInputStream(InputStream stream, int size) {
        super(stream, size);
    }

    public int readNBits(int len) {
        int bytesNeeded = len/8 + 1;
        if (len % 8 == 0) bytesNeeded--;
        return bytesNeeded;
    }

}
