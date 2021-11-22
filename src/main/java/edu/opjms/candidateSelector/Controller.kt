package edu.opjms.candidateSelector

import edu.opjms.candidateSelector.undo.AddItemEvent
import edu.opjms.candidateSelector.undo.EditItemEvent
import edu.opjms.candidateSelector.undo.RemoveItemsEvent
import edu.opjms.candidateSelector.undo.UndoEvent
import net.kilobyte1000.Houses
import edu.opjms.common.Posts
import edu.opjms.common.Sidebar
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import javafx.css.PseudoClass
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.CacheHint
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.util.Callback
import javafx.util.Duration
import org.controlsfx.control.PopOver
import java.lang.ref.WeakReference
import java.util.*
import kotlin.io.path.ExperimentalPathApi

@OptIn(ExperimentalPathApi::class)
internal class Controller(
    private val boyList: ListView<String>,
    private val girlList: ListView<String>,
    private val sidebar: Sidebar,
    private val tabBar: Editor.TabBar
//        private val root: Pane
) {


    private val listData = ListData()
    private val undoManager = UndoManager()
    /*private val timer = Timer("Saving data to file", true).apply {
        val task = object : TimerTask() {
            override fun run() {
                println("saving file")
            }
        }
        this.scheduleAtFixedRate(task, PERIOD_MILLIS, PERIOD_MILLIS)
    }*/

    init {
        initList(boyList, true)
        initList(girlList, false)

        loadData(sidebar.selectedHouse, tabBar.activeTab, false)
        sidebar.onHouseChange = { loadData(this, tabBar.activeTab, animateOnChange) }

        tabBar.onChange = { loadData(sidebar.selectedHouse, this, animateOnChange) }

    }

    private fun initList(listView: ListView<String>, isBoy: Boolean) {
        listView.isEditable = true
        val validator = NameValidator()
        listView.selectionModel.selectionMode = SelectionMode.MULTIPLE

        listView.cellFactory = Callback {
            ValidatableCell(validator) { item, i -> deleteItem(listView, listOf(item), i) }
        }

        listView.addEventFilter(KeyEvent.KEY_PRESSED) {
            val isNotEditing = listView.editingIndex == -1
            if (isNotEditing) {
                @Suppress("NON_EXHAUSTIVE_WHEN")
                when (it.code) {
                    KeyCode.ESCAPE -> listView.selectionModel.clearSelection()
                    KeyCode.INSERT -> {

                        it.consume()
                        listView.items.add("")
                        listView.layout()
                        listView.edit(listView.items.size - 1)
                    }
                    KeyCode.DELETE -> {
                        it.consume()
                        val model = listView.selectionModel
                        val items = model.selectedItems.toList()  //store copy of list

                        if (items.isNotEmpty())
                            deleteItem(listView, items, model.selectedIndices.minOf { int -> int })

                    }
                }
            }
        }

        listView.onKeyPressed = EventHandler {

        }


        listView.onEditCancel = EventHandler {
            val items = it.source.items
            val i = items.size - 1

            // new value is always inserted at end
            // we check if last item is empty to delete
            // the item because it was cancelled while adding

            if (items[i].isEmpty()) {
                items.removeAt(i)
                return@EventHandler
            }
        }


        listView.addEventFilter(ListView.editCommitEvent<String>()) {
            val isElementAdded = it.source.items[it.index].isEmpty()
            val evt = if (isElementAdded) {
                AddItemEvent<String>(
                    it.newValue,
                    it.index,
                    sidebar.selectedHouse,
                    tabBar.activeTab,
                    isBoy
                )
            } else {
                EditItemEvent<String>(
                    it.source.items[it.index],
                    it.newValue,
                    it.index,
                    sidebar.selectedHouse,
                    tabBar.activeTab,
                    isBoy
                )
            }
            undoManager.addUndoEvent(evt)

            //sometimes listView loses focus after edit
            //commit for no apparent reason
            //solution is to request focus
            it.source.requestFocus()
        }

//        listView.contextMenu = createContextMenu(listView)
    }

    private fun deleteItem(list: ListView<String>, items: List<String>, index: Int) {
        list.items.removeAll(items)
        val evt = RemoveItemsEvent(
            items,
            index,
            sidebar.selectedHouse,
            tabBar.activeTab,
            list == boyList
        )
        undoManager.addUndoEvent(evt)
    }

    private fun loadData(house: Houses, post: Posts, animate: Boolean = true) {

        //cancel edit
        if (boyList.editingIndex != -1) {
            boyList.items[boyList.editingIndex] = boyList.items[boyList.editingIndex]
        }
        if (girlList.editingIndex != -1) {
            girlList.items[girlList.editingIndex] = girlList.items[girlList.editingIndex]
        }

        if (animate) {
            val boyNode = boyList.lookup(".sheet")
            val girlNode = girlList.lookup(".sheet")

            boyNode.cacheHint = CacheHint.SPEED
            girlNode.cacheHint = CacheHint.SPEED

            val fadeIn = Timeline(
                KeyFrame(
                    Duration.ZERO,
                    KeyValue(boyNode.opacityProperty(), 0),
                    KeyValue(girlNode.opacityProperty(), 0),
                    KeyValue(boyNode.translateYProperty(), 15.0),
                    KeyValue(girlNode.translateYProperty(), 15.0)
                ),
                KeyFrame(
                    Duration.millis(200.0),
                    KeyValue(boyNode.opacityProperty(), 1, Interpolator.SPLINE(0.0, .26, .14, .66)),
                    KeyValue(girlNode.opacityProperty(), 1, Interpolator.SPLINE(0.0, .26, .14, .66)),
                    KeyValue(boyNode.translateYProperty(), 0, Interpolator.EASE_OUT),
                    KeyValue(girlNode.translateYProperty(), 0, Interpolator.EASE_OUT)
                )
            )
            fadeIn.onFinished = EventHandler {
                boyNode.cacheHint = CacheHint.QUALITY
                girlNode.cacheHint = CacheHint.QUALITY
            }

            val fadeOut = Timeline(
                KeyFrame(
                    Duration.ZERO,
                    KeyValue(boyNode.opacityProperty(), 1),
                    KeyValue(girlNode.opacityProperty(), 1),
                    KeyValue(boyNode.translateYProperty(), 0),
                    KeyValue(girlNode.translateYProperty(), 0)
                ),
                KeyFrame(
                    Duration.millis(100.0),
                    KeyValue(boyNode.opacityProperty(), 0, Interpolator.EASE_OUT),
                    KeyValue(girlNode.opacityProperty(), 0, Interpolator.EASE_OUT),
                    KeyValue(boyNode.translateYProperty(), -10, Interpolator.EASE_IN),
                    KeyValue(girlNode.translateYProperty(), -10, Interpolator.EASE_IN)
                )
            )

            fadeOut.onFinished = EventHandler {
                val list = listData.getList(house, post)

                boyList.items = list.maleList
                girlList.items = list.femaleList

                boyList.selectionModel.clearSelection()
                fadeIn.play()

            }
            fadeOut.play()
        } else {
            val list = listData.getList(house, post)

            boyList.items = list.maleList
            girlList.items = list.femaleList

            boyList.selectionModel.clearSelection()

            // this is triggered manually, not by user
            // so we have to also update view
            if (tabBar.activeTab != post)
                tabBar.switchTab(true)
            sidebar.selectedHouse = house
        }
    }

    //public API
    var animateOnChange = true
    val isUndoAvailableProperty = undoManager.isUndoAvailableProperty
    val isRedoAvailableProperty = undoManager.isRedoAvailableProperty

    fun undo() = undoManager.undoLast()
    fun redo() = undoManager.redoLast()

    val isAnyListFocused = boyList.focusedProperty().or(girlList.focusTraversableProperty())
    val focusedList: ListView<String>?
        get() {
            return when {
                boyList.isFocused -> boyList
                girlList.isFocused -> girlList
                else -> null
            }
        }

    private inner class UndoManager {
        private val undoDeque = ArrayDeque<UndoEvent<String>>(UNDO_DEQUE_SIZE)
        private val redoDeque = ArrayDeque<UndoEvent<String>>(UNDO_DEQUE_SIZE)

        val isUndoAvailableProperty = SimpleBooleanProperty(false)
        val isRedoAvailableProperty = SimpleBooleanProperty(false)

        fun addUndoEvent(evt: UndoEvent<String>) = addUndoEvent(evt, true)

        fun undoLast() {
            val evt: UndoEvent<String>? = undoDeque.pollLast()

            if (evt != null) {
                loadData(evt.house, evt.post, false)
                val list = if (evt.isBoy) boyList else girlList
                list.selectionModel.clearSelection()

                val doSelect = evt.undo(list.items)
                if (doSelect) {
                    list.selectionModel.selectRange(evt.startIndex, evt.endIndex)
                    list.requestFocus()
                }

                isUndoAvailableProperty.set(undoDeque.isNotEmpty())
                addRedoEvent(evt)
            }
        }

        fun redoLast() {
            val evt: UndoEvent<String>? = redoDeque.pollLast()

            if (evt != null) {
                loadData(evt.house, evt.post, false)
                val list = if (evt.isBoy) boyList else girlList
                list.selectionModel.clearSelection()

                val doSelect = evt.redo(list.items)
                if (doSelect) {
                    list.selectionModel.selectRange(evt.startIndex, evt.endIndex)
                    list.requestFocus()
                }

                isRedoAvailableProperty.set(redoDeque.isNotEmpty())
                addUndoEvent(evt, false)
            }
        }

        private fun addUndoEvent(evt: UndoEvent<String>, clearRedo: Boolean) {
            isUndoAvailableProperty.set(true)
            if (undoDeque.size == UNDO_DEQUE_SIZE)
                undoDeque.pollFirst()
            undoDeque.add(evt)

            if (clearRedo)
                redoDeque.clear()
        }

        private fun addRedoEvent(evt: UndoEvent<String>) {
            isRedoAvailableProperty.set(true)
            if (redoDeque.size == UNDO_DEQUE_SIZE)
                redoDeque.pollFirst()
            redoDeque.add(evt)
        }
    }

    private inner class NameValidator : Validator<ValidatableCell.EditData, Pair<String, String>> {
        private val heading = Text().apply {
            styleClass += "pop-heading"
        }
        private val content = Text().apply {
            styleClass += "pop-body"
        }

        lateinit var textField: WeakReference<TextField>
        private val vBox = VBox(heading, content).apply {
            spacing = 2.0
        }
        private val popOver = PopOver(vBox).apply {
            arrowLocation = PopOver.ArrowLocation.TOP_LEFT
            isDetachable = false

            val listener = ChangeListener<String> { _, _, _ -> this.hide() }

            onShowing = EventHandler {
                val field = textField.get()!!
                vBox.maxWidthProperty().bind(field.widthProperty())
                field.textProperty().addListener(listener)
                field.pseudoClassStateChanged(errClass, true)
            }

            // cleanup
            onHiding = EventHandler {
                val field = textField.get()!!
                field.pseudoClassStateChanged(errClass, false)
                vBox.maxWidthProperty().unbind()
                field.textProperty().removeListener(listener)
            }

        }
        private val errClass = PseudoClass.getPseudoClass("err")

        override fun validate(data: ValidatableCell.EditData): Pair<String, String>? {

            if (data.newItem == null) {
                return "Please Enter a Valid Value" to ""
            }
            if (data.oldItem != null &&
                !data.oldItem.equals(data.newItem, true) &&
                data.itemList.any { it.equals(data.newItem, true) }
            )
                return "The name is duplicate" to "Candidates standing for same post must have unique names.\nTry adding or removing last name"
            return null
        }

        override fun showError(error: Pair<String, String>, receiver: Control) {
            heading.text = error.first
            content.text = error.second

            val bounds = receiver.localToScreen(receiver.boundsInLocal)
            val x = bounds.minX + 20
            val y = bounds.maxY + 2

            textField = WeakReference(receiver as TextField)

            popOver.show(receiver, x, y)
        }

    }

    companion object {
        private const val UNDO_DEQUE_SIZE = 16
        private const val PERIOD_MILLIS = 5 * 60 * 1000L //every 5 minutes
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun menuItem(
    name: String,
    accelerator: KeyCombination? = null,
    action: EventHandler<ActionEvent>
): MenuItem {
    val menu = MenuItem(name)
    menu.onAction = action
    menu.accelerator = accelerator
    return menu
}