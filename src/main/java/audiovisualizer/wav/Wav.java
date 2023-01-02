package audiovisualizer.wav;

import java.io.File;

public record WAV(

    File file,
    short audioFormat,
    short numChannels,
    int sampleRate,
    int byteRate,
    short blockAlign, 
    int bitsPerSample,
    byte[] data

) {
    
}
