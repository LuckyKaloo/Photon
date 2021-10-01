module Project {
    requires javafx.controls;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.fxml;
    requires reactfx;
    requires MaterialFX;

    opens application.Model.Components;
    opens application.Model.Geometry;
    opens application.Controller;
    opens application.Model.Light;
}