package audiovisualizer.wav;

import java.io.File;
import java.util.function.Supplier;

public record WAV(

    File file,
    short audioFormat,
    short numChannels,
    int sampleRate,
    int byteRate,
    short blockAlign, 
    int bitsPerSample,
    byte[] data

) implements Supplier<File> {
    public File get() {
        return file;
    }
}
