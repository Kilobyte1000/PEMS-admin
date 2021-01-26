package edu.opjms.resultView.main;

//import com.jfoenix.controls.JFXMasonryPane;

import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.FlowPane;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class listViewController implements Initializable {


    public FlowPane a;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        BarChart<Number, String> barChart = new BarChart<>(new NumberAxis(), new CategoryAxis());
        a.getChildren().add(barChart);
        List<XYChart.Data<Number, String>> b = List.of(new XYChart.Data<>(2, "Keshav"),
                new XYChart.Data<>(3,"Aditya"),
                new XYChart.Data<>(5,"Ojaswi"));

        var c = FXCollections.observableArrayList(b);
        barChart.getData().add(new XYChart.Series<>(
                c
        ));

        a.getChildren().addAll(new BarChart<>(new NumberAxis(), new CategoryAxis()),
                new BarChart<>(new NumberAxis(), new CategoryAxis()),
                new BarChart<>(new NumberAxis(), new CategoryAxis()));

        new JMetro(a.getParent(), Style.DARK);

    }
}
