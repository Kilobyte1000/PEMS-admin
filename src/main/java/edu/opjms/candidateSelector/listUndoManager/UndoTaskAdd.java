package edu.opjms.candidateSelector.listUndoManager;


import edu.opjms.candidateSelector.util.HouseIndex;
import javafx.collections.ObservableList;

public class UndoTaskAdd<T> extends UndoTasks<T> {
    private final T item;

    public UndoTaskAdd(ObservableList<T> listItems, HouseIndex houseIndex, byte prefectPost, T item) {
        super(listItems, houseIndex, prefectPost);
        this.item = item;
    }

    @Override
    public int[] undo() {
        listItems.remove(item);
        return this.returnUniquePost();
    }

    @Override
    public int[] redo() {
        listItems.add(item);
        return this.returnUniquePost();
    }

    @Override
    public String toString() {
        return "UndoTaskAdd{" +
                "item=" + item +
                ", listItems=" + listItems +
                '}';
    }
}
