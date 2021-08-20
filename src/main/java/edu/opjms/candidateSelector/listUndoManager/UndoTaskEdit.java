package edu.opjms.candidateSelector.listUndoManager;

import net.kilobyte1000.Houses;
import javafx.collections.ObservableList;

public class UndoTaskEdit<T> extends UndoTasks<T> {
    private final T oldItem;
    private final T newItem;

    public UndoTaskEdit(Houses houseIndex, byte prefectPost, T oldItem, T newItem) {
        super(houseIndex, prefectPost);
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    @Override
    public void undo(ObservableList<T> listItems) {
        listItems.set(listItems.indexOf(newItem), oldItem);
    }

    @Override
    public void redo(ObservableList<T> listItems) {
        listItems.set(listItems.indexOf(oldItem), newItem);
    }

    @Override
    public String toString() {
        return "UndoTaskEdit{" +
                "oldItem=" + oldItem +
                ", newItem=" + newItem +
                '}';
    }
}
