package templating;

abstract public class templateBase {
    protected static String type;
    protected String labelText;
    protected String id;

    abstract public String generateHTML();
}
