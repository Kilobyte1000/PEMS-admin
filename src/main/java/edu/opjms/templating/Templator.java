package edu.opjms.templating;


import edu.opjms.templating.controls.Snackbar;
import  edu.opjms.templating.inputPanes.*;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.HostServices;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.MDL2IconFont;
import jfxtras.styles.jmetro.Style;
import net.kilobyte1000.LoginPageProviderKt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.Executors;

@SuppressWarnings("SpellCheckingInspection")
final public class Templator {

    protected VBox root;
    private final Model model;
    private final HostServices services;
    private final Path dataFolder;

    private static final String LIGHT_THEME = Templator.class.getResource("/css/templateLight.css").toExternalForm();
    private static final String DARK_THEME = Templator.class.getResource("/css/templateDark.css").toExternalForm();

    private boolean isDarkModeActive = false;
    private ComboBox<Path> fileSelector;

    public Templator(Stage stage, HostServices services, Path dataFolder) {

        this.services = services;


        //create data folder if not exists
        try {
            Files.createDirectories(dataFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.dataFolder = dataFolder;

        root = new VBox();

        root.setSpacing(15);

        root.getStyleClass().add("root-box");
        root.getStylesheets().addAll(getClass().getResource("/css/template.css").toExternalForm(),
                LIGHT_THEME);
//                getClass().getResource("/css/templateDark.css").toExternalForm());

        final int lastStyleSheet = root.getStylesheets().size() - 1;

        final var recentFile = createTopNav();

        //wrapper for input panes
        var inputWrapper = new VBox();
        inputWrapper.setSpacing(6);
        model = new Model(inputWrapper.getChildren(), recentFile);


        //add Field Button
        MenuButton addFieldButton = new MenuButton("Add new Field");
        var addIcon =  new SVGPath();
        addIcon.setContent("M24,13 L15,13 L15,4 C15,3.447 14.553,3 14,3 C13.447,3 13,3.447 13,4 L13,13 L4,13 C3.447,13 3,13.447 3,14 C3,14.553 3.447,15 4,15 L13,15 L13,24 C13,24.553 13.447,25 14,25 C14.553,25 15,24.553 15,24 L15,15 L24,15 C24.553,15 25,14.553 25,14 C25,13.447 24.553,13 24,13");
        addFieldButton.setGraphic(addIcon);
        addFieldButton.setGraphicTextGap(15);


        var snackBar = new Snackbar(root);
        snackBar.setBottomMarginPercent(.1);

        //menu items for add fields
        MenuItem textFieldMenu = new MenuItem("Add new Text Field");
        textFieldMenu.setOnAction(a ->  {
            var inputField = new TextFieldPane();
            inputField.setDeletable();
            inputField.allowDND();
            inputField.setOnLabelChange(model::checkForDuplicates);
            inputField.setSnackbar(snackBar);
            addToLastOfPane(inputField, inputWrapper);
        });

        var selectFieldMenu = new MenuItem("Add new Select");
        selectFieldMenu.setOnAction(event -> {
            var inputField = new SelectFieldPane();
            inputField.setDeletable();
            inputField.allowDND();
            inputField.setSnackbar(snackBar);
            inputField.setOnLabelChange(model::checkForDuplicates);
            addToLastOfPane(inputField, inputWrapper);
        });

        var dateInputMenu = new MenuItem("Add new Date Input");
        dateInputMenu.setOnAction(event -> {
            var inputField = new DateInputPane();
            inputField.setDeletable();
            inputField.allowDND();
            inputField.setSnackbar(snackBar);
            inputField.setOnLabelChange(model::checkForDuplicates);
            addToLastOfPane(inputField, inputWrapper);
        });

        addFieldButton.getItems().addAll(textFieldMenu, selectFieldMenu, dateInputMenu);
        VBox.setMargin(addFieldButton, new Insets(20, 0, 30, 30));


        /*
        *
        * Button Bar
        * This houses all the buttons
        *
        * */

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button genPage = new Button("Preview Page");
        genPage.setOnAction(event -> genPage());
        var genIcon = new MDL2IconFont("");
        genIcon.setSize(48);
        genPage.setGraphic(genIcon);
        genPage.setGraphicTextGap(15);

        Button duplicate = new Button("Duplicate");
        duplicate.setOnAction(event -> System.out.println("duplicated"));
        var duplIcon = new MDL2IconFont("");
        duplIcon.setSize(48);
        duplicate.setGraphic(duplIcon);
        duplicate.setGraphicTextGap(15);

        Button serialise = new Button("Save");
        serialise.setOnAction(event -> System.out.println(model.serialize()));
        var saveIcon = new MDL2IconFont("");
        saveIcon.setSize(48);
        serialise.setGraphic(saveIcon);
        serialise.setGraphicTextGap(15);


        var buttonBar = new HBox(addFieldButton, spacer, genPage, duplicate, serialise);
        buttonBar.setSpacing(10);

        root.getChildren().addAll(new HBox(fileSelector), inputWrapper, buttonBar);


        ScrollPane sp = new ScrollPane(root);
        sp.setFitToHeight(true);
        sp.setFitToWidth(true);
        sp.setCache(true);
        sp.setPadding(Insets.EMPTY); //prevent blurriness


        Scene scene = new Scene(sp);
        var j = new JMetro(scene, Style.LIGHT);

        //theme testing and pleasure for my eyes

        final var th = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);

        scene.getAccelerators().put(th, () -> {
            if (isDarkModeActive) {
                j.setStyle(Style.LIGHT);
                root.getStylesheets().set(lastStyleSheet, LIGHT_THEME);
            } else {
                j.setStyle(Style.DARK);
                root.getStylesheets().set(lastStyleSheet, DARK_THEME);
            }

            isDarkModeActive = !isDarkModeActive;
        });


        InputPaneBase.animateParent(inputWrapper);

        stage.setWidth(800);
        stage.setHeight(600);

        var a = new Service<Void>() {
            private Path path;

            public void setPath(Path path) {
                this.path = path;
            }

            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        model.changeToPath(path);
                        return null;
                    }
                };
            }
        };

        a.setExecutor(Executors.newSingleThreadExecutor());
        fileSelector.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            a.reset();
            a.setPath(newValue);
            a.start();
        });

        a.setOnFailed(workerStateEvent -> workerStateEvent.getSource().getException());
        
        stage.setScene(scene);

    }

    private Path createTopNav() {
        final var nav = new ComboBox<Path>();
        Path recentPath = null;
        try (final var templates = Files.newDirectoryStream(dataFolder, "*.templ")) {
            FileTime recentTime = null;

            for (var template: templates) {
                final var thisTime = Files.getLastModifiedTime(template);

                //get most recent file

                if (recentTime != null) {
                    if (thisTime.compareTo(recentTime) < 0) {
                        recentTime = thisTime;
                        recentPath = template;
                    }
                } else {
                    recentTime = thisTime;
                    recentPath = template;
                }


                nav.getItems().add(template);
            }

            //if recent file is null, then it means no file was found, make default one
            if (recentPath == null) {
                recentPath = dataFolder.resolve("unnamed.templ");
                Files.createFile(recentPath);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        nav.setConverter(new StringConverter<>() {
            @Override
            public String toString(Path path) {
                if (path != null) {
                    final var fileName = path.getFileName().toString();
                    return fileName.substring(0, fileName.lastIndexOf('.'));
                } else
                    return null;
            }

            @Override
            public Path fromString(String s) {
                return dataFolder.resolve(s.strip() + ".templ");
            }
        });

        fileSelector = nav;
        nav.getSelectionModel().select(recentPath);

        return recentPath;
    }


    private void addToLastOfPane(InputPaneBase node, Pane root) {


        root.getChildren().add(node);
        node.setCacheHint(CacheHint.SPEED);
        var fadeAnim = new FadeTransition(Duration.millis(500));
        fadeAnim.setFromValue(.4);
        fadeAnim.setToValue(1);

        var moveAnim = new TranslateTransition(Duration.millis(200));
        moveAnim.setFromY(node.getLayoutY() + 10);
        moveAnim.setToY(0);
        var anim = new ParallelTransition(node, fadeAnim, moveAnim);
        anim.setOnFinished(event -> node.setCacheHint(CacheHint.QUALITY));
        anim.play();
    }

    private void genPage() {
        final var html = model.generateHTML();
        final var path = LoginPageProviderKt.loadLoginPageTest(html);
        services.showDocument(path.toString());

        //todo: duplicate label scan

        //todo: true/false field
        /*
        *   I'll first have to make this field in HTML and CSS before I can decide how to implement this in application
        *
        */
    }

}