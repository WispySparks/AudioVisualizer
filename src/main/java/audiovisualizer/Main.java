package audiovisualizer;

import audiovisualizer.gui.MainChart;
import audiovisualizer.gui.TopPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    private MainChart chart = new MainChart();
    
    public static void main(String[] args) {
        launch(args);
        
    }

    @Override
    public void start(Stage stage) throws Exception {
        BorderPane pane = new BorderPane();
        pane.setCenter(chart.getChart());
        pane.setTop(new TopPane(stage, chart));
        Scene scene = new Scene(pane); 
        stage.setScene(scene);
        stage.setTitle("Audio Visualizer");
        stage.setMaximized(true);
        stage.show();
        // MP3Decoder decoder = new MP3Decoder();
        // MPEG Version 1 Audio Layer 3
        // File mp3 = new File("C:\\Users\\wispy\\Music\\Music\\SMW Star Road Rip.mp3");
        // List<File> files = new ArrayList<>();
        // files.add(new File("C:\\Users\\wispy\\ProgrammingProjects\\CPP\\out.bin"));
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
        // files.forEach((file) -> decoder.decode(file));
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        chart.service.shutdownNow();
    }

}
