package edu.opjms.candidateSelector.undo

import net.kilobyte1000.Houses
import edu.opjms.common.Posts

sealed class UndoEvent<T>(
    val house: Houses,
    val post: Posts,
    var startIndex: Int,
    val endIndex: Int,
    val isBoy: Boolean
) {
    abstract fun undo(list: MutableList<T>): Boolean
    abstract fun redo(list: MutableList<T>): Boolean
    override fun toString(): String {
        return "UndoEvent(house=$house, post=$post, startIndex=$startIndex, endIndex=$endIndex, isBoy=$isBoy)"
    }

}

class AddItemEvent<T>(
    private val item: T,
    index: Int,
    house: Houses,
    post: Posts,
    isBoy: Boolean
) : UndoEvent<T>(house, post, index, index + 1, isBoy) {
    override fun undo(list: MutableList<T>): Boolean {
        list.remove(item)
        return false
    }

    override fun redo(list: MutableList<T>): Boolean {
        //always added at last
        startIndex = list.size
        list.add(item)
        return true
    }

    override fun toString(): String {
        return "AddItemEvent(item=$item) ${super.toString()}"
    }

}

class EditItemEvent<T>(
    private val old: T,
    private val new: T,
    index: Int,
    house: Houses,
    post: Posts,
    isBoy: Boolean
) : UndoEvent<T>(house, post, index, index + 1, isBoy) {
    override fun undo(list: MutableList<T>): Boolean {
        startIndex = list.indexOf(new)
        list[startIndex] = old
        return true
    }

    override fun redo(list: MutableList<T>): Boolean {
        startIndex = list.indexOf(old)
        list[startIndex] = new
        return true
    }

    override fun toString(): String {
        return "EditItemEvent(old=$old, new=$new) ${super.toString()}"
    }


}

class RemoveItemsEvent<T>(
    private val items: List<T>,
    index: Int,
    house: Houses,
    post: Posts,
    isBoy: Boolean
) : UndoEvent<T>(house, post, index, index + items.size, isBoy) {
    override fun undo(list: MutableList<T>): Boolean {
        list.addAll(startIndex, items)
        return true
    }

    override fun redo(list: MutableList<T>): Boolean {
        list.removeAll(items)
        return false
    }


}