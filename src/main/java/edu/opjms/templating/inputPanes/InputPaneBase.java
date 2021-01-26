package edu.opjms.templating.inputPanes;

import edu.opjms.global.inputForms.RawInputFormBase;
import edu.opjms.templating.ReorderAnimator;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public abstract class InputPaneBase extends TitledPane {

    private StringProperty labelTextProperty;
    private static final ReorderAnimator reorderAnimator = new ReorderAnimator();
    private String suffix;

    /*
    *
    * Action Handlers for how to delete, add and re-order fields
    *
    * */


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
            skinProperty().addListener(observable -> addDeleteButton());
        } else 
            addDeleteButton();
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

        Button deleteButton = new Button();
        deleteButton.setTooltip(new Tooltip("Delete"));
        deleteButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        deleteButton.getStyleClass().clear();
        Label titleLabel = new Label();
        titleLabel.textProperty().bind(labelTextProperty.concat(suffix));

        var deleteIcon = new SVGPath();
        deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zm2.46-7.12l1.41-1.41L12 12.59l2.12-2.12 1.41 1.41L13.41 14l2.12 2.12-1.41 1.41L12 15.41l-2.12 2.12-1.41-1.41L10.59 14l-2.13-2.12zM15.5 4l-1-1h-5l-1 1H5v2h14V4z");
        deleteButton.setGraphic(deleteIcon);
        deleteButton.visibleProperty().bind(currentTitle.hoverProperty());

        deleteButton.setOnAction(ActionEvent -> {
            var parent = getParent();
            //remove titled Pane from Parent
            EventHandler<ActionEvent> evt = parent instanceof Accordion
                    ? event1 -> ((Accordion) parent).getPanes().remove(this)
                    : event1 -> ((Pane) parent).getChildren().remove(this);

            Animator.animateExit(this, evt);
        });

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
            System.out.println("in here");
            var db = dragEvent.getDragboard();
            if (db.hasString() && dragEvent.getGestureSource() != null) {
                reorderAnimator.animateNextChange();

                int sourceIndex = Integer.parseInt(db.getString());
                int targetIndex = root.getChildrenUnmodifiable().indexOf(this);
                Node sourceNode = root.getChildren().remove(sourceIndex);

                root.getChildren().add(targetIndex, sourceNode);

            } else
                dragEvent.setDropCompleted(false);
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


    private final static class Animator {

        private final static Duration ANIM_DURATION = Duration.millis(200);

        private final static TranslateTransition move = new TranslateTransition(ANIM_DURATION);

        private final static ParallelTransition anim;

        static {
            var fadeOut = new FadeTransition(ANIM_DURATION);
            fadeOut.setToValue(0.4);

            var scaleDown = new ScaleTransition(ANIM_DURATION);
            scaleDown.setToY(0.5);

            anim = new ParallelTransition(move, fadeOut, scaleDown);
        }

        public static void animateExit(InputPaneBase node, EventHandler<ActionEvent> evt) {
            move.setToX(((Region) node.getParent()).getWidth());
            anim.setNode(node);
            anim.setOnFinished(evt);
            anim.play();

            /*var undoSnackBar = new Snackbar(root);
            final var layout = new SnackbarLayout("Field was removed", "Undo", event -> System.out.println("done!"));
            final var SnackBarEvent = new Snackbar.SnackbarEvent(layout, Duration.seconds(5), null);
            undoSnackBar.enqueue(SnackBarEvent);*/
        }

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
