package audiovisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AudioPlayer {

    private List<MediaPlayer> players = new ArrayList<>();

    /**
     * Plays PCM audio in a new thread.
     * @param PCM data
     * @param sampleRate number of samples per second
     * @param bitsPerSample bits for each value of a sample
     * @param numChannels number of channels pcm data is broken up into. 1 = mono, ect.
     * @param loopAmount number of times you want audio to play
     */
    public void playPCMData(byte[] PCM, int sampleRate, int bitsPerSample, int numChannels) {
        try {
            ExecutorService service = Executors.newSingleThreadExecutor();
            AudioFormat audioFormat = new AudioFormat(sampleRate, bitsPerSample, numChannels, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat);
            line.open(audioFormat);
            line.start();
            service.execute(() -> {
                line.write(PCM, 0, PCM.length);
                line.drain();
                line.stop();
                line.close();
            });
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void playAudioFile(File file) {
        Media media = new Media(file.toURI().toString());
        MediaPlayer player = new MediaPlayer(media);
        players.add(player);
        player.play();
    }

    public void stopMediaPlayers() {
        for (MediaPlayer mediaPlayer : players) {
            mediaPlayer.stop();
        }
    }

}
