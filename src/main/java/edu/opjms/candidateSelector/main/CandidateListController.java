package edu.opjms.candidateSelector.main;

import edu.opjms.candidateSelector.animations.CustomFadeInUp;
import edu.opjms.candidateSelector.controls.ActionButtonBase;
import edu.opjms.candidateSelector.controls.ActionButtonDelete;
import edu.opjms.candidateSelector.controls.ActionButtonDeleteAll;
import edu.opjms.candidateSelector.util.HouseIndex;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.FlatAlert;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import static edu.opjms.candidateSelector.controls.ActionButtonNew.DEFAULT_NAME;
import static edu.opjms.candidateSelector.util.ListUtil.addNewItem;
import static edu.opjms.candidateSelector.util.ListUtil.deleteSelectedItem;
import static java.lang.Byte.parseByte;
import static javafx.scene.control.ButtonBar.ButtonData.*;
import static javafx.scene.control.cell.TextFieldListCell.forListView;

final public class CandidateListController implements Initializable {

    private final Model model = new Model();
    private final FileChooser fileChooser = new FileChooser();
    private final SimpleStringProperty fileName = new SimpleStringProperty("");
    private Stage stage;
    private Button selectedButton;
    @FXML
    private GridPane mainArea;
    @FXML
    private BorderPane root;
    @FXML
    private Button houseTab;
    @FXML
    private Button sportsTab;
    @FXML
    private ListView<String> prefectBoyList;
    @FXML
    private ListView<String> prefectGirlList;
    @FXML
    private Button buttonTilak;
    @FXML
    private Button buttonKabir;
    @FXML
    private Button buttonRaman;
    @FXML
    private Button buttonTagore;
    @FXML
    private Button buttonVashishth;
    @FXML
    private Button buttonVivekanand;
    @FXML
    private MenuItem undoMenu;
    @FXML
    private MenuItem redoMenu;
    @FXML
    private MenuItem insertMenu;
    @FXML
    private MenuItem deleteMenu;
    private CustomFadeInUp a;
    private List<Button> menuButtons;
    private FlatAlert saveConfirmDialog;

    private final byte HOUSE_PREFECT_INDEX = 0;
    private final byte SPORTS_PREFECT_INDEX = 2;

    private byte selectedPost = HOUSE_PREFECT_INDEX;

    // Methods related to initialisation of controller

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Initialise prefectList
        List<ListView<String>> prefectList = List.of(prefectBoyList, prefectGirlList);

        //Set Details Of all the ListViews
        prefectList.forEach(stringListView -> {
            stringListView.setCellFactory(forListView());
            stringListView.setEditable(true);
            stringListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            stringListView.setOnEditCommit(this::validateListInput);
            stringListView.setOnEditCancel(stringEditEvent -> stringEditEvent.getSource().getItems().removeIf(s -> s.equals(DEFAULT_NAME)));

            //set keyboard shortcuts
            final byte listIndex = getIndexFromData(stringListView);
            stringListView.setOnKeyPressed(keyEvent -> {

                if (stringListView.getEditingIndex() == -1) { //if not editing
                    switch (keyEvent.getCode()) {
                        case DELETE -> {
                            final var selectedItems = stringListView.getSelectionModel().getSelectedItems();
                            if (!selectedItems.isEmpty()) {
                                model.addUndoTaskDelete((byte) (selectedPost + listIndex), selectedItems.toArray(new String[0]));
                                deleteSelectedItem(stringListView);
                            }
                        }
                        case INSERT -> addNewItem(stringListView, DEFAULT_NAME);
                    }
                }

                keyEvent.consume();

            });

        });

        //Initialise the menus
        deleteMenu.disableProperty().bind(Bindings.not(prefectBoyList.focusedProperty()
                .or(prefectGirlList.focusedProperty())));
        insertMenu.disableProperty().bind(Bindings.not(prefectBoyList.focusedProperty()
                .or(prefectGirlList.focusedProperty())));


        //Initialise Button List
        menuButtons = List.of(buttonTilak, buttonKabir, buttonRaman, buttonTagore, buttonVashishth, buttonVivekanand);

        //Set Current Editing House as Tilak
        model.setCurrentHouse(HouseIndex.TILAK);
        selectedButton = buttonTilak;

        //bind the listView with ObservableLists in model
        getDataInList(model.getCurrentHouse());

        //Set Extension Filter for the fileChooser
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OPJMS Candidate List", "*.jmslist"));

        selectedButton.getStyleClass().add("active");

        undoMenu.disableProperty().bind(model.undoNotAvailable);
        redoMenu.disableProperty().bind(model.redoNotAvailable);

        //initialise saveDialog
        saveConfirmDialog = new FlatAlert(Alert.AlertType.CONFIRMATION,
                "Do you wish to save your changes?",
                new ButtonType("Save", YES),
                new ButtonType("Don't Save", NO),
                new ButtonType("Cancel", CANCEL_CLOSE));
        saveConfirmDialog.setHeaderText("Unsaved Changes found");
        new JMetro(saveConfirmDialog.getDialogPane(), Style.LIGHT);


    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(this::fileSaveConfirmDialogHandle);
        stage.titleProperty().bind(Bindings.when(model.isDataSaved()).then("Candidate List Editor ").otherwise("*Candidate List Editor ").concat(fileName));

    }

    //Methods related to listView validation

    /**
     * Error code details :-
     * <ol start="0">
     *     <li>means string is ok</li>
     *     <li>means name is greater than 32 characters</li>
     *     <li>means name contains other characters than alphabets</li>
     *     <li>name is is Duplicate In List</li>
     * </ol>
     *
     * @param name     the string to be evaluated
     * @param listView if given, checks if the string is duplicate
     * @return errorCode
     */
    private byte isStringValidInList(String name, ListView<String> listView) {
        if (name.length() > 32)
            return 1;
        else if (name.chars().anyMatch(value -> !((value == ' ') || (value >= 'a' && value <= 'z') || (value >= 'A' && value <= 'Z')))) //name contains other characters than spaces and alphabets
            return 2;
        else if (listView != null && isDuplicateInList(name, listView))
            return 3;
        else
            return 0;
    }

    private boolean isDuplicateInList(String name, ListView<String> listView) {
        var list = listView.getItems();
        var editIndex = listView.getEditingIndex();

        return (IntStream.range(0, list.size()).anyMatch(i -> list.get(i).equalsIgnoreCase(name) && editIndex != i));

    }

    private void validateListInput(ListView.EditEvent<String> stringEditEvent) {
        final var listView = stringEditEvent.getSource();
        final var index = stringEditEvent.getIndex();
        final var name = stringEditEvent.getNewValue().strip();
        final var oldName = listView.getItems().get(index);

        if (name.equals(DEFAULT_NAME)) {
            listView.getItems().remove(index);
            return;
        }

        if (name.length() == 0) {//If name is just whitespace, dont do anything
            if (oldName.equals(DEFAULT_NAME))
                listView.getItems().remove(index);
            return;
        }

        var errCode = isStringValidInList(name, listView);

        if (errCode == 0) { //String is OK
            listView.getItems().set(index, name);

            //add the appropriate undoable task
            if (oldName.equals(DEFAULT_NAME))
                model.addUndoTaskAdd((byte) (selectedPost + getIndexFromData(listView)), name);
            else
                model.addUndoTaskEdit((byte) (selectedPost + getIndexFromData(listView)), oldName, name);
        } else {
            FlatAlert alert = new FlatAlert(Alert.AlertType.ERROR);
            switch (errCode) {
                case 1 -> {
                    alert.setHeaderText("Name Is Too Long");
                    alert.setContentText("The name should not be longer than 32 characters");
                }
                case 2 -> {
                    alert.setHeaderText("Invalid Name");
                    alert.setContentText("Name can only contain english letters and spaces");
                }
                case 3 -> {
                    alert.setHeaderText("Duplicate Name");
                    alert.setContentText("Names of candidates standing for same post must be unique\nTry adding or removing Initials");
                    alert.setResizable(true);
                }
                default -> {
                    alert.setHeaderText("Bug Encountered");
                    alert.setContentText("This text was not supposed to be shown. its a bug");
                }
            }
            new JMetro(alert.getDialogPane(), Style.LIGHT);
            listView.getItems().set(index, name);
            alert.setOnCloseRequest(dialogEvent -> {
                listView.getItems().set(index, oldName);
                listView.edit(index);
                ((TextField) listView.lookup("TextField")).setText(name);
            });
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.show();
        }
    }

    @FXML
    private void buttonAction(ActionEvent event) {
        var eventSource = event.getSource();
        var list = ((ActionButtonBase) eventSource).getListView();

        //Add undo tasks
        if (eventSource instanceof ActionButtonDelete)
            model.addUndoTaskDelete((byte) (selectedPost + parseByte(list.getUserData().toString())),
                    list.getSelectionModel().getSelectedItems().toArray(new String[0]));
        else if (eventSource instanceof ActionButtonDeleteAll) {
            model.addUndoTaskDelete((byte) (selectedPost + parseByte(list.getUserData().toString())), list.getItems().toArray(new String[0]));
        }

        ((ActionButtonBase) eventSource).action();
    }

    private void getDataInList(final HouseIndex houseIndex) {
        prefectBoyList.setItems(model.getItems().getCandidateList(houseIndex, selectedPost));
        prefectGirlList.setItems(model.getItems().getCandidateList(houseIndex, selectedPost + 1));
    }


    //Methods related to menus
    @SuppressWarnings("unchecked")
    @FXML
    private void deleteMenu() {
        final var list = (ListView<String>) stage.getScene().getFocusOwner();

        final var selectedItems = list.getSelectionModel().getSelectedItems();

        if (!selectedItems.isEmpty()) {
            model.addUndoTaskDelete((byte) (selectedPost + getIndexFromData(list)), selectedItems.toArray(new String[0]));
            deleteSelectedItem(list);
        }

    }

    @SuppressWarnings("unchecked")
    @FXML
    private void insertMenu() {
        final var list = (ListView<String>) mainArea.getScene().getFocusOwner();
        addNewItem(list, DEFAULT_NAME);
    }


    //Methods related to house changing

    @FXML
    private void changeHouseButton(ActionEvent event) {
        final var houseIndex = HouseIndex.valueOf(((Button) event.getSource()).getUserData().toString());
        changeHouse(true, houseIndex);
    }

    @FXML
    private void changeHouseMenu(ActionEvent event) {
        changeHouse(false,
                HouseIndex.valueOf(((MenuItem) event.getSource()).getUserData().toString()));
    }

    private void changeHouse(final boolean animateChange, final HouseIndex houseIndex) {
        model.setCurrentHouse(houseIndex);

        Button button = menuButtons.get(houseIndex.ordinal());

        //Get Data
        getDataInList(houseIndex);

        if (animateChange) {
            if (a == null) {
                a = new CustomFadeInUp(mainArea);
            }
            //Animate tabPane
            a.stop();
            a.play();
        }

        //set accent color according to house
        root.setStyle("accent_color:" + houseIndex.color);

        //change selected button and apply appropriate style to it
        if (button != null) {
            selectedButton.getStyleClass().remove("active");
            selectedButton = button;
            selectedButton.getStyleClass().add("active");
        }

//        sportsPrefectBoyList.
    }

    @FXML
    private void changePostButton(ActionEvent event) {
        /*switch ( (String) ((Button)event.getSource()).getUserData() ) {
            case "0":
                changePost(HOUSE_PREFECT_INDEX);
                break;
            case "1":
                changePost(SPORTS_PREFECT_INDEX);
                break;
        }*/
        var button = event.getSource();
        if (button == houseTab) {
            if (selectedPost != HOUSE_PREFECT_INDEX) {
                changePost(HOUSE_PREFECT_INDEX);
                sportsTab.getStyleClass().remove("active");
                houseTab.getStyleClass().add("active");
            }
        } else {
            if (selectedPost != SPORTS_PREFECT_INDEX) {
                changePost(SPORTS_PREFECT_INDEX);
                houseTab.getStyleClass().remove("active");
                sportsTab.getStyleClass().add("active");
            }
        }
    }

    private void changePost(final byte post) {
        selectedPost = post;
        getDataInList(model.getCurrentHouse());
    }

    //methods related to file IO
    @FXML
    private void newFile(Event event) {
        if (!fileSaveConfirmDialogHandle(event))
            return;
        model.newFile();
        getDataInList(model.getCurrentHouse());
    }

    @FXML
    private void close() {
        Platform.exit();
    }

    @FXML
    private void saveFile() {
        saveToFile(false);
    }

    @FXML
    private void saveAs() {
        saveToFile(true);
    }

    @FXML
    private void openFile(ActionEvent event) {
        if (!fileSaveConfirmDialogHandle(event))
            return;
        File file = fileChooser.showOpenDialog(stage);
        if (file != null)
            loadFile(file);
    }

    /**
     * @param saveToNewFile whether a new file should be created or data should be saved to an existing file.
     *                      Always saves to a new file if current file is null
     */
    private void saveToFile(boolean saveToNewFile) {
        File file = (saveToNewFile || model.currentFile() == null) ? fileChooser.showSaveDialog(stage) : model.currentFile();
        if (file == null) return;
        try {
            model.writeDataToFile(file);
            model.setCurrentFile(file);
            fileName.setValue("- " + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFile(File file) {
        if (file.length() != 0) {
            try {
                model.getDataFromFile(file);
                getDataInList(model.getCurrentHouse());
                model.setCurrentFile(file);
                fileName.setValue("- " + file.getName());
            } catch (IOException | ClassNotFoundException e) {
                var alert = new FlatAlert(Alert.AlertType.ERROR);
                alert.setHeaderText("Could not load file");
                alert.setContentText("Could not read/access file.\nIt might me corrupt or inaccessible");
                new JMetro(alert.getDialogPane(), Style.LIGHT);
                alert.setResizable(true);
                alert.show();
            }
        } else
            model.setCurrentFile(file);

    }

    /**
     * @param event ignored
     * @return whether the function should move forward
     */
    private boolean fileSaveConfirmDialogHandle(Event event) {
        if (!model.isDataSaved().getValue()) {
            final Optional<ButtonType> dialogButtonType = saveConfirmDialog.showAndWait();
            if (dialogButtonType.isEmpty() || dialogButtonType.get().getButtonData().equals(CANCEL_CLOSE)) {
                event.consume();
                return false;
            } else if (dialogButtonType.get().getButtonData().equals(YES))
                saveToFile(false);
        }
        return true;
    }


    //Undo and Redo
    @FXML
    private void undo() {
        manageListAndButton(model.undoLastTask());
    }

    @FXML
    private void redo() {
        manageListAndButton(model.redoLastTask());
    }

    private void manageListAndButton(byte[] a) {
        var houseIndex = model.getCurrentHouse().ordinal();

        if ( (a[1] & 1) == 0) // boy prefect
            prefectBoyList.requestFocus();
        else prefectGirlList.requestFocus();

        if ((a[1] & 2)  != selectedPost) { //if post is changed
            if (a[1] < 2) {
                selectedPost = HOUSE_PREFECT_INDEX;
                sportsTab.getStyleClass().remove("active");
                houseTab.getStyleClass().add("active");
            } else {
                selectedPost = SPORTS_PREFECT_INDEX;
                houseTab.getStyleClass().remove("active");
                sportsTab.getStyleClass().add("active");
            }

            if (a[0] == houseIndex) {//house is not changed
                getDataInList(model.getCurrentHouse());
                return;
            }
        }


        if (a[0] != houseIndex) //if house is changed
            changeHouse(false, HouseIndex.getFromIndex(a[0]));
    }


    //General Utilities

    private byte getIndexFromData(Node node) {
        return Byte.parseByte(node.getUserData().toString());
    }

}