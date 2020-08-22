//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.opjms.candidateSelector.animations;

import javafx.animation.Animation.Status;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

public abstract class AnimationFX {
    public static final int INDEFINITE = -1;
    private Timeline timeline;
    private boolean reset;
    private Node node;
    private AnimationFX nextAnimation;
    private boolean hasNextAnimation;

    public AnimationFX(Node node) {
        this.setNode(node);
    }

    public AnimationFX() {
        this.hasNextAnimation = false;
        this.reset = false;
    }

    private AnimationFX onFinished() {
        if (this.reset) {
            this.resetNode();
        }

        if (this.nextAnimation != null) {
            this.nextAnimation.play();
        }

        return this;
    }

    public AnimationFX playOnFinished(AnimationFX animation) {
        this.setNextAnimation(animation);
        return this;
    }

    public void play() {
        this.timeline.play();
    }

    public AnimationFX stop() {
        this.timeline.stop();
        return this;
    }

    abstract AnimationFX resetNode();

    abstract void initTimeline();

    public Timeline getTimeline() {
        return this.timeline;
    }

    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }

    public boolean isResetOnFinished() {
        return this.reset;
    }

    public AnimationFX setResetOnFinished(boolean reset) {
        this.reset = reset;
        return this;
    }

    protected void setReset(boolean reset) {
        this.reset = reset;
    }

    public Node getNode() {
        return this.node;
    }

    public void setNode(Node node) {
        this.node = node;
        this.initTimeline();
        this.timeline.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(Status.STOPPED)) {
                this.onFinished();
            }

        });
    }

    public AnimationFX getNextAnimation() {
        return this.nextAnimation;
    }

    protected void setNextAnimation(AnimationFX nextAnimation) {
        this.hasNextAnimation = true;
        this.nextAnimation = nextAnimation;
    }

    public boolean hasNextAnimation() {
        return this.hasNextAnimation;
    }

    protected void setHasNextAnimation(boolean hasNextAnimation) {
        this.hasNextAnimation = hasNextAnimation;
    }

    public AnimationFX setCycleCount(int value) {
        this.timeline.setCycleCount(value);
        return this;
    }

    public AnimationFX setSpeed(double value) {
        this.timeline.setRate(value);
        return this;
    }

    public AnimationFX setDelay(Duration value) {
        this.timeline.setDelay(value);
        return this;
    }

    public final void setOnFinished(EventHandler<ActionEvent> value) {
        this.timeline.setOnFinished(value);
    }
}
