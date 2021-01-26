package edu.opjms.templating.inputPanes;

import edu.opjms.global.inputForms.RawTextFieldInput;
import edu.opjms.templating.RawTypes;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import jfxtras.styles.jmetro.JMetroStyleClass;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.util.Objects.requireNonNullElse;
import static javafx.collections.FXCollections.observableArrayList;
import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;


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

        this.isUniqueField = isUniqueField;

        //Label Text
        var label = new TextField(requireNonNullElse(labelText, ""));
        VBox nameWrapper = wrapInVBox(new Label("Label Text"), label);


        //placeholder
        var placeholderField = new TextField(requireNonNullElse(placeholderText, ""));
        VBox placeholderWrapper = wrapInVBox(new Label("Placeholder"), placeholderField);
        placeholderTextProperty = placeholderField.textProperty();


        //Type of input - helpful in many ways
        var box = new ComboBox<>(observableArrayList(RawTypes.NUMBER, RawTypes.TEXT));
        VBox typeWrapper = wrapInVBox(new Label("Type"), box);
        selectedTypeProperty = box.getSelectionModel().selectedItemProperty();

        //if this is unique field, then it must be a number
        if (isUniqueField) {
            box.getSelectionModel().selectFirst();
            box.setDisable(true);
        }

        //regex if type is text
        var regexField = new TextField(requireNonNullElse(regex, "")); //avoid Null Pointer Exception
        Label errLabel = new Label("Regex Is Invalid");
        regexProperty = regexField.textProperty();

        errLabel.setVisible(false);
        errLabel.setStyle("-fx-text-fill: red");

        //if regex is invalid set error = true and warn user
        regexField.focusedProperty().addListener((observableValue, aBoolean, newValue) -> {
            if (!newValue) {
                try {
                    Pattern.compile(regexField.getText());
                } catch (PatternSyntaxException p) {
                    regexField.setStyle("-fx-background-color: red, white;"); //red border
                    errLabel.setVisible(true);
                    isRegexInvalid = true;
                }
            } else {
                regexField.setStyle("");
                if (errLabel.isVisible()) errLabel.setVisible(false);
                isRegexInvalid = false;
            }
        });
        VBox regexWrapper = wrapInVBox(new Label("Regex (Optional)"), regexField, errLabel);

        //tooltip
        Label tooltipLabel = new Label("Tooltip (Optional)");
        Tooltip tooltip = new Tooltip("A tooltip is a popup text (like this one) useful for giving explaination.\nIf you are using regex, then the tooltip should define how the text should be formatted");

        tooltipLabel.setTooltip(tooltip);
        var tooltipTextArea = new TextArea(requireNonNullElse(tooltipText, ""));
        tooltipProperty = tooltipTextArea.textProperty();

        tooltipTextArea.setPrefRowCount(3);
        tooltipTextArea.setPrefColumnCount(25);

        VBox tooltipWrapper = wrapInVBox(tooltipLabel, tooltipTextArea);

        FlowPane wrapper = wrapInFlowPane(nameWrapper, placeholderWrapper, typeWrapper, tooltipWrapper);
        JMetroStyleClass.addIfNotPresent(wrapper.getStyleClass(), JMetroStyleClass.BACKGROUND);

        //type and regex binds
        box.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
            if (t1.intValue() == 0) //number is selected
                wrapper.getChildren().remove(regexWrapper); //remove last (regex) node
            else
                wrapper.getChildren().add(3, regexWrapper);

        });
        if (rawType != null)
            box.getSelectionModel().select(rawType);
        else if (regex != null)
            box.getSelectionModel().select(1);

        configureSuper(label.textProperty(), isUniqueField? UNIQUE_SUFFIX: SUFFIX);

        setContent(wrapper);
    }

    @Override
    public String generateHTML(int id) {
        if (containsError())
            return "";

        //we add id to prevent collisions with other fields
        final var idText = "c" + id + "-";

        final var labelText = escapeHtml4(getLabelText().strip());
        final var placeholderText = escapeHtml4(placeholderTextProperty.getValue().strip());
        final var typeText = selectedTypeProperty.get().toString();
        System.out.println(typeText);
        final var regexText = escapeHtml4(regexProperty.getValue().strip());
        final var tooltipText = escapeHtml4(tooltipProperty.getValue().strip());
        final var dbName = escapeHtml4(idText + getLabelText().substring(0, 3).strip());
        final var fieldID = dbName + "_field";

        final var isRegexUsed = !regexText.isBlank() && selectedTypeProperty.get().equals(RawTypes.TEXT);
        final var isTooltipUsed = !tooltipText.isBlank();

        /*
        * we do some calculation to find out size of html in advance
        * and set size of StringBuilder accordingly to prevent resize
        * and improve performance
        */

        //length of doc without regex, tooltip and any data
        final short baseDoc = 252;
        //length of regex template
        final byte baseRegex = 9;
        final byte baseTooltip = 11;

        //perform calculations

        //adding length of necessary fields and base
        int length = baseDoc + fieldID.length()*3 + labelText.length()
                + typeText.length() + dbName.length() + placeholderText.length();

        if (isRegexUsed)
            length += baseRegex + regexText.length();
        if (isTooltipUsed)
            length += baseTooltip + tooltipText.length();
        
        StringBuilder gen = new StringBuilder(length);

        //construct the page

        //container div and label
        gen.append("<div class='container'><div class='label'><label for='");
        gen.append(fieldID);
        gen.append("'>");
        gen.append(labelText);

        //the input
        gen.append("</label></div><div class='input-text-field'><input id='");
        gen.append(fieldID);
        gen.append("' type='");
        gen.append(typeText);
        gen.append("' name='");
        gen.append(dbName);
        gen.append("' placeholder=' ' autocomplete='off'");
        if (isTooltipUsed) {
            gen.append(" title='");
            gen.append(tooltipText);
            gen.append("'");
        }
        if (isRegexUsed) {
            gen.append(" pattern='");
            gen.append(regexText);
            gen.append("'");
        }

        //the placeholder label
        gen.append(" required><label for='");
        gen.append(fieldID);
        gen.append("' class='tooltip'>");
        gen.append(placeholderText);

        //close label, add material underline, close input-div, close container-div
        gen.append("</label><div class='underline'></div></div></div>");

        return gen.toString();
    }

    @Override
    public boolean containsError() {
        return isRegexInvalid || getLabelText().isBlank()
                || placeholderTextProperty.getValue().isBlank();
    }

    @Override
    public RawTextFieldInput toRawInput() {
        return new RawTextFieldInput(getLabelText(),
                placeholderTextProperty.getValue(),
                selectedTypeProperty.get(),
                regexProperty.get(),
                tooltipProperty.getValue(),
                isUniqueField);
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
