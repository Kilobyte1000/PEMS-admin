package edu.opjms.fileListPopup;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import java.util.function.Consumer;
import java.io.File;
import java.io.IOException;

public class FileListPopup extends VBox {

    static final String TEMP_FILENAME = "UNNAMED.temp";
    static final String EXTENSION = ".jmslist";

    private static final String STYLESHEET = "/css/filePopup.css";

    final File oldDir;

    public FileListPopup(File dir, File oldDir) {
        super();

        this.oldDir = oldDir;

        getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());

        /*
        * The Search Bar Text Box
        */

        TextField searchBox = new TextField();
        HBox.setHgrow(searchBox, Priority.ALWAYS);
        searchBox.setPromptText("Search");
        var searchIcon = new SVGPath();
        searchIcon.setContent("M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z");
//        SVGGlyph searchIcon = new SVGGlyph("M19.5 3C14.265 3 10 7.265 10 12.5c0 2.25.81 4.307 2.125 5.938L3.28 27.28l1.44 1.44 8.843-8.845C15.193 21.19 17.25 22 19.5 22c5.235 0 9.5-4.265 9.5-9.5S24.735 3 19.5 3zm0 2c4.154 0 7.5 3.346 7.5 7.5S23.654 20 19.5 20 12 16.654 12 12.5 15.346 5 19.5 5z");
//        searchIcon.setSize(32);
        searchIcon.setFill(Paint.valueOf("#666"));

        HBox searchBar = new HBox(searchIcon, searchBox);
        searchBar.getStyleClass().add("search-bar");
        searchBar.setSpacing(2);
        searchBar.setAlignment(Pos.CENTER_LEFT);



        /*
        * The List View which will display the available files
        */
        final ListView<File> listView = new ListView<>();

//        final var files = FXCollections.observableArrayList(dir.listFiles((dir1, name) -> name.endsWith(EXTENSION)));
        final var files = FXCollections.<File>observableArrayList(new File("D:\\as.fg"));



//        files.sort(File::compareTo);
        Label placeholder = new Label("No file found");
        placeholder.setTextFill(Paint.valueOf("rgb(25,25,25)"));

        listView.setPlaceholder(placeholder);

        //set files to ListView's data



        var filteredFiles = new FilteredList<>(files);
        listView.setItems(filteredFiles);
        listView.setEditable(true);

        Consumer<File> action = this::openFile;

        listView.setCellFactory(fileListView -> new FileListCell(action));



        /*
         * FilteredList does not allow data to be directly committed
         * so, we use out own event handler
         */
        listView.setOnEditCommit(fileEditEvent -> {
            final int index = filteredFiles.getSourceIndex(fileEditEvent.getIndex());
            files.set(index, fileEditEvent.getNewValue());
        });


        VBox.setVgrow(listView, Priority.ALWAYS);

        /*
        * The Add Button
        */

        //menus - these will rarely be used
        var importMenu = new MenuItem("Import");
        var showMoreMenu = new MenuItem("Show Older");

        var addFileButton = new SplitMenuButton(importMenu, showMoreMenu);
        addFileButton.setAlignment(Pos.CENTER);
        addFileButton.setText("Add new");
        addFileButton.setPopupSide(Side.RIGHT);

        addFileButton.setOnAction(event -> {
            if (listView.getEditingIndex() == -1) {



                try {
                    var tempFile = new File(dir.getCanonicalPath() + "/" + TEMP_FILENAME);

                    files.add(tempFile);
                    listView.layout();
                    Platform.runLater(() -> listView.edit(files.indexOf(tempFile)));

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        addFileButton.setMaxWidth(Double.MAX_VALUE);
        addFileButton.getStyleClass().add("add-button");

        getChildren().addAll(searchBar, listView, addFileButton);
        searchBar.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK,
                1, 1, .1, .1));

        searchBox.textProperty().addListener((observableValue, s, t1) -> {
            if (!t1.isBlank()) {
                var searchQuery = t1.strip().toLowerCase();
                filteredFiles.setPredicate(file -> file.getName().toLowerCase().contains(searchQuery));
            } else
                filteredFiles.setPredicate(null);
        });

        //actions for menu items
        importMenu.setOnAction(event -> System.out.println("open file-chooser and import"));
        showMoreMenu.setOnAction(event -> System.out.println("Show last year lists"));


    }

    void openFile(File file) {
        System.out.println("Opening " + file);
    }

    //todo styling for menu items
}
