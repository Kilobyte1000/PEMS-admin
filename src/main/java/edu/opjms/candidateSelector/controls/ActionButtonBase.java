package edu.opjms.candidateSelector.controls;

import javafx.scene.control.ListView;

public abstract class ActionButtonBase extends javafx.scene.control.Button {
    private ListView<String> listView;

    @SuppressWarnings("unchecked")
    private ListView<String> setListView() {
        listView = (ListView<String>) getParent().lookup("ListView");
        return listView;

    }

    public ListView<String> getListView() {
        return listView == null ? setListView() : listView;
    }

    abstract public void action();
}
