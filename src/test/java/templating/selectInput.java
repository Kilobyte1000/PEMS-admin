package templating;

import javafx.util.Pair;

import java.util.List;

abstract public class selectInput<E> extends templateBase {

    static {
        type = "select";
    }

    private List<Pair<String, E>> labelAndValuePair;

}
