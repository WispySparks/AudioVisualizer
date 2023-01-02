package audiovisualizer.gui;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class TopPane extends GridPane {

    private final MainChart chart;
    
    public TopPane(MainChart chart) {
        super();
        this.chart = chart;
        setup();
    }

    private void setup() {
        Button button = new Button("Start");
        button.setOnAction((event) -> {
            chart.start();
        });
        add(button, 0, 0);
    }

}
