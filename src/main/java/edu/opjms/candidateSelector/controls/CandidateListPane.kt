package edu.opjms.candidateSelector.controls

import edu.opjms.candidateSelector.controls.richtextarea.*
import net.kilobyte1000.Houses
import edu.opjms.candidateSelector.util.Posts
import javafx.collections.FXCollections
import javafx.scene.control.Spinner
import javafx.scene.layout.VBox
import javafx.util.StringConverter

class CandidateListPane : VBox() {

    var onValueChange: ((Houses, Posts) -> Unit)? = null

    private val maleList = RTList.create()
    private val femaleList = RTList.create()
    private val listItemHeap = mutableListOf<RTListItem>()
    private val document: RTDocument
    private var placeholder = 1

    init {
        //spinners for selecting post
        val houseSpinner = Spinner(FXCollections.observableList(Houses.values().toList()))
        val postSpinner = Spinner(FXCollections.observableList(Posts.values().toList()))

        //style
        houseSpinner.styleClass.add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL)
        postSpinner.styleClass.add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL)

        //string converter
        houseSpinner.valueFactory.converter = object : StringConverter<Houses?>() {
            override fun toString(p0: Houses?): String {
                return p0!!.uiText
            }

            override fun fromString(p0: String?): Houses? {
                TODO("Not yet implemented")
            }
        }
        postSpinner.valueFactory.converter = object : StringConverter<Posts?>() {
            override fun toString(p0: Posts?): String {
                return p0!!.uiText
            }

            override fun fromString(p0: String?): Posts? {
                TODO("Not yet implemented")
            }
        }

        //listeners
        houseSpinner.valueProperty().addListener {
            _, _, _ -> onValueChange?.invoke(houseSpinner.value, postSpinner.value)}
        postSpinner.valueProperty().addListener {
            _, _, _ -> onValueChange?.invoke(houseSpinner.value, postSpinner.value)
        }

        isFillWidth = true

        val rtTextArea = RichTextArea()

        document = RTDocument.create(
                RTHeading.create("Boys"),
                maleList,
                RTHeading.create("Girls"),
                femaleList
        )

        rtTextArea.document = document

        children.addAll(houseSpinner, postSpinner, rtTextArea)
    }

    fun setMaleList(list: List<String>) {
        document.elements[1] = RTList.create(RTListItem.create(placeholder++.toString()))
        println(placeholder - 1)
        setListImpl(maleList.items, list)
    }

    fun setFemaleList(list: List<String>) {
        setListImpl(femaleList.items, list)
    }

    private fun setListImpl(itemList: MutableList<RTListItem>, list: List<String>) {
        val length = list.size

        setCapacity(itemList, length)
        for (i in 0 until length) {
            println("setting ${itemList[i].getText().text} to ${list[i]}")
//            itemList[i].getText().text = list[i]
//            itemList[i] = itemList[i].withElements(itemList[i].getText().withText(list[i]))
        }
    }

    private fun setCapacity(list: MutableList<RTListItem>, size: Int) {
        val listSize = list.size
        if (listSize < size) {
            //put listItems from heap to list
            val subList = getListItems(size - listSize)
            list.addAll(subList)
            subList.clear()

        } else if (listSize > size) {
            //put listItems from list to heap
            val subList = list.subList(size - 1, listSize - 1)
            listItemHeap.addAll(subList)
            subList.clear()
        }
    }

    private fun getListItems(count: Int): MutableList<RTListItem> {
        ensureHeapSize(count)
        val beginIndex = listItemHeap.size - count
        return listItemHeap.subList(beginIndex, listItemHeap.size)
    }

    private fun ensureHeapSize(size: Int) {
        if (size > listItemHeap.size) {
            for (_i in 0 until size - listItemHeap.size) {
                listItemHeap.add(RTListItem.create("df"))
            }
        }
    }


}

private fun RTListItem.getText(): RTText {
    //does not supply a method, so we use implementation details
    //to achieve our goal
    return elements[0] as RTText
}