package edu.opjms.utils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

public class NodeUtils {
    private NodeUtils() {}
    public static void waitForScene(@NotNull Node node,
                                    Runnable action,
                                    boolean addListenerIfNotNull,
                                    boolean isOneShot) {
        final var scene = node.getScene();
        if (scene != null) {
            action.run();
        }
        if (scene == null || addListenerIfNotNull) {
            node.sceneProperty().addListener(new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
                    if (newValue != null) {
                        action.run();
                        if (isOneShot) {
                            node.sceneProperty().removeListener(this);
                        }
                    }
                }
            });
        }
    }
    public static void waitForScene(@NotNull Node node,
                                    Runnable action,
                                    boolean addListenerIfNotNull) {
        final var scene = node.getScene();
        if (scene != null) {
            action.run();
        }
        if (scene == null || addListenerIfNotNull) {
            node.sceneProperty().addListener((observableValue, scene1, t1) -> action.run());
        }
    }
    @NotNull
    public static String getCSS(@NotNull String url) {
        URL css = NodeUtils.class.getResource(url);
        assert css != null;
        return css.toExternalForm();
    }

}
