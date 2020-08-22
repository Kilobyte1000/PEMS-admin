package edu.opjms.candidateSelector.controls;

import static edu.opjms.candidateSelector.util.ListUtil.editItemInList;

public class ActionButtonEdit extends ActionButtonBase {
    @Override
    public void action() {
        var list = getListView();
        try {
            var index = list.getSelectionModel().getSelectedIndices().get(0);
            list.getSelectionModel().clearSelection();
            editItemInList(list, index);
        } catch (IndexOutOfBoundsException e) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
}
