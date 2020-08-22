package edu.opjms.candidateSelector.util;

import javafx.scene.control.ListView;

public class ListUtil {
    public static <T> void editItemInList(ListView<T> list, int index) throws IndexOutOfBoundsException {
        list.scrollTo(index);
        list.edit(index);
    }


    public static <T> void deleteSelectedItem(ListView<T> list) {
        list.getItems().removeAll(list.getSelectionModel().getSelectedItems());
    }

    public static <T> void deleteAllItems(ListView<T> list) {
        list.getItems().clear();
    }

    public static <T> void addNewItem(ListView<T> list, T defaultObject) {
        if (list.getEditingIndex() == -1) {
            list.getItems().add(defaultObject);
            list.layout();
            editItemInList(list, list.getItems().size() - 1);
        } else {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
}
