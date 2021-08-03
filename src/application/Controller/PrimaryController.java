package application.Controller;

import application.Model.Components.Component;
import application.Model.Components.Material;
import application.Model.Geometry.Line;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

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
    private ToggleButton refractor;
    @FXML
    private ToggleButton mirror;
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

        graphicsContext = canvas.getGraphicsContext2D();
        components = new ArrayList<>();

        ToggleGroup group = new ToggleGroup();
        source.setToggleGroup(group);
        absorber.setToggleGroup(group);
        refractor.setToggleGroup(group);
        mirror.setToggleGroup(group);

        canvas.setOnMouseClicked(e -> {

        });


        Ray beam = new Ray(80, new Point(150, 150));
        Line edge = new Line(new Point(100, 200), new Point(300, 700));
        Point intersection = beam.intersection(edge);
        System.out.println(intersection);
        Material start = new Material(1.2);
        Material end = new Material(1);

        Ray outgoing = Material.refract(beam, edge, start, end);

        graphicsContext.beginPath();
        graphicsContext.moveTo(edge.getStart().getX(), edge.getStart().getY());
        graphicsContext.setStroke(Color.rgb(220, 150, 150));
        graphicsContext.lineTo(edge.getEnd().getX(), edge.getEnd().getY());
        graphicsContext.stroke();
        graphicsContext.closePath();

        graphicsContext.beginPath();
        graphicsContext.moveTo(beam.getStart().getX(), beam.getStart().getY());
//        graphicsContext.lineTo(beam.getStart().getX() + 800 * Math.cos(Math.toRadians(beam.getAngle())),
//                beam.getStart().getY() + 800 * Math.sin(Math.toRadians(beam.getAngle())));
        graphicsContext.lineTo(intersection.getX(), intersection.getY());
        graphicsContext.stroke();
        graphicsContext.closePath();

        graphicsContext.beginPath();
        graphicsContext.moveTo(intersection.getX(), intersection.getY());
        graphicsContext.lineTo(intersection.getX() + 100 * Math.cos(Math.toRadians(outgoing.getAngle())),
                intersection.getY() + 100 * Math.sin(Math.toRadians(outgoing.getAngle())));
        graphicsContext.stroke();
        graphicsContext.closePath();

        graphicsContext.setFill(Color.rgb(10, 12, 20));
        graphicsContext.fill();
    }
}
