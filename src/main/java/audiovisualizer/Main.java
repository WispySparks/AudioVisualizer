package audiovisualizer;

import java.io.File;

import audiovisualizer.audio.MP3Decoder;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        MP3Decoder decoder = new MP3Decoder();
        // MPEG Version 1 Audio Layer 3
        File mp3 = new File("C:\\Users\\wispy\\Music\\Music\\SMW Star Road Rip.mp3");
        // File mp3 = new File("C:\\Users\\wispy\\Music\\Music\\RegularShowCredits.mp3");
        // File mp3 = new File("C:\\Users\\wispy\\Music\\Music\\World 8 Bowser Theme.mp3");
        decoder.decode(mp3);
    }
    
}
