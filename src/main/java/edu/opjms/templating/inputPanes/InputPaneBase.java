package edu.opjms.templating.inputPanes;

import edu.opjms.global.inputForms.RawInputFormBase;
import edu.opjms.templating.ReorderAnimator;
import edu.opjms.templating.controls.Snackbar;
import javafx.animation.*;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.function.BiConsumer;

public abstract class InputPaneBase extends TitledPane {

    private StringProperty labelTextProperty;
    private static final ReorderAnimator reorderAnimator = new ReorderAnimator();
    private String suffix;
    private boolean isDeletable = false;


    private Snackbar snackbar = null;

    private static final String DUPLICATE_ERR = "The label is Duplicated";
    protected static final String INVALID_LABEL_ERR = "Label must be set";
    protected final static PseudoClass ERR_CLASS = PseudoClass.getPseudoClass("error");

    protected boolean isLabelDuplicate = false;
    protected boolean isLabelValid = true;

    //subclasses must set these
    protected TextFieldChange labelField;
    protected Label labelErr;

    public InputPaneBase() {
        //all the shortcut keys are added here
        this.setOnKeyPressed(keyEvent -> {

            final var keyCode = keyEvent.getCode();
            //opening and close convenience
            if (keyCode == KeyCode.LEFT)
                super.setExpanded(false);
            else if (keyCode == KeyCode.RIGHT)
                super.setExpanded(true);

            //shortcut keys
            if (keyEvent.isShortcutDown()) {
                if (keyCode == KeyCode.DELETE && isDeletable)
                    deleteThis(null);

            }
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
                labelField.pseudoClassStateChanged(ERR_CLASS, true);
            } else {
                labelErr.setText(isLabelValid? "": INVALID_LABEL_ERR);
                labelField.pseudoClassStateChanged(ERR_CLASS, !isLabelValid);
            }
            isLabelDuplicate = val;
        }
    }



    /**
     * Public method to allow re-ordering of InputPanes with other panes
     * by drag and drop
     */
    public void allowDND() {
        if (getSkin() == null) {
            skinProperty().addListener(observable -> configureDND());
        } else
            configureDND();
    }


    /**
     * Adds a delete button to the end of the title pane
     * which allows deleting the input pane via clicking it
     */
    public void setDeletable() {
        if (getSkin() == null) {
            skinProperty().addListener(observable -> {
                addDeleteButton();
                isDeletable = true;
            });
        } else {
            addDeleteButton();
            isDeletable = true;
        }
    }

    final protected void addDeleteButton() {
        textProperty().unbind();
        textProperty().setValue("");


        var currentTitle = lookup(".title");
        currentTitle.setOnMouseClicked(currentTitle.getOnMouseReleased());
        currentTitle.setOnMouseReleased(null);

        StackPane title = new StackPane();

        title.getStyleClass().add("inner-title");
        title.minWidthProperty().bind(widthProperty().subtract(50));
        title.setMaxHeight(Region.USE_COMPUTED_SIZE);

        var deleteTooltip = new Tooltip("Delete");
            var shortcutText = new Text(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHORTCUT_DOWN).getDisplayText());
        shortcutText.getStyleClass().add("shortcut");
        deleteTooltip.setGraphic(shortcutText);
        deleteTooltip.setContentDisplay(ContentDisplay.RIGHT);

        Button deleteButton = new Button();
        deleteButton.setTooltip(deleteTooltip);
        deleteButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        deleteButton.getStyleClass().clear();
        Label titleLabel = new Label();
        titleLabel.textProperty().bind(labelTextProperty.concat(suffix));

        var deleteIcon = new SVGPath();
        deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zm2.46-7.12l1.41-1.41L12 12.59l2.12-2.12 1.41 1.41L13.41 14l2.12 2.12-1.41 1.41L12 15.41l-2.12 2.12-1.41-1.41L10.59 14l-2.13-2.12zM15.5 4l-1-1h-5l-1 1H5v2h14V4z");
        deleteButton.setGraphic(deleteIcon);
        deleteButton.visibleProperty().bind(currentTitle.hoverProperty().or(this.focusedProperty()));

        deleteButton.setOnAction(this::deleteThis);

        StackPane.setAlignment(titleLabel, Pos.CENTER_LEFT);
        StackPane.setAlignment(deleteButton, Pos.CENTER_RIGHT);

        title.getChildren().addAll(titleLabel, deleteButton);

        setGraphic(title);
    }


    final protected void configureDND() {
        var title = this.lookup(".title");
        var root = (Pane) this.getParent();
        title.setOnMouseDragged(detectDrag);
        title.setOnDragDetected(mouseEvent -> {
            var db = this.startDragAndDrop(TransferMode.MOVE);

            db.setDragView(this.snapshot(null, null), mouseEvent.getX(), mouseEvent.getY());
            ClipboardContent content = new ClipboardContent();

            final int index = root.getChildrenUnmodifiable().indexOf(this);

            content.putString(String.valueOf(index));

            db.setContent(content);
            mouseEvent.consume();
        });

        this.setOnDragOver(dragEvent -> {
            if (dragEvent.getGestureSource() != this && dragEvent.getDragboard().hasString())
                dragEvent.acceptTransferModes(TransferMode.MOVE);
            dragEvent.consume();
        });


        this.setOnDragEntered(dragEvent -> {
            if (dragEvent.getDragboard().hasString() && dragEvent.getGestureSource() != this) {
                this.setStyle("-fx-background-color: #ebebeb");
                this.getContent().setStyle("-fx-background-color: #ebebeb");
            }
            dragEvent.consume();
        });

        this.setOnDragExited(dragEvent -> {
            this.setStyle("");
            this.getContent().setStyle("");
        });

        this.setOnDragDropped(dragEvent -> {
            var db = dragEvent.getDragboard();
            if (db.hasString() && dragEvent.getGestureSource() != null) {
                reorderAnimator.animateNextChange();

                int sourceIndex = Integer.parseInt(db.getString());
                int targetIndex = root.getChildrenUnmodifiable().indexOf(this);
                Node sourceNode = root.getChildren().remove(sourceIndex);

                root.getChildren().add(targetIndex, sourceNode);

            }
            dragEvent.setDropCompleted(true);
            dragEvent.consume();
        });
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

    private void deleteThis(ActionEvent actionEvent) {
        var parent = getParent();
        //remove titled Pane from Parent


        var anim = getExitAnim();

        anim.setOnFinished(event -> {
            int index;

            if (parent instanceof Accordion) {
                var accordion = (Accordion) parent;
                index = accordion.getPanes().indexOf(this);
                accordion.getPanes().remove(index);
            } else {
                var pane = (Pane) parent;
                index = pane.getChildren().indexOf(this);
                pane.getChildren().remove(index);
            }

            labelField.getOnTextChange().accept(labelField.getText(), null);

            if (snackbar != null) {
                var label = getLabelText() + suffix;
                snackbar.enque(new Snackbar.SnackBarEvent(label + " was deleted", aVoid -> {
                    if (parent instanceof Accordion)
                        ((Accordion) parent).getPanes().add(index, this);
                    else
                        ((Pane) parent).getChildren().add(index, this);
                    anim.play();
                    callScan();
                }));
            }
            anim.setOnFinished(null);
            anim.setRate(-1);
        });
        anim.playFromStart();

        callScan();
    }

    protected void callScan() {
        labelField.getOnTextChange().accept(getLabelText(), null);
    }

    private Timeline getExitAnim() {
        var interpolator = Interpolator.SPLINE(.52,.16,.97,.84);
        return new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(this.opacityProperty(), 1),
                        new KeyValue(this.scaleYProperty(), 1),
                        new KeyValue(this.translateXProperty(), 0)
                ),
                new KeyFrame(
                        Duration.millis(400),
                        new KeyValue(this.opacityProperty(), .4, interpolator),
                        new KeyValue(this.scaleYProperty(), .5, interpolator),
                        new KeyValue(this.translateXProperty(), getParent().getLayoutBounds().getWidth() * 2, interpolator)
                )
        );
    }

    /*

        Static Action Events.

     */

    private static final EventHandler<MouseEvent> detectDrag = mouseEvent -> mouseEvent.setDragDetect(true);


    /*
    *
    *
    * */

    abstract public RawInputFormBase toRawInput();

    //todo: undo delete input panes
}
