package edu.opjms.templating.inputPanes

import edu.opjms.global.ERROR_CLASS
import edu.opjms.global.ERROR_STYLECLASS
import edu.opjms.global.inputForms.RawInputFormBase
import edu.opjms.global.inputForms.RawTextFieldInput
import edu.opjms.global.inputForms.genTextErrMessage
import edu.opjms.global.inputForms.genTextInput
import edu.opjms.templating.RawTypes
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import java.util.*
import java.util.regex.Pattern

class TextFieldPaneKt(
    labelText: String = "",
    placeholderText: String = "",
    regex: String = "",
    tooltipText: String = "",
    rawType: RawTypes? = null,
    val isUniqueField: Boolean = false
): InputPaneBaseKt(labelText) {

    private val placeholderTextProperty: StringProperty
    private val selectedTypeProperty: ReadOnlyObjectProperty<RawTypes>
    private val regexProperty: StringProperty
    private val tooltipProperty: StringProperty
    private var isRegexValid = true

    override val suffix: String = if (!isUniqueField) " - Text Field" else " - Unique ID Field"

    init {
        val nameField = wrapInVBox(Label("Label Text"), labelField, errLabel)

        val placeholder = kotlin.run {
            val textField = ValidatedTextField(placeholderText)
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
            val regexField = ValidatedTextField(regex)
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

            val isNotNumber = typeBox.selectionModel
                .selectedItemProperty()
                .isNotEqualTo(RawTypes.NUMBER)

            visibleProperty().bind(isNotNumber)
            managedProperty().bind(isNotNumber)
            
            wrapInVBox(Label("Regex"), regexField, errLabel)
        }
        
        //tooltip
        val tooltipWrapper = kotlin.run {
            val tooltipLabel = Label("Tooltip (Optional)")
            val tooltipTextArea = TextArea(Objects.requireNonNullElse(tooltipText, "")).apply {
                prefRowCount = 3
                prefColumnCount = 25
                tooltipProperty = textProperty()
            }
            
            
            wrapInVBox(tooltipLabel, tooltipTextArea)
        }
        
        content = wrapInFlowPane(nameField, placeholder, typeWrapper, regexWrapper, tooltipWrapper)

    }

    override fun containsError(): Boolean {
        return isDuplicate 
                ||labelText.isBlank() 
                || placeholderTextProperty.get().isBlank() 
                || selectedTypeProperty.get() == null 
                || !isRegexValid
    }

    override fun generateHTML(id: Int): String {
        val labelText: String = labelText.trim()
        val placeholderText = placeholderTextProperty.value.trim()
        val type = selectedTypeProperty.get()
        val regexText = regexProperty.value.trim()
        val tooltipText = tooltipProperty.value.trim()

        return genTextInput(
            labelText,
            placeholderText,
            type,
            regexText,
            tooltipText,
            genErrMessage()
        )
    }

    override fun toRawInput(): RawInputFormBase {
        return RawTextFieldInput(
            labelText.trim(),
            placeholderTextProperty.value.trim(),
            selectedTypeProperty.get(),
            regexProperty.get().trim(),
            tooltipProperty.value.trim(),
            isUniqueField,
            isDuplicate
        )
    }

    private fun genErrMessage(): String? {
        val isLabelBlank: Boolean = labelText.isBlank()
        val isPlaceholderBlank = placeholderTextProperty.value.isBlank()
        val isTypeSelected = selectedTypeProperty.get() == null

        return genTextErrMessage(
            isLabelBlank,
            isDuplicate,
            isPlaceholderBlank,
            isTypeSelected,
            !isRegexValid,
            regexProperty.get(),
            labelText
        )

    }




    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TextFieldPaneKt) return false


        if (isUniqueField != other.isUniqueField) return false
        if (placeholderTextProperty != other.placeholderTextProperty) return false
        if (selectedTypeProperty != other.selectedTypeProperty) return false
        if (regexProperty != other.regexProperty) return false
        if (tooltipProperty != other.tooltipProperty) return false
        if (isRegexValid != other.isRegexValid) return false
        if (suffix != other.suffix) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isUniqueField.hashCode()
        result = 31 * result + placeholderTextProperty.hashCode()
        result = 31 * result + selectedTypeProperty.hashCode()
        result = 31 * result + regexProperty.hashCode()
        result = 31 * result + tooltipProperty.hashCode()
        result = 31 * result + isRegexValid.hashCode()
        result = 31 * result + suffix.hashCode()
        return result
    }

    override fun toString(): String {
        return "TextFieldPaneKt(isUniqueField=$isUniqueField, placeholderTextProperty=$placeholderTextProperty, selectedTypeProperty=$selectedTypeProperty, regexProperty=$regexProperty, tooltipProperty=$tooltipProperty, isRegexValid=$isRegexValid, suffix='$suffix')"
    }


}