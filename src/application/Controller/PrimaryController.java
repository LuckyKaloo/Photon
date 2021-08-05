package application.Controller;

import application.Model.Components.Component;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Objects;

public class PrimaryController {
    @FXML
    private BorderPane borderPane;
    @FXML
    private MenuBar menuBar;
    @FXML
    private VBox componentOptions;
    @FXML
    private ToggleButton source;
    @FXML
    private ToggleButton absorber;
    @FXML
    private ToggleButton shape;
    @FXML
    private ToggleButton mirror;
    @FXML
    private Pane pane;
    @FXML
    private Canvas canvas;
    @FXML
    private AnchorPane information;


    private GraphicsContext graphicsContext;
    private ArrayList<Component> components;

    @FXML
    private void initialize() {
        borderPane.getStylesheets().add(Objects.requireNonNull(
                getClass().getClassLoader().getResource("application/View/primary.css")).toExternalForm());
        canvas.heightProperty().bind(pane.heightProperty());
        canvas.widthProperty().bind(pane.widthProperty());

        graphicsContext = canvas.getGraphicsContext2D();
        components = new ArrayList<>();

        ToggleGroup group = new ToggleGroup();
        source.setToggleGroup(group);
        absorber.setToggleGroup(group);
        shape.setToggleGroup(group);
        mirror.setToggleGroup(group);

        canvas.setOnMouseClicked(e -> {
            if (source.isSelected()) {
//                components.add(new Source());
            } else if (absorber.isSelected()) {
//                components.add(new Absorber());
            } else if (shape.isSelected()) {
                // add new shape
            } else if (mirror.isSelected()) {
//                components.add(new Mirror());
            }
        });
    }
}
