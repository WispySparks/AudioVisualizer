package audiovisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import audiovisualizer.mp3.MP3Decoder;
import audiovisualizer.wav.WavDecoder;
import audiovisualizer.wav.WavFile;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

@SuppressWarnings("unused")
public class Main {

    private static Media media;
    private static MediaPlayer player;

    public static void main(String[] args) {
        System.out.println("Hello World!");
        MP3Decoder decoder = new MP3Decoder();
        WavDecoder wavDecoder = new WavDecoder();
        WavFile wav = wavDecoder.decode(new File("C:\\Users\\wispy\\Music\\Music\\chromemusicsong.wav"));
        AudioPlayer player = new AudioPlayer();
        // player.playPCMData(wav.data(), wav.sampleRate(), wav.bitsPerSample(), wav.numChannels(), 1);
        // MPEG Version 1 Audio Layer 3
        File mp3 = new File("C:\\Users\\wispy\\Music\\Music\\SMW Star Road Rip.mp3");
        List<File> files = new ArrayList<>();
        files.add(new File("C:\\Users\\wispy\\Music\\Music\\SMW Star Road Rip.mp3"));
        files.add(new File("C:\\Users\\wispy\\Music\\Music\\RegularShowCredits.mp3"));
        files.add(new File("C:\\Users\\wispy\\Music\\Music\\World 8 Bowser Theme.mp3"));
        files.add(new File("C:\\Users\\wispy\\Music\\Music\\Madness8.mp3"));
        files.forEach((file) -> decoder.decode(file));
        // Play the audio file
        // Platform.startup(() -> {
        //     media = new Media(mp3.toURI().toString());
        //     player = new MediaPlayer(media);
        //     player.play();
        // });
    }
    
}
