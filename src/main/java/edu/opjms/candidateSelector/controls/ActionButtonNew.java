package edu.opjms.candidateSelector.controls;

import static edu.opjms.candidateSelector.util.ListUtil.addNewItem;

public class ActionButtonNew extends ActionButtonBase {

    public static final String DEFAULT_NAME = "(Name)";

    @Override
    public void action() {
        addNewItem(getListView(), DEFAULT_NAME);
    }
}
