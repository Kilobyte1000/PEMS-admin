package edu.opjms.templating.inputPanes;

import edu.opjms.global.inputForms.RawSelectField;
import edu.opjms.global.inputForms.RawSelectGroup;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import jfxtras.styles.jmetro.JMetroStyleClass;

import java.util.List;

import static java.util.Objects.requireNonNullElse;

/**
 * Houses multiple select fields connected via a delimiter
 */
final public class SelectFieldGroupPane extends InputPaneBase {
    private final ObservableList<? extends TitledPane> selectFieldPanes;
    private static final String SUFFIX = "Select Group";
    private final StringProperty delimiter;

    public SelectFieldGroupPane() {
        this(null, null);
    }

    public SelectFieldGroupPane(String delimiter, List<SelectFieldPane> fieldPanes) {
        Label label = new Label("Delimiter:");
        label.setAlignment(Pos.BOTTOM_CENTER);
        var field = new TextField(requireNonNullElse(delimiter, ""));
        this.delimiter = field.textProperty();

        HBox delimiterWrapper = new HBox(label, field);
        delimiterWrapper.setSpacing(8);

        Accordion accordion = new Accordion();

        if (fieldPanes != null)
            accordion.getPanes().addAll(fieldPanes);

        selectFieldPanes = accordion.getPanes();

        Button addButton = new Button("Add new Select");
        addButton.setOnAction(a -> {
            var selectPane = new SelectFieldPane();
            selectPane.setDeletable();
            accordion.getPanes().add(selectPane);
        });

        final var vBox = wrapInVBox(delimiterWrapper, accordion, addButton);
        JMetroStyleClass.addIfNotPresent(vBox.getStyleClass(), JMetroStyleClass.BACKGROUND);
        setContent(vBox);

        configureSuper(new SimpleStringProperty(""), SUFFIX);
    }

    public int getNumberOfSelects() {return selectFieldPanes.size();}

    /**
     * <p>
     *     Tells whether the input contains an error which will prevent
     *     the input from generating html content.
     * </p>
     * <p>
     *     Return true when all of it child select-fields return trye
     * </p>
     * @return {@inheritDoc}
     */
    @Override
    public boolean containsError() {
        for (TitledPane selectFieldPane : selectFieldPanes) {
            if (!((SelectFieldPane) selectFieldPane).containsError())
                return false;
        }
        return true;
    }

    @Override
    public String generateHTML(int id) {
        if (containsError()) return "";

        var delimiter = this.delimiter.get().isBlank()? " ": " " + this.delimiter.get() + " ";
        var delimiterLength = delimiter.length();
        final var pre = "<div class='container'><div class='label'>";
        var builder = new StringBuilder(pre);


        /*
        * containsError does not scan for all errors and generateOptions may return empty
        * string even when containsError is false.
        *
        * Hence we append options first rather than label. We construct a boolean array and use it to store
        * whether the input will generate options or not.
        *
        * We have to use a index based loop to make sure the id's are same for the options and respective label
        */
        var inputSize = selectFieldPanes.size();
        var labelIndex = pre.length();
        boolean isFirstInput = true;

        //generation of options
        for (int i = 0; i < inputSize; i++) {
            final var inputPane = (SelectFieldPane) selectFieldPanes.get(i);

            if (!inputPane.containsError()) {
                final var selectInput = inputPane.generateOptions(id + i);

                if (!selectInput.isEmpty()) {
                    //we can generate label and options here

                    final var label = inputPane.generateLabel(id + i);

                    if (!isFirstInput) {
                        builder.insert(labelIndex, delimiter);
                        labelIndex += delimiterLength;
                    }
                    isFirstInput = false;

                    builder.insert(labelIndex, label);
                    labelIndex += label.length();

                    builder.append(selectInput);

                }
            }
        }

        //currently label and select's are in succession
        //we just separate them in their own div's here

        builder.insert(labelIndex, "</div><div class='input-field'>");
        builder.append("</div></div>");




        /*
        * In the loops, we check for error before the generation even though
        * it generation, itself checks for errors and only returns if there is no error.
        *
        * But we can't know which input is actually the first before we check for errors
        * and thus can't set the delimiter appropriately without it. So we have to
        * perform this double check
        *
        * but we don't have this restriction while generating the options and
        * that's why we are using index-based loop. It allows the id to be id + i
        * which makes id consistent across both loops
        */

        /*//generate the labels
        for (int i = 0; i < selectFieldPanes.size(); i++) {
            final var inputPane = (SelectFieldPane) selectFieldPanes.get(i);

            if (!inputPane.containsError()) {
                //all inputs except first one should be preceded by a delimiter
                if (!isFirstInput) {
                    builder.append(delimiter);
                }
                isFirstInput = false;

                //we need a unique id for each field
                builder.append(inputPane.generateLabel(id + i));
            }
        }

        builder.append("</div>");
        builder.append("<div class='input-field'>");

        //generate the options
        for (int i = 0; i < selectFieldPanes.size(); i++) {
            final var inputPane = (SelectFieldPane) selectFieldPanes.get(i);

            builder.append(inputPane.generateOptions(id + i));
        }

        //closing the container
        builder.append("</div></div>");*/
        return builder.toString();
    }

    @Override
    public RawSelectGroup toRawInput() {
        var inputFields = new RawSelectField[selectFieldPanes.size()];

        for (int i = 0; i < inputFields.length; i++) {
            inputFields[i] = ((SelectFieldPane) selectFieldPanes.get(i)).toRawInput();
        }

        return new RawSelectGroup(delimiter.get(), List.of(inputFields));
    }


    //fixme: html generation doesnt work

    //boiler plate

    @Override
    public String toString() {
        return "SelectFieldGroupPane{" +
                "selectFieldPanes=" + selectFieldPanes +
                ", delimiter=" + delimiter +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SelectFieldGroupPane that = (SelectFieldGroupPane) o;

        if (!selectFieldPanes.equals(that.selectFieldPanes)) return false;
        return delimiter.equals(that.delimiter);
    }

    @Override
    public int hashCode() {
        int result = selectFieldPanes.hashCode();
        result = 31 * result + delimiter.hashCode();
        return result;
    }
}
