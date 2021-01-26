package edu.opjms.main;

//import com.jfoenix.controls.JFXDialog;
//import com.jfoenix.controls.JFXTextField;
/*public class FileSelectPopup extends JFXDialog {

    public FileSelectPopup() {
        super();
        construct();
    }

    public FileSelectPopup(StackPane dialogContainer, DialogTransition transitionType) {
        super(dialogContainer, null, transitionType);
        construct();
    }

    public FileSelectPopup(StackPane dialogContainer, DialogTransition transitionType, boolean overlayClose) {
        super(dialogContainer, null, transitionType, overlayClose);
        construct();
    }

    private void construct() {
        var root = new VBox();

        //construct search bar
        var topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(10);
        topBar.setPadding(new Insets(0, 0, 10, 0));

        var placeHolder = new Label("a");
        var searchBar = new JFXTextField();
        searchBar.setPromptText("Search");

        topBar.getChildren().setAll(placeHolder, searchBar);

        //show files pane
        var filesPane = new VBox();
        var wrapper = new ScrollPane(filesPane);
        wrapper.setFitToWidth(true);
        wrapper.setFitToHeight(true);

        root.getChildren().setAll(topBar, wrapper);

        super.setContent(root);
    }
}
*/