package edu.opjms.candidateSelector.main

import net.kilobyte1000.Houses
import javafx.collections.FXCollections
import javafx.collections.ObservableList

class ListDataKt {
    private val candidateList: Array<Array<ObservableList<String>>> = Array(6) { Array(4) {FXCollections.observableArrayList()} }

    fun getCandidateList(houseIndex: Houses, post: Int) =
            candidateList[houseIndex.ordinal][post]

    override fun toString() = "ListDataKt(candidateList=${candidateList.contentToString()})"



}

/*
*
* If I EVER decide to switch from java serialisation to kotlin serialisation
* I'll use this class*
* */