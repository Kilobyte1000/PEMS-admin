package edu.opjms.candidateSelector

import edu.opjms.common.Posts
import edu.opjms.common.Posts.*
import edu.opjms.common.Sidebar
import edu.opjms.common.underlineColor
import javafx.animation.*
import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Bounds
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyCombination.*
import javafx.scene.layout.*
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style
import javafx.scene.input.KeyCodeCombination as ShortcutKey

internal class Editor : BorderPane() {

    private var debugBool = true

    private val controller: Controller

    init {
        stylesheets += this::class.java.getResource("editor.css").toExternalForm()

        val tabBar = TabBar().apply {
            sceneProperty().addListener(InvalidationListener {
                scene.accelerators[ShortcutKey(TAB, SHORTCUT_DOWN)] =
                    Runnable { switchTab(true) }
            })
        }
        val sidebar = Sidebar().apply { prefWidth = 221.0 }
//        top = createMenuBar()
        left = sidebar
        center = VBox().apply {
            styleClass += "center"
            spacing = 10.0
            children.addAll(
                tabBar,
                HBox().apply {
                    styleClass += "main"
                    spacing = 10.0
                    var boyList: ListView<String>? = null
                    var girlList: ListView<String>? = null
                    children.addAll(
                        createListSegment("Boys") { boyList = this },
                        createListSegment("Girls") { girlList = this }
                    )

                    controller = Controller(boyList!!, girlList!!, sidebar, tabBar)
                }.apply { VBox.setVgrow(this, Priority.ALWAYS) }
            )
        }
        top = createMenuBar(sidebar, tabBar, controller).apply { JMetro(this, Style.LIGHT) }

    }

    private inline fun createListSegment(label: String, configList: ListView<String>.() -> Unit): VBox {
        val list = ListView<String>().apply {
            VBox.setVgrow(this, Priority.ALWAYS)
            configList()
        }
        return VBox(
            HBox().apply {
                padding = Insets(.0, 5.0, .0, 5.0)
                children.addAll(
                    Label(label).apply { styleClass += "heading" },
                    Region().apply { HBox.setHgrow(this, Priority.ALWAYS) },
                    Button("Add new").apply {
                        styleClass.setAll("add")
                        onAction = EventHandler {
                            list.items.add("")
                            list.layout()
                            list.edit(list.items.size - 1)
                        }
                    }
                )
            },
            list
        ).apply {
            HBox.setHgrow(this, Priority.ALWAYS)
            padding = Insets(20.0)
        }
    }

    internal class TabBar : HBox() {

        //we initialise these using swap, so first position is also swapped
        private var activeTabButton = Button("Sports Prefect")
        private var inactiveTabButton = Button("House Prefect")
        private val rect = Rectangle().apply {
            arcHeight = 5.0
            arcWidth = 5.0
            isManaged = false
            height = 3.0
            fill = underlineColor
            yProperty().bind(this@TabBar.heightProperty().subtract(height))
        }
        var onChange: Posts.() -> Unit = {}

        private val changeListener = ChangeListener<Bounds> { _, _, bounds ->
            val width = bounds.width * 3.0 / 4
            rect.x = bounds.minX + (bounds.width - width) / 2.0
            rect.width = width
        }
        var activeTab = SPORTS_PREFECT

        init {
            alignment = Pos.BOTTOM_LEFT
            padding = Insets(.0, .0, .0, 20.0)
            spacing = 10.0
            prefHeight = 55.0
            styleClass += "tab-bar"
            activeTabButton.isFocusTraversable = false
            inactiveTabButton.isFocusTraversable = false
            children.addAll(rect, inactiveTabButton, activeTabButton)
            switchTab(false)
        }

        fun switchTab(playAnim: Boolean = true) {
            val temp = activeTabButton
            activeTabButton = inactiveTabButton
            inactiveTabButton = temp

            //swap between 0 and 1
            activeTab = if (activeTab == SPORTS_PREFECT)
                HOUSE_PREFECT
            else SPORTS_PREFECT
            onChange(activeTab)

            activeTabButton.onAction = null
            activeTabButton.styleClass.add(activeClass)

            inactiveTabButton.onAction = EventHandler { switchTab() }
            inactiveTabButton.styleClass.remove(activeClass)

            activeTabButton.boundsInParentProperty().removeListener(changeListener)


            if (playAnim) {
                val bounds = activeTabButton.boundsInParent
                val width = bounds.width * 3.0 / 4
                val x = bounds.minX + (bounds.width - width) / 2.0
                val interpolator = Interpolator.SPLINE(0.250, 0.460, 0.450, 0.940)

                Timeline(
                    KeyFrame(
                        Duration.millis(200.0),
                        KeyValue(rect.xProperty(), x, interpolator),
                        KeyValue(rect.widthProperty(), width, interpolator)
                    )
                ).apply {
                    onFinished = EventHandler {
                        activeTabButton.boundsInParentProperty().addListener(changeListener)
                    }
                }.play()
            } else {
                activeTabButton.boundsInParentProperty().addListener(changeListener)
            }
        }

        companion object {
            const val activeClass = "active"
        }
    }

    private fun createMenuBar(sidebar: Sidebar, tabBar: TabBar, controller: Controller): MenuBar {
        val animMenu = CheckMenuItem("Disable Switch Animation")
        animMenu.onAction = EventHandler {
            controller.animateOnChange = !animMenu.isSelected
        }

        return MenuBar(
            Menu(
                "View",
                null,
                menuItem("Next House", ShortcutKey(DOWN, ALT_DOWN)) { sidebar.offsetHouse(1) },
                menuItem("Previous House", ShortcutKey(UP, ALT_DOWN)) { sidebar.offsetHouse(-1) },
                menuItem("Switch Tab", ShortcutKey(TAB, SHORTCUT_DOWN)) { tabBar.switchTab() },
                animMenu
            ),
            Menu(
                "Edit",
                null,
                menuItem("Undo", ShortcutKey(Z, SHORTCUT_DOWN)) { controller.undo() }.apply {
                    disableProperty().bind(controller.isUndoAvailableProperty.not())
                },
                menuItem("Redo", ShortcutKey(Y, SHORTCUT_DOWN)) { controller.redo() }.apply {
                    disableProperty().bind(controller.isRedoAvailableProperty.not())
                },
                menuItem("Add Candidate", ShortcutKey(INSERT)) {
                    controller.focusedList?.let { list ->
                        list.items.add("")
                        list.layout()
                        list.edit(list.items.size - 1)
                    }
                }
            )
        )
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