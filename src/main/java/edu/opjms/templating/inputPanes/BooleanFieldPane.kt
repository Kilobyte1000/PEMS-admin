package edu.opjms.templating.inputPanes

import edu.opjms.global.ERROR_CLASS
import edu.opjms.global.inputForms.RawBooleanField
import edu.opjms.global.inputForms.RawInputFormBase
import edu.opjms.global.inputForms.genBooleanInput
import javafx.beans.property.StringProperty
import javafx.scene.control.Label

class BooleanFieldPane(
        label: String = "",
        trueLabel: String = "",
        falseLabel: String = ""
) : InputPaneBase() {

    private val trueText: StringProperty
    private val falseText: StringProperty

    init {
        val labelInput = TextFieldChange(label)
        labelErr = Label().apply { styleClass.add("err") }
        labelField = labelInput
        if (label.isBlank()) {
            isLabelValid = false
            labelErr.text = INVALID_LABEL_ERR
            labelInput.pseudoClassStateChanged(ERROR_CLASS, true)
        }
        labelInput.textProperty().addListener { _, _, new ->
            isLabelValid = new.isNotBlank()
            if (!isLabelDuplicate) {
                labelErr.text = if (isLabelValid) "" else INVALID_LABEL_ERR
                labelInput.pseudoClassStateChanged(ERROR_CLASS, !isLabelValid)
            }
        }

        val trueInput = ValidatedTextField(trueLabel)
        trueText = trueInput.textProperty()
        val trueErrLabel = Label().apply {
            isWrapText = true
            maxWidthProperty().bind(trueInput.widthProperty())
        }

        val falseInput = ValidatedTextField(falseLabel)
        falseText = falseInput.textProperty()
        val falseErrLabel = Label().apply {
            isWrapText = true
            maxWidthProperty().bind(falseInput.widthProperty())
        }



        trueInput.nonBlankValidation(trueErrLabel, TRUE_BLANK) {
            if (it.newValue.equals(falseInput.text, true)) {
                falseInput.setError(SAME_ERROR)
                SAME_ERROR
            } else
                null
        }

        falseInput.nonBlankValidation(falseErrLabel, FALSE_BLANK) {
            if (it.newValue.equals(trueInput.text, true)) {
                trueInput.setError(SAME_ERROR)
                SAME_ERROR
            } else
                null
        }

        trueInput.onErrorStatusChange = {
            if (it == null && falseInput.errReason == SAME_ERROR)
                falseInput.setError(null)
        }


        falseInput.onErrorStatusChange = {
            if (it == null && trueInput.errReason == SAME_ERROR)
                trueInput.setError(null)
        }


        content = wrapInFlowPane(
                wrapInVBox(Label("Label Text"), labelInput, labelErr),
                wrapInVBox(Label("True Label"), trueInput, trueErrLabel),
                wrapInVBox(Label("False Label"), falseInput, falseErrLabel)
        )

        trueInput.fireValidation()
        falseInput.fireValidation()

        configureSuper(labelInput.textProperty(), " - Boolean Input")
    }

    override fun containsError(): Boolean {
        val trueField = trueText.get()
        val falseField = falseText.get()
        return isLabelDuplicate
                || labelText.isBlank()
                || trueField.isBlank()
                || falseField.isBlank()
                || falseField.equals(trueField, true)
    }

    override fun generateHTML(id: Int): String {
        return genBooleanInput(isLabelDuplicate, labelText, trueText.get(), falseText.get())
    }

    override fun toRawInput(): RawInputFormBase {
        return RawBooleanField(labelText, trueText.get(), falseText.get(), isLabelDuplicate)
    }

    private companion object {
        const val TRUE_BLANK = "True label must be set"
        const val FALSE_BLANK = "False label must be set"
        const val SAME_ERROR = "Both Labels must be Distinct"
    }
}