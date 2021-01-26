 package edu.opjms.templating.inputPanes;

 import javafx.application.Platform;
 import javafx.beans.binding.BooleanBinding;
 import javafx.beans.property.ReadOnlyDoubleProperty;
 import javafx.beans.property.SimpleBooleanProperty;
 import javafx.collections.ObservableList;
 import javafx.geometry.Pos;
 import javafx.scene.Node;
 import javafx.scene.Parent;
 import javafx.scene.control.Label;
 import javafx.scene.control.TextField;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.Pane;
 import javafx.scene.layout.VBox;
 import kotlin.Pair;

 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;

final class MultiOptionEditor extends VBox {

    private final SimpleBooleanProperty isLastFieldFilled = new SimpleBooleanProperty();
    private final SimpleBooleanProperty doValidateNumber = new SimpleBooleanProperty();

    private final double HSpacing;

    private final ReadOnlyDoubleProperty labelWidth;

    private int errors = 0;

    public MultiOptionEditor(Pair<String, String>[] pairs, double HSpacing, double VSpacing, ReadOnlyDoubleProperty labelWidth, BooleanBinding whenDoValidation) {
        super();
        this.doValidateNumber.bind(whenDoValidation);
        this.HSpacing = HSpacing;
        this.labelWidth = labelWidth;

        super.setSpacing(VSpacing);

        if (pairs != null && pairs.length > 0) {

            var firstPair = pairs[0];
            addTextField(firstPair.getFirst(), firstPair.getSecond(), true, false);

            for (int i = 1; i < pairs.length; i++) {
                var pair = pairs[i];
                addTextField(pair.getFirst(), pair.getSecond(), false, false);
            }

            //last empty field
            addTextField(false);

        } else
            addTextField(true);

        isLastFieldFilled.addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                addTextField(false);
            }
        });

        doValidateNumber.addListener((observableValue, aBoolean, newValue) -> {
            List<Node> HBoxes = getChildren();

            if(newValue) {
                //validate all existing fields
                for (int i = 1; i < HBoxes.size(); i++) {
                    var fields = ((Parent) HBoxes.get(i)).getChildrenUnmodifiable();
                    doNumberValidation( (TextField) fields.get(1), (Label) fields.get(2));
                }
            }
            else {
                //remove red borders and error labels
                for (int i = 1; i < HBoxes.size(); i++) {
                    var fields = ((Parent) HBoxes.get(i)).getChildrenUnmodifiable();
                    var label = (Label) fields.get(2);

                    //if it contains error, label will be visible
                    if (label.isVisible())
                        removeErrorHints( (TextField) fields.get(1), (Label) fields.get(2));
                }
            }

        });

    }

    private void addTextField(boolean firstBox) {addTextField(null, null, firstBox, true);}


    /**
     * Adds a new text field pair. Sets all the bindings
     *
     * @param firstBox Whether it is the first box being added
     */
    private void addTextField(String visibleValue, String internalValue, boolean firstBox, boolean bindEmptyField) {
        if (!firstBox) { //set deletable
            HBox hBox = (HBox) getChildren().get(getChildren().size() - 1);
            var hBoxChildren = hBox.getChildren();

            TextField lastVisibleField = (TextField) hBoxChildren.get(0);

            TextField lastInternalField = (TextField) hBoxChildren.get(1);

            //delete if both fields are empty and unfocused

            lastVisibleField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue && !lastInternalField.isFocused() && lastVisibleField.getText().isBlank() && lastInternalField.getText().isBlank()) {
                    System.out.println("entering delete method");
                    setDeletable(hBox);
                }
            });

            lastInternalField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue && !lastVisibleField.isFocused() && lastVisibleField.getText().isBlank() && lastInternalField.getText().isBlank()) {
                    setDeletable(hBox);
                }
            });

        } // set deletable
        else {
            var visibleLabel = new Label("Visible Values");
            visibleLabel.minWidthProperty().bind(labelWidth);
            var internalLabel = new Label("Internal Values");
            internalLabel.minWidthProperty().bind(labelWidth);
            getChildren().add(wrapInHBox(visibleLabel, internalLabel));
        }

        TextField visibleField = new TextField(Objects.requireNonNullElse(visibleValue, ""));
        TextField internalField = new TextField(Objects.requireNonNullElse(internalValue, ""));
        Label errLabel = new Label("Provided value is not a number");
        errLabel.setVisible(false);
        errLabel.setStyle("-fx-text-fill: red");

        if (bindEmptyField)
            isLastFieldFilled.bind(visibleField.textProperty().isNotEmpty().and(internalField.textProperty().isNotEmpty()));

        internalField.focusedProperty().addListener((observableValue, aBoolean, newValue) -> {
            if (doValidateNumber.getValue()) {
                if (!newValue) { //when on focus lost
                    doNumberValidation(internalField, errLabel);

                } else {
                    if (errLabel.isVisible())
                        removeErrorHints(internalField, errLabel);
                }
            }
        });

        getChildren().add(wrapInHBox(visibleField, internalField, errLabel));

    }

    private void setDeletable(HBox parent) {

        Platform.runLater(() -> {
            if (!((Pane) parent.getParent()).getChildren().remove(parent)) {
                Logger.getLogger("templating.MultiOptionEditor").log(Level.WARNING, "Could Not delete HBox");
                Platform.runLater(() -> ((Pane) parent.getParent()).getChildren().remove(parent));
            }
        });
    }

    private HBox wrapInHBox(Node... nodes) {
        HBox wrapper = new HBox(nodes);
        wrapper.setSpacing(HSpacing);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        return wrapper;
    }

    private void doNumberValidation(TextField field, Label errLabel) {
        var text = field.getText().strip();
        field.setText(text);

        if (text.isEmpty())
            return;

        if (!isStringDouble(text)) {
            field.setStyle("-fx-background-color: red, white;");
            errLabel.setVisible(true);
            errors++;
            System.out.println("increment: " + errors);
        }

    }

    private void removeErrorHints(TextField field, Label label) {
        field.setStyle("");
        label.setVisible(false);
        errors--;
        System.out.println("decrement: " + errors);
    }

    private static boolean isStringDouble(String input) {
        int length = input.length();

        if (length > 306) //double overflows at 1.7*10^308, so it is good idea to stop at 306 digits
            return false;

        int i = 0;
        if (input.charAt(0) == '-') {
            if (length == 1) return false;
            i = 1;
        }

        boolean encounteredDecimal = false;

        for (; i < length; i++) {
            char c = input.charAt(i);

            if (c == '.') {
                if (!encounteredDecimal) {

                    encounteredDecimal = true;
                    continue;
                } else
                    return false;
            }

            if (c < '0' || c > '9')
                return false;
        }

        return true;
    }


    /**
     * <p>returns whether this contains a non-numeric input when input type is set to number</p>
     * <p>
     *     this does not account for duplicates or input being absent. One should also check nullability of {@link this#toArray()} method
     *     for complete surety
     * </p>
     * @return whether this contains a non-numeric input when input type is set to number
     */
    public boolean containsError() {
        System.out.println(toString());
        return errors != 0;
    }

    /**
     * <p>Returns a view of the of the pairs entered in this class.</p>
     * <p>It ignores unpaired entries.
     * Returns null if duplicate internal values are found or no pairs exist.</p>
     *
     * @return an array view of all the entries or null
     * @implNote this first feeds all data to a {@link LinkedHashMap} in order to manage duplicates. <br>
     *           This makes this method fairly expensive and should be invoked carefully
     */
    public Pair<String, String>[] toArray() {
        final ObservableList<Node> hBoxes = getChildren();
        final int size = hBoxes.size() - 1; //exclude last empty fields

        LinkedHashMap<CaseInsensitiveString, String> pairs = new LinkedHashMap<>(size);

        for (int i = 1; i < size; i++) {
            var hBox = (Parent) hBoxes.get(i);

            var displayText = ((TextField) hBox.getChildrenUnmodifiable().get(0)).getText();
            var internalText = ((TextField) hBox.getChildrenUnmodifiable().get(1)).getText();

            if (!displayText.isBlank() && !internalText.isBlank()) {

                var displayTextCaseInsensitive = new CaseInsensitiveString(displayText);

                if (pairs.containsKey(displayTextCaseInsensitive)) {

                    return null; // duplicate values not allowed, fatal error

                } else
                    pairs.put(displayTextCaseInsensitive, internalText);

            }
        }

        int sizeMap = pairs.size();

        if (sizeMap == 0) return null; // no paired string, fatal error

        @SuppressWarnings("unchecked")
        Pair<String, String>[] ret = new Pair[sizeMap];

        int i = 0;
        for (Map.Entry<CaseInsensitiveString, String> entry: pairs.entrySet()) {
            CaseInsensitiveString caseInsensitiveString = entry.getKey();
            String s = entry.getValue();
            ret[i++] = new Pair<>(caseInsensitiveString.self, s);
        }

        return ret;
    }

    /**
     * <p>Returns a view of the pairs entered in this class</p>
     * <p>unlike toArray(), it never returns null and does not eliminate duplicate fields</p>
     *
     * @return an array view of all the entries or null
     */
    @SuppressWarnings("unchecked")
    public Pair<String, String>[] toArraySoft() {
        var children = getChildrenUnmodifiable();
        Pair<String, String>[] pairs = new Pair[children.size() - 2];

        //first hBox houses labels and last fields are always empty

        for (int i = 1; i < children.size() - 1; i++) {
            Pane hBox = ((Pane) children.get(i));

            var displayText = ((TextField) hBox.getChildrenUnmodifiable().get(0)).getText();
            var internalText = ((TextField) hBox.getChildrenUnmodifiable().get(1)).getText();

            pairs[i - 1] = new Pair<>(displayText, internalText);
        }

        return pairs;
    }
    /**
     * A quick and dirty class thrown together to forward equals to equals ignore case
     * for use in a case insensitive map
     */
    private static class CaseInsensitiveString {
        private final String self;
        public CaseInsensitiveString(String string) {
            self = string;
        }

        @Override
        public String toString() {
            return self;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;

            if (obj == this)
                return true;

            if (obj instanceof CaseInsensitiveString)
                return self.equalsIgnoreCase(((CaseInsensitiveString) obj).self);
            return false;
        }
    }


    //todo display warning about duplicate visible field values


    @Override
    public String toString() {
        return "MultiOptionEditor{" +
                "errors=" + errors +
                "pairs=" + Arrays.toString(toArray()) +
                '}';
    }
}