package audiovisualizer.gui;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import audiovisualizer.AudioPlayer;
import audiovisualizer.wav.WAV;
import audiovisualizer.wav.WavDecoder;
import javafx.application.Platform;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

public class MainChart extends LineChart<Number, Number> {
    
    static int count = 0;
    private final int MAX_SIZE = Integer.MAX_VALUE;
    public ScheduledExecutorService scheduledExecutorService;

    public MainChart(Axis<Number> xAxis, Axis<Number> yAxis) {
        super(xAxis, yAxis);
        setTitle("Realtime JavaFX Charts");
        setAnimated(false); // disable animations

        WavDecoder wavDecoder = new WavDecoder();
        WAV wav = wavDecoder.decode(new File("C:\\Users\\wispy\\Music\\Music\\chromemusicsong.wav"));
        AudioPlayer player = new AudioPlayer();
        List<List<Long>> samples = player.parsePCMData(wav.data(), wav.bitsPerSample(), wav.numChannels());
        System.out.println(samples.get(0).size());
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        player.playPCMData(wav.data(), wav.sampleRate(), wav.bitsPerSample(), wav.numChannels(), 1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                Series<Number, Number> series = getData().get(0);
                if (series.getData().size() > MAX_SIZE) {
                    series.getData().remove(0);
                }
                series.getData().add(new XYChart.Data<Number, Number>(count, samples.get(0).get(count)));
                count++;
            });
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

}
