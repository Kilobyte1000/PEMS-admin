package edu.opjms.candidateSelector.controls;

import static edu.opjms.candidateSelector.util.ListUtil.deleteSelectedItem;

public class ActionButtonDelete extends ActionButtonBase {

    @Override
    public void action() {
        deleteSelectedItem(getListView());
    }
}
