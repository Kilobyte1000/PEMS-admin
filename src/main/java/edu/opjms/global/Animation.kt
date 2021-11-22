package edu.opjms.global

import javafx.animation.Interpolator
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.util.Duration

inline fun button(text: String = "", graphic: Node? = null, crossinline action: (ActionEvent) -> Unit): Button {
    return Button(text, graphic).apply {
        onAction = EventHandler { action(it) }
    }
}

@JvmField
val shortDuration: Duration = Duration.millis(200.0)

@JvmField
val QUAD_EASE_OUT: Interpolator = Interpolator.SPLINE(0.250, 0.460, 0.450, 0.940)

@JvmField
val MFX_INTERPOLATOR_V2 = Interpolator.SPLINE(0.0825, 0.3025, 0.0875, 0.9975)