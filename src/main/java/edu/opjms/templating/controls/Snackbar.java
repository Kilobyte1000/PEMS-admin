package edu.opjms.templating.controls;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Snackbar extends Group {
    private static final String DEFAULT_STYLE_CLASS = "jfx-snackbar";
    private Pane snackbarContainer;
    private ChangeListener<? super Number> sizeListener;
    private final WeakChangeListener<? super Number> weakSizeListener;
    private AtomicBoolean processingQueue;
    private ConcurrentLinkedQueue<Snackbar.SnackbarEvent> eventQueue;
    private ConcurrentHashMap.KeySetView<Object, Boolean> eventsSet;
    private Interpolator easeInterpolator;
    private Pane content;
    private PseudoClass activePseudoClass;
    private PauseTransition pauseTransition;
    private Timeline openAnimation;
    private Snackbar.SnackbarEvent currentEvent;

    public Snackbar() {
        this((Pane)null);
    }

    public Snackbar(Pane snackbarContainer) {
        this.sizeListener = (o, oldVal, newVal) -> {
            this.refreshPopup();
        };
        this.weakSizeListener = new WeakChangeListener(this.sizeListener);
        this.processingQueue = new AtomicBoolean(false);
        this.eventQueue = new ConcurrentLinkedQueue();
        this.eventsSet = ConcurrentHashMap.newKeySet();
        this.easeInterpolator = Interpolator.SPLINE(0.25D, 0.1D, 0.25D, 1.0D);
        this.activePseudoClass = null;
        this.openAnimation = null;
        this.currentEvent = null;
        this.initialize();
        this.content = new StackPane();
        this.content.getStyleClass().add("jfx-snackbar-content");
        this.getChildren().add(this.content);
        this.setManaged(false);
        this.setVisible(false);
        this.registerSnackbarContainer(snackbarContainer);
        this.layoutBoundsProperty().addListener((o, oldVal, newVal) -> {
            this.refreshPopup();
        });
        this.addEventHandler(Snackbar.SnackbarEvent.SNACKBAR, this::enqueue);
    }

    private void initialize() {
        this.getStyleClass().add("jfx-snackbar");
    }

    public Pane getPopupContainer() {
        return this.snackbarContainer;
    }

    public void setPrefWidth(double width) {
        this.content.setPrefWidth(width);
    }

    public double getPrefWidth() {
        return this.content.getPrefWidth();
    }

    public void registerSnackbarContainer(Pane snackbarContainer) {
        if (snackbarContainer != null) {
            if (this.snackbarContainer != null) {
                throw new IllegalArgumentException("Snackbar Container already set");
            }

            this.snackbarContainer = snackbarContainer;
            this.snackbarContainer.getChildren().add(this);
            this.snackbarContainer.heightProperty().addListener(this.weakSizeListener);
            this.snackbarContainer.widthProperty().addListener(this.weakSizeListener);
        }

    }

    public void unregisterSnackbarContainer(Pane snackbarContainer) {
        if (snackbarContainer != null) {
            if (this.snackbarContainer == null) {
                throw new IllegalArgumentException("Snackbar Container not set");
            }

            this.snackbarContainer.getChildren().remove(this);
            this.snackbarContainer.heightProperty().removeListener(this.weakSizeListener);
            this.snackbarContainer.widthProperty().removeListener(this.weakSizeListener);
            this.snackbarContainer = null;
        }

    }

    private void show(Snackbar.SnackbarEvent event) {
        this.content.getChildren().setAll(new Node[]{event.getContent()});
        this.openAnimation = this.getTimeline(event.getTimeout());
        if (event.getPseudoClass() != null) {
            this.activePseudoClass = event.getPseudoClass();
            this.content.pseudoClassStateChanged(this.activePseudoClass, true);
        }

        this.openAnimation.play();
    }

    private Timeline getTimeline(Duration timeout) {
        Timeline animation = new Timeline(new KeyFrame[]{new KeyFrame(Duration.ZERO, (e) -> {
            this.toBack();
        }, new KeyValue[]{new KeyValue(this.visibleProperty(), false, Interpolator.EASE_BOTH), new KeyValue(this.translateYProperty(), this.getLayoutBounds().getHeight(), this.easeInterpolator), new KeyValue(this.opacityProperty(), 0, this.easeInterpolator)}), new KeyFrame(Duration.millis(10.0D), (e) -> {
            this.toFront();
        }, new KeyValue[]{new KeyValue(this.visibleProperty(), true, Interpolator.EASE_BOTH)}), new KeyFrame(Duration.millis(300.0D), new KeyValue[]{new KeyValue(this.opacityProperty(), 1, this.easeInterpolator), new KeyValue(this.translateYProperty(), 0, this.easeInterpolator)})});
        animation.setCycleCount(1);
        this.pauseTransition = Duration.INDEFINITE.equals(timeout) ? null : new PauseTransition(timeout);
        if (this.pauseTransition != null) {
            animation.setOnFinished((finish) -> {
                this.pauseTransition.setOnFinished((done) -> {
                    this.pauseTransition = null;
                    this.eventsSet.remove(this.currentEvent);
                    this.currentEvent = (Snackbar.SnackbarEvent)this.eventQueue.peek();
                    this.close();
                });
                this.pauseTransition.play();
            });
        }

        return animation;
    }

    public void close() {
        if (this.openAnimation != null) {
            this.openAnimation.stop();
        }

        if (this.isVisible()) {
            Timeline closeAnimation = new Timeline(new KeyFrame[]{new KeyFrame(Duration.ZERO, (e) -> {
                this.toFront();
            }, new KeyValue[]{new KeyValue(this.opacityProperty(), 1, this.easeInterpolator), new KeyValue(this.translateYProperty(), 0, this.easeInterpolator)}), new KeyFrame(Duration.millis(290.0D), new KeyValue[]{new KeyValue(this.visibleProperty(), true, Interpolator.EASE_BOTH)}), new KeyFrame(Duration.millis(300.0D), (e) -> {
                this.toBack();
            }, new KeyValue[]{new KeyValue(this.visibleProperty(), false, Interpolator.EASE_BOTH), new KeyValue(this.translateYProperty(), this.getLayoutBounds().getHeight(), this.easeInterpolator), new KeyValue(this.opacityProperty(), 0, this.easeInterpolator)})});
            closeAnimation.setCycleCount(1);
            closeAnimation.setOnFinished((e) -> {
                this.resetPseudoClass();
                this.processSnackbar();
            });
            closeAnimation.play();
        }

    }

    public Snackbar.SnackbarEvent getCurrentEvent() {
        return this.currentEvent;
    }

    public void enqueue(Snackbar.SnackbarEvent event) {
        synchronized(this) {
            if (!this.eventsSet.contains(event)) {
                this.eventsSet.add(event);
                this.eventQueue.offer(event);
            } else if (this.currentEvent == event && this.pauseTransition != null) {
                this.pauseTransition.playFromStart();
            }
        }

        if (this.processingQueue.compareAndSet(false, true)) {
            Platform.runLater(() -> {
                this.currentEvent = (Snackbar.SnackbarEvent)this.eventQueue.poll();
                if (this.currentEvent != null) {
                    this.show(this.currentEvent);
                }

            });
        }

    }

    private void resetPseudoClass() {
        if (this.activePseudoClass != null) {
            this.content.pseudoClassStateChanged(this.activePseudoClass, false);
            this.activePseudoClass = null;
        }

    }

    private void processSnackbar() {
        this.currentEvent = (Snackbar.SnackbarEvent)this.eventQueue.poll();
        if (this.currentEvent != null) {
            this.eventsSet.remove(this.currentEvent);
            this.show(this.currentEvent);
        } else {
            this.processingQueue.getAndSet(false);
        }

    }

    private void refreshPopup() {
        Bounds contentBound = this.getLayoutBounds();
        double offsetX = Math.ceil(this.snackbarContainer.getWidth() / 2.0D) - Math.ceil(contentBound.getWidth() / 2.0D);
        double offsetY = this.snackbarContainer.getHeight() - contentBound.getHeight();
        this.setLayoutX(offsetX);
        this.setLayoutY(offsetY);
    }

    public static class SnackbarEvent extends Event {
        public static final EventType<Snackbar.SnackbarEvent> SNACKBAR;
        private final Node content;
        private final PseudoClass pseudoClass;
        private final Duration timeout;

        public SnackbarEvent(Node content) {
            this(content, Duration.millis(1500.0D), (PseudoClass)null);
        }

        public SnackbarEvent(Node content, PseudoClass pseudoClass) {
            this(content, Duration.millis(1500.0D), pseudoClass);
        }

        public SnackbarEvent(Node content, Duration timeout, PseudoClass pseudoClass) {
            super(SNACKBAR);
            this.content = content;
            this.pseudoClass = pseudoClass;
            this.timeout = timeout;
        }

        public Node getContent() {
            return this.content;
        }

        public PseudoClass getPseudoClass() {
            return this.pseudoClass;
        }

        public Duration getTimeout() {
            return this.timeout;
        }

        public EventType<? extends Event> getEventType() {
            return super.getEventType();
        }

        public boolean isPersistent() {
            return Duration.INDEFINITE.equals(this.getTimeout());
        }

        static {
            SNACKBAR = new EventType(Event.ANY, "SNACKBAR");
        }
    }

}
