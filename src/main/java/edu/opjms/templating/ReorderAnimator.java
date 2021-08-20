package edu.opjms.templating;

import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Animates an object when its position is changed. For instance, when
 * additional items are added to a Region, and the layout has changed, then the
 * layout animator makes the transition by sliding each item into its final
 * place.
 */
public class ReorderAnimator implements ChangeListener<Number>, ListChangeListener<Node> {

    private final Map<Node, MoveYTransition> nodeYTransitions = new WeakHashMap<>();
    private boolean animateNextChange = false;

    /**
     * Animates all the children of a Region.
     * <code>
     *   VBox myVbox = new VBox();
     *   LayoutAnimator animator = new LayoutAnimator();
     *   animator.observe(myVbox.getChildren());
     * </code>
     *
     * @param nodes the nodes which should be animated
     */
    public void observe(ObservableList<Node> nodes) {
        for (Node node : nodes) {
            this.observe(node);
        }
        nodes.addListener(this);
    }

    public void unobserve(ObservableList<Node> nodes) {
        nodes.removeListener(this);
    }

    public void animateNextChange() {
        animateNextChange = true;
    }

    public void observe(Node n) {
        n.layoutYProperty().addListener(this);
    }

    public void unobserve(Node n) {
        n.layoutYProperty().removeListener(this);
    }

    @Override
    public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
        if (animateNextChange) {
            final double delta = newValue.doubleValue() - oldValue.doubleValue();
            final DoubleProperty doubleProperty = (DoubleProperty) ov;
            final Node node = (Node) doubleProperty.getBean();

            node.setCacheHint(CacheHint.SPEED);

            MoveYTransition ty = nodeYTransitions.get(node);
            if (ty == null) {
                ty = new MoveYTransition(node);
                nodeYTransitions.put(node, ty);
            }
            ty.setFromY(ty.getTranslateY() - delta);

            ty.setOnFinished(event -> {
                animateNextChange = false;
                node.setCacheHint(CacheHint.QUALITY);
            });
            ty.playFromStart();

        }
    }

    private abstract static class MoveTransition extends Transition {

        protected final Translate translate;

        public MoveTransition(final Node node) {
            final Duration movementAnimationDuration = new Duration(500);
            setCycleDuration(movementAnimationDuration);
            translate = new Translate();

            node.getTransforms().add(translate);
        }

        public double getTranslateY() {
            return translate.getY();
        }
    }


    private static class MoveYTransition extends MoveTransition {
        private double fromY;

        public MoveYTransition(final Node node) {
            super(node);
        }

        @Override protected void interpolate(double frac) {
            translate.setY(fromY * (1 - frac));
        }

        public void setFromY(double fromY) {
            translate.setY(fromY);
            this.fromY = fromY;
        }
    }

    @Override
    public void onChanged(Change change) {
        while (change.next()) {
            if (change.wasAdded()) {
                for (Node node : (List<Node>) change.getAddedSubList()) {
                    this.observe(node);
                }
            } else if (change.wasRemoved()) {
                for (Node node : (List<Node>) change.getRemoved()) {
                    this.unobserve(node);
                }
            }
        }
    }

    // todo unobserving nodes should cleanup any intermediate transitions they may have and ensure they are removed from transition cache to prevent memory leaks.
}
