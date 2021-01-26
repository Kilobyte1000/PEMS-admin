package edu.opjms.candidateSelector.main;

import edu.opjms.candidateSelector.listUndoManager.UndoTaskAdd;
import edu.opjms.candidateSelector.listUndoManager.UndoTaskDelete;
import edu.opjms.candidateSelector.listUndoManager.UndoTaskEdit;
import edu.opjms.candidateSelector.listUndoManager.UndoTasks;
import edu.opjms.candidateSelector.util.HouseIndex;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Objects;

/**
 * The Model Data <br />
 * <p> Firstly, stores prefect list <br>
 * Each Post corresponds a number as follows:-
 *     <ol>
 *            <li>Boy House Prefect</li>
 *            <li>Girl House Prefect</li>
 *            <li>Boy Sports Prefect</li>
 *            <li>Girl Sports Prefect</li>
 *     </ol>
 * </p>
 */
class Model {
    /* undo and redo */
    private final ArrayDeque<UndoTasks<String>> undoDeque;
    private final ArrayDeque<UndoTasks<String>> redoDeque;
    public SimpleBooleanProperty undoNotAvailable = new SimpleBooleanProperty(true);
    public SimpleBooleanProperty redoNotAvailable = new SimpleBooleanProperty(true);
    /* Required Data */
    private HouseIndex currentHouse;
    private File currentFile;
    /* Observable List To Store Prefect List Data */
    private ListData items;
    private final SimpleBooleanProperty isDataSaved = new SimpleBooleanProperty(true);

    public Model() {
        items = new ListData();
        undoDeque = new ArrayDeque<>();
        redoDeque = new ArrayDeque<>();
    }

    public HouseIndex getCurrentHouse() {
        return currentHouse;
    }

    public void setCurrentHouse(HouseIndex currentHouse) {
        this.currentHouse = currentHouse;
    }

    public File currentFile() {
        return currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    public ListData getItems() {
        return items;
    }

    /* Save and retrieve data from file*/
    public void writeDataToFile(File file) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(file); ObjectOutputStream serialise = new ObjectOutputStream(fileOut)) {

            serialise.writeObject(this.items);
            isDataSaved.setValue(true);
        }

    }

    public void getDataFromFile(File file) throws IOException, ClassNotFoundException {
        try (FileInputStream fileIn = new FileInputStream(file); ObjectInputStream deSerialise = new ObjectInputStream(fileIn)) {
            items = (ListData) deSerialise.readObject();

            //clear undo and redo deck
            undoDeque.clear();
            undoNotAvailable.setValue(true);

            redoDeque.clear();
            redoNotAvailable.setValue(true);

            isDataSaved.setValue(true);
        }

    }

    public void newFile() {
        items = new ListData();
        currentFile = null;
    }

    public SimpleBooleanProperty isDataSaved() {
        return isDataSaved;
    }

    private void addToUndoDeck(UndoTasks<String> undoTask, boolean clearRedo) {
        if (undoDeque.size() >= 32)
            undoDeque.removeLast();
        undoDeque.push(undoTask);
        undoNotAvailable.setValue(false);

        isDataSaved.setValue(false);

        if (!redoNotAvailable.getValue() && clearRedo) {
            redoDeque.clear();
            redoNotAvailable.setValue(true);
        }
    }

    public void addUndoTaskAdd(byte prefectPost, String item) {
        addToUndoDeck(new UndoTaskAdd<>(getCurrentHouse(),
                prefectPost,
                item), true);
    }

    public void addUndoTaskDelete(byte prefectPost, String[] items) {
        addToUndoDeck(new UndoTaskDelete<>(getCurrentHouse(),
                prefectPost,
                items), true);
    }

    public void addUndoTaskEdit(byte prefectPost, String oldItem, String newItem) {
        addToUndoDeck(new UndoTaskEdit<>(getCurrentHouse(),
                prefectPost,
                oldItem,
                newItem), true);
    }

    public byte[] undoLastTask() {
        var undoAbleTask = undoDeque.pop();
        undoNotAvailable.setValue(undoDeque.isEmpty());

        if (redoDeque.size() >= 32)
            redoDeque.removeLast();
        redoDeque.push(undoAbleTask);
        redoNotAvailable.setValue(false);

        var undoHouse = undoAbleTask.getHouseIndex();
        var undoPost = undoAbleTask.getPrefectPost();

        undoAbleTask.undo(items.getCandidateList(undoHouse, undoPost));

        return new byte[] {(byte)undoHouse.ordinal(), undoPost};
    }

    public byte[] redoLastTask() {
        var redoAbleTask = redoDeque.pop();
        redoNotAvailable.setValue(redoDeque.isEmpty());

        addToUndoDeck(redoAbleTask ,false);

        var redoHouse = redoAbleTask.getHouseIndex();
        var redoPost = redoAbleTask.getPrefectPost();

        redoAbleTask.redo(items.getCandidateList(redoHouse, redoPost));

        return new byte[] {(byte)redoHouse.ordinal(), redoPost};
    }


    /* boilerplate code */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Model model = (Model) o;

        if (currentHouse != model.currentHouse) return false;
        if (!Objects.equals(currentFile, model.currentFile)) return false;
        if (!items.equals(model.items)) return false;
        if (!undoDeque.equals(model.undoDeque)) return false;
        return redoDeque.equals(model.redoDeque);
    }

    @Override
    public int hashCode() {
        int result = currentHouse.hashCode();
        result = 31 * result + (currentFile != null ? currentFile.hashCode() : 0);
        result = 31 * result + items.hashCode();
        result = 31 * result + undoDeque.hashCode();
        result = 31 * result + redoDeque.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Model{" +
                "currentHouse=" + currentHouse +
                ", currentFile=" + currentFile +
                ", candidateList=" + items +
                '}';
    }
}