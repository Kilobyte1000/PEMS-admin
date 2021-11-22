package edu.opjms.templating.inputPanes

import edu.opjms.global.ERROR_STYLECLASS
import edu.opjms.templating.inputPanes.ValidatedTextField.EditData
import javafx.scene.control.Label
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

fun ValidatedTextField.validate(errLabel: Label, validation: (EditData) -> String?) {
    errLabel.apply {
        isVisible = false
        isManaged = false
        styleClass += ERROR_STYLECLASS
    }

    inputValidator = validation
    showError = { s ->
        errLabel.text = s ?: ""
        val show = s != null
        errLabel.isVisible = show
        errLabel.isManaged = show
    }

}

@Suppress("NOTHING_TO_INLINE")
inline fun ValidatedTextField.nonBlankValidation(
    errLabel: Label,
    blankErrMessage: String
) {
    validate(errLabel) { if (it.newValue.isEmpty()) blankErrMessage else null }
}

@Suppress("NOTHING_TO_INLINE")
inline fun ValidatedTextField.nonBlankValidation(
    errLabel: Label,
    blankErrMessage: String,
    crossinline validation: (EditData) -> String?
) {
    validate(errLabel) { if (it.newValue.isEmpty()) blankErrMessage else validation(it) }
}



