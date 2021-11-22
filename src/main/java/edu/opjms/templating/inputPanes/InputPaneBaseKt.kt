package edu.opjms.templating.inputPanes

import edu.opjms.global.arrowDown24
import edu.opjms.global.arrowUp24
import edu.opjms.global.deleteIcon
import edu.opjms.global.inputForms.RawInputFormBase
import javafx.beans.Observable
import javafx.beans.binding.BooleanBinding
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.*
import javafx.scene.shape.SVGPath
import javafx.scene.text.Text
import java.util.function.IntConsumer

abstract class InputPaneBaseKt(label: String = ""): TitledPane() {
    protected val labelField = ValidatedTextField(label)
    protected val errLabel = Label()

    private val labelTextProperty = labelField.textProperty()
    val labelText: String
       get() = labelTextProperty.get()

    init {
        labelField.nonBlankValidation(errLabel, DUPLICATE_ERR)
    }

    override fun createDefaultSkin(): Skin<*> {
        textProperty().bind(labelTextProperty.concat(suffix))
        return super.createDefaultSkin()
    }

    var isDuplicate: Boolean = false
        set(value) {
            field = value
            val reason = labelField.errReason
            val wasDuplicate = reason == DUPLICATE_ERR

            // do nothing if text field is blank
            if (wasDuplicate != value && reason != BLANK_LABEL_ERR) {
                labelField.setError( if (labelText.isBlank()) BLANK_LABEL_ERR else null )
            }
        }

    fun addControlButtons(evt: EventHandler<ActionEvent>, moveWithOffset: IntConsumer) {
        if (skin == null) {
            skinProperty().addListener { _ ->
                addControlButtonsImpl(evt, moveWithOffset)
//                hasControls = true
            }
        } else {
            addControlButtonsImpl(evt, moveWithOffset)
//            hasControls = true
        }
    }

    private fun addControlButtonsImpl(evt: EventHandler<ActionEvent>, offset: IntConsumer) {
        textProperty().unbind()
        textProperty().value = ""

        val currentTitle = lookup(".title")
        currentTitle.onMouseClicked = currentTitle.onMouseReleased
        currentTitle.onMouseReleased = null

        val title = HBox()

        title.styleClass.add("inner-title")
        title.spacing = 5.0
        title.alignment = Pos.CENTER_LEFT
        title.minWidthProperty().bind(widthProperty().subtract(50))
        title.maxHeight = Region.USE_COMPUTED_SIZE

        val titleLabel = Label()
        titleLabel.textProperty().bind(labelTextProperty.concat(suffix))
        val isVisible = currentTitle.hoverProperty().or(this.focusedProperty())

        val deleteButton = getDeleteButton(evt, isVisible)
        val moveUpButton = getMoveButton(offset, isVisible, arrowUp24, "Move Up", -1)
        val moveDownButton = getMoveButton(offset, isVisible, arrowDown24, "Move Down", 1)


        val spacer = Region()
        HBox.setHgrow(spacer, Priority.ALWAYS)

        title.children.addAll(
            titleLabel,
            spacer,
            moveUpButton,
            moveDownButton,
            deleteButton
        )

        this.onKeyPressed = EventHandler { keyEvent: KeyEvent ->
            val keyCode = keyEvent.code
            //opening and close convenience
            if (keyCode == KeyCode.LEFT)
                super.setExpanded(false)
            else if (keyCode == KeyCode.RIGHT)
                super.setExpanded(true)

            //shortcut keys
            if (keyEvent.isShortcutDown) {
                
                when (keyCode) {
                    KeyCode.DELETE -> deleteButton.fire()
                    KeyCode.UP -> offset.accept(-1)
                    KeyCode.DOWN -> offset.accept(1)
                    else -> {}
                }
            }
        }
        graphic = title

    }

    private fun getDeleteButton(evt: EventHandler<ActionEvent>, isVisible: BooleanBinding): Button {
        val shortcutText = Text(KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHORTCUT_DOWN).displayText)
        shortcutText.styleClass.add("shortcut")

        val deleteTooltip = Tooltip("Delete").apply {
            graphic = shortcutText
            contentDisplay = ContentDisplay.RIGHT
        }

        val graphic = deleteIcon
        graphic.styleClass.add("delete")

        val deleteButton = Button("", graphic).apply {
            tooltip = deleteTooltip
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            styleClass.setAll("delete")
            visibleProperty().bind(isVisible)
            onAction = evt
        }

        return deleteButton
    }

    private fun getMoveButton(
        action: IntConsumer,
        isVisible: BooleanBinding,
        icon: SVGPath,
        tooltipText: String,
        offset: Int
    ): Button {
        val shortcutText = Text(KeyCodeCombination(KeyCode.UP, KeyCombination.SHORTCUT_DOWN).displayText)
        shortcutText.styleClass.add("shortcut")

        val deleteTooltip = Tooltip(tooltipText).apply {
            graphic = shortcutText
            contentDisplay = ContentDisplay.RIGHT
        }

        icon.styleClass.add("move")

        val move = Button("", icon).apply {
            tooltip = deleteTooltip
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            styleClass.setAll("move")
            visibleProperty().bind(isVisible)
            onAction = EventHandler { action.accept(offset) }
        }

        return move
    }

    /**
     * Tells whether the input contains an error which will prevent
     * the input from generating valid html content.
     */
    abstract fun containsError(): Boolean
    abstract fun generateHTML(id: Int): String
    abstract fun toRawInput(): RawInputFormBase
    abstract val suffix: String


    protected companion object {
        private const val DUPLICATE_ERR = "The label is Duplicated"
        private const val BLANK_LABEL_ERR = "Label must be set"

        @JvmStatic
        protected fun wrapInVBox(vararg nodes: Node): VBox {
            val wrapper = VBox(*nodes)
            wrapper.spacing = 5.0
            return wrapper
        }

        @JvmStatic
        protected fun wrapInFlowPane(vararg nodes: Node): FlowPane {
            val wrapper = FlowPane(*nodes)
            wrapper.hgap = 20.0
            wrapper.vgap = 15.0
            return wrapper
        }
    }

}