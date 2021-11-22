package edu.opjms.templating.inputPanes;

import edu.opjms.global.inputForms.PageGeneratorKt;
import edu.opjms.global.inputForms.RawDateInput;
import edu.opjms.global.inputForms.RawInputFormBase;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import jfxtras.styles.jmetro.JMetroStyleClass;
import java.util.Objects;

import static edu.opjms.global.CommonKt.ERROR_CLASS;

final public class DateInputPane extends InputPaneBase {

    private static final String SUFFIX = " - Date Input";

    public DateInputPane() {
        this(null);
    }

    public DateInputPane(String label) {
        super();

        var labelInput = new TextFieldChange(Objects.requireNonNullElse(label, ""));
        labelField = labelInput;
        var labelErr = new Label();
        this.labelErr = labelErr;
        labelErr.getStyleClass().add("err");

        if (label == null || label.isBlank()) {
            isLabelValid = false;
            labelErr.setText(INVALID_LABEL_ERR);
            labelInput.pseudoClassStateChanged(ERROR_CLASS, true);
        }
        labelInput.textProperty().addListener((observableValue, s, t1) -> {
            isLabelValid = !t1.isBlank();
            if (!isLabelDuplicate) {
                labelErr.setText(isLabelValid? "": INVALID_LABEL_ERR);
                labelInput.pseudoClassStateChanged(ERROR_CLASS, !isLabelValid);
            }
        });

        var wrapper = new HBox(new Label("Label Text: "), labelInput, labelErr);
        wrapper.setSpacing(8);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.getStyleClass().add(JMetroStyleClass.BACKGROUND);

        configureSuper(labelInput.textProperty(), SUFFIX);

        setContent(wrapper);
    }

    @Override
    public boolean containsError() {
        return getLabelText().isBlank() || isLabelDuplicate;
    }

    @Override
    public String generateHTML(int id) {
        final var labelText = getLabelText().strip();
        final var message = PageGeneratorKt.genDateErrMessage(isLabelDuplicate, labelText);
        return PageGeneratorKt.genDateInput(labelText, message);
    }

    @Override
    public RawInputFormBase toRawInput() {
        return new RawDateInput(getLabelText().strip(), isLabelDuplicate);
    }
}
