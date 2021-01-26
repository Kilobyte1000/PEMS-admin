package edu.opjms.templating;

import edu.opjms.templating.inputPanes.InputPaneBase;
import edu.opjms.templating.inputPanes.SelectFieldGroupPane;
import edu.opjms.templating.inputPanes.SelectFieldPane;
import edu.opjms.templating.inputPanes.TextFieldPane;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("SpellCheckingInspection")
final public class Templator {

    protected VBox root;
    private final Model model;
    private final HostServices services;


    public Templator(Stage stage, HostServices services) {

        this.services = services;

        root = new VBox();

        root.setSpacing(15);

        root.getStyleClass().add("root-box");
        root.getStylesheets().addAll(getClass().getResource("/css/template.css").toExternalForm(),
                getClass().getResource("/css/beta.css").toExternalForm());



        //wrapper for input panes
        var uniqueIDField = new TextFieldPane(true);
        var inputWrapper = new VBox(uniqueIDField);
        model = new Model(inputWrapper.getChildren());


        //add Field Button
        MenuButton addFieldButton = new MenuButton("Add new Field");
        var addIcon = new SVGPath();
        addIcon.setContent("M24,13 L15,13 L15,4 C15,3.447 14.553,3 14,3 C13.447,3 13,3.447 13,4 L13,13 L4,13 C3.447,13 3,13.447 3,14 C3,14.553 3.447,15 4,15 L13,15 L13,24 C13,24.553 13.447,25 14,25 C14.553,25 15,24.553 15,24 L15,15 L24,15 C24.553,15 25,14.553 25,14 C25,13.447 24.553,13 24,13");
        addFieldButton.setGraphic(addIcon);
        addFieldButton.setGraphicTextGap(15);



        //menu items for add fields
        MenuItem textFieldMenu = new MenuItem("Add new Text Field");
        textFieldMenu.setOnAction(a ->  {
            var inputField = new TextFieldPane();
            inputField.setDeletable();
            inputField.allowDND();
            addToLastOfPane(inputField, inputWrapper);
        });

        var selectFieldMenu = new MenuItem("Add new Select");
        selectFieldMenu.setOnAction(event -> {
            var inputField = new SelectFieldPane();
            inputField.setDeletable();
            inputField.allowDND();
            addToLastOfPane(inputField, inputWrapper);
        });

        var selectFieldGroupMenu = new MenuItem("Add new Select Group");
        selectFieldGroupMenu.setOnAction(event -> {
            var inputField = new SelectFieldGroupPane();
            inputField.setDeletable();
            inputField.allowDND();
            addToLastOfPane(inputField, inputWrapper);
        });

        addFieldButton.getItems().addAll(textFieldMenu, selectFieldMenu, selectFieldGroupMenu);
        VBox.setMargin(addFieldButton, new Insets(20, 0, 30, 30));


        ScrollPane sp = new ScrollPane(root);
        sp.setFitToHeight(true);
        sp.setFitToWidth(true);
        sp.setCache(true);
        sp.setPadding(Insets.EMPTY); //prevent blurriness



        /*
        * Preview Page Button
        */
        Button genPage = new Button("Preview Page");
        genPage.setOnAction(event -> genPage());
        Button serialise = new Button("Save");
        serialise.setOnAction(event -> System.out.println(model.serialise()));

        root.getChildren().addAll(inputWrapper, addFieldButton, genPage, serialise);

        Scene scene = new Scene(sp);
        new JMetro(scene, Style.LIGHT);

        InputPaneBase.animateParent(root);

        stage.setWidth(800);
        stage.setHeight(600);


        stage.setScene(scene);
    }

    private void addToLastOfPane(InputPaneBase node, Pane root) {

        root.getChildren().add(node);
        var fadeAnim = new FadeTransition(Duration.millis(500));
        fadeAnim.setFromValue(.4);
        fadeAnim.setToValue(1);

        var moveAnim = new TranslateTransition(Duration.millis(200));
        moveAnim.setFromY(node.getLayoutY() + 10);
        moveAnim.setToY(0);
        var anim = new ParallelTransition(node, fadeAnim, moveAnim);

        anim.play();
    }

    private void genPage() {
        final var html = model.generateHTML();
        Path path = null;
        try {
            path = Files.createTempFile("sample", ".html");
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert path != null;
        try {
            Files.writeString(path,
                    Files.readString(Path.of(getClass().getResource("/html/loginPage.txt").toURI()))
                            .replace("<!--inputs-->", html)
            );
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        services.showDocument(path.toString());

        //todo: duplicate label scan
        //todo? fix document showing

        //todo: true/false field
        /*
        *   I'll first have to make this field in HTML and CSS before I can decide how to implement this in application
        *
        */
    }

}