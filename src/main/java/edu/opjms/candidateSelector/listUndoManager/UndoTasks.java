package edu.opjms.candidateSelector.listUndoManager;

import net.kilobyte1000.Houses;
import javafx.collections.ObservableList;

abstract public class UndoTasks<T> {
    private final Houses houseIndex;
    private final byte prefectPost;

    public UndoTasks(Houses houseIndex, byte prefectPost) {
        if (prefectPost > 3 || prefectPost < 0)
            throw new NumberFormatException("post id should be either 0/1/2/3, found: " + prefectPost);
        this.houseIndex = houseIndex;
        this.prefectPost = prefectPost;
    }

/*    protected int[] returnUniquePost() {
        return new int[]{houseIndex.ordinal(), prefectPost};
    }*/


    public Houses getHouseIndex() {
        return houseIndex;
    }

    public byte getPrefectPost() {
        return prefectPost;
    }

    /**
     * undo's the action represented be this object
     *
     * @return an array containing the houseIndex and the postIndex respectively which represent the listView
     */
    abstract public void undo(ObservableList<T> listItems);

    /**
     * redo's the action represented be this object
     *
     * @return an array containing the houseIndex and the postIndex respectively
     */
    abstract public void redo(ObservableList<T> listItems);
}
