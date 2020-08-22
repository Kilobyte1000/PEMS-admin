package edu.opjms.candidateSelector.main;

import edu.opjms.candidateSelector.util.HouseIndex;
import javafx.collections.ObservableList;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import static edu.opjms.candidateSelector.controls.ActionButtonNew.DEFAULT_NAME;
import static java.util.Arrays.deepToString;
import static javafx.collections.FXCollections.observableArrayList;

class ListData implements Externalizable {
    public static final long serialVersionUID = 234523452312L;
    private final ObservableList<String>[][] candidateList;

    @SuppressWarnings("unchecked")
    public ListData() {
        candidateList = new ObservableList[6][4];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                candidateList[i][j] = observableArrayList();
            }
        }
    }

    private void validatePost(int post) throws NumberFormatException {
        if (post < 0 || post > 3)
            throw new NumberFormatException("post id should be either 0/1/2/3, found: " + post);
    }

    public ObservableList<String> getCandidateList(HouseIndex houseIndex, int post) {
        validatePost(post);
        return candidateList[houseIndex.ordinal()][post];
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                candidateList[i][j].removeIf(s -> s.equals(DEFAULT_NAME));
                out.writeObject(List.of(candidateList[i][j].sorted().toArray()));
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                candidateList[i][j].setAll((List<String>) in.readObject());
//                System.out.println( in.readObject());
            }
        }
    }

    @Override
    public String toString() {
        return "ListData{" +
                "candidateList=" + deepToString(candidateList) +
                '}';
    }
}
