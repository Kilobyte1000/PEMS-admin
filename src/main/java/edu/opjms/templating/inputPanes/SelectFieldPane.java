package edu.opjms.templating.inputPanes;

import edu.opjms.global.inputForms.RawSelectField;
import edu.opjms.templating.RawTypes;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import jfxtras.styles.jmetro.JMetroStyleClass;
import kotlin.Pair;

import static java.util.Objects.requireNonNullElse;
import static javafx.collections.FXCollections.observableArrayList;
import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

final public class SelectFieldPane extends InputPaneBase {

    private static final String SUFFIX = " - Select";

    public SelectFieldPane() {
        this(null, null, null);
    }

    private final MultiOptionEditor multiOptionEditor;
    private final ReadOnlyObjectProperty<RawTypes> type;

    public SelectFieldPane(String labelText, RawTypes rawType, Pair<String, String>[] pairs) {
        var label = new TextField(requireNonNullElse(labelText, ""));
        VBox nameWrapper = new VBox(new Label("Label Text"), label);
        nameWrapper.setSpacing(5);

        var box = new ComboBox<>(observableArrayList(RawTypes.values()));

        if (rawType == null)
            box.getSelectionModel().selectFirst();
        else
            box.getSelectionModel().select(rawType);

        VBox typeWrapper = new VBox(new Label("Type"), box);
        typeWrapper.setSpacing(5);

        type = box.getSelectionModel().selectedItemProperty();

        multiOptionEditor = new MultiOptionEditor(pairs, 20, 5, label.widthProperty(), box.getSelectionModel().selectedIndexProperty().isEqualTo(1));

        configureSuper(label.textProperty(), SUFFIX);
        FlowPane wrapper = wrapInFlowPane(nameWrapper, typeWrapper, multiOptionEditor);
        JMetroStyleClass.addIfNotPresent(wrapper.getStyleClass(), JMetroStyleClass.BACKGROUND);
        setContent(wrapper);
    }


    /**
     * Forwards to {@link MultiOptionEditor}'s containError method - {@link MultiOptionEditor#containsError()}
     */
    @Override
    public boolean containsError() {
        return multiOptionEditor.containsError();
    }

    @Override
    public String generateHTML(int id) {
        if (containsError())
            return "";
        var labelText = generateLabel(id);
        var optionsText = generateOptions(id);

        return "<div class='container'><div class='label'>" +
                labelText +
                "</div><div class='input-field'>" +
                optionsText +
                "</div></div>";
    }

    public String generateLabel(int id) {
        var idText = "c" + id + "-" + getLabelText();

        return "<label for='" + escapeHtml4(idText) + "_field'>" + getLabelText() + "</label>";
    }

    /**
     * <p>Generates the options for a select field.</p>
     * <p>
     *     Note: This forces the field to generate options, bypassing containsError check. It returns empty string if there are
     *     no paired inputs in the editor
     * </p>
     * @param id The id used in html code.
     * @return The select field or empty string if no paired inputs are present
     */
    public String generateOptions(int id) {
        /*
        * we could run complete calculations to determine the size
        * of stringBuilder in advance, but that would mean running a loop twice
        * Considering that the inputs are going to be small,
        * calculating would be costlier than resizing
        */
        var pairs = multiOptionEditor.toArray();
        if (pairs == null)
            return "";

        final byte baseSize = 80;
        final byte baseForEach = 26;

        var idText = "c" + id + "-" + getLabelText();

        StringBuilder builder = new StringBuilder(baseSize + baseForEach + pairs.length * 4);
        //we multiplied by 4 to get size with semi-reasonable assumption that
        //length of text for internal value and db-value would 2 for each option
        //highly probable to be wrong. But prevents frequent resizing

        builder.append("<select id='");
        builder.append(idText);
        builder.append("_field' name='");
        builder.append(idText);
        builder.append("' required><option value='' selected>-</option>");
        for (Pair<String, String> pair : pairs) {
            builder.append("<option value='");
            builder.append(pair.getSecond()); //internal db value
            builder.append("'>");
            builder.append(pair.getFirst()); //visible value
            builder.append("</option>");
        }
        builder.append("</select>");

        return builder.toString();
    }

    @Override
    public RawSelectField toRawInput() {
        return new RawSelectField(getLabelText(), type.get(), multiOptionEditor.toArraySoft());
    }


    //boilerplate

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectFieldPane)) return false;

        SelectFieldPane that = (SelectFieldPane) o;

        return multiOptionEditor.equals(that.multiOptionEditor);
    }

    @Override
    public int hashCode() {
        return multiOptionEditor.hashCode();
    }

    @Override
    public String toString() {
        return "SelectFieldPane{" +
                "multiOptionEditor=" + multiOptionEditor +
                '}';
    }
}
