open module server {
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk7;
    requires io.activej.inject;
    requires io.activej.worker;
    requires io.activej.http;
    requires io.activej.promise;
    requires io.activej.launcher;
    requires io.activej.launchers.http;
    requires io.activej.common;
    requires java.sql;
    requires org.mariadb.jdbc;


    exports net.kilobyte1000;

}