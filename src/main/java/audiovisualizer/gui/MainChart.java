package audiovisualizer.gui;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import audiovisualizer.audio.AudioMagician;
import audiovisualizer.audio.AudioPlayer;
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
    private final AudioPlayer player = new AudioPlayer();
    private Supplier<File> currentFile;
    private LineChart<Number, Number> chart;
    private double[] samples = new double[0];
    private int index = 0;
    private boolean started = false;

    public MainChart() {
        setupChart();
    }

    public void start() {
        clearData();
        player.playAudioFile(currentFile.get());
        started = true;
    }

    public void setFile(File file) {
        started = false;
        clearData();
        if (getFileExtension(file).equals("wav")) {
            WAV wav = new WavDecoder().decode(file);
            samples = magician.getAudioVolume(wav.data(), wav.sampleRate(), wav.bitsPerSample(), wav.numChannels());
            currentFile = wav;
        } else if (getFileExtension(file).equals("mp3")) {

        }
        else throw new IllegalArgumentException("Invalid File.");
    }

    private void setupChart() {
        NumberAxis xAxis = new NumberAxis(); 
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time/s");
        xAxis.setAnimated(false); 
        yAxis.setLabel("Relative Volume");
        yAxis.setAnimated(false); 
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Audio Graph");
        chart.setAnimated(true);
        chart.setStyle("-fx-background-color: lightgray");
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        chart.getData().add(series);
        service.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                if (index < samples.length && started) {
                    Series<Number, Number> dataSeries = chart.getData().get(0);
                    double x = index*AudioMagician.windowSeconds; // 400 ms window ??
                    dataSeries.getData().add(new XYChart.Data<Number, Number>(x, samples[index]));
                    index++;
                }
            });
        }, 0, (long) (AudioMagician.windowSeconds*1000), TimeUnit.MILLISECONDS);
    }
    
    public LineChart<Number, Number> getChart() {
        return chart;
    }

    private void clearData() {
        chart.getData().get(0).getData().clear();
        index = 0;
        player.stopMediaPlayers();
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        if (lastIndex == -1) {
            return "";
        }
        return name.substring(lastIndex+1).toLowerCase();
    }

}
