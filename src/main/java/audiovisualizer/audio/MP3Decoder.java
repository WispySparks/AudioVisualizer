package audiovisualizer.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MP3Decoder {
    
    public void decode(File file) {
        try {
            func(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void func(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        System.out.println(inputStream.read());
        inputStream.close();
    }

}
