package edu.opjms.candidateSelector

import edu.opjms.global.deleteIcon24
import edu.opjms.common.ShortcutTooltip
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Control
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.text.Text
import javafx.util.converter.DefaultStringConverter

class ValidatableCell(
    private val validator: Validator<EditData, Pair<String, String>>,
    private val onDelete: (String, Int) -> Unit
) : TextFieldListCell<String>(DefaultStringConverter()) {

    private val label = Text()
    private val deleteButton = Button().apply {
        visibleProperty().bind(this@ValidatableCell.focusedProperty().or(this@ValidatableCell.hoverProperty()))
        styleClass.setAll("delete")
        graphic = deleteIcon24
        onAction = EventHandler {
            onDelete(item, index)
        }
    }
    private val wrapper = HBox().apply {
        children.setAll(
            label,
            Region().apply { HBox.setHgrow(this, Priority.ALWAYS) },
            deleteButton.apply {
                tooltip = ShortcutTooltip("Delete")
            }
        )
        alignment = Pos.CENTER
    }

    override fun commitEdit(newValue: String?) {
        val newItem = newValue?.trim()
        val data = EditData(item, newItem, listView.items)
        val err = validator.validate(data)

        when {
            newItem == "" -> super.cancelEdit()
            err == null -> super.commitEdit(newItem)
            else -> validator.showError(err, childrenUnmodifiable[0] as Control)
        }
    }

    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
        } else {
            label.text = item
            graphic = wrapper
        }

        text = null
    }

    override fun cancelEdit() {
        super.cancelEdit()
        // prevents cells being squashed
        updateItem(item, false)
    }

    data class EditData(val oldItem: String?, val newItem: String?, val itemList: List<String>)
}


/**
 * U - The type of Input that has to be validated
 * V - The type that contains the error
 * */
interface Validator<U, V> {
    fun validate(data: U): V?
    fun showError(error: V, receiver: Control)
}

