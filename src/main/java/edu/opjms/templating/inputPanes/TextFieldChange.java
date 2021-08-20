package edu.opjms.templating.inputPanes;

import javafx.scene.control.TextField;
import java.util.function.BiConsumer;

class TextFieldChange extends TextField {
    private String oldText = null;
    private BiConsumer<String, String> onTextChange = null;

    private void addListener() {
        focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (isFocused()) {
                //on focus gain
                oldText = getText();
            } else {
                var text = getText().strip();
                setText(text);
                if (!text.equals(oldText) && onTextChange != null) {
                    onTextChange.accept(oldText, text);
                    oldText = null;
                }
            }
        });
    }

    public TextFieldChange() {
        super();
        addListener();
    }

    public TextFieldChange(String text) {
        super(text);
        addListener();
    }

    public void setOnTextChange(BiConsumer<String,String> onTextChange) {
        this.onTextChange = onTextChange;
    }

    public BiConsumer<String, String> getOnTextChange() {
        return onTextChange;
    }
}