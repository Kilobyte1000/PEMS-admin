open module ElectFX {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.desktop;
    requires org.jfxtras.styles.jmetro;
    requires java.logging;
    requires com.jfoenix;
    requires kotlin.stdlib;
    requires org.apache.commons.lang3;
    requires org.apache.commons.text;
    requires org.controlsfx.controls;
    requires kotlinx.serialization.core.jvm;
    requires kotlinx.serialization.json.jvm;
    requires kotlin.csv.jvm;
    requires org.apache.commons.io;

    requires server;
    requires kotlin.stdlib.jdk7;

    exports edu.opjms.candidateSelector.util;
    exports edu.opjms.candidateSelector.controls;
    exports edu.opjms.templating;


}