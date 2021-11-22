package edu.opjms.common

import javafx.animation.FadeTransition
import javafx.event.EventHandler
import javafx.scene.control.Tooltip
import javafx.stage.WindowEvent
import javafx.util.Duration

class ShortcutTooltip(text: String? = null): Tooltip(text) {

    init {
        //animate on show
        skinProperty().addListener {_ ->
            addEventFilter(WindowEvent.WINDOW_SHOWING) {
                FadeTransition(Duration.millis(200.0), skin.node).apply {
                    fromValue = 0.0
                    toValue = 1.0
                }.play()
            }
        }
    }

    override fun hide() {
        FadeTransition(Duration.millis(200.0), skin.node).apply {
            fromValue = 1.0
            toValue = 0.0
            onFinished = EventHandler { super.hide() }
        }.play()
    }
}