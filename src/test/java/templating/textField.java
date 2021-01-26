package templating;

public class textField extends templateTextInputBase {

    static {
        type = "text";
    }

    private String regex;

    @Override
    public String generateHTML() {

        final String name = "text-" + id; //id is null, figure unique generation later

        return "<div class='container'>" +

                //label part
                "<div class='label'>" +
                "<label for='" +
                name +
                "'> Insert Admission No. : </label>" +
                "</div>" +

                //input
                "<div class='input-text-field'>" +
                "<input id='" +
                id +
                "' type='text' name='" +
                name +
                "' placeholder=' ' autocomplete='off' title='" +
                title +
                "' pattern='" +
                regex +
                "' min='" +
                min +
                "' max='" +
                max +
                "' required>" +
                "<label for='name_field' class='tooltip'>First Name</label>" +
                "<div class='underline'></div>" +
                "</div></div>";
    }
}
