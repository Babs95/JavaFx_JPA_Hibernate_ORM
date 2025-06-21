module sn.babs.sanbox_java_fx {
    requires javafx.controls;
    requires javafx.fxml;

    opens sn.babs.sanbox_java_fx to javafx.fxml;
    exports sn.babs.sanbox_java_fx;
    exports sn.babs.sanbox_java_fx.controllers;
    opens sn.babs.sanbox_java_fx.controllers to javafx.fxml;
}