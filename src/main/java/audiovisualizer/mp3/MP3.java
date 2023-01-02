package audiovisualizer.mp3;

import java.io.File;
import java.util.List;

public record MP3(

    File file,
    List<MP3Frame> frames
    
) {
    
}
