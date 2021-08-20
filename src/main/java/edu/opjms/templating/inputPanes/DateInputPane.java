package edu.opjms.templating.inputPanes;

import edu.opjms.global.inputForms.RawDateInput;
import edu.opjms.global.inputForms.RawInputFormBase;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import jfxtras.styles.jmetro.JMetroStyleClass;

import java.util.Objects;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

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
            labelInput.pseudoClassStateChanged(ERR_CLASS, true);
        }
        labelInput.textProperty().addListener((observableValue, s, t1) -> {
            isLabelValid = !t1.isBlank();
            if (!isLabelDuplicate) {
                labelErr.setText(isLabelValid? "": INVALID_LABEL_ERR);
                labelInput.pseudoClassStateChanged(ERR_CLASS, !isLabelValid);
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
        return getLabelText().isBlank();
    }

    @Override
    public String generateHTML(int id) {
        if (containsError())
            return "";

        //we add id to prevent collisions with other fields
        final var idText = "c" + id + "-";

        final var labelText = escapeHtml4(getLabelText().strip());
        final var dbName = escapeHtml4(idText + getLabelText().substring(0, 3).strip());
        final var fieldID = dbName + "_field";


        return "<div class='container'><div class='label'><label for='" + fieldID + "'>" +
                labelText +
                //close label
                "</label></div>" +
                //input field
                "<div class='input-text-field date'><input type='date' name='" + dbName + "'" +
                "id='" + fieldID + "' required>" +
                //close label, and material underline, close container div
                "<div class='underline'></div></div></div>";
    }

    @Override
    public RawInputFormBase toRawInput() {
        return new RawDateInput(getLabelText().strip(), isLabelDuplicate);
    }
}
