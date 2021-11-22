package edu.opjms.templating.inputPanes

import edu.opjms.global.inputForms.genDateErrMessage
import edu.opjms.global.inputForms.genDateInput
import edu.opjms.templating.inputPanes.InputPaneBase
import edu.opjms.global.inputForms.RawInputFormBase
import edu.opjms.global.inputForms.RawDateInput
import edu.opjms.templating.inputPanes.TextFieldChange
import java.util.Objects
import javafx.beans.value.ObservableValue
import javafx.scene.layout.HBox
import jfxtras.styles.jmetro.JMetroStyleClass
import edu.opjms.templating.inputPanes.DateInputPane
import javafx.geometry.Pos
import javafx.scene.control.Label

class DateInputPane(label: String = "") : InputPaneBaseKt(label) {

    override val suffix = " - Date Input"

    override fun containsError(): Boolean {
        return labelText.isBlank() || isDuplicate
    }

    override fun generateHTML(id: Int): String {
        val labelText = labelText.trim()
        val message = genDateErrMessage(isDuplicate, labelText)
        return genDateInput(labelText, message)
    }

    override fun toRawInput(): RawInputFormBase {
        return RawDateInput(labelText.trim(), isDuplicate)
    }


    init {
        val wrapper = HBox(Label("Label Text: "), labelField, errLabel)
        wrapper.spacing = 8.0
        wrapper.alignment = Pos.CENTER_LEFT
        wrapper.styleClass.add(JMetroStyleClass.BACKGROUND)
        content = wrapper
    }
}