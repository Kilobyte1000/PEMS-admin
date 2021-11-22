package edu.opjms.templating.inputPanes

import edu.opjms.global.ERROR_CLASS
import javafx.beans.value.ChangeListener
import javafx.scene.control.TextField

class ValidatedTextField : TextField {
    var inputValidator: (EditData) -> String? = { null }
    var onErrorStatusChange: (String?) -> Unit = { }
    var showError: (String?) -> Unit = { }

    private var oldText: String = ""
    private var fireOnNextFocusLost = false
    private val editData = EditData()

    var errReason: String? = null
        private set

    // used to remove error hints when 
    // user interacts with field
    private val removeHints = ChangeListener<String> { _, _, _ ->
        fireOnNextFocusLost = true
        setError(null)
    }

    init {
        focusedProperty().addListener { _, _, _ ->
            if (isFocused) {
                //on focus gain
                oldText = text
            } else {

                val text = text.trim()
                setText(text)
                if (fireOnNextFocusLost || text != oldText) {
                    setError(inputValidator(editData.of(oldText, text)))
                    fireOnNextFocusLost = false
                    oldText = ""

                }
            }
        }
    }

    fun setError(reason: String?) {
        if (reason != errReason) {
            val newError = reason != null
            val oldError = errReason != null

            errReason = reason

            if (newError != oldError) {
                pseudoClassStateChanged(ERROR_CLASS, newError)
                if (newError) {
                    textProperty().addListener(removeHints)
                } else {
                    textProperty().removeListener(removeHints)
                }
            }

            showError(reason)
            onErrorStatusChange(reason)
        }

    }

    fun fireValidation() {
        val text = text.trim()
        this.text = text

        setError(inputValidator(editData.of("", text)))
    }

    constructor() : super()

    constructor(text: String?) : super(text)

    data class EditData(var oldValue: String = "", var newValue: String = "")

    private fun EditData.of(oldValue: String, newValue: String): EditData {
        this.oldValue = oldValue
        this.newValue = newValue
        return this
    }
}