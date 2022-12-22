package audiovisualizer.decoding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MP3Decoder {
    
    public void decode(File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            System.out.println(checkForMetadata(stream));
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkForMetadata(FileInputStream stream) throws IOException {
        String id3 = "ID3";
        byte[] bytes = stream.readNBytes(3);
        for (int i = 0; i < bytes.length; i++) {
            if (AsciiTable.convert(bytes[i]) != id3.charAt(i)) return false;
        }
        return true;
    }

}
