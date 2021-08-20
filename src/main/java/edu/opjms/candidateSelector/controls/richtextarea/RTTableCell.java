package edu.opjms.candidateSelector.controls.richtextarea;

import javafx.geometry.Pos;

public class RTTableCell extends RTElementContainer<RTTableCell> {

    private int rowSpan = 1;
    private int colSpan = 1;
    private Pos alignment = Pos.TOP_LEFT;

    private RTTableCell() {
    }

    public static RTTableCell create() {
        return new RTTableCell();
    }

    public final RTTableCell withRowSpan(int span) {
        rowSpan = span;
        return this;
    }

    public final RTTableCell withColSpan(int span) {
        colSpan = span;
        return this;
    }

    public final RTTableCell withAlignment(Pos alignment) {
        this.alignment = alignment;
        return this;
    }

    public final int getRowSpan() {
        return rowSpan;
    }

    public final int getColSpan() {
        return colSpan;
    }

    public final Pos getAlignment() {
        return alignment;
    }
}
