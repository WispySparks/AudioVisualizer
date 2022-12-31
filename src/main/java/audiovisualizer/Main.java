package audiovisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import audiovisualizer.gui.MainChart;
import audiovisualizer.mp3.decoding.MP3Decoder;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        MP3Decoder decoder = new MP3Decoder();
        // System.out.println(samples.get(0).size()/wav.sampleRate()); // returns 18 - audio file is 18 seconds, LETS GOO!!
        // MPEG Version 1 Audio Layer 3
        // File mp3 = new File("C:\\Users\\wispy\\Music\\Music\\SMW Star Road Rip.mp3");
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
        launch(args);
    }

    MainChart chart;

    @Override
    public void start(Stage primaryStage) throws Exception {
        NumberAxis xAxis = new NumberAxis(); 
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time/s");
        xAxis.setAnimated(false); 
        yAxis.setLabel("Value");
        yAxis.setAnimated(false); 
        chart = new MainChart(xAxis, yAxis);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Data Series");
        chart.getData().add(series);
        Scene scene = new Scene(chart); 
        scene.setFill(Color.grayRgb(40));
        primaryStage.setScene(scene);
        primaryStage.setTitle("Audio Visualizer");
        primaryStage.setMaximized(true);
        primaryStage.show();
        
        
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        chart.scheduledExecutorService.shutdownNow();
    }
    
}
