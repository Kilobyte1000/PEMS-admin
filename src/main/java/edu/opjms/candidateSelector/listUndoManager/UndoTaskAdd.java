package edu.opjms.candidateSelector.listUndoManager;


import edu.opjms.candidateSelector.util.HouseIndex;
import javafx.collections.ObservableList;

public class UndoTaskAdd<T> extends UndoTasks<T> {
    private final T item;

    public UndoTaskAdd(HouseIndex houseIndex, byte prefectPost, T item) {
        super(houseIndex, prefectPost);
        this.item = item;
    }

    @Override
    public void undo(ObservableList<T> listItems) {
        listItems.remove(item);
    }

    @Override
    public void redo(ObservableList<T> listItems) {
        listItems.add(item);
    }

    @Override
    public String toString() {
        return "UndoTaskAdd{" +
                "item=" + item +
                '}';
    }
}
