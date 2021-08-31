package edu.opjms.templating.inputPanes;

import edu.opjms.global.inputForms.PageGeneratorKt;
import edu.opjms.global.inputForms.RawSelectField;
import edu.opjms.templating.RawTypes;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import jfxtras.styles.jmetro.JMetroStyleClass;

import java.util.Collections;

import static java.util.Objects.requireNonNullElse;
import static javafx.collections.FXCollections.observableArrayList;

final public class SelectFieldPane extends InputPaneBase {

    private static final String SUFFIX = " - Select";

    public SelectFieldPane() {
        this(null, null, null, false);
    }

    private final MultiOptionEditor multiOptionEditor;
    private final ReadOnlyObjectProperty<RawTypes> type;

    public SelectFieldPane(String labelText, RawTypes rawType, MultiOptionEditor.FieldData data, boolean isDuplicate) {
        super();
        var label = new TextFieldChange(requireNonNullElse(labelText, ""));
        labelField = label;
        var labelErr = new Label();
        labelErr.getStyleClass().add("err");
        super.labelErr = labelErr;

        //checking
        if (labelText == null || labelText.isBlank()) {
            isLabelValid = false;
            labelErr.setText(INVALID_LABEL_ERR);
            label.pseudoClassStateChanged(ERR_CLASS, true);
        }
        label.textProperty().addListener((observableValue, s, t1) -> {
            isLabelValid = !t1.isBlank();
            if (!isLabelDuplicate) {
                labelErr.setText(isLabelValid? "": INVALID_LABEL_ERR);
                label.pseudoClassStateChanged(ERR_CLASS, !isLabelValid);
            }
        });

        VBox nameWrapper = new VBox(new Label("Label Text"), label, labelErr);
        nameWrapper.setSpacing(5);

        var box = new ComboBox<>(observableArrayList(RawTypes.values()));

        if (rawType == null)
            box.getSelectionModel().selectFirst();
        else
            box.getSelectionModel().select(rawType);

        VBox typeWrapper = new VBox(new Label("Type"), box);
        typeWrapper.setSpacing(5);

        type = box.getSelectionModel().selectedItemProperty();

        /*multiOptionEditor = new MultiOptionEditor(pairs,
                20,
                5,
                label.widthProperty(),
                box.getSelectionModel().selectedIndexProperty().isEqualTo(1),
                box.getSelectionModel().selectedIndexProperty().isEqualTo(2));*/

        multiOptionEditor = new MultiOptionEditor(20,
                5,
                box.getSelectionModel().selectedIndexProperty().isEqualTo(1),
                box.getSelectionModel().selectedIndexProperty().isEqualTo(2),
                data);

        configureSuper(label.textProperty(), SUFFIX);
        FlowPane wrapper = wrapInFlowPane(nameWrapper, typeWrapper, multiOptionEditor);
        JMetroStyleClass.addIfNotPresent(wrapper.getStyleClass(), JMetroStyleClass.BACKGROUND);
        setContent(wrapper);
        showDuplicateError(isDuplicate);
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
        final var labelText = getLabelText();
        if (type.get() == RawTypes.NUMBER) {
            final var fieldData = multiOptionEditor.getPairsAndData();
            final var duplicates = fieldData.getDuplicates();
            final var pairs = fieldData.getPairs();
            final var nonNumerics = fieldData.getNonNumeric();
            final var message = PageGeneratorKt.genSelectErrMessage(isLabelDuplicate, labelText, duplicates, nonNumerics);
            return PageGeneratorKt.genSelectInput(labelText, pairs, message);
        }
        final var fieldData = multiOptionEditor.getPairsAndDuplicates();
        final var duplicates = fieldData.getDuplicates();
        final var pairs = fieldData.getPairs();
        final var message = PageGeneratorKt.genSelectErrMessage(isLabelDuplicate, labelText, duplicates, Collections.emptyList());
        return PageGeneratorKt.genSelectInput(labelText, pairs, message);
        /*final var duplicatesAndPairs = multiOptionEditor.newToArray();
        final var duplicates = duplicatesAndPairs.getDuplicates();
        final var pairs = duplicatesAndPairs.getPairs();
        final var labelText = getLabelText();
        final var message = PageGeneratorKt.genSelectErrMessage(isLabelDuplicate, labelText, duplicates);
        return PageGeneratorKt.genSelectInput(labelText, pairs, message);*/
    }


    @Override
    public RawSelectField toRawInput() {
        return new RawSelectField(getLabelText(), type.get(), multiOptionEditor.getPairsAndData(), isLabelDuplicate);
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
