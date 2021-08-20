package edu.opjms.candidateSelector.listUndoManager;

import net.kilobyte1000.Houses;
import javafx.collections.ObservableList;

import java.util.Arrays;

public class UndoTaskDelete<T> extends UndoTasks<T> {
    T[] deletedItems;

    public UndoTaskDelete(Houses houseIndex, byte prefectPost, T[] deletedItems) {
        super(houseIndex, prefectPost);
        this.deletedItems = deletedItems;
    }

    @Override
    public void undo(ObservableList<T> listItems) {
        listItems.addAll(deletedItems);
    }

    @Override
    public void redo(ObservableList<T> listItems) {
        listItems.removeAll(deletedItems);
    }

    @Override
    public String toString() {
        return "UndoTaskDelete{" +
                "deletedItems=" + Arrays.toString(deletedItems) +
                '}';
    }
}
