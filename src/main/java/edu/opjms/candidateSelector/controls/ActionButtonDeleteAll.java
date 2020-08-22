package edu.opjms.candidateSelector.controls;

import static edu.opjms.candidateSelector.util.ListUtil.deleteAllItems;

public class ActionButtonDeleteAll extends ActionButtonBase {

    @Override
    public void action() {
        deleteAllItems(getListView());
    }
}
