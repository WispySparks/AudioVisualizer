package audiovisualizer.util;

public class BitBuffer {
    
    private String buffer = "";
    
    public BitBuffer(byte[] bytes, boolean signed) {
        if (bytes == null) throw new IllegalArgumentException("Array is null.");
        for (int i = 0; i < bytes.length; i++) {
            int value = signed ? bytes[i] : bytes[i] & 0xff;
            String binary = Integer.toBinaryString(value);
            buffer = buffer.concat(String.format("%8s", binary).replace(' ', '0'));
        }
    }

    public int readNBits(int len) {
        if (len > buffer.length()) throw new IllegalArgumentException("BufferUnderflowException: Requested " + len + " bits is out of bounds for buffer length: " + buffer.length());
        if (len < 0) throw new IllegalArgumentException("len < 0");
        String bits = buffer.substring(0, len);
        buffer = buffer.substring(len);
        return Integer.parseInt(bits, 2);
    }

    public int readNBytes(int len) {
        return readNBits(len*8);
    }

    public int getBufferLength() {
        return buffer.length();
    }

}
