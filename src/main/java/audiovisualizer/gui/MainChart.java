package audiovisualizer.gui;

import java.io.File;

import audiovisualizer.AudioMagician;
import audiovisualizer.wav.WAV;
import audiovisualizer.wav.WavDecoder;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

public class MainChart implements Runnable {
    
    private final AudioMagician magician = new AudioMagician();
    private LineChart<Number, Number> chart;
    private double[] samples;
    private int count = 0;

    public MainChart() {
        setupChart();
        WavDecoder wavDecoder = new WavDecoder();
        WAV wav = wavDecoder.decode(new File("C:\\Users\\wispy\\Music\\Music\\chromemusicsong.wav"));
        samples = magician.getAudioVolume(wav.data(), wav.sampleRate(), wav.bitsPerSample(), wav.numChannels());
        // AudioPlayer player = new AudioPlayer();
        // player.playPCMData(wav.data(), wav.sampleRate(), wav.bitsPerSample(), wav.numChannels(), this);
    }

    private void setupChart() {
        NumberAxis xAxis = new NumberAxis(); 
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Sample");
        xAxis.setAnimated(false); 
        yAxis.setLabel("Amplitude");
        yAxis.setAnimated(false); 
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("PCM Graph");
        chart.setAnimated(false);
        chart.setStyle("-fx-background-color: lightgray");
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        chart.getData().add(series);
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            Series<Number, Number> dataSeries = chart.getData().get(0);
            int x = count*400; // 400 ms window ??
            if (count < samples.length) {
                dataSeries.getData().add(new XYChart.Data<Number, Number>(x, samples[count]));
            }
            count++;
        });
    }

    public LineChart<Number, Number> getChart() {
        return chart;
    }

}
