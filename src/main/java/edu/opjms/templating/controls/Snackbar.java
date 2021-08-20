package edu.opjms.templating.controls;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.Mnemonic;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;

public class Snackbar {

    private final KeyCodeCombination undoShortcut = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);

    private Pane parent;
    private final SnackBarNode node1 = new SnackBarNode();
    private final SnackBarNode node2 = new SnackBarNode();
    private final Group wrapper1 = new Group(node1);
    private final Group wrapper2 = new Group(node2);


    private boolean isFirstShowing = false;
    private boolean isSecondShowing = false;
    private boolean isInAnimation = false;

    private final Timeline pauseAnim = pauseAnim();
    private final Interpolator interpolator = Interpolator.SPLINE(.250, .100, .250, 1);


    //The queue of events waiting to be shown
    private final Queue<SnackBarEvent> waitingQueue = new ArrayDeque<>();
    private final ChangeListener<? super Number> strongListener = (observableValue, number, t1) -> relocateAll();
    private final WeakChangeListener<? super Number> listener = new WeakChangeListener<>(strongListener);

    public double getBottomMarginPercent() {
        return bottomMarginPercent;
    }

    public void setBottomMarginPercent(double bottomMarginPercent) {
        if (bottomMarginPercent >= 0 && bottomMarginPercent <= 1)
            this.bottomMarginPercent = bottomMarginPercent;
        else
            throw new IllegalArgumentException("Bottom margin ratio must lie between 0.0 and 1.0");
    }

    private double bottomMarginPercent = 0;

    public Snackbar() {
        this(null);
    }

    public Snackbar(Pane parent) {
        this.parent = parent;

        wrapper1.setVisible(false);
        wrapper2.setVisible(false);

        wrapper1.setManaged(false);
        wrapper2.setManaged(false);

        wrapper1.setAutoSizeChildren(true);
        wrapper2.setAutoSizeChildren(true);

//        node1.getStyleClass().add(JMetroStyleClass.BACKGROUND);
//        node2.getStyleClass().add(JMetroStyleClass.BACKGROUND);

        node1.layoutBoundsProperty().addListener((observableValue, bounds, t1) -> relocate(node1, wrapper1));
        node2.layoutBoundsProperty().addListener((observableValue, bounds, t1) -> relocate(node2, wrapper2));

        setParent(parent);
    }

    public Region getParent() {
        return parent;
    }

    public void setParent(Pane parent) {
        if (this.parent != null) {
            this.parent.getChildren().removeAll(wrapper1, wrapper2);
            this.parent.widthProperty().removeListener(listener);
            this.parent.heightProperty().removeListener(listener);
            node1.prefWidthProperty().unbind();
            node2.prefWidthProperty().unbind();
        }
        parent.getChildren().addAll(wrapper1, wrapper2);
        parent.widthProperty().addListener(listener);
        parent.heightProperty().addListener(listener);


        node1.prefWidthProperty().bind(parent.widthProperty().multiply(.5));
        node2.prefWidthProperty().bind(parent.widthProperty().multiply(.5));

        this.parent = parent;

        wrapper1.setViewOrder(-1);
        wrapper2.setViewOrder(-2);
    }

    public void enque(SnackBarEvent event) {
        if (!isInAnimation && waitingQueue.isEmpty()) {
            if (!isFirstShowing) {
                isFirstShowing = true;
                show(node1, wrapper1, event);
            } else {
                pauseAnim.stop();
                if (!isSecondShowing) {
                    isSecondShowing = true;
                    show(node2, wrapper2, event);
                } else
                    swapAndShow(event);
            }
        } else {
            waitingQueue.add(event);
        }
    }

    private void show(SnackBarNode node, Group wrapper, SnackBarEvent evt) {
//        undo.setNode(node.button);
//        parent.getScene().addMnemonic(undo);
        parent.getScene().getAccelerators().put(undoShortcut, () -> {
            evt.evt.accept(null);
            hide();
        });
        node.setEvent(evt);
        wrapper.setVisible(true);
        var timeline = getAnim(wrapper);

        timeline.setOnFinished(event -> {
            isInAnimation = false;

            //if a new snackbar has been submitted while it was
            //animating, then we replace the current one with the new one

            var newEvent = waitingQueue.poll();
            if (newEvent == null) {
                pauseAnim.playFromStart();

            } else {
                // if it is the first snackbar, then we can
                // simply show the second one
                // else we need to swap and show the second one
                if (isSecondShowing)
                    swapAndShow(newEvent);
                else {
                    isSecondShowing = true;
                    show(node2, wrapper2, newEvent);
                }
            }
        });
        isInAnimation = true;
        timeline.playFromStart();
//        timeline.setOnFinished();
    }

    private void hide() {

        // prevent being re-called
        pauseAnim.stop();
        //remove shortcut
        parent.getScene().getAccelerators().remove(undoShortcut);

        var topSnackBar = isSecondShowing ? wrapper2 : wrapper1;
        wrapper1.setVisible(!isSecondShowing);

        var anim = new Timeline(
                new KeyFrame(
                        Duration.millis(200),
                        new KeyValue(topSnackBar.translateYProperty(), topSnackBar.getLayoutBounds().getHeight(), Interpolator.EASE_IN),
                        new KeyValue(topSnackBar.opacityProperty(), 0, Interpolator.EASE_IN)
                )
        );
        anim.setOnFinished(event -> {
            wrapper2.setVisible(false);
            isSecondShowing = false;
            isInAnimation = false;
            node1.setEvent(null);
            node2.setEvent(null);

            // check queue for event
            var newEvent = waitingQueue.poll();
            if (newEvent != null) {
                show(node1, wrapper1, newEvent);
            } else {
                wrapper1.setVisible(false);
                isFirstShowing = false;
            }
        });
        isInAnimation = true;
        anim.playFromStart();
    }

    private void swapAndShow(SnackBarEvent evt) {
        node1.setEvent(node2.snackBarEvent);
        show(node2, wrapper2, evt);
    }

    private Timeline getAnim(Group group) {
        var interpolator = Interpolator.LINEAR;
        return new Timeline (
                new KeyFrame(Duration.ZERO,
                        new KeyValue(group.translateYProperty(), group.getLayoutBounds().getHeight()),
                        new KeyValue(group.opacityProperty(), 0),
                        new KeyValue(group.scaleXProperty(), .95),
                        new KeyValue(group.scaleYProperty(), .95)
                ),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(group.translateYProperty(), 0, interpolator),
                        new KeyValue(group.opacityProperty(), 1, interpolator),
                        new KeyValue(group.scaleXProperty(), 1, interpolator),
                        new KeyValue(group.scaleYProperty(), 1, interpolator)
                )
        );
    }

    private Timeline pauseAnim() {
        var anim = new Timeline(new KeyFrame(Duration.seconds(3)));
        anim.setOnFinished(event -> hide());
        return anim;
    }

    private void relocateAll() {
        relocate(node1, wrapper1);
        relocate(node2, wrapper2);
    }

    private void relocate(Node node, Group wrapper) {
        var bounds = node.getLayoutBounds();
        var x = parent.snapPositionX((parent.getWidth() - bounds.getWidth()) / 2.0);

        var parentHeight = parent.getHeight();
        var bottomMargin = parentHeight * bottomMarginPercent;
        var y = parent.snapPositionY(parentHeight - bounds.getHeight() - bottomMargin);
        wrapper.setLayoutX(x);
        wrapper.setLayoutY(y);
    }

    private class SnackBarNode extends HBox {
        private final Label label;
        private final Button button;
        private SnackBarEvent snackBarEvent;

        public SnackBarNode() {
            this(null);
        }
        public SnackBarNode(SnackBarEvent evt) {
            setAlignment(Pos.CENTER);

            getStyleClass().add("snackbar");

            this.snackBarEvent = evt;
            var spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            var labelText = evt != null? evt.label: "";

            var label = new Label(labelText);
            this.label = label;

            var button = new Button("Undo (" + undoShortcut.getDisplayText() + ")");
            button.setOnAction(event -> {
                hide();
                if (evt != null)
                    evt.evt.accept(null);
            });
            this.button = button;


            getChildren().addAll(label, spacer, button);
        }

        public void setEvent(SnackBarEvent evt) {
            var text = evt != null ? evt.label : "";
            label.setText(text);
            button.setOnAction(event -> {
                hide();
                if (snackBarEvent != null)
                    this.snackBarEvent.evt.accept(null);
            });
            this.snackBarEvent = evt;
        }

        public SnackBarEvent getEvent() {
            return snackBarEvent;
        }
    }

    public static class SnackBarEvent {
        private final String label;
        private final Consumer<Void> evt;

        public SnackBarEvent(String label, Consumer<Void> evt) {
            this.label = label;
            this.evt = evt;
        }

        public String getLabel() {
            return label;
        }

        public Consumer<Void> getEvt() {
            return evt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SnackBarEvent that = (SnackBarEvent) o;

            if (!Objects.equals(label, that.label)) return false;
            return Objects.equals(evt, that.evt);
        }

        @Override
        public int hashCode() {
            int result = label != null ? label.hashCode() : 0;
            result = 31 * result + (evt != null ? evt.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "SnackBarEvent{" +
                    "label='" + label + '\'' +
                    ", evt=" + evt +
                    '}';
        }
    }
}

