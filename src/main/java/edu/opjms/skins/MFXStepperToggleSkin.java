package edu.opjms.skins;


import edu.opjms.controls.MFXStepperToggle;
import edu.opjms.controls.StepperToggleState;
import edu.opjms.controls.TextPosition;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
/**
 * This is the implementation of the {@code Skin} associated with every {@link MFXStepperToggle}.
 * <p>
 * It consists of a {@link Circle} with the css id set to "circle" and a {@link Label}
 */
public class MFXStepperToggleSkin extends SkinBase<MFXStepperToggle> {
    private final StackPane container;
    private final Circle circle;
    private final Label label;

    public MFXStepperToggleSkin(MFXStepperToggle stepperToggle) {
        super(stepperToggle);

        circle = new Circle(0, Color.LIGHTGRAY);
        circle.setId("circle");
        circle.radiusProperty().bind(stepperToggle.sizeProperty());
        circle.strokeWidthProperty().bind(stepperToggle.strokeWidthProperty());
        circle.setStrokeType(StrokeType.CENTERED);

        container = new StackPane(circle, stepperToggle.getIcon());
        container.getStylesheets().setAll(stepperToggle.getUserAgentStylesheet());

        label = new Label();
        label.getStylesheets().addAll(stepperToggle.getUserAgentStylesheet());
        label.setText(stepperToggle.getText());
        label.setManaged(false);


        getChildren().addAll(container, label);
        setListeners();
    }

    /**
     * Adds the following listeners, handlers/filters.
     * <p> - Adds a listener to the {@link MFXStepperToggle#textPositionProperty()} to re-compute the layout when it changes.
     */
    private void setListeners() {
        MFXStepperToggle stepperToggle = getSkinnable();

        stepperToggle.labelTextGapProperty().addListener(invalidated -> stepperToggle.requestLayout());
        stepperToggle.textPositionProperty().addListener(invalidated -> stepperToggle.requestLayout());

        label.visibleProperty().bind(stepperToggle.textProperty().isEmpty().not());
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return topInset + container.prefWidth(-1) + (getSkinnable().getLabelTextGap() * 2) + (label.getHeight() * 2) + bottomInset;
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return leftInset + Math.max(circle.getRadius() * 2, label.getWidth()) + rightInset;
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        MFXStepperToggle stepperToggle = getSkinnable();

        final Bounds bounds = label.getLayoutBounds();
        double lw = snapSizeX(bounds.getWidth());
        double lh = snapSizeY(bounds.getHeight());
        double lx = snapPositionX(circle.getBoundsInParent().getCenterX() - (lw / 2.0));
        double ly = 0;

        if (stepperToggle.getTextPosition() == TextPosition.BOTTOM) {
            label.setTranslateY(0);
            ly = snapPositionY(circle.getBoundsInParent().getMaxY() + stepperToggle.getLabelTextGap());
            label.resizeRelocate(lx, ly, lw, lh);
        } else {
            label.resizeRelocate(lx, ly, lw, lh);
            label.setTranslateY(-stepperToggle.getLabelTextGap() - lh);
        }

        double ix = snapPositionX(circle.getBoundsInParent().getMaxX());
        double iy = snapPositionY(circle.getBoundsInParent().getMinY() - 6);
    }
}
