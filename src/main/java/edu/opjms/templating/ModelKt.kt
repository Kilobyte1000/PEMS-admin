package edu.opjms.templating

import edu.opjms.global.inputForms.RawInputFormBase
import edu.opjms.templating.inputPanes.InputPaneBase
import edu.opjms.templating.inputPanes.TextFieldPane
import javafx.concurrent.Service
import javafx.concurrent.Task
import net.kilobyte1000.loadLoginPageTest
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.io.path.*

class ModelKt(dataFolder: Path, private val extension: String) {

    lateinit var currentFile: Path
    private var readingFile: Path? = null
    private var panes: List<InputPaneBase>? = null

    val executor: ExecutorService = Executors.newCachedThreadPool()

    var initPane: InputPaneBase.() -> Unit = {}
    val readService: Service<List<InputPaneBase>> = object : Service<List<InputPaneBase>>() {
        override fun createTask(): Task<List<InputPaneBase>> {
            return object: Task<List<InputPaneBase>>() {
                override fun call(): List<InputPaneBase> {
                    val readFile = readingFile ?: throw IllegalStateException("Reading file can't be null")


                    val ret = if (readFile.fileSize() > 0) {
                        val inp = readFile.objectInput()
                        updateMessage("Reading")

                        inp.use {
                            // first boolean stores whether if all
                            // panes return HasError = false
                            it.readBoolean()
                            val size = it.readInt()
                            val max = size.toLong() * 2
                            var progress = 0L

                            val list = List(size) {_ ->
                                updateProgress(progress++, max)
                                val input = it.readObject() as RawInputFormBase
                                input.toInputPane()
                            }
                            updateMessage("Constructing")
                            for (pane in list) {
                                updateProgress(progress++, max)
                                pane.initPane()
                            }
                            list
                        }

                    } else {
                        defaultTemplate()
                    }
                    currentFile = readFile
                    readingFile = null
                    currentFile.setLastModifiedTime(FileTime.from(Instant.now()))
                    return ret
                }

            }
        }

        init {
            executor = this@ModelKt.executor
        }
    }
    val writeService: Service<Unit> = object : Service<Unit>() {

        init {
            executor = this@ModelKt.executor
        }

        override fun createTask(): Task<Unit> {
            return object: Task<Unit>() {
                override fun call() {
                    val panes = panes ?: throw IllegalStateException("Panes required for serialisation not provided")
                    updateMessage("Saving")

                    val max = panes.size * 2L
                    var progress = 0L
                    var hasError = false

                    val inputs = List(panes.size) {
                        val pane = panes[it]
                        hasError = hasError || pane.containsError()
                        updateProgress(progress++, max)
                        pane.toRawInput()
                    }

                    currentFile.objectOutput().use {
                        it.writeBoolean(hasError)
                        it.writeInt(inputs.size)
                        for (input in inputs) {
                            it.writeObject(input)
                            updateProgress(progress++, max)
                        }
                    }

                    this@ModelKt.panes = null
                }
            }
        }

    }

    init {
        try {
            dataFolder.createDirectories()
        } catch (ignore: IOException) {}
    }

    fun getTemplateFiles(dataFolder: Path): Task<List<Path>> {
        val task = object : Task<List<Path>>() {
            override fun call(): List<Path> {
                return dataFolder.listDirectoryEntries("*${extension}")
            }
        }
        executor.execute(task)
        return task
    }

    fun writeToFile(panes: List<InputPaneBase>) {
        this.panes = panes
        writeService.restart()
    }

    fun loadFile(path: Path): Service<List<InputPaneBase>> {
        readingFile = path
        readService.restart()
        return readService
    }

    fun defaultTemplate(): List<InputPaneBase> = listOf(TextFieldPane(true))

    fun setFile(path: Path) {
        path.setLastModifiedTime(FileTime.from(Instant.now()))
        currentFile = path
    }

    fun getHtml(panes: List<InputPaneBase>): Path {
        val builder = StringBuilder()
        for (i in panes.indices) {
            val pane = panes[i]
            builder.append(pane.generateHTML(i))
        }
        val html = builder.toString()
        return loadLoginPageTest(html)
    }

    fun tryRename(name: String): Pair<Path?, String?> {
        val file = getSiblingFile(name)

        return if (file.notExists()) {
            try {
                val ret = currentFile.moveTo(file) to null
                currentFile = file
                ret
            } catch (e: FileAlreadyExistsException) {
                e.printStackTrace()
                null to "File with same name already exists"
            } catch (e: AccessDeniedException) {
                e.printStackTrace()
                null to "Could not rename File. Permission Denied"
            } catch (e: IOException) {
                e.printStackTrace()
                null to "Could not rename File"
            }
        } else null to "File with same name already exists"

    }

    fun tryDuplicate(name: String): Pair<Path?, String?> {
        val file = getSiblingFile(name)

        return tryWriteFile(currentFile) { currentFile.copyTo(file, false) }
        /*return if (file.notExists()) {
            try {
                currentFile.copyTo(file, false) to null

            } catch (e: FileAlreadyExistsException) {
                e.printStackTrace()
                null to "File with same name already exists"
            } catch (e: AccessDeniedException) {
                e.printStackTrace()
                null to "Could not write File. Permission Denied"
            } catch (e: IOException) {
                e.printStackTrace()
                null to "Could not write File"
            }
        } else null to "File with same name already exists"*/
    }

    fun tryCreateFile(name: String): Pair<Path?, String?> {
        val file = getSiblingFile(name)
        return tryWriteFile(file) { file.createFile() }
    }

    private fun getSiblingFile(name: String): Path {
        val fileName = name + extension
        val file = currentFile.resolveSibling(fileName)

        file.parent.let {
            if (it != null && it.notExists()) {
                it.createDirectories()
            }
        }
        return file
    }

    private inline fun tryWriteFile(file: Path, action: (Path) -> Path): Pair<Path?, String?> {
        return if (file.notExists()) {
            try {
                action(file) to null
            } catch (e: FileAlreadyExistsException) {
                e.printStackTrace()
                null to "File with same name already exists"
            } catch (e: AccessDeniedException) {
                e.printStackTrace()
                null to "Could not write File. Permission Denied"
            } catch (e: IOException) {
                e.printStackTrace()
                null to "Could not write File"
            }
        } else null to "File with same name already exists"
    }
}

private fun Path.objectInput() = ObjectInputStream(this.inputStream().buffered())
private fun Path.objectOutput() = ObjectOutputStream(this.outputStream().buffered())