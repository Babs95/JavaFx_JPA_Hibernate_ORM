module sn.babs.sanbox_java_fx {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires java.desktop;

    opens sn.babs.sanbox_java_fx.entities to org.hibernate.orm.core;
    opens sn.babs.sanbox_java_fx to javafx.fxml;
    opens sn.babs.sanbox_java_fx.controllers to javafx.fxml;

    exports sn.babs.sanbox_java_fx;
    exports sn.babs.sanbox_java_fx.controllers;
    exports sn.babs.sanbox_java_fx.services;
    exports sn.babs.sanbox_java_fx.entities;

}