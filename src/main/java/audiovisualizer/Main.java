package audiovisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import audiovisualizer.mp3.decoding.MP3Decoder;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

@SuppressWarnings("unused")
public class Main {

    private static Media media;
    private static MediaPlayer player;
    public static void main(String[] args) {
        MP3Decoder decoder = new MP3Decoder();
        // WavDecoder wavDecoder = new WavDecoder();
        // WAV wav = wavDecoder.decode(new File("C:\\Users\\wispy\\Music\\Music\\chromemusicsong.wav"));
        // AudioPlayer player = new AudioPlayer();
        // player.playPCMData(wav.data(), wav.sampleRate(), wav.bitsPerSample(), wav.numChannels(), 1);
        // List<List<Long>> samples = player.parsePCMData(wav.data(), wav.bitsPerSample(), wav.numChannels());
        // System.out.println(samples.get(0).size()/wav.sampleRate()); // returns 18 - audio file is 18 seconds, LETS GOO!!
        // MPEG Version 1 Audio Layer 3
        File mp3 = new File("C:\\Users\\wispy\\Music\\Music\\SMW Star Road Rip.mp3");
        List<File> files = new ArrayList<>();
        files.add(new File("C:\\Users\\wispy\\ProgrammingProjects\\CPP\\out.bin"));
        /*
            Layer           I
            Fs              32 kHz
            bit rate        384 kbit/s
            CRC             yes
            mode            intensity stereo
            signal          -20 dB sweep in left and right
            #frames         49
         */
        // files.add(new File("C:\\Users\\wispy\\Music\\Music\\SMW Star Road Rip.mp3"));
        // files.add(new File("C:\\Users\\wispy\\Music\\Music\\RegularShowCredits.mp3"));
        // files.add(new File("C:\\Users\\wispy\\Music\\Music\\World 8 Bowser Theme.mp3"));
        // files.add(new File("C:\\Users\\wispy\\Music\\Music\\Madness8.mp3"));
        files.forEach((file) -> decoder.decode(file));
        // Play the audio file
        // Platform.startup(() -> {
        //     media = new Media(mp3.toURI().toString());
        //     player = new MediaPlayer(media);
        //     player.play();
        // });
    }
    
}
