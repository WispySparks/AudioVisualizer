package audiovisualizer.gui;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import audiovisualizer.AudioMagician;
import audiovisualizer.AudioPlayer;
import audiovisualizer.wav.WAV;
import audiovisualizer.wav.WavDecoder;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

public class MainChart {
    
    private final AudioMagician magician = new AudioMagician();
    public final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private LineChart<Number, Number> chart;
    private double[] samples = new double[0];
    private int count = 0;
    private AudioPlayer player;

    public MainChart(AudioPlayer player) {
        setupChart();
        this.player = player;
    }

    public void start() {
        WavDecoder wavDecoder = new WavDecoder();
        WAV wav = wavDecoder.decode(new File("C:\\Users\\wispy\\Downloads\\SMW Star Road Rip.wav"));
        samples = magician.getAudioVolume(wav.data(), wav.sampleRate(), wav.bitsPerSample(), wav.numChannels());
        clearData();
        player.playAudioFile(wav.file());
    }

    private void setupChart() {
        NumberAxis xAxis = new NumberAxis(); 
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time/s");
        xAxis.setAnimated(false); 
        yAxis.setLabel("Volume/dB");
        yAxis.setAnimated(false); 
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Audio Graph");
        chart.setAnimated(false);
        chart.setStyle("-fx-background-color: lightgray");
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        chart.getData().add(series);
        service.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                if (count < samples.length) {
                    Series<Number, Number> dataSeries = chart.getData().get(0);
                    double x = count*0.4; // 400 ms window ??
                    dataSeries.getData().add(new XYChart.Data<Number, Number>(x, samples[count]));
                    count++;
                }
            });
        }, 0, 400, TimeUnit.MILLISECONDS);
    }
    
    public LineChart<Number, Number> getChart() {
        return chart;
    }

    private void clearData() {
        chart.getData().get(0).getData().clear();
        count = 0;
    }

}
