package edu.opjms.resultView.main;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;

import java.net.URL;
import java.util.ResourceBundle;

public class listViewController implements Initializable {

    @FXML
    private PieChart houseBoyChart;
    @FXML
    private PieChart houseGirlChart;
    @FXML
    private PieChart sportsBoyChart;
    @FXML
    private PieChart sportGirlChart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        houseBoyChart.getData().addAll(new PieChart.Data("Manan", 10),
                new PieChart.Data("Rishit", 7),
                new PieChart.Data("Mandeep", 4));
        houseGirlChart.getData().addAll(new PieChart.Data("Kashish", 30),
                new PieChart.Data("Tanya", 12),
                new PieChart.Data("Archie", 1));
        sportsBoyChart.getData().addAll(new PieChart.Data("Divyam", 4),
                new PieChart.Data("Bhavyam", 8),
                new PieChart.Data("Ishit", 0));
        sportGirlChart.getData().addAll(new PieChart.Data("I ran out of Names",5),
                new PieChart.Data("Someone", 12),
                new PieChart.Data("Someone else", 2));
    }
}
