package edu.opjms.candidateSelector.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.File;

public class Main extends Application {

    public static JMetro j;
    static public Scene scene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        var loader = new FXMLLoader(getClass().getResource("/fxml/listViewer.fxml"));
//        var loader = new FXMLLoader(getClass().getResource("/fxml/resultView.fxml"));
        Parent root = loader.load();

        scene = new Scene(root);

        scene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.F11))
                stage.setFullScreen(!stage.isFullScreen());
        });
        j = new JMetro(Style.LIGHT);
        j.setScene(scene);
        
        stage.setTitle("Candidate List Editor");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/app_icon.png")));
        stage.show();
        var controller = (CandidateListController) loader.getController();
        controller.setStage(stage);
        if (!getParameters().getRaw().isEmpty())
            controller.loadFile(new File(getParameters().getRaw().get(0)));
    }

    @Override
    public void stop() {
    }
}
