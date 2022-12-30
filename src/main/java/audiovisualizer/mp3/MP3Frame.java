package audiovisualizer.mp3;

public record MP3Frame(
    MP3Header header,
    short crcToCheck, // -1 if there isn't error protection for this frame.
    boolean passedCrc, // Always true if there isn't error protection for this frame.
    int[][][] pcm,
    byte[] ancillaryData
) {
    
}
