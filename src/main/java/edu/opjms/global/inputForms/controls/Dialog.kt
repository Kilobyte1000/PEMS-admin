package edu.opjms.global.inputForms.controls

import edu.opjms.global.shortDuration
import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.animation.ScaleTransition
import javafx.beans.InvalidationListener
import javafx.beans.WeakListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.WeakChangeListener
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.DialogPane
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import java.lang.Double.max

class Dialog {

    // private properties
    private val dialog = Group().apply {
        isManaged = false
//        isAutoSizeChildren = true
        addEventFilter(KeyEvent.ANY) {
            val code = it.code
            if (code == KeyCode.ESCAPE && closeOnEscape)
                close()
            it.consume()
        }
    }

    private val skimRectangle = Rectangle().apply {
        fill = Color.BLACK
        isManaged = false
        x = .0
        y = .0
        onMouseClicked = EventHandler {
            if (closeOnClickOut)
                close()
        }
    }
    private var parent: Pane? = null
    private val changeXListener = ChangeListener<Number> {_, _, _ -> relocateX()}
    private val changeYListener = ChangeListener<Number> {_, _, _ -> relocateY()}

    private val weakXListener = WeakChangeListener(changeXListener)
    private val weakYListener = WeakChangeListener(changeYListener)

    // public properties
    var content: Region? = null
        set(value) {
            if (isShowing)
                throw IllegalStateException("Can't change root while showing")
            else
                field = value
        }

    val isShowing: Boolean
        get() = parent != null

    var closeOnClickOut = false
    var closeOnEscape = false

    @Throws(IllegalStateException::class)
    fun show(parent: Pane) {
        val root = content ?: throw IllegalStateException("No content supplied")
        if (isShowing)
            throw IllegalStateException("Already Showing")
        this.parent = parent
        dialog.children.setAll(root)

        val fadeTransition = FadeTransition(shortDuration, skimRectangle).apply {
            fromValue = .0
            toValue = .5
        }
        val scaleTransition = ScaleTransition(shortDuration, dialog).apply {
            fromX = .8
            fromY = .8
            toX = 1.0
            toY = 1.0
        }
        val anim = ParallelTransition(fadeTransition, scaleTransition)

        skimRectangle.apply {
            widthProperty().bind(parent.widthProperty())
            heightProperty().bind(parent.heightProperty())
        }

        dialog.sceneProperty().addListener(InvalidationListener {
            root.requestFocus()
        })

        addListeners(parent)
        addListeners(root)

        parent.children.addAll(skimRectangle, dialog)

        relocateX()
        relocateY()
        anim.play()
    }

    fun close() {
        val parent = parent ?: throw IllegalStateException("Already closed")
        val duration = Duration.millis(250.0)
        val fadeTransition = FadeTransition(duration, skimRectangle).apply {
            fromValue = .5
            toValue = .0
        }
        val fade2 = FadeTransition(duration, dialog).apply {
            fromValue = 1.0
            toValue = .3
        }
        val scaleTransition = ScaleTransition(duration, dialog).apply {
            fromX = 1.0
            fromY = 1.0
            toX = .9
            toY = .9
        }
        val anim = ParallelTransition(fadeTransition, scaleTransition, fade2)

        anim.onFinished = EventHandler {
            parent.children.removeAll(skimRectangle, dialog)
            skimRectangle.apply {
                widthProperty().unbind()
                heightProperty().unbind()
            }
            removeListeners(parent)
            removeListeners(content!!)
            this.parent = null
        }

        anim.play()
    }


    private fun addListeners(node: Region) {
        node.widthProperty().addListener(weakXListener)
        node.heightProperty().addListener(weakYListener)
    }
    private fun removeListeners(node: Region) {
        node.widthProperty().removeListener(weakXListener)
        node.heightProperty().removeListener(weakYListener)
    }

    private fun relocateX() {
        val parent = parent ?: throw IllegalStateException("Dialog is not showing")
        val bounds = dialog.layoutBounds
        dialog.layoutX = parent.snapPositionX(max((parent.width - bounds.width), .0) / 2.0)
    }
    private fun relocateY() {
        val parent = parent ?: throw IllegalStateException("Dialog is not showing")
        val bounds = dialog.layoutBounds
        dialog.layoutY = parent.snapPositionY(max((parent.height - bounds.height), .0) / 2.0)
    }


}