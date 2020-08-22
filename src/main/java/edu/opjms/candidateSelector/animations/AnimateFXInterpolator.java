package edu.opjms.candidateSelector.animations;

import javafx.animation.Interpolator;

public class AnimateFXInterpolator {
    public static final Interpolator EASE = Interpolator.SPLINE(0.25D, 0.1D, 0.25D, 1.0D);

    private AnimateFXInterpolator() {
        throw new IllegalStateException("AnimateFX Interpolator");
    }
}
