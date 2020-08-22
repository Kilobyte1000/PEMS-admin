//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.opjms.candidateSelector.animations;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

public class CustomFadeInUp extends AnimationFX {
    public CustomFadeInUp(Node node) {
        super(node);
    }

    AnimationFX resetNode() {
        return this;
    }

    void initTimeline() {
        this.setTimeline(new Timeline(new KeyFrame(Duration.millis(0.0D), new KeyValue(this.getNode().opacityProperty(), 0, AnimateFXInterpolator.EASE), new KeyValue(this.getNode().translateYProperty(), this.getNode().getBoundsInParent().getHeight() / 10, AnimateFXInterpolator.EASE)), new KeyFrame(Duration.millis(750.0D), new KeyValue(this.getNode().opacityProperty(), 1, AnimateFXInterpolator.EASE), new KeyValue(this.getNode().translateYProperty(), 0, AnimateFXInterpolator.EASE))));
    }
}
