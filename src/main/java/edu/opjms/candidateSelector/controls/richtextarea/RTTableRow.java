package edu.opjms.candidateSelector.controls.richtextarea;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RTTableRow extends RTElement<RTTableRow> {

    private final ObservableList<RTTableCell> cells = FXCollections.observableArrayList();

    private RTTableRow() {
    }

    public static RTTableRow create() {
        return new RTTableRow();
    }

    public final RTTableRow withCells(RTTableCell... cells) {
        this.cells.setAll(cells);
        return this;
    }

    public final ObservableList<RTTableCell> getCells() {
        return cells;
    }
}
