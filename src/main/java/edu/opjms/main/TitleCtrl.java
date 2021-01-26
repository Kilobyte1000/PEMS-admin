package edu.opjms.main;

import com.jfoenix.controls.JFXDialogLayout;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class TitleCtrl implements Initializable {

    public VBox root;
    public StackPane topBar;
    public Label a;
    private Map<String, String> fileNames;

    private final FXMLLoader listLoader = new FXMLLoader(getClass().getResource("/fxml/listViewer.fxml"));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        var a = new Task<Map<String, String>>() {
            @Override
            protected Map<String, String> call() throws Exception {
                File folder = new File("D:\\photos");
                var filesInFolder = folder.listFiles();

                if (filesInFolder != null) {

                    Map<String, String> fileName = new HashMap<>(filesInFolder.length);

                    for(var file: filesInFolder) {
                        String fileShort = file.getName();
                        fileShort = fileShort.substring(0, fileShort.length() - 8);

                        fileName.put(fileShort, file.getPath());
                    }

                    return fileName;

                } else return null;
            }
        };

        try {
            fileNames = a.call();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void populate(ActionEvent event) throws IOException {
//        var dialog = new JFXDialog(root.)
//        var dialog = new FileSelectPopup((StackPane)root.getParent(), JFXDialog.DialogTransition.CENTER);
//        var dialog = new JFXDialog( (StackPane) root.getParent(), FXMLLoader.load(getClass().getResource("/fxml/selectPopup.fxml")), JFXDialog.DialogTransition.CENTER );
//        dialog.show();

/*        Parent a = listLoader.load();
        fileNames.forEach((fileName, filePath) -> {
            Button button = new Button(fileName);
            button.setOnAction(event1 -> {
                root.getScene().setRoot(a);
            });
            box.getChildren().add(button);
        });*/
/*        Platform.runLater(() -> {
            try {
                Parent a = listLoader.load();
                new JMetro(a, Style.LIGHT);
                VBox.setVgrow(a, Priority.ALWAYS);
                root.getChildren().setAll(a);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });*/
        /*var a = new JFXAlert<>();
        var dummy = new Pane();
        dummy.setPrefWidth(200);
        dummy.setPrefHeight(200);
        a.setContent(dummy);*/


        var c = new JFXDialogLayout();
        c.setHeading(new Label("sample"));
        c.setBody(new Label("Pretend this is file selection"));
//        var d = new FileListPopup();

//        var b = new JFXDialog((StackPane) root.getParent(), d, JFXDialog.DialogTransition.CENTER);
//        b.setCacheContainer(true);
//        b.show();
    }
}
