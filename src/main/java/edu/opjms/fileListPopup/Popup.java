package edu.opjms.fileListPopup;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.util.Callback;

import java.io.File;

public class Popup extends Dialog<File> {
    public Popup() {
        setResultConverter(new Callback<ButtonType, File>() {
            @Override
            public File call(ButtonType buttonType) {
                return Popup.this.getResult();
            }
        });
    }
}
