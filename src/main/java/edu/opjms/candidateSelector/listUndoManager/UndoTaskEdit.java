package edu.opjms.candidateSelector.listUndoManager;

import edu.opjms.candidateSelector.util.HouseIndex;
import javafx.collections.ObservableList;

public class UndoTaskEdit<T> extends UndoTasks<T> {
    private final T oldItem;
    private final T newItem;

    public UndoTaskEdit(ObservableList<T> listItems, HouseIndex houseIndex, byte prefectPost, T oldItem, T newItem) {
        super(listItems, houseIndex, prefectPost);
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    @Override
    public int[] undo() {
        this.listItems.set(listItems.indexOf(newItem), oldItem);
        return this.returnUniquePost();
    }

    @Override
    public int[] redo() {
        this.listItems.set(listItems.indexOf(oldItem), newItem);
        return returnUniquePost();
    }

    @Override
    public String toString() {
        return "UndoTaskEdit{" +
                "oldItem=" + oldItem +
                ", newItem=" + newItem +
                ", listItems=" + listItems +
                '}';
    }
}
