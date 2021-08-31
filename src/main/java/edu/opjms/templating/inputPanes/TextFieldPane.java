package edu.opjms.templating.inputPanes;

import edu.opjms.global.inputForms.PageGeneratorKt;
import edu.opjms.global.inputForms.RawTextFieldInput;
import edu.opjms.templating.RawTypes;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import jfxtras.styles.jmetro.JMetroStyleClass;

import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.util.Objects.requireNonNullElse;
import static javafx.collections.FXCollections.observableArrayList;


final public class TextFieldPane extends InputPaneBase {

    private boolean isRegexInvalid = false;
    private static final String SUFFIX = " - Text Field";
    private static final String UNIQUE_SUFFIX = " - Unique ID Field";

    private final StringProperty placeholderTextProperty;
    private final ReadOnlyObjectProperty<RawTypes> selectedTypeProperty;
    private final StringProperty regexProperty;
    private final StringProperty tooltipProperty;
    private final boolean isUniqueField;

    public TextFieldPane() {
        this(null, null, null, null, null, false);
    }
    public TextFieldPane(boolean isUniqueField) {
        this(null, null, null, null, null, isUniqueField);
    }


    public TextFieldPane(String labelText,
                         String placeholderText,
                         String regex,
                         String tooltipText,
                         RawTypes rawType,
                         boolean isUniqueField) {
        super();

        this.isUniqueField = isUniqueField;

        //Label Text
        var label = new TextFieldChange(requireNonNullElse(labelText, ""));
        super.labelField = label;
        var labelErr = new Label();
        super.labelErr = labelErr;
        labelErr.getStyleClass().add("err");
        if (labelText == null || labelText.isBlank()) {
            isLabelValid = false;
            label.pseudoClassStateChanged(ERR_CLASS, true);
            labelErr.setText(INVALID_LABEL_ERR);
        }
        label.textProperty().addListener((observableValue, s, newVal) -> {
            isLabelValid = !newVal.isBlank();
            if (!isLabelDuplicate) {
                labelErr.setText(isLabelValid? "": INVALID_LABEL_ERR);
                label.pseudoClassStateChanged(ERR_CLASS, !isLabelValid);
            }
        });
        VBox nameWrapper = wrapInVBox(new Label("Label Text"), label, labelErr);


        //placeholder
        final var placeholderField = new TextField(requireNonNullElse(placeholderText, ""));
        final var placeholderErr = new Label();
        placeholderErr.getStyleClass().add("err");
        placeholderErr.setText("placeholder must be set");
        //error handling
        final var isPlaceholderInvalid = placeholderText == null || placeholderText.isBlank();
        placeholderField.pseudoClassStateChanged(ERR_CLASS, isPlaceholderInvalid);
        labelErr.setVisible(isPlaceholderInvalid);
        labelErr.setManaged(isPlaceholderInvalid);
        //listener
        placeholderField.textProperty()
                .addListener((observableValue, s, newVal) -> {
                    final var isBlank = newVal.isBlank();
                    placeholderErr.setVisible(isBlank);
                    placeholderErr.setManaged(isBlank);
                    placeholderField.pseudoClassStateChanged(ERR_CLASS, isBlank);
                });
        VBox placeholderWrapper = wrapInVBox(new Label("Placeholder"), placeholderField, placeholderErr);
        placeholderTextProperty = placeholderField.textProperty();


        //Type of input - helpful in many ways
        var box = new ComboBox<>(observableArrayList(RawTypes.NUMBER, RawTypes.TEXT));
        var typeErr = new Label();
        typeErr.getStyleClass().add("err");
        typeErr.setText("type must be set");
        //error handling
        final var isTypeNull = rawType == null;
        box.pseudoClassStateChanged(ERR_CLASS, isTypeNull);
        typeErr.setManaged(isTypeNull);
        typeErr.setVisible(isTypeNull);
        //listener
        box.getSelectionModel().selectedItemProperty().addListener((observableValue, rawTypes, t1) -> {
            var isNull = t1 == null;
            typeErr.setVisible(isNull);
            typeErr.setManaged(isNull);
            box.pseudoClassStateChanged(ERR_CLASS, isNull);
        });
        VBox typeWrapper = wrapInVBox(new Label("Type"), box, typeErr);
        selectedTypeProperty = box.getSelectionModel().selectedItemProperty();

        //if this is unique field, override provided type and set to number
        if (isUniqueField) {
            box.getSelectionModel().selectFirst();
            box.setDisable(true);
        }

        //regex if type is text
        var regexField = new TextField(requireNonNullElse(regex, "")); //avoid Null Pointer Exception
        Label errLabel = new Label("Regex Is Invalid");
        regexProperty = regexField.textProperty();

        errLabel.setVisible(false);
        errLabel.getStyleClass().add("err");

        //if regex is invalid set error = true and warn user
        regexField.focusedProperty().addListener((observableValue, aBoolean, newValue) -> {
            if (!newValue) {
                try {
                    Pattern.compile(regexField.getText());
                } catch (PatternSyntaxException p) {
                    regexField.setStyle("-fx-background-color: red, white;"); //red border
                    errLabel.setVisible(true);
                    errLabel.setManaged(true);
                    isRegexInvalid = true;
                }
            } else {
                regexField.setStyle("");
                if (errLabel.isVisible()) {
                    errLabel.setVisible(false);
                    errLabel.setManaged(false);
                }
                isRegexInvalid = false;
            }
        });
        VBox regexWrapper = wrapInVBox(new Label("Regex (Optional)"), regexField, errLabel);

        //tooltip
        Label tooltipLabel = new Label("Tooltip (Optional)");
        Tooltip tooltip = new Tooltip("A tooltip is a popup text (like this one) useful for giving explanation.\nIf you are using regex, then the tooltip should define how the text should be formatted");

        tooltipLabel.setTooltip(tooltip);
        var tooltipTextArea = new TextArea(requireNonNullElse(tooltipText, ""));
        tooltipProperty = tooltipTextArea.textProperty();

        tooltipTextArea.setPrefRowCount(3);
        tooltipTextArea.setPrefColumnCount(25);

        VBox tooltipWrapper = wrapInVBox(tooltipLabel, tooltipTextArea);

        FlowPane wrapper = wrapInFlowPane(nameWrapper, placeholderWrapper, typeWrapper, regexWrapper, tooltipWrapper);
        JMetroStyleClass.addIfNotPresent(wrapper.getStyleClass(), JMetroStyleClass.BACKGROUND);

        //type and regex binds
        box.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
            if (t1.intValue() == 0) { //number is selected
                //remove regex node from layout
                regexWrapper.setVisible(false);
                regexWrapper.setManaged(false);
//                wrapper.getChildren().remove(regexWrapper); //remove last (regex) node
            } else {
                regexWrapper.setVisible(true);
                regexWrapper.setManaged(true);
//                wrapper.getChildren().add(3, regexWrapper);
            }

        });
        regexWrapper.setVisible(false);
        regexWrapper.setManaged(false);
        if (rawType != null)
            box.getSelectionModel().select(rawType);
        else if (regex != null)
            box.getSelectionModel().select(1);

        configureSuper(label.textProperty(), isUniqueField? UNIQUE_SUFFIX: SUFFIX);

        /*this.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (isFocused()) hideErrHints();
            else showErrHints();
        });*/


        setContent(wrapper);
    }

    /*@Override
    public void showDuplicateError(boolean value) {
        if (isLabelDuplicate != value) {
            if (value) {
                labelErr.setText(DUPLICATE_ERR);
                labelField.pseudoClassStateChanged(ERR_CLASS, true);
            } else {
                labelErr.setText(isLabelValid? "": INVALID_LABEL_ERR);
                labelField.pseudoClassStateChanged(ERR_CLASS, !isLabelValid);
            }
            isLabelDuplicate = value;
        }
    }*/

    @Override
    public void setOnLabelChange(BiConsumer<String, String> func) {
        labelField.setOnTextChange(func);
    }


    @Override
    public String generateHTML(int id) {
        final var labelText = getLabelText().strip();
        final var placeholderText = placeholderTextProperty.getValue().strip();
        final var type = selectedTypeProperty.get();
        final var regexText = regexProperty.getValue().strip();
        final var tooltipText = tooltipProperty.getValue().strip();

        return PageGeneratorKt.genTextInput(labelText,
                placeholderText,
                type,
                regexText,
                tooltipText,
                genErrMessage());
    }

    @Override
    public boolean containsError() {
        return isRegexInvalid
                || getLabelText().isBlank()
                || selectedTypeProperty.get() == null
                || placeholderTextProperty.getValue().isBlank()
                || isLabelDuplicate;
    }

    @Override
    public RawTextFieldInput toRawInput() {
        return new RawTextFieldInput(getLabelText(),
                placeholderTextProperty.getValue(),
                selectedTypeProperty.get(),
                regexProperty.get(),
                tooltipProperty.getValue(),
                isUniqueField,
                isLabelDuplicate);
    }


    private String genErrMessage() {
        final var isLabelBlank = getLabelText().isBlank();
        final var isPlaceholderBlank = placeholderTextProperty.getValue().isBlank();
        final var isTypeSelected = selectedTypeProperty.get() == null;

        return PageGeneratorKt.genTextErrMessage(isLabelBlank,
                isLabelDuplicate,
                isPlaceholderBlank,
                isTypeSelected,
                isRegexInvalid,
                regexProperty.get(),
                getLabelText());
    }

    //boilerplate

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextFieldPane)) return false;

        TextFieldPane that = (TextFieldPane) o;

        if (!placeholderTextProperty.equals(that.placeholderTextProperty)) return false;
        if (!selectedTypeProperty.equals(that.selectedTypeProperty)) return false;
        if (!regexProperty.equals(that.regexProperty)) return false;
        return tooltipProperty.equals(that.tooltipProperty);
    }

    @Override
    public int hashCode() {
        int result = placeholderTextProperty.hashCode();
        result = 31 * result + selectedTypeProperty.hashCode();
        result = 31 * result + regexProperty.hashCode();
        result = 31 * result + tooltipProperty.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TextFieldPane{" +
                "placeholderTextProperty=" + placeholderTextProperty +
                ", selectedTypeProperty=" + selectedTypeProperty +
                ", regexProperty=" + regexProperty +
                ", tooltipProperty=" + tooltipProperty +
                '}';
    }
}
