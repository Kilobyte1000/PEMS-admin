package edu.opjms.templating.inputPanes

import edu.opjms.global.ERROR_CLASS
import edu.opjms.global.ERROR_STYLECLASS
import edu.opjms.global.inputForms.RawInputFormBase
import edu.opjms.templating.RawTypes
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.Region
import java.util.*
import java.util.regex.Pattern

class TextFieldPaneKt(
    labelText: String? = null,
    placeholderText: String? = null,
    regex: String? = null,
    tooltipText: String? = null,
    rawType: RawTypes? = null,
    val isUniqueField: Boolean = false
): InputPaneBase() {

    private val placeholderTextProperty: StringProperty
    private val selectedTypeProperty: ReadOnlyObjectProperty<RawTypes>
    private val regexProperty: StringProperty
    private val tooltipProperty: StringProperty
    private var isRegexValid

    init {
        val (field, label) = getLabelInput(labelText)
        val nameField = wrapInVBox(Label("Label Text"), field, label)

        val placeholder = kotlin.run {
            val textField = ValidatedTextField(placeholderText ?: "")
            placeholderTextProperty = textField.textProperty()
            val errLabel = Label()
            textField.nonBlankValidation(errLabel, "placeholder must be set")

            wrapInVBox(Label("Placeholder"), textField, errLabel)
        }

        //Type of input - a combobox
        val typeBox = ComboBox(FXCollections.observableArrayList(RawTypes.NUMBER, RawTypes.TEXT))
        val typeWrapper = wrapInVBox(Label("Type"), typeBox)
        selectedTypeProperty = typeBox.selectionModel.selectedItemProperty()

        //one time error
        if (rawType == null && !isUniqueField) {
            val typeErrLabel = Label("type must be set")
            typeErrLabel.styleClass += ERROR_STYLECLASS
            typeBox.pseudoClassStateChanged(ERROR_CLASS, true)

            typeBox.selectionModel.selectedItemProperty().addListener(object : ChangeListener<RawTypes> {
                override fun changed(p0: ObservableValue<out RawTypes>?, p1: RawTypes?, p2: RawTypes?) {
                    typeWrapper.children.removeLast()
                    typeBox.selectionModel.selectedItemProperty().removeListener(this)
                }
            })
        }

        //unique field must have type number
        if (isUniqueField) {
            typeBox.selectionModel.selectFirst()
            typeBox.isDisable = true
        }

        // regex
        val regexWrapper = kotlin.run {
            val regexField = ValidatedTextField(regex ?: "")
            val errLabel = Label()
            regexProperty = regexField.textProperty()

            regexField.validate(errLabel) {
                if (it.newValue.isNotEmpty()
                    && kotlin.runCatching { Pattern.compile(it.newValue) }.isFailure) {
                    isRegexValid = false
                    "Regex is Invalid"
                } else {
                    isRegexValid = true
                    null
                }
            }

            wrapInVBox(Label("Regex"), regexField, errLabel)
        }


        val isNotNumber = typeBox.selectionModel
            .selectedItemProperty()
            .isNotEqualTo(RawTypes.NUMBER)

        regexWrapper.visibleProperty().bind(isNotNumber)
        regexWrapper.managedProperty().bind(isNotNumber)


        //tooltip
        val tooltipLabel = Label("Tooltip (Optional)")
        val tooltipTextArea = TextArea(Objects.requireNonNullElse(tooltipText, ""))

        tooltipTextArea.prefRowCount = 3
        tooltipTextArea.prefColumnCount = 25
    }

    override fun containsError(): Boolean {
        TODO("Not yet implemented")
    }

    override fun generateHTML(id: Int): String {
        TODO("Not yet implemented")
    }

    override fun toRawInput(): RawInputFormBase {
        TODO("Not yet implemented")
    }
}