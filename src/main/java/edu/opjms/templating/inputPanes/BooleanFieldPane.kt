package edu.opjms.templating.inputPanes

import edu.opjms.global.inputForms.RawBooleanField
import edu.opjms.global.inputForms.RawInputFormBase
import edu.opjms.global.inputForms.genBooleanInput
import javafx.beans.property.StringProperty
import javafx.scene.control.Label
import javafx.scene.control.TextField

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
            labelInput.pseudoClassStateChanged(ERR_CLASS, true)
        }
        labelInput.textProperty().addListener { _, _, new ->
            isLabelValid = new.isNotBlank()
            if (!isLabelDuplicate) {
                labelErr.text = if (isLabelValid) "" else INVALID_LABEL_ERR
                labelInput.pseudoClassStateChanged(ERR_CLASS, !isLabelValid)
            }
        }

        val trueInput = TextFieldChange(trueLabel)
        trueText = trueInput.textProperty()
        val trueErrLabel = Label().apply {
            isVisible = false
            isWrapText = true
            styleClass.add("err")
            maxWidthProperty().bind(trueInput.widthProperty())
        }

        val falseInput = TextFieldChange(falseLabel)
        falseText = falseInput.textProperty()
        val falseErrLabel = Label().apply {
            isVisible = false
            isWrapText = true
            styleClass.add("err")
            maxWidthProperty().bind(falseInput.widthProperty())
        }

        trueInput.textProperty().addListener { _, _, _ -> updateErrHints(trueInput, trueErrLabel, falseInput, falseErrLabel) }
        falseInput.textProperty().addListener { _, _, _ -> updateErrHints(trueInput, trueErrLabel, falseInput, falseErrLabel) }


        content = wrapInFlowPane(
                wrapInVBox(Label("Label Text"), labelInput, labelErr),
                wrapInVBox(Label("True Label"), trueInput, trueErrLabel),
                wrapInVBox(Label("False Label"), falseInput, falseErrLabel)
        )

        updateErrHints(trueInput, trueErrLabel, falseInput, falseErrLabel)
        configureSuper(labelInput.textProperty(), "- Boolean Input")
    }

    private fun updateErrHints(trueInput: TextField, trueLabel: Label, falseInput: TextField, falseLabel: Label) {
        val trueText = trueInput.text
        val falseText = falseInput.text

        if (trueText.equals(falseText, true)) {
            trueLabel.apply {
                text = "Both Labels must be Distinct"
                isVisible = true
            }
            falseLabel.apply {
                text = "Both Labels must be Distinct"
                isVisible = true
            }

            trueInput.pseudoClassStateChanged(ERR_CLASS, true)
            falseInput.pseudoClassStateChanged(ERR_CLASS, true)
            return
        }

        val isTrueTextEmpty = trueText.isEmpty()
        trueInput.pseudoClassStateChanged(ERR_CLASS, isTrueTextEmpty)
        trueLabel.apply {
            text = if (isTrueTextEmpty) "True label must be set" else ""
            isVisible = isTrueTextEmpty
        }

        val isFalseTextEmpty = falseText.isEmpty()
        falseInput.pseudoClassStateChanged(ERR_CLASS, isFalseTextEmpty)
        falseLabel.apply {
            text = if (isFalseTextEmpty) "False label must be set" else ""
            isVisible = isFalseTextEmpty
        }
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
}