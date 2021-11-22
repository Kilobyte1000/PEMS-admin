package edu.opjms.templating.inputPanes;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

public class FieldData implements Externalizable {

    private List<Pair<String, String>> pairs;
    private Set<String> duplicates;
    private Set<String> nonNumeric;

    /**
     * Only for {@link Externalizable}, not meant to be called by users
     */
    public FieldData() {
    }

    public FieldData(@NotNull List<Pair<String, String>> pairs,
                     @NotNull Set<String> duplicates,
                     @NotNull Set<String> nonNumeric) {
        this.pairs = pairs;
        this.duplicates = duplicates;
        this.nonNumeric = nonNumeric;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(pairs.size());
        for (var pair: pairs) {
            out.writeUTF(pair.getFirst());
            out.writeUTF(pair.getSecond());
        }

        out.writeInt(duplicates.size());
        for (var dupl: duplicates) {
            out.writeUTF(dupl);
        }

        out.writeInt(nonNumeric.size());
        for (var non: nonNumeric) {
            out.writeUTF(non);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        final var pairs = in.readInt();
        final var list = new ArrayList<Pair<String, String>>(pairs);
        for (int i = 0; i < pairs; i++) {
            list.add(new Pair<>(in.readUTF(), in.readUTF()));
        }
        this.pairs = Collections.unmodifiableList(list);

        final var duplicates = in.readInt();
        final var duplSet = new HashSet<String>(duplicates);
        for (int i = 0; i < duplicates; i++) {
            duplSet.add(in.readUTF());
        }
        this.duplicates = Collections.unmodifiableSet(duplSet);

        final var nonNumerics = in.readInt();
        final var nonNumericSet = new HashSet<String>(nonNumerics);
        for (int i = 0; i < nonNumerics; i++) {
            nonNumericSet.add(in.readUTF());
        }
        this.nonNumeric = Collections.unmodifiableSet(nonNumericSet);
    }


    //getters
    @NotNull
    public List<Pair<String, String>> pairs() {
        return pairs;
    }

    @NotNull
    public Set<String> duplicates() {
        return duplicates;
    }

    @NotNull
    public Set<String> nonNumeric() {
        return nonNumeric;
    }

    //boilerplate
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldData fieldData)) return false;

        if (!pairs.equals(fieldData.pairs)) return false;
        if (!duplicates.equals(fieldData.duplicates)) return false;
        return nonNumeric.equals(fieldData.nonNumeric);
    }

    @Override
    public int hashCode() {
        int result = pairs.hashCode();
        result = 31 * result + duplicates.hashCode();
        result = 31 * result + nonNumeric.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FieldData{" +
                "pairs=" + pairs +
                ", duplicates=" + duplicates +
                ", nonNumeric=" + nonNumeric +
                '}';
    }

    //for kotlin
    @NotNull
    public List<Pair<String, String>> component1() {
        return pairs;
    }
    @NotNull
    public Set<String> component2() {
        return duplicates;
    }
    @NotNull
    public Set<String> component3() {
        return nonNumeric;
    }
}
