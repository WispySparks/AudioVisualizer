package audiovisualizer;

import java.io.File;

import audiovisualizer.decoding.MP3Decoder;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

@SuppressWarnings("unused")
public class Main {

    private static Media media;
    private static MediaPlayer player;

    public static void main(String[] args) {
        System.out.println("Hello World!");
        MP3Decoder decoder = new MP3Decoder();
        // MPEG Version 1 Audio Layer 3
        File mp3 = new File("C:\\Users\\wispy\\Music\\Music\\SMW Star Road Rip.mp3");
        // File mp3 = new File("C:\\Users\\wispy\\Music\\Music\\RegularShowCredits.mp3");
        // File mp3 = new File("C:\\Users\\wispy\\Music\\Music\\World 8 Bowser Theme.mp3");
        // File mp3 = new File("C:\\Users\\wispy\\Music\\Music\\Madness8.mp3");
        decoder.decode(mp3);
        // Play the audio file
        // Platform.startup(() -> {
        //     media = new Media(mp3.toURI().toString());
        //     player = new MediaPlayer(media);
        //     player.play();
        // });
    }
    
}
