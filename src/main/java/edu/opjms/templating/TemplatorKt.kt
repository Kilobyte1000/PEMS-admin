package edu.opjms.templating

import edu.opjms.global.*
import edu.opjms.templating.inputPanes.*
import javafx.animation.*
import javafx.application.HostServices
import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.WritableDoubleValue
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.CacheHint
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.ClipboardContent
import javafx.scene.input.KeyCode
import javafx.scene.input.TransferMode
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Text
import javafx.stage.WindowEvent
import javafx.util.Duration
import javafx.util.StringConverter
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style
import org.controlsfx.control.PopOver
import java.io.IOException
import java.io.ObjectStreamException
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.math.min

class TemplatorKt(
    private val hostServices: HostServices,
    dataFolder: Path
): ScrollPane(), View {

    private val manager = DragAndDropManager()
    private val animManager = LayoutAnimateManager()
    private var processFileChange = true
    private var fileToBeLoaded: Path? = null
    private val initPane: (InputPaneBase.() -> Unit) = {
        if (!(this is TextFieldPane && isUniqueField)) {

            addControlButtons({ removePane(this) }) {
                val children = inputWrapper.children
                val i = children.indexOf(this)
                //not second if up
                //not last if down
                if ((it == -1 && i > 1) || (it == 1 && i < children.lastIndex)) {
                    //remove this
                    animManager.doAnimation = true
                    children.removeAt(i)
                    viewOrder = -1.0
                    children.add(i + it, this)
                }
                //if moved, focus lost due to removal
                //if clicked, focus shifts anyway for some reason
                requestFocus()
            }

            skinProperty().addListener {_ ->
                manager.setDnd(this)
            }
            layoutYProperty().addListener(animManager.changeListener)

            if (this is SelectFieldPane) {
                executor = model.executor
            }
        }

        cacheHint = CacheHint.SPEED
        isSnapToPixel = false

    }
    private val model = ModelKt(dataFolder, EXTENSION).apply {
        initPane = this@TemplatorKt.initPane

        writeService.let {
            it.onScheduled = EventHandler {_ ->
                val progress = ProgressBar(.0).apply { progressProperty().bind(it.progressProperty()) }
                val label = Label()
                label.labelFor = progress
                label.textProperty().bind(it.messageProperty())
                inputWrapper.isDisable = true

                navBar.children.addAll(progress, label)
            }
            it.onSucceeded = EventHandler {

                val size = navBar.children.size
                navBar.children.remove(size - 2, size)

                fileToBeLoaded.let { file ->
                    if (file != null) {
                        loadFile(file)
                        fileToBeLoaded = null
                    } else inputWrapper.isDisable = false
                }
            }
            it.onFailed = EventHandler {_ ->
                throw it.exception
            }
        }

        //todo: user response on failure
        readService.onFailed = EventHandler {
            try {
                throw readService.exception
            } catch (e: ClassNotFoundException) {
                System.err.println("File is corrupted")
                println(e.message)
                e.printStackTrace()
            } catch (e: ObjectStreamException) {
                System.err.println("File is corrupted")
                println(e.message)
                e.printStackTrace()
            }
        }
    }
    private val inputWrapper = VBox().apply {
        isFillWidth = true
        spacing = 6.0
    }
    private val deleteConfirm: PopOver = PopOver().apply {
        val errLabel = Label("Could not delete file")
        errLabel.isManaged = false
        errLabel.isVisible = false
        errLabel.styleClass += ERROR_STYLECLASS

        contentNode = VBox(
            5.0,
            Text("Are you sure you want to delete this template?").apply {
                styleClass += "heading"
            },
            button("Delete") {
                if (deleteFile()) {
                    hide()
                } else {
                    errLabel.isManaged = true
                    errLabel.isVisible = true
                }
            },
            errLabel
        )

        arrowSize = .0
        isDetachable = false
        fadeInDuration = shortDuration
        fadeOutDuration = Duration.millis(150.0)
        arrowLocation = PopOver.ArrowLocation.TOP_CENTER

        // cannot use setOnShowing because it is
        // overwritten by the class
        addEventFilter(WindowEvent.WINDOW_SHOWN) {
            //y property of Window is read-only
            val yProperty = object: WritableDoubleValue {
                override fun getValue() = y

                override fun get() = y

                override fun setValue(n: Number) {
                    y = n.toDouble()
                }

                override fun set(n: Double) {
                    y = n
                }


            }
            val y = y

            Timeline(
                KeyFrame(
                    Duration.ZERO,
                    KeyValue(yProperty, this.y - 5.0)
                ),
                KeyFrame(
                    shortDuration,
                    KeyValue(yProperty, y, QUAD_EASE_OUT),
                )
            ).play()
        }
        onHidden = EventHandler {
            errLabel.isManaged = false
            errLabel.isVisible = false
        }
    }

    @Suppress("UNCHECKED_CAST")
    private val fileNav: ComboBox<Path> = ComboBox<Path>().apply {
        converter = object : StringConverter<Path?>() {
            override fun toString(p: Path?): String? {
                return p?.fileName?.toString()?.let { it.substring(0, it.length - EXTENSION.length) }
            }

            override fun fromString(str: String?): Path? {
                return if (str != null) {
                    dataFolder.resolve(str + EXTENSION)
                } else {
                    null
                }
            }
        }

        minWidth = 150.0

        selectionModel.selectedItemProperty().addListener { _, old, new ->
            if (processFileChange && old != null && new != null
                && old.exists() && !old.isSameFileAs(new)) {
                val panes = inputWrapper.children as List<InputPaneBase>
                fileToBeLoaded = new
                model.writeToFile(panes)
            }
            processFileChange = true
        }
    }
    private val fileErrLabel = Label().apply {
        styleClass += ERROR_STYLECLASS
        isVisible = false
        VBox.setMargin(this, Insets(-10.0, .0, -10.0, .0))
    }
    private val navBar = HBox().apply {
        val editButton = Button("", editIcon20).apply {
            onAction = EventHandler { startRename() }
        }
        val addButton = Button("", plusIcon).apply {
            onAction = EventHandler { newFile() }
        }
        val deleteButton = Button("", deleteIcon20).apply {
            onAction = EventHandler {
                deleteConfirm.show(this)
            }
        }
        children.addAll(fileNav, editButton, deleteButton)
        alignment = Pos.CENTER_LEFT
        spacing = 6.0
    }

    init {

        val view = getView()
        padding = Insets.EMPTY
        isFitToHeight = true
        isFitToWidth = true
        content = view

        //load on start
        val placeholder = getPlaceholder()
        val anim = getPlaceholderAnim(placeholder)
        inputWrapper.children.add(placeholder)
        anim.play()

        JMetro(this, Style.LIGHT)

        val ring = ProgressIndicator()
        ring.maxHeightProperty().bind(fileNav.heightProperty())
        navBar.children.add(ring)
        val task = model.getTemplateFiles(dataFolder)

        task.onSucceeded = EventHandler {
            val paths = task.get()
            fileNav.items.setAll(paths)

            val latest = paths.maxByOrNull { it.getLastModifiedTime() }

            navBar.children.removeLast()
            if (latest != null) {
                fileNav.selectionModel.select(latest)

                val progress = ProgressBar(.0)
                val label = Label()
                navBar.children.addAll(progress, label)

                val exec = model.loadFile(latest)
                progress.progressProperty().bind(exec.progressProperty())
                label.textProperty().bind(exec.messageProperty())

                exec.onSucceeded = EventHandler {
                    anim.stop()
                    inputWrapper.children.clear()
                    navBar.children.let { it.remove(it.size - 2, it.size) }
                    addPanes(exec.value)
                    setUpReadService()
                }
            } else { //no file exists yet
                dataFolder.resolve(DEFAULT_FILE_NAME).createFile().let {
                    inputWrapper.children.clear()
                    fileNav.items.add(it)
                    fileNav.selectionModel.select(it)
                    model.setFile(it)
                }

                val pane = TextFieldPane(true)
                pane.initPane()
                addPane(pane)
                setUpReadService()
            }

        }

    }

    private fun setUpReadService() {
        val it = model.readService
        var anim: Animation? = null
        var placeholder: Region?


        it.onScheduled = EventHandler {_ ->
            val progress = ProgressBar(.0).apply { progressProperty().bind(it.progressProperty()) }
            val label = Label().apply {
                labelFor = progress
                textProperty().bind(it.messageProperty())
            }
            navBar.children.addAll(progress, label)

            placeholder = getPlaceholder()
            anim = getPlaceholderAnim(placeholder!!).also { it.play() }

            inputWrapper.isDisable = true
            inputWrapper.children.clear()
            inputWrapper.children.add(placeholder)

        }

        it.onSucceeded = EventHandler {_ ->
            navBar.children.let { it.remove(it.size - 2, it.size) }
            inputWrapper.isDisable = false
            inputWrapper.children.clear()
            anim?.stop()
            anim = null
            placeholder = null
            addPanes(it.value)
        }
    }

    //****************************************
    // UI Related
    //****************************************
    private fun getView(): Pane {
        val root = VBox(15.0).apply {
            isFillWidth = true
        }

        val buttonWrapper = HBox(10.0).apply {
            val addButton = MenuButton("Add new Field").apply {
                graphic = plusIcon
                graphicTextGap = 15.0
                val textMenu = MenuItem(addFieldText("Text")).apply {
                    onAction = EventHandler { addPane(TextFieldPane()) }
                }
                val selectMenu = MenuItem(addFieldText("Select")).apply {
                    onAction = EventHandler { addPane(SelectFieldPane()) }
                }
                val dateMenu = MenuItem(addFieldText("Date")).apply {
                    onAction = EventHandler { addPane(DateInputPane()) }
                }
                val booleanMenu = MenuItem(addFieldText("Boolean")).apply {
                    onAction = EventHandler { addPane(BooleanFieldPane()) }
                }
                items.setAll(textMenu, selectMenu, dateMenu, booleanMenu)
            }
            val genPage = Button("Preview Page").apply {
                graphic = fileIcon24
                graphicTextGap = 15.0
                onAction = EventHandler { showHtml() }
            }
            val duplicate = Button("Duplicate").apply {
                graphic = copyIcon24
                graphicTextGap = 15.0
                onAction = EventHandler { startDuplicate() }
            }
            val serialise = Button("Save").apply {
                graphic = Text("")
                graphicTextGap = 15.0
            }
            val debug = Button("DEBUG").apply {
                onAction = EventHandler {

                }
            }
            children.addAll(addButton, genPage, duplicate, serialise, debug)
        }
        return root.apply {
            styleClass += "root-box"
            stylesheets.addAll(BASE, LIGHT_THEME)
            children.addAll(
                navBar,
                fileErrLabel,
                inputWrapper,
                buttonWrapper
            )
        }
    }

    private fun addPane(pane: InputPaneBase) {
        pane.initPane()
        val duration = Duration.millis(300.0)
        val fade = FadeTransition(duration, pane).apply {
            fromValue = .0
            toValue = 1.0
        }
        val move = TranslateTransition(duration, pane).apply {
            fromY = 20.0
            toY = .0
        }
        inputWrapper.children.add(pane)
        ParallelTransition(fade, move).apply {
            onFinished = EventHandler {
                pane.cacheHint = CacheHint.DEFAULT
                pane.isSnapToPixel = true
            }
        }.play()

    }

    private fun addPanes(panes: List<InputPaneBase>) {
        val duration = Duration.millis(300.0)
        val transitions = arrayListOf<Transition>().apply { ensureCapacity(panes.size * 2) }
        val delayIncrease = 100.0
        val maxDelay = 1000.0
        var currentDelay = .0
        val maxDelayDuration = Duration.millis(maxDelay)

        for (pane in panes) {
            pane.opacity = .0

            val delay = if (currentDelay < maxDelay) Duration.millis(currentDelay) else maxDelayDuration

            val fade = FadeTransition(duration, pane).apply {
                fromValue = .0
                toValue = 1.0
                this.delay = delay
            }
            val move = TranslateTransition(duration, pane).apply {
                fromY = 20.0
                toY = .0
                this.delay = delay
                onFinished = EventHandler {
                    pane.cacheHint = CacheHint.DEFAULT
                    pane.isSnapToPixel = true
                }
            }
            currentDelay += delayIncrease
            transitions.add(fade)
            transitions.add(move)
        }

        val anim = ParallelTransition()
        anim.children.setAll(transitions)

        inputWrapper.children.addAll(panes)
        Platform.runLater { anim.play() }
    }

    private fun removePane(pane: InputPaneBase) {
        inputWrapper.children.let {
            val i = it.indexOf(pane)
            it.removeAt(i)
            it[min(it.lastIndex, i)].requestFocus()
        }
    }

    private fun getPlaceholder(): Region {
        return Region().apply {
            //Magic number: initial height of text pane
            prefHeight = 259.2
            background = Background(BackgroundFill(Color.BLACK, CornerRadii(8.0), Insets.EMPTY))
        }
    }

    private fun getPlaceholderAnim(region: Region): Animation {
        val darken = FadeTransition(Duration.millis(700.0), region).apply {
            fromValue = .2
            toValue = .3
        }
        val lighten = FadeTransition(Duration.millis(700.0), region).apply {
            fromValue = .3
            toValue = .2
        }
        return SequentialTransition(darken, lighten).apply {
            cycleCount = Transition.INDEFINITE
        }
    }


    //****************************************
    // File Related
    //****************************************
    private fun startRename() {
        val fileText = fileNav.converter.toString(fileNav.selectionModel.selectedItem)

        startEdit( {it.isNotBlank() && it != fileText} ) {
            val data = model.tryRename(it)
            data.first?.let { path ->
                val i = fileNav.selectionModel.selectedIndex
                fileNav.items[i] = path
                fileNav.selectionModel.select(i)
            }
            data
        }
    }

    private fun startDuplicate() {
        startEdit( {it.isNotBlank()} ) {
            val data = model.tryDuplicate(it)
            processFileChange = data.second != null

            data.first?.let { path ->
                fileNav.items.add(path)
                fileNav.selectionModel.selectLast()
            }
            data
        }
    }

    private fun deleteFile(): Boolean {
        val current = model.currentFile

        try {
            current.deleteExisting()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        processFileChange = false
        fileNav.items.remove(current)
        val latestFile = fileNav.items.maxByOrNull { it.getLastModifiedTime() }

        processFileChange = false
        if (latestFile != null) {
            fileNav.selectionModel.select(latestFile)
            model.loadFile(latestFile)
        } else {
            val default = current.resolveSibling(DEFAULT_FILE_NAME).createFile()
            fileNav.selectionModel.select(default)

            inputWrapper.children.clear()
            addPanes(model.defaultTemplate())
        }
        return true
    }

    private fun newFile() {
        val fileText = fileNav.converter.toString(fileNav.selectionModel.selectedItem)

        startEdit({ it.isNotBlank() && it != fileText }) {
            val data = model.tryCreateFile(it)
            processFileChange = data.second != null
            data.first?.let { path ->
                fileNav.items.add(path)
                fileNav.selectionModel.select(path)
                //todo: save current file

                inputWrapper.children.clear()
                addPanes(model.defaultTemplate())
            }
            data
        }
    }

    private inline fun startEdit(
        crossinline doCommitField: (String) -> Boolean,
        crossinline onCommit: (String) -> Pair<Path?, String?>
    ) {
        val children = navBar.children.toTypedArray()
        val fileText = fileNav.run {
            val path = selectionModel.selectedItem
            converter.toString(path)
        }

        val textField = TextField().apply {
            prefWidth = fileNav.width
            text = fileText

            onKeyPressed = EventHandler {
                if (it.code == KeyCode.ESCAPE)
                    cancelEdit(children)
            }
            onAction = EventHandler {

                if (doCommitField(text)) {
                    val (path, error) = onCommit(text)
                    tryFinishEdit(path, this, children, error)
                } else {
                    cancelEdit(children)
                }
            }
        }

        val doneButton = Button().apply {
            graphic = checkmark20
            onAction = EventHandler {
                val text = textField.text

                if (doCommitField(text)) {
                    val (path, error) = onCommit(text)
                    tryFinishEdit(path, textField, children, error)
                } else {
                    cancelEdit(children)
                }
            }
        }

        val cancelButton = Button().apply {
            graphic = cross20

            onAction = EventHandler {
                cancelEdit(children)
            }
        }

        navBar.children.setAll(textField, doneButton, cancelButton)

        this.vvalue = .0
        textField.requestFocus()
        textField.selectAll()
    }

    private fun tryFinishEdit(path: Path?, field: TextField, oldChildren: Array<Node>, error: String?) {
        if (error == null) {
            fileNav.items.apply {
                val i = fileNav.selectionModel.selectedIndex
                set(i, path!!)
                fileNav.selectionModel.select(i)
            }
            navBar.children.setAll(*oldChildren)
            oldChildren[0].requestFocus()

        } else {

            var onTextChange: ChangeListener<String>? = null

            onTextChange = ChangeListener { _, _, _ ->
                field.pseudoClassStateChanged(ERROR_CLASS, false)
                fileErrLabel.isVisible = false
                field.textProperty().removeListener(onTextChange)
            }

            field.pseudoClassStateChanged(ERROR_CLASS, true)
            field.textProperty().addListener(onTextChange)
            fileErrLabel.text = error
            fileErrLabel.isVisible = true

        }
    }

    private fun cancelEdit(oldChildren: Array<Node>) {
        navBar.children.setAll(*oldChildren)
        fileErrLabel.isVisible = false
        oldChildren[0].requestFocus()

    }


    //****************************************
    // Overrides
    //****************************************
    override fun cleanup() = model.executor.shutdown()

    override var onNavigateBackRequest: (() -> Unit)? = null

    @Suppress("UNCHECKED_CAST")
    private fun showHtml() {
        model.executor.execute {
            val panes = inputWrapper.children as List<InputPaneBase>
            val path = model.getHtml(panes)
            hostServices.showDocument(path.toString())
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private companion object {
        const val EXTENSION = ".templ"
        const val DEFAULT_FILE_NAME = "Untitled${EXTENSION}"

        val LIGHT_THEME = TemplatorKt::class.java.getResource("/css/templateLight.css")!!.toExternalForm()!!
        val DARK_THEME = TemplatorKt::class.java.getResource("/css/templateDark.css")!!.toExternalForm()!!
        val BASE = TemplatorKt::class.java.getResource("/css/template.css")!!.toExternalForm()!!


        private inline fun addFieldText(type: String) = "Add new $type Field"

    }

    inner class DragAndDropManager {

        var isLineAdded = false

        private val line = Line().apply {
            isVisible = false
            isManaged = false
            startX = .0
            strokeWidth = 2.0
            stroke = Color.grayRgb(50)
        }

        private fun configureLine() {
            line.endXProperty().bind(inputWrapper.widthProperty())
            inputWrapper.children.add(line)
            isLineAdded = true
        }

        private fun removeLine() {
            line.endXProperty().unbind()
            inputWrapper.children.remove(line)
            isLineAdded = false
        }

        fun setDnd(pane: InputPaneBase) {
            pane.apply {
                val title = lookup(".title")

                title.onMouseDragged = EventHandler { it.isDragDetect = true }

                title.onDragDetected = EventHandler {
                    val db = this.startDragAndDrop(TransferMode.MOVE)
                    db.setDragView(this.snapshot(null, null), it.x, it.y)
                    val content = ClipboardContent()

                    configureLine()
                    //have to put something for drag and drop
                    content.putString("")
                    db.setContent(content)
                    it.consume()
                }

                onDragOver = EventHandler {
                    val source = it.gestureSource
                    if (source != null && source !== this) {
                        it.acceptTransferModes(TransferMode.MOVE)
                    }
                    it.consume()
                }

                onDragEntered = EventHandler {
                    val source = it.gestureSource
                    if (source != null && source !== this) {
                        val space = inputWrapper.spacing / 2
                        val thisIndex = inputWrapper.children.indexOf(this)
                        val otherIndex = inputWrapper.children.indexOf(it.gestureSource)

                        val isTargetBelow = thisIndex < otherIndex
                        val y = boundsInParent.let { bounds -> if (isTargetBelow) {
                                bounds.minY - space
                            } else {
                                bounds.maxY + space
                            }
                        }
                        line.startY = y
                        line.endY = y
                        line.isVisible = true
                    }
                    it.consume()
                }

                onDragExited = EventHandler {
                    line.isVisible = false
                    it.consume()
                }

                onDragDone = EventHandler {
                    removeLine()
                }

                onDragDropped = EventHandler {
                    val children = inputWrapper.children
                    val thisIndex = children.indexOf(this)
                    val otherIndex = children.indexOf(it.gestureSource)
                    val isTargetBelow = thisIndex < otherIndex

                    val targetIndex = thisIndex + if (isTargetBelow) 0 else 1
                    animManager.doAnimation = true
                    viewOrder = -1.0
                    children.add(targetIndex, children.removeAt(otherIndex))

                    it.isDropCompleted = true
                    it.consume()
                }
            }
        }
    }

    class LayoutAnimateManager {
        var doAnimation = false

        private val _interpolator = QUAD_EASE_OUT

        val changeListener: ChangeListener<Number> = ChangeListener { ob, old, new ->
            if (doAnimation) {
                ob as DoubleProperty
                val node = ob.bean as Region
                val delta = old.toDouble() - new.toDouble()

                node.translateY += delta
                node.cacheHint = CacheHint.SPEED
                node.isSnapToPixel = false

                val anim = TranslateTransition().apply {
                    toY = .0
                    this.node = node
                    interpolator = _interpolator
                    duration = Duration.millis(500.0)

                    onFinished = EventHandler {
                        node.cacheHint = CacheHint.DEFAULT
                        node.isSnapToPixel = true
                        node.viewOrder = 0.0
                        doAnimation = false
                    }
                }

                anim.play()
            }
        }
    }

    //fixme: The programs breaks after loading a corrupted file
}