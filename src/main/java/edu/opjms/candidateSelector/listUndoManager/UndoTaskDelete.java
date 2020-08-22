package edu.opjms.candidateSelector.listUndoManager;

import edu.opjms.candidateSelector.util.HouseIndex;
import javafx.collections.ObservableList;

import java.util.Arrays;

public class UndoTaskDelete<T> extends UndoTasks<T> {
    T[] deletedItems;

    public UndoTaskDelete(ObservableList<T> listItems, HouseIndex houseIndex, byte prefectPost, T[] deletedItems) {
        super(listItems, houseIndex, prefectPost);
        this.deletedItems = deletedItems;
    }

    @Override
    public int[] undo() {
        this.listItems.addAll(deletedItems);
        return this.returnUniquePost();
    }

    @Override
    public int[] redo() {
        this.listItems.removeAll(deletedItems);
        return this.returnUniquePost();
    }

    @Override
    public String toString() {
        return "UndoTaskDelete{" +
                "deletedItems=" + Arrays.toString(deletedItems) +
                ", listItems=" + listItems +
                '}';
    }
}
