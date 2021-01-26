module ElectFX {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.jfxtras.styles.jmetro;
    requires java.logging;
    requires com.jfoenix;
    requires kotlin.stdlib;
    requires org.apache.commons.text;
    requires kotlinx.serialization.core.jvm;
    requires kotlinx.serialization.json.jvm;

    opens edu.opjms.candidateSelector.main;

    exports edu.opjms.candidateSelector.util;
    exports edu.opjms.candidateSelector.controls;
    exports edu.opjms.templating;
    exports edu.opjms.templating.controls;


    opens edu.opjms.resultView.main;
    opens edu.opjms.main;
}