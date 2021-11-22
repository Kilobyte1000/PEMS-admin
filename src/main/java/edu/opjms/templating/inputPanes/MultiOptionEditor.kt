package edu.opjms.templating.inputPanes

import edu.opjms.global.ERROR_CLASS
import edu.opjms.global.deleteIcon24
import edu.opjms.global.plusIcon24
import edu.opjms.templating.searchString
import javafx.beans.binding.BooleanExpression
import javafx.beans.property.SimpleBooleanProperty
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.GridPane
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.BiConsumer

typealias StringPair = Pair<String, String>

class MultiOptionEditor(
        hGap: Double,
        vGap: Double,
        private val doValidation: BooleanExpression,
        private val doAutoFill: BooleanExpression,
        fieldData: FieldData? = null
): GridPane() {

    private var duplicates = 0

    @Suppress("SpellCheckingInspection")
    private var nonNumerics = 0

    private val fieldMetadata = ArrayList<FieldMetadata>()

    private var _executor: ExecutorService? = null

    // lazy executor initialisation prevents from starting
    // executors that are shutdown without doing anything
    var executor: ExecutorService
        get() {
            return _executor ?: Executors.newCachedThreadPool { Thread(it).apply { isDaemon = true } }.also {
                _executor = it
                println("default executor init")
            }
        }
        set(value) {
            _executor = value
        }

    private val isLastFieldEmpty = SimpleBooleanProperty(true)

    init {
        hgap = hGap
        vgap = vGap
        val visibleLabel = Label("Visible Values")
        val internalLabel = Label("Internal Values")
        addRow(0, visibleLabel, internalLabel)

        if (fieldData != null) {
            val (pairs, duplicates, nonNumerics) = fieldData
            fieldMetadata.ensureCapacity(pairs.size)

            for (i in pairs.indices) {
                val pair = pairs[i]
                addField(pair.first, pair.second, bindEmptyFieldHandler = false)
                fieldMetadata[i].apply {
                    isDuplicate = pair.first.lowercase() in duplicates
                    isNumeric = pair.second.lowercase() !in nonNumerics
                }
                updateErrorHints(i + 1)
            }
        }
        addField()

        isLastFieldEmpty.addListener { _, _, isEmpty ->
            if (!isEmpty)
                addField(bindEmptyFieldHandler = true)
        }
        doValidation.addListener { _, _, _ ->
            repeat(rowCount - 2) {
                updateErrorHints(it + 1)
            }
        }
    }

    private fun addField(
            visibleValue: String? = null,
            internalValue: String? = null,
            rowIndex: Int = rowCount,
            bindEmptyFieldHandler: Boolean = rowIndex == rowCount
    ) {

        fieldMetadata.add(rowIndex - 1, FieldMetadata())
        val isLast = rowIndex == rowCount
        val visibleField = getTextField(visibleValue)
        val internalField = getTextField(internalValue)

        //start with invisible message to prevent re-layout
        val errLabel = Label(NON_NUMERIC_MESSAGE).apply { isVisible = false }

        visibleField.onAction = EventHandler { internalField.requestFocus() }
        internalField.onAction = EventHandler {
            //index of visible field in next row
            val i = children.indexOf(internalField) + NODES_IN_EACH_ROW - 1

            if (i < children.size) {
                children[i].requestFocus()
            }
        }

        visibleField.textProperty().addListener { _, old, new -> autoFill(internalField, old, new) }
        errLabel.styleClass += "err"

        visibleField.onTextChange = BiConsumer { old, new ->
            val i = getRowIndex(visibleField) - 1
            if (old.isNotEmpty() && fieldMetadata[i].isDuplicate) {
                updateDuplicates(old)
            }
            if (new.isNotEmpty()) {
                updateDuplicates(new)
            } else {
                setDuplicate(i, false)
            }
            
        }

        internalField.focusedProperty().addListener { _, _, _ ->
            if (!internalField.isFocused) {
                val i = getRowIndex(internalField)
                setNumeric(i, internalField.text)
            }
        }
        internalField.textProperty().addListener { _, _, new ->
            val i = getRowIndex(internalField)
            if (!fieldMetadata[i - 1].isNumeric) {
                setNumeric(i, new)
            }
        }

        val deleteButton = Button().apply {
            graphic = deleteIcon24
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            onAction = EventHandler { deleteRow(getRowIndex(this)) }
        }
        val addButton = Button(null, plusIcon24).apply {
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            onAction = EventHandler { addField(rowIndex = getRowIndex(this) + 1) }
        }

        if (bindEmptyFieldHandler) {
            isLastFieldEmpty.bind(visibleField
                    .textProperty()
                    .isEmpty
                    .and(internalField
                            .textProperty().isEmpty))
        }

        if (isLast) {
            if (rowIndex > 1) {
                val i = children.size - 1
                //set add button and remove button visible
                //and make label empty
                (children[i] as Label).run {
                    isVisible = true
                    text = ""
                }
                children[i - 1].isVisible = true
                children[i - 2].isVisible = true

            }

            deleteButton.isVisible = false
            addButton.isVisible = false
            addRow(rowIndex, visibleField, internalField, deleteButton, addButton, errLabel)
        } else {
            shiftRows(rowIndex, 1)

            val list = listOf(visibleField, internalField, deleteButton, addButton, errLabel)
            list.forEachIndexed { i, node -> setColumnIndex(node, i) }
            list.forEach { setRowIndex(it, rowIndex) }

            val index = getFirstIndexOfRow(rowIndex)
            children.addAll(index, list)
        }
    }

    private fun deleteRow(rowIndex: Int) {
        val rows = rowCount

        //removing last row is forbidden
        if (rowIndex < rows - 1) {
            val data = fieldMetadata.removeAt(rowIndex - 1)
            if (data.isDuplicate) {
                duplicates--
            }
            if (!data.isNumeric) {
                nonNumerics--
            }

            val nodesBeforeRow = getFirstIndexOfRow(rowIndex)
            val children = children

            //will search after removing
            val visibleText = (children[nodesBeforeRow] as TextField).text
            var nodeIndex = nodesBeforeRow + NODES_IN_EACH_ROW - 1

            //remove row
            repeat(NODES_IN_EACH_ROW) { children.removeAt(nodeIndex--) }

            // we provide our own node index
            // since gridView has not updated the
            // rows but has updated the children list
            // Thus, rows and no. of nodes are out of sync
            shiftRows(rowIndex + 1, -1, nodeIndex + 1)

            if (visibleText.isNotBlank()) {
                updateDuplicates(visibleText)
            }
        }
    }

    private fun shiftRows(
            rowIndex: Int,
            offset: Int,
            nodeIndex: Int = getFirstIndexOfRow(rowIndex)
    ) {
        var nodeIndex1 = nodeIndex
        val rows = rowCount
        val children = children

        for (i in rowIndex until rows) {
            repeat(NODES_IN_EACH_ROW) {
                setRowIndex(children[nodeIndex1++], i + offset)
            }
        }

    }

    private fun setDuplicate(index: Int, isDuplicate: Boolean) {
        val data = fieldMetadata[index]
        if (data.isDuplicate != isDuplicate) {
            data.isDuplicate = isDuplicate
            duplicates += boolToInt(isDuplicate)

            updateErrorHints(index + 1)
        }
    }

    private fun setNumeric(rowIndex: Int, text: String) {
        val isNumeric = text.isBlank() || text.isDouble()
        val data = fieldMetadata[rowIndex - 1]
        if (data.isNumeric != isNumeric) {
            nonNumerics += boolToInt(!isNumeric)
        }
        data.isNumeric = isNumeric
        updateErrorHints(rowIndex)
    }

    private fun updateErrorHints(index: Int) {
        val data = fieldMetadata[index - 1]
        val children = children
        val i = getFirstIndexOfRow(index)
        val visibleField = children[i]
        val internalField = children[i+1]
        val label = children[i + NODES_IN_EACH_ROW - 1] as Label

        val isNonNumeric = !data.isNumeric && doValidation.get()
        visibleField.pseudoClassStateChanged(ERROR_CLASS, data.isDuplicate)
        internalField.pseudoClassStateChanged(ERROR_CLASS, isNonNumeric)

        label.text = when {
            data.isDuplicate -> DUPLICATE_MESSAGE
            isNonNumeric -> NON_NUMERIC_MESSAGE
            else -> ""
        }
    }

    //todo: add function updateErrorHintsRange

    private fun updateDuplicates(string: String) {
        val task = object : Task<Unit>() {
            lateinit var indices: IntArray
            init {
                onSucceeded = EventHandler {
                    val isDuplicate = indices.size > 1
                    for (i in indices) {
                        setDuplicate(i, isDuplicate)
                    }
                }
            }
            override fun call() {
                indices = searchString(string, visibleValueIterator(), rowCount - 1)
            }
        }
        executor.execute(task)
    }

    private fun getFirstIndexOfRow(rowIndex: Int): Int {
        assert(rowIndex > 0)
        return NUMBER_OF_LABELS + ((rowIndex - 1) * NODES_IN_EACH_ROW)
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun getTextField(text: String?): TextFieldChange {
        return TextFieldChange(text ?: "").also { field ->
            field.onKeyPressed = EventHandler {
                when (it.code) {
                    KeyCode.UP -> {
                        var i = childrenUnmodifiable.indexOf(field)
                        i -= NODES_IN_EACH_ROW
                        if (i >= NUMBER_OF_LABELS) {
                            childrenUnmodifiable[i].requestFocus()
                        }
                        it.consume()
                    }
                    KeyCode.DOWN -> {
                        var i = childrenUnmodifiable.indexOf(field)
                        i += NODES_IN_EACH_ROW
                        if (i < childrenUnmodifiable.size) {
                            childrenUnmodifiable[i].requestFocus()
                        }
                        it.consume()
                    }
                }
            }
        }
    }

    private fun autoFill(textField: TextField, old: String, new: String) {
        if (doAutoFill.get() && textField.text.equals(old, true)) {
            val text = new.lowercase(Locale.getDefault())
            textField.text = text
            setNumeric(getRowIndex(textField), text)
        }
    }

    fun visibleValueIterator(): Iterator<String> {
        val children = childrenUnmodifiable
        val rows = rowCount
        return object : AbstractIterator<String>() {
            var i = 1
            var j = NUMBER_OF_LABELS
            override fun computeNext() {
                if (i++ < rows) {
                    val field = children[j] as TextField
                    j += NODES_IN_EACH_ROW
                    setNext(field.text)

                } else {
                    done()
                }
            }
        }
    }

    fun containsError() = duplicates != 0 || (doValidation.get() && nonNumerics != 0)

    fun getPairsAndDuplicates(): FieldsAndDuplicates {
        //excluding first (header) and last (always empty)
        var nodeIndex = getFirstIndexOfRow(1)
        val children = childrenUnmodifiable
        val duplicates = HashSet<String>(duplicates)

        val fields = List(rowCount - 2) {
            val visibleText = (children[nodeIndex] as TextField).text
            val internalText = (children[nodeIndex + 1] as TextField).text
            val lowCaseVisibleText = visibleText.lowercase()

            if (fieldMetadata[it].isDuplicate && lowCaseVisibleText !in duplicates) {
                duplicates.add(lowCaseVisibleText)
            }

            nodeIndex += NODES_IN_EACH_ROW
            visibleText to internalText
        }

        return FieldsAndDuplicates(fields, duplicates)
    }

    fun getPairsAndData(): FieldData {
        //excluding first (header) and last (always empty)
        var nodeIndex = getFirstIndexOfRow(1)
        val children = childrenUnmodifiable
        val duplicates = HashSet<String>(duplicates)
        val nonNumeric = HashSet<String>(duplicates)

        val fields = List(rowCount - 2) {
            val visibleText = (children[nodeIndex] as TextField).text
            val internalText = (children[nodeIndex + 1] as TextField).text
            val lowCaseVisibleText = visibleText.lowercase()
            val metadata = fieldMetadata[it]

            if (metadata.isDuplicate && lowCaseVisibleText !in duplicates) {
                duplicates.add(lowCaseVisibleText)
            }
            if (!metadata.isNumeric && lowCaseVisibleText !in nonNumeric) {
                nonNumeric.add(lowCaseVisibleText)
            }

            nodeIndex += NODES_IN_EACH_ROW
            visibleText to internalText
        }

        return FieldData(fields, duplicates, nonNumeric)
    }


    private companion object {
        private const val NUMBER_OF_LABELS = 2
        private const val NODES_IN_EACH_ROW = 5
        private const val DUPLICATE_MESSAGE = "Display Text is Duplicate"
        private const val NON_NUMERIC_MESSAGE = "Internal Value is not a Number"
        private fun String.isDouble(): Boolean {
            val length: Int = length

            if (length > 306) //double overflows at 1.7*10^308, so it is good idea to stop at 306 digits
                return false

            var i = 0

            if (this[0] == '-') {
                if (length == 1) return false
                i = 1
            }

            var encounteredDecimal = false

            while (i < length) {
                val c: Char = this[i]
                if (c == '.') {
                    if (!encounteredDecimal) {
                        encounteredDecimal = true
                        i++
                        continue
                    } else return false
                }
                if (c < '0' || c > '9') return false
                i++
            }

            return true
        }
        private fun boolToInt(boolean: Boolean) = if (boolean) 1 else -1
    }

    private data class FieldMetadata(var isDuplicate: Boolean = false, var isNumeric: Boolean = true)
    data class FieldsAndDuplicates(val pairs: List<StringPair>, val duplicates: Set<String>)

//    @Serializable @JvmRecord
//    data class FieldData(val pairs: List<StringPair>, val duplicates: Set<String>, val nonNumeric: Set<String>)

}