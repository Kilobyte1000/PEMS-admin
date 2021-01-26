package edu.opjms.templating.controls;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class SnackbarLayout extends BorderPane {
    private Label toast;
    private Button action;
    private StackPane actionContainer;
    private static final String DEFAULT_STYLE_CLASS = "jfx-snackbar-layout";

    public SnackbarLayout(String message) {
        this(message, (String)null, (EventHandler)null);
    }

    public SnackbarLayout(String message, String actionText, EventHandler<ActionEvent> actionHandler) {
        this.initialize();
        this.toast = new Label();
        this.toast.setMinWidth(-1.0D / 0.0);
        this.toast.getStyleClass().add("jfx-snackbar-toast");
        this.toast.setWrapText(true);
        this.toast.setText(message);
        StackPane toastContainer = new StackPane(this.toast);
        toastContainer.setPadding(new Insets(20.0D));
        this.actionContainer = new StackPane();
        this.actionContainer.setPadding(new Insets(0.0D, 10.0D, 0.0D, 0.0D));
        this.toast.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> {
            if (this.getPrefWidth() == -1.0D) {
                return this.getPrefWidth();
            } else {
                double actionWidth = this.actionContainer.isVisible() ? this.actionContainer.getWidth() : 0.0D;
                return this.prefWidthProperty().get() - actionWidth;
            }
        }, this.prefWidthProperty(), this.actionContainer.widthProperty(), this.actionContainer.visibleProperty()));
        this.setLeft(toastContainer);
        this.setRight(this.actionContainer);
        if (actionText != null) {
            this.action = new Button();
            this.action.setText(actionText);
            this.action.setOnAction(actionHandler);
            this.action.setMinWidth(Double.POSITIVE_INFINITY);
            this.action.getStyleClass().add("jfx-snackbar-action");
            this.actionContainer.getChildren().add(this.action);
            if (!actionText.isEmpty()) {
                this.action.setVisible(true);
                this.actionContainer.setVisible(true);
                this.actionContainer.setManaged(true);
                this.action.setText("");
                this.action.setText(actionText);
                this.action.setOnAction(actionHandler);
            } else {
                this.actionContainer.setVisible(false);
                this.actionContainer.setManaged(false);
                this.action.setVisible(false);
            }
        }

    }

    public String getToast() {
        return this.toast.getText();
    }

    public void setToast(String toast) {
        this.toast.setText(toast);
    }

    private void initialize() {
        this.getStyleClass().add("jfx-snackbar-layout");
    }

}
