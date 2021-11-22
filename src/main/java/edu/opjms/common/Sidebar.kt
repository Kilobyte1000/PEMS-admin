package edu.opjms.common

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.layout.*
import javafx.scene.paint.Color
import net.kilobyte1000.Houses

class Sidebar: VBox() {

    private val activeShadow = DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, .25), 3.0, .3, .0, 1.0)
    private val activeMargins = Insets(0.0, 10.0, 0.0, 10.0)
    private val activePadding = Insets(5.0, .0, 5.0, -1.0)
    private val leftPadding = Insets(5.0, 0.0, 5.0, 19.0)
    private val activeClass = "active-nav"
    private val activeRect = Region().apply {
        prefWidth = 4.0
        background = Background(BackgroundFill(underlineColor, CornerRadii(.0, 8.0, 8.0, .0, false), Insets.EMPTY))
    }
    private val houses = Houses.values()

    private var selectedButton: Button

    var onHouseChange: Houses.() -> Unit = {}
    var selectedHouse = Houses.KABIR
        set(value) {
            if (value != field) {
                //reset style of current active button
                selectedButton.apply {
                    effect = null
                    padding = leftPadding
                    setMargin(this, Insets.EMPTY)
                    styleClass.remove(activeClass)

                    // we make a copy of the field because the property
                    // will refer to the value when it is called, rather
                    // than the value it currently has
                    val oldHouse = field
                    onAction = EventHandler { selectedHouse = oldHouse }
                    graphic = null
                }
                activeRect.prefHeightProperty().unbind()
                setAsSelected(children[value.ordinal] as Button)

                field = value
                onHouseChange(value)
            }
        }

    init {
        stylesheets.add(this::class.java.getResource("style.css").toExternalForm())
        styleClass.add("sidebar")

        isFillWidth = true
        padding = Insets(60.0, 0.0, 0.0, 0.0)
        spacing = 10.0

        val navButtons = createButtons()
        selectedButton = navButtons[selectedHouse.ordinal] //to make kotlin happy
        setAsSelected(selectedButton)

        children.addAll(navButtons)
    }

    fun offsetHouse(offset: Int) {
        val houses = Houses.values()
        val i = Math.floorMod(selectedHouse.ordinal + offset, houses.size)
        val newHouse = houses[i]
        selectedHouse = newHouse
    }

    private fun setAsSelected(button: Button) {
        activeRect.prefHeightProperty().bind(button.heightProperty().multiply(2.0/3))
        selectedButton = button
        button.apply {
            padding = activePadding
            setMargin(this, activeMargins)
            effect = activeShadow
            styleClass += activeClass
            graphic = activeRect
            onAction = null

        }
    }
    private fun createButtons(): List<Button> {
        return List(houses.size) {
            //convert to title case
            val name = houses[it].displayText

            Button(name).apply {
                maxWidth = Double.POSITIVE_INFINITY
                prefHeight = 31.0
                alignment = Pos.CENTER_LEFT
                padding = leftPadding
                graphicTextGap = 8.0
                onAction = EventHandler {_ -> selectedHouse = houses[it] }
            }
        }
    }

}
