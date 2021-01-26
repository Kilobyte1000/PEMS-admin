package edu.opjms.fileListPopup;

import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import static java.lang.Double.MAX_VALUE;

final class FileListCell extends TextFieldListCell<File> {

    private final HBox wrapper = new HBox();
    private final Label fileName = new Label();
    private final OpenAction action;

    public FileListCell(OpenAction action) {
        super();

        this.action = action;

        setEditable(true);

        //Configure the Buttons
        Button deleteButton = new Button("D");
        Button editButton = new Button("e");

        deleteButton.visibleProperty().bind(hoverProperty());
        editButton.visibleProperty().bind(hoverProperty());
        editButton.setOnAction(event -> super.startEdit());

        /*editButton.getStyleClass().clear();
        deleteButton.getStyleClass().clear();*/


        //use our own label to push the buttons to other side
        fileName.setMaxWidth(MAX_VALUE);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(fileName, Priority.ALWAYS);


        wrapper.setSpacing(20);
        wrapper.getChildren().addAll(fileName, editButton, deleteButton);


        setConverter(new StringConverter<>() {
            @Override
            public String toString(File file) {
                var name = file.getName();
                return name.substring(0, name.lastIndexOf('.'));
            }

            @Override
            public File fromString(String s) {
                if (!s.isBlank())
                    return new File(getItem().getParent() + "/" + s + FileListPopup.EXTENSION);
                else
                    return null;
            }
        });

    }

    /**
     * Commits the given file to list.
     * <p>The method does nothing if the provided file equals the file it currently represents</p>
     * <p>It checks whether the file currently exists, in which case it displays an alert and does not commit the file to the list</p>
     * <p>It throws an exception if renaming the underlying file fails</p>
     * @param file The File to be committed to the list
     * @throws UncheckedIOException If renaming the underlying file fails
     */
    @Override
    public void commitEdit(File file) {
        //null is returned if user types a blank string
        if (file == null) {
            super.cancelEdit();
            return;
        }

        var item = getItem();

        if (file.equals(item))
            return;


        if (!file.exists()) {

            //we need to create a new file if it temp file
            //otherwise we rename the old one

            if (item.getName().equals(FileListPopup.TEMP_FILENAME)) {
                try {
                    if (file.createNewFile()) {
                        super.commitEdit(file);
                    } else
                    throw new UncheckedIOException(new IOException("Could not rename file"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                if (item.renameTo(file)) {
                    super.commitEdit(file);

                } else //can't do anything here
                    throw new UncheckedIOException(new IOException("Could not rename file"));

            }
        } else {
            System.err.println("file exists error");
            final var alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("err");
            alert.showAndWait();
        }
    }

    /**
     * This method is not for editing. We are just using the list cell's
     * Edit Detection to do whatever we want here.
     * Calling super.edit actually starts the editing
     */
    @Override
    public void startEdit() {
        if (getItem().getName().equals(FileListPopup.TEMP_FILENAME))
            super.startEdit();
        else
            action.openFile(getItem());
    }

    @Override
    public void updateItem(File file, boolean empty) {
        super.updateItem(file, empty);


        if (file == null || empty) {
            setGraphic(null);
        } else {
            fileName.setText(getText());
            setGraphic(wrapper);
        }

        //we use our own label, so this should always be null
        setText(null);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        if (getItem().getName().equals(FileListPopup.TEMP_FILENAME))
            delete();
    }

    /*
    * Private Methods
    */

    /**
     * removes itself from parent ListView
     */
    private void delete() {
        final var file = getItem();

        var filteredList = (FilteredList<File>) getListView().getItems();
        filteredList.getSource().remove(file);
    }

}
