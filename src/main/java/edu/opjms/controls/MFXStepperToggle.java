package edu.opjms.controls;


import edu.opjms.skins.MFXStepperSkin;
import edu.opjms.skins.MFXStepperToggleSkin;
import edu.opjms.utils.NodeUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.*;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.List;

/**
 * A {@code MFXStepperToggle} is a special toggle that has 4 possible states.
 * <p> In a {@link MFXStepper} these states are used as follows:
 * <p> - NONE (when initialized)
 * <p> - SELECTED (self explanatory)
 * <p> - COMPLETED (when the validator's state in valid and the stepper goes to the next toggle)
 * <p></p>
 * Every {@code MFXStepperToggle} has an icon, and a text which will be displayed in a label above or below the toggle
 * depending on the value of {@link #textPositionProperty()}.
 * <p>
 * They also specify the content to be shown in the {@link MFXStepper} when the toggle is selected, the content
 * can be any {@code Node}.
 * <p></p>
 * This control specifies three new PseudoClasses: ":selected", ":completed" to specify a different style in css
 * for each state.
 */
public class MFXStepperToggle extends Control {
    //================================================================================
    // Properties
    //================================================================================
    private static final StyleablePropertyFactory<MFXStepperToggle> FACTORY = new StyleablePropertyFactory<>(Control.getClassCssMetaData());
    private final String STYLE_CLASS = "mfx-stepper-toggle";
    private final String STYLESHEET = NodeUtils.getCSS("/css/mfx/MFXStepperToggle.css");

    private Node content;
    private final StringProperty text = new SimpleStringProperty();
    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>();
    private final ObjectProperty<StepperToggleState> state = new SimpleObjectProperty<>(StepperToggleState.NONE);

    protected static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    protected static final PseudoClass COMPLETED_PSEUDO_CLASS = PseudoClass.getPseudoClass("completed");

    //================================================================================
    // Constructors
    //================================================================================
    public MFXStepperToggle() {
        this("", null);
    }

    public MFXStepperToggle(String text) {
        this(text, null);
    }

    public MFXStepperToggle(String text, Node icon) {
        this(text, icon, null);
    }

    public MFXStepperToggle(String text, Node icon, Node content) {
        setText(text);
        setIcon(icon);
        this.content = content;
        initialize();
    }


    //================================================================================
    // Methods
    //================================================================================
    private void initialize() {
        getStyleClass().setAll(STYLE_CLASS);
        addListeners();
    }

    /**
     * Adds the following listeners:
     * <p> - state property to fire a STATE_CHANGED event when invalidated.
     * <p> - state property to update the PseudoClasses when it changes.
     */
    private void addListeners() {
        state.addListener(invalidated -> fireEvent(new MFXStepperToggleEvent(MFXStepperToggleEvent.STATE_CHANGED, state.get())));
        state.addListener((observable, oldValue, newValue) -> {
            resetPseudoClass();
            switch (newValue) {
                case SELECTED -> pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
                case COMPLETED -> pseudoClassStateChanged(COMPLETED_PSEUDO_CLASS, true);
                default -> resetPseudoClass();
            }
        });
    }

    /**
     * Resets all state PseudoClasses to false.
     */
    private void resetPseudoClass() {
        pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
        pseudoClassStateChanged(COMPLETED_PSEUDO_CLASS, false);
    }

    /**
     * This method is necessary to get the bounds of the toggle's circle, which is
     * used in the {@link MFXStepperSkin} to resize the progress bar properly.
     */
    public Bounds getGraphicBounds() {
        Node node = lookup("#circle");
        return node != null ? node.getBoundsInParent() : null;
    }

    /**
     * @return the content to be shown in the stepper when selected
     */
    public Node getContent() {
        return content;
    }

    /**
     * Sets the content to be shown in the stepper when selected.
     */
    public void setContent(Node content) {
        this.content = content;
    }

    public String getText() {
        return text.get();
    }

    /**
     * Specifies the text to be shown above or below the toggle.
     */
    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public Node getIcon() {
        return icon.get();
    }

    /**
     * Specifies the icon shown in the circle of the toggle.
     */
    public ObjectProperty<Node> iconProperty() {
        return icon;
    }

    public void setIcon(Node icon) {
        this.icon.set(icon);
    }

    public StepperToggleState getState() {
        return state.get();
    }

    /**
     * Specifies the state of the toggle.
     */
    public ObjectProperty<StepperToggleState> stateProperty() {
        return state;
    }

    public void setState(StepperToggleState state) {
        this.state.set(state);
    }


    //================================================================================
    // Styleable Properties
    //================================================================================
    private final StyleableDoubleProperty labelTextGap = new SimpleStyleableDoubleProperty(
            StyleableProperties.LABEL_TEXT_GAP,
            this,
            "textGap",
            10.0
    );

    private final StyleableObjectProperty<TextPosition> textPosition = new SimpleStyleableObjectProperty<>(
            StyleableProperties.TEXT_POSITION,
            this,
            "textPosition",
            TextPosition.BOTTOM
    );

    private final StyleableDoubleProperty size = new SimpleStyleableDoubleProperty(
            StyleableProperties.SIZE,
            this,
            "size",
            22.0
    );

    private final StyleableDoubleProperty strokeWidth = new SimpleStyleableDoubleProperty(
            StyleableProperties.STROKE_WIDTH,
            this,
            "strokeWidth",
            2.0
    );

    public double getLabelTextGap() {
        return labelTextGap.get();
    }

    /**
     * Specifies the gap between the toggle's circle and the label.
     */
    public StyleableDoubleProperty labelTextGapProperty() {
        return labelTextGap;
    }

    public void setLabelTextGap(double labelTextGap) {
        this.labelTextGap.set(labelTextGap);
    }

    public TextPosition getTextPosition() {
        return textPosition.get();
    }


    /**
     * Specifies the position of the label.
     */
    public StyleableObjectProperty<TextPosition> textPositionProperty() {
        return textPosition;
    }

    public void setTextPosition(TextPosition textPosition) {
        this.textPosition.set(textPosition);
    }

    public double getSize() {
        return size.get();
    }

    /**
     * Specifies the radius of the toggle's circle.
     */
    public StyleableDoubleProperty sizeProperty() {
        return size;
    }

    public void setSize(double size) {
        this.size.set(size);
    }

    public double getStrokeWidth() {
        return strokeWidth.get();
    }

    /**
     * Specifies the stroke width of the toggle's circle.
     */
    public StyleableDoubleProperty strokeWidthProperty() {
        return strokeWidth;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth.set(strokeWidth);
    }

    //================================================================================
    // CssMetaData
    //================================================================================
    private static class StyleableProperties {
        private static final List<CssMetaData<? extends Styleable, ?>> cssMetaDataList;

        private static final CssMetaData<MFXStepperToggle, Number> LABEL_TEXT_GAP =
                FACTORY.createSizeCssMetaData(
                        "-mfx-label-text-gap",
                        MFXStepperToggle::labelTextGapProperty,
                        10.0
                );

        private static final CssMetaData<MFXStepperToggle, TextPosition> TEXT_POSITION =
                FACTORY.createEnumCssMetaData(
                        TextPosition.class,
                        "-mfx-text-position",
                        MFXStepperToggle::textPositionProperty,
                        TextPosition.BOTTOM
                );

        private static final CssMetaData<MFXStepperToggle, Number> SIZE =
                FACTORY.createSizeCssMetaData(
                        "-mfx-size",
                        MFXStepperToggle::sizeProperty,
                        22.0
                );

        private static final CssMetaData<MFXStepperToggle, Number> STROKE_WIDTH =
                FACTORY.createSizeCssMetaData(
                        "-mfx-stroke-width",
                        MFXStepperToggle::strokeWidthProperty,
                        2.0
                );

        static {
            cssMetaDataList = List.of(LABEL_TEXT_GAP, TEXT_POSITION, SIZE, STROKE_WIDTH);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getControlCssMetaDataList() {
        return StyleableProperties.cssMetaDataList;
    }

    //================================================================================
    // Override Methods
    //================================================================================
    @Override
    protected Skin<?> createDefaultSkin() {
        return new MFXStepperToggleSkin(this);
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return MFXStepperToggle.getControlCssMetaDataList();
    }

    @Override
    public String getUserAgentStylesheet() {
        return STYLESHEET;
    }

    //================================================================================
    // Events
    //================================================================================

    /**
     * Events class for MFXStepperToggles.
     * <p>
     * Defines a new EventType:
     * <p>
     * - STATE_CHANGED: when the {@link MFXStepperToggle#stateProperty()} ()} changes. <p></p>
     * <p>
     * One of the constructors requires to specify the new toggle's state, the class has also a getter for the
     * state.
     * <p></p>
     *  These events are automatically fired by the control so they should not be fired by users.
     */
    public static class MFXStepperToggleEvent extends Event {
        private StepperToggleState state;

        public static final EventType<MFXStepperToggleEvent> STATE_CHANGED = new EventType<>(ANY, "STATE_CHANGED");

        public MFXStepperToggleEvent(EventType<? extends Event> eventType) {
            super(eventType);
        }

        public MFXStepperToggleEvent(EventType<? extends Event> eventType, StepperToggleState state) {
            super(eventType);
            this.state = state;
        }

        public StepperToggleState getState() {
            return state;
        }
    }
}

