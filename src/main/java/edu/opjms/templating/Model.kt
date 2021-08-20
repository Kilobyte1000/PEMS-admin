package edu.opjms.templating

import edu.opjms.global.inputForms.RawInputFormBase
import edu.opjms.global.inputForms.RawTextFieldInput
import edu.opjms.templating.inputPanes.InputPaneBase
import edu.opjms.templating.inputPanes.TextFieldPane
import javafx.application.Platform
import javafx.concurrent.Service
import javafx.concurrent.Task
import javafx.scene.Node
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.util.concurrent.Executors
import kotlin.io.path.ExperimentalPathApi

@OptIn(ExperimentalPathApi::class)
internal class Model(inputPanes: MutableList<Node>, dataFile: Path) {
    private val duplicateChecker = DuplicateChecker().also { it.executor = Executors.newCachedThreadPool() }


    var currentFile: Path = dataFile
        set(value) {
            field = value
            Files.setLastModifiedTime(field, FileTime.from(Instant.now()))
        }

    init {
        val input = Files.readString(currentFile)

        if (input.isNotEmpty()) {
            //construct form from file
            val form = Json.decodeFromString<List<RawInputFormBase>>(input)

            //assert first pane is unique ID Pane
            val uniquePane = form[0]
            if (!(uniquePane is RawTextFieldInput && uniquePane.isUniqueField))
                throw IllegalArgumentException("First Input pane must be a unique pane")


            @Suppress("UNCHECKED_CAST")
            constructForm(form, inputPanes as MutableList<InputPaneBase>)
        } else {
            //create single, blank unique field
            val inputPane = TextFieldPane(true)
            inputPane.setOnLabelChange(this::checkForDuplicates)
            inputPanes.add(inputPane)
        }

    }

    @Suppress("UNCHECKED_CAST")
    private val inputPanes: MutableList<InputPaneBase> = inputPanes as MutableList<InputPaneBase>

    fun generateHTML(): String {
        val builder = StringBuilder()

        for ( (id, inputPane) in inputPanes.withIndex()) {
            builder.append(inputPane.generateHTML(id))
        }
        return builder.toString()
    }

    fun serialize(): String {
        val immutableList = List(inputPanes.size) {inputPanes[it].toRawInput()}

        return Json.encodeToString(immutableList)
    }

    fun changeToPath(input: Path) {
        val inputString = Files.readString(input)

        val inputPanes = Json.decodeFromString<List<RawInputFormBase>>(inputString)

        //assert first pane is unique ID Pane
        val uniquePane = inputPanes[0]
        if (!(uniquePane is RawTextFieldInput && uniquePane.isUniqueField))
            throw IllegalArgumentException("First Input pane must be a unique pane")

        //given data is ok, go forth

        //save data
        Files.writeString(currentFile, serialize())

        Platform.runLater { constructForm(inputPanes, this.inputPanes) }


        currentFile = input


    }

    private fun constructForm(inputPanes: List<RawInputFormBase>, wrapper: MutableList<InputPaneBase>) {
        wrapper.clear()

        for (rawInputPane in inputPanes) {
            val pane = rawInputPane.toInputPane()
            pane.setOnLabelChange { oldText, newText -> checkForDuplicates(oldText, newText) }
            wrapper.add(rawInputPane.toInputPane())
        }
    }

    fun checkForDuplicates(oldText: String?, newText: String?) {
        duplicateChecker.oldText = oldText?.trim()
        duplicateChecker.newText = newText?.trim()
        duplicateChecker.inputPanes = inputPanes
        duplicateChecker.restart()
    }
}

open class DuplicateChecker: Service<Void>() {
    var oldText: String? = null
    var newText: String? = null
    lateinit var inputPanes: List<InputPaneBase>

    @Suppress("SpellCheckingInspection")
    override fun createTask(): Task<Void?> {

        return object: Task<Void?>() {
            override fun call(): Void? {
                /*
                * We have to check for duplicates here.
                * So we cache the index of first pane found with required value
                * So that we can set it duplicate when another one is found
                * */

                val doOldTextChecks = !oldText.isNullOrEmpty()
                val doNewTextChecks = !newText.isNullOrEmpty()

                var newDuplPaneIndex = -1
                var isNewDuplicated = false

                var oldDuplPaneIndex = -1
                var isOldDuplicated = false
                for (i in inputPanes.indices) {
                    val pane = inputPanes[i]

                    val label = pane.labelText.trim()

                    if (label.isEmpty() && pane.containsError()) {
                        Platform.runLater { pane.showDuplicateError(false) }
                        continue
                    }

                    //old text checks
                    if (doOldTextChecks) {
                        if (label.equals(oldText, true)) {
                            if (oldDuplPaneIndex == -1) { // no duplicate discovered yet
                                oldDuplPaneIndex = i
                            } else {  //duplicate discovered
                                isOldDuplicated = true
                                Platform.runLater {pane.showDuplicateError(true)}
                            }
                        }
                    }
                    //new text checks
                    if (doNewTextChecks) {
                        if (label.equals(newText, true)) {
                            if (newDuplPaneIndex == -1) { //no duplicate discovered yet
                                newDuplPaneIndex = i
                            } else { // duplicate discovered
                                isNewDuplicated = true
                                Platform.runLater {pane.showDuplicateError(true)}
                            }
                        }
                    }


                }

                //if the first found pane was duplicate
                //set to reflect the same

                if (oldDuplPaneIndex != -1) {
                    val pane = inputPanes[oldDuplPaneIndex]
                    setDuplicated(pane, isOldDuplicated)
                }
                if (newDuplPaneIndex != -1) {
                    val pane = inputPanes[newDuplPaneIndex]
                    setDuplicated(pane, isNewDuplicated)
                }

                oldText = null
                newText = null

                return null
            }


        }


    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun setDuplicated(pane: InputPaneBase, isDuplicated: Boolean) {
        Platform.runLater {pane.showDuplicateError(isDuplicated)}

    }
}