package edu.opjms.templating.inputPanes;

import edu.opjms.global.CommonKt;
import edu.opjms.global.inputForms.RawInputFormBase;
import edu.opjms.templating.ReorderAnimator;
import edu.opjms.templating.controls.Snackbar;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import kotlin.Pair;

import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

import static edu.opjms.global.CommonKt.ERROR_CLASS;
import static java.util.Objects.requireNonNullElse;

public abstract class InputPaneBase extends TitledPane {

    private StringProperty labelTextProperty;
    private static final ReorderAnimator reorderAnimator = new ReorderAnimator();
    private String suffix;
    private boolean hasControls = false;


    private Snackbar snackbar = null;

    private static final String DUPLICATE_ERR = "The label is Duplicated";
    protected static final String INVALID_LABEL_ERR = "Label must be set";
//    protected final static PseudoClass ERR_CLASS = PseudoClass.getPseudoClass("error");

    protected boolean isLabelDuplicate = false;
    protected boolean isLabelValid = true;

    //subclasses must set these
    protected TextFieldChange labelField;
    protected Label labelErr;

    protected Pair<TextField, Label> getLabelInput(String labelText) {
        labelField = new TextFieldChange(requireNonNullElse(labelText, ""));

        var labelErr = new Label();
        labelErr.getStyleClass().add(CommonKt.ERROR_STYLECLASS);

        if (labelText == null || labelText.isBlank()) {
            isLabelValid = false;
            labelField.pseudoClassStateChanged(ERROR_CLASS, true);
            labelErr.setText(INVALID_LABEL_ERR);
        }

        labelField.textProperty().addListener((observableValue, s, newVal) -> {
            isLabelValid = !newVal.isBlank();
            if (!isLabelDuplicate) {
                labelErr.setText(isLabelValid? "": INVALID_LABEL_ERR);
                labelField.pseudoClassStateChanged(ERROR_CLASS, !isLabelValid);
            }
        });

        return new Pair<>(labelField, labelErr);
    }

    public InputPaneBase() {
        //all the shortcut keys are added here
        this.setOnKeyPressed(keyEvent -> {
            final var keyCode = keyEvent.getCode();
            //opening and close convenience
            if (keyCode == KeyCode.LEFT)
                super.setExpanded(false);
            else if (keyCode == KeyCode.RIGHT)
                super.setExpanded(true);
        });
    }

    /**
     * <p>
     *     Tells whether the input contains an error which will prevent
     *     the input from generating html content.
     * </p>
     * <p>
     *     The exact definition depends upon the subclass. The subclass
     *     may choose to ignore or attempt to rectify the errors and
     *     generate HTML content, in which case, it will return false
     * </p>
     * @return whether the input will generate html content
     */
    abstract public boolean containsError();
    abstract public String generateHTML(int id);

    public void setOnLabelChange(BiConsumer<String, String> func) {
        labelField.setOnTextChange(func);
    }

    public void setSnackbar(Snackbar snackbar) {
        this.snackbar = snackbar;
    }

    public void showDuplicateError(boolean val) {
        if (val != isLabelDuplicate) {
            if (val) {
                labelErr.setText(DUPLICATE_ERR);
                labelField.pseudoClassStateChanged(ERROR_CLASS, true);
            } else {
                labelErr.setText(isLabelValid? "": INVALID_LABEL_ERR);
                labelField.pseudoClassStateChanged(ERROR_CLASS, !isLabelValid);
            }
            isLabelDuplicate = val;
        }
    }


    public void addControlButtons(EventHandler<ActionEvent> evt, IntConsumer offset) {
        if (getSkin() == null) {
            skinProperty().addListener(observable -> {
                addControlButtonsImpl(evt, offset);
                hasControls = true;
            });
        } else {
            addControlButtonsImpl(evt, offset);
            hasControls = true;
        }
    }

    private void addControlButtonsImpl(EventHandler<ActionEvent> evt, IntConsumer offset) {
        textProperty().unbind();
        textProperty().setValue("");

        final var currentTitle = lookup(".title");
        currentTitle.setOnMouseClicked(currentTitle.getOnMouseReleased());
        currentTitle.setOnMouseReleased(null);

        final var title = new HBox();

        title.getStyleClass().add("inner-title");
        title.setSpacing(5);
        title.setAlignment(Pos.CENTER_LEFT);
        title.minWidthProperty().bind(widthProperty().subtract(50));
        title.setMaxHeight(Region.USE_COMPUTED_SIZE);

        final var titleLabel = new Label();
        titleLabel.textProperty().bind(labelTextProperty.concat(suffix));
        final var isVisible = currentTitle.hoverProperty().or(this.focusedProperty());

        final var deleteButton = getDeleteButton(evt, isVisible);
        final var moveUpButton = getMoveUpButton(offset, isVisible);
        final var moveDownButton = getMoveDownButton(offset, isVisible);

        final var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        title.getChildren().addAll(titleLabel, spacer, moveUpButton, moveDownButton, deleteButton);

        this.setOnKeyPressed(keyEvent -> {
            final var keyCode = keyEvent.getCode();
            //opening and close convenience
            if (keyCode == KeyCode.LEFT)
                super.setExpanded(false);
            else if (keyCode == KeyCode.RIGHT)
                super.setExpanded(true);

            //shortcut keys
            if (keyEvent.isShortcutDown()) {
                switch (keyCode) {
                    case DELETE -> deleteButton.fire();
                    case UP -> offset.accept(-1);
                    case DOWN -> offset.accept(1);
                }
            }
        });
        setGraphic(title);

    }

    private Button getDeleteButton(EventHandler<ActionEvent> evt, BooleanBinding isVisible) {
        var shortcutText = new Text(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHORTCUT_DOWN).getDisplayText());
        shortcutText.getStyleClass().add("shortcut");
        var deleteTooltip = new Tooltip("Delete");
        deleteTooltip.setGraphic(shortcutText);
        deleteTooltip.setContentDisplay(ContentDisplay.RIGHT);

        final var graphic = CommonKt.getDeleteIcon();
        graphic.getStyleClass().add("delete");
        Button deleteButton = new Button("", graphic);
        deleteButton.setTooltip(deleteTooltip);
        deleteButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        deleteButton.getStyleClass().setAll("delete");
        deleteButton.visibleProperty().bind(isVisible);

        deleteButton.setOnAction(evt);
        return deleteButton;
    }

    private Button getMoveUpButton(IntConsumer fun, BooleanBinding isVisible) {
        var shortcutText = new Text(new KeyCodeCombination(KeyCode.UP, KeyCombination.SHORTCUT_DOWN).getDisplayText());
        shortcutText.getStyleClass().add("shortcut");
        var deleteTooltip = new Tooltip("Move up");
        deleteTooltip.setGraphic(shortcutText);
        deleteTooltip.setContentDisplay(ContentDisplay.RIGHT);

        final var graphic = CommonKt.getArrowUp24();
        graphic.getStyleClass().add("move");
        Button moveUp = new Button("", graphic);
        moveUp.setTooltip(deleteTooltip);
        moveUp.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        moveUp.getStyleClass().setAll("move");
        moveUp.visibleProperty().bind(isVisible);

        moveUp.setOnAction(actionEvent -> fun.accept(-1));
        return moveUp;
    }
    private Button getMoveDownButton(IntConsumer fun, BooleanBinding isVisible) {
        var shortcutText = new Text(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.SHORTCUT_DOWN).getDisplayText());
        shortcutText.getStyleClass().add("shortcut");
        var deleteTooltip = new Tooltip("Move Down");
        deleteTooltip.setGraphic(shortcutText);
        deleteTooltip.setContentDisplay(ContentDisplay.RIGHT);

        final var graphic = CommonKt.getArrowDown24();
        graphic.getStyleClass().add("move");
        Button moveDown = new Button("", graphic);
        moveDown.setTooltip(deleteTooltip);
        moveDown.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        moveDown.getStyleClass().clear();
        moveDown.getStyleClass().setAll("move");
        moveDown.visibleProperty().bind(isVisible);

        moveDown.setOnAction(actionEvent -> fun.accept(1));
        return moveDown;
    }





    final protected void configureSuper(StringProperty labelTextProperty, String suffix) {
        if (this.labelTextProperty == null) {
            this.suffix = suffix;
            this.labelTextProperty = labelTextProperty;
            textProperty().bind(this.labelTextProperty.concat(suffix));

        } else
            throw new IllegalCallerException("labelTextProperty is already set");
    }

    //utility methods
    /*
        These methods are routinely used by subclasses to construct the form
    */
    protected static VBox wrapInVBox(Node... nodes) {
        VBox wrapper = new VBox(nodes);
        wrapper.setSpacing(5);
        return wrapper;
    }

    protected static FlowPane wrapInFlowPane(Node... nodes) {
        FlowPane wrapper = new FlowPane(nodes);
        wrapper.setHgap(20);
        wrapper.setVgap(15);
        return wrapper;
    }

    /**
     * This method is used to denote which parent should be animated
     * when inputPanes are re-ordered using drag and drop
     * @param pane The pane whose children should be animated
     */
    public static void animateParent(Pane pane) {
        reorderAnimator.observe(pane.getChildren());
    }


    //getter
//    public StringProperty labelTextProperty() {
//        return labelTextProperty;
//    }
    public String getLabelText() {return  labelTextProperty.getValue();}


    abstract public RawInputFormBase toRawInput();

    //todo: undo delete input panes
}
