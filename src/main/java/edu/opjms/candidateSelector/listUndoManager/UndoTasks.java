package edu.opjms.candidateSelector.listUndoManager;

import edu.opjms.candidateSelector.util.HouseIndex;
import javafx.collections.ObservableList;

abstract public class UndoTasks<T> {
    protected final ObservableList<T> listItems;
    private final HouseIndex houseIndex;
    private final byte prefectPost;

    public UndoTasks(ObservableList<T> listItems, HouseIndex houseIndex, byte prefectPost) {
        if (prefectPost > 3 || prefectPost < 0)
            throw new NumberFormatException("post id should be either 0/1/2/3, found: " + prefectPost);
        this.listItems = listItems;
        this.houseIndex = houseIndex;
        this.prefectPost = prefectPost;
    }

    protected int[] returnUniquePost() {
        return new int[]{houseIndex.ordinal(), prefectPost};
    }

    /**
     * undo's the action represented be this object
     *
     * @return an array containing the houseIndex and the postIndex respectively which represent the listView
     */
    abstract public int[] undo();

    /**
     * redo's the action represented be this object
     *
     * @return an array containing the houseIndex and the postIndex respectively
     */
    abstract public int[] redo();
}
