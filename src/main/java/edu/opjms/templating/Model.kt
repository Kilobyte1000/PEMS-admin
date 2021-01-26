package edu.opjms.templating

import edu.opjms.templating.inputPanes.InputPaneBase
import edu.opjms.templating.inputPanes.SelectFieldGroupPane
import javafx.scene.Node
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Suppress("UNCHECKED_CAST")
internal class Model(inputPanes: List<Node>) {


    private val inputPanes: List<InputPaneBase> = inputPanes as List<InputPaneBase>

    fun generateHTML(): String {
        val builder = StringBuilder()

        var id = 0

        for (inputPane in inputPanes) {
            builder.append(inputPane.generateHTML(id))
            if (inputPane is SelectFieldGroupPane) id += inputPane.numberOfSelects else id++
        }
        return builder.toString()
    }

    fun serialise(): String {
        val immutableList = List(inputPanes.size) {inputPanes[it].toRawInput()}

        return Json.encodeToString(immutableList)
    }

}