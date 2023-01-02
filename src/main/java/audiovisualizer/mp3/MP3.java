package audiovisualizer.mp3;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

public record MP3(

    File file,
    List<MP3Frame> frames
    
) implements Supplier<File> {
    public File get() {
        return file;
    }
}