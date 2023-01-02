package audiovisualizer.gui;

import java.io.File;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class TopPane extends GridPane {

    private final Stage stage;
    private final MainChart chart;  
    private final FileChooser chooser = new FileChooser();
    
    public TopPane(Stage stage, MainChart chart) {
        super();
        this.stage = stage;
        this.chart = chart;
        chooser.getExtensionFilters().add(new ExtensionFilter("Audio Files", "*.MP3", "*.WAV"));
        setup();
    }

    private void setup() {
        Button selectFile = new Button("Select Audio File");
        Button startButton = new Button("Start");
        startButton.setOnAction((e) -> {
            chart.start();
        });
        selectFile.setOnAction((e) -> {
            File file;
            if ((file = chooser.showOpenDialog(stage)) != null ) {
                chart.setFile(file);
            }
        });
        add(startButton, 0, 0);
        add(selectFile, 1, 0);
    }

}
