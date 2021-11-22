package edu.opjms.templating.inputPanes

import edu.opjms.global.inputForms.RawSelectField
import edu.opjms.global.inputForms.genSelectErrMessage
import edu.opjms.global.inputForms.genSelectInput
import edu.opjms.templating.RawTypes
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.collections.FXCollections
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import jfxtras.styles.jmetro.JMetroStyleClass
import java.util.concurrent.ExecutorService

class SelectFieldPaneKt(
    labelText: String = "",
    rawType: RawTypes = RawTypes.AUTO_DETECT,
    data: FieldData? = null
): InputPaneBaseKt(labelText) {

    override val suffix = " - Select"
    private val multiOptionEditor: MultiOptionEditor
    private val type: ReadOnlyObjectProperty<RawTypes>

    init {
        val labelWrapper = wrapInVBox(Label("Label Text"), labelField, errLabel)

        val box = ComboBox(FXCollections.observableArrayList(*RawTypes.values()))
        box.selectionModel.select(rawType)
        type = box.selectionModel.selectedItemProperty()

        val typeWrapper = wrapInVBox(Label("Type"), box)

        multiOptionEditor = MultiOptionEditor(
            20.0,
            5.0,
            box.selectionModel.selectedIndexProperty().isEqualTo(1),
            box.selectionModel.selectedIndexProperty().isEqualTo(2),
            data
        )

        val wrapper = InputPaneBase.wrapInFlowPane(labelWrapper, typeWrapper, multiOptionEditor)
        wrapper.styleClass += JMetroStyleClass.BACKGROUND
        content = wrapper
    }

    /**
     * Forwards to [MultiOptionEditor]'s containError method - [MultiOptionEditor.containsError]
     */
    override fun containsError(): Boolean {
        return multiOptionEditor.containsError()
    }

    fun setExecutor(executor: ExecutorService) {
        multiOptionEditor.executor = executor
    }

    fun getExecutor(): ExecutorService {
        return multiOptionEditor.executor
    }

    override fun generateHTML(id: Int): String {
        val labelText = labelText.trim()

        if (type.get() == RawTypes.NUMBER) {
            val fieldData = multiOptionEditor.getPairsAndData()
            val (pairs, duplicates, nonNumerics) = fieldData
            val message = genSelectErrMessage(isDuplicate, labelText, duplicates, nonNumerics)
            return genSelectInput(labelText, pairs, message)
        }

        val (pairs, duplicates) = multiOptionEditor.getPairsAndDuplicates()
        val message = genSelectErrMessage(isDuplicate, labelText, duplicates, emptyList())
        return genSelectInput(labelText, pairs, message)
    }


    override fun toRawInput(): RawSelectField {
        return RawSelectField(labelText, type.get(), multiOptionEditor.getPairsAndData(), isDuplicate)
    }
}