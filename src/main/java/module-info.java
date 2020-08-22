module ElectFX {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.jfxtras.styles.jmetro;

    opens edu.opjms.candidateSelector.main;

    exports edu.opjms.candidateSelector.util;
    exports edu.opjms.candidateSelector.controls;

    opens edu.opjms.resultView.main;
}