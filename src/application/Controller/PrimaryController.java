package application.Controller;

import application.Model.Components.*;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import application.Model.Light.Beam;
import application.Model.Light.LightComponent;
import application.Model.Light.LightRay;
import application.Model.Light.LightSegment;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

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

    // non FXML stuff
    private GraphicsContext graphicsContext;
    private ArrayList<Component> components;

    private Point startPoint;  // if user selected mirror or absorber, this is the first point of the component (line)
    private ArrayList<Point> shapePoints;  // if user selected shape, more than 1 point will be used

    public void start() {
        graphicsContext = canvas.getGraphicsContext2D();
        components = new ArrayList<>();

        // initialising canvas
        borderPane.getStylesheets().add(Objects.requireNonNull(
                getClass().getClassLoader().getResource("application/View/primary.css")).toExternalForm());
        canvas.heightProperty().bind(pane.heightProperty());
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.setScaleY(-1);

        graphicsContext.setFill(Color.rgb(2, 6, 15));
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());


        // initialising buttons
        ToggleGroup group = new ToggleGroup();
        source.setToggleGroup(group);
        absorber.setToggleGroup(group);
        shape.setToggleGroup(group);
        mirror.setToggleGroup(group);

        try {
            source.setGraphic(new ImageView(new Image(new FileInputStream("Resources/images/Source.png"), 80, 80, false, false)));
            absorber.setGraphic(new ImageView(new Image(new FileInputStream("Resources/images/Absorber.png"), 80, 80, false, false)));
            mirror.setGraphic(new ImageView(new Image(new FileInputStream("Resources/images/Mirror.png"), 80, 80, false, false)));
            shape.setGraphic(new ImageView(new Image(new FileInputStream("Resources/images/Shape.png"), 80, 80, false, false)));
        } catch (FileNotFoundException ignored) {}

        // initialising actions
        canvas.setOnMouseClicked(e -> {
            if (source.isSelected()) {
                components.add(new Source(new Point(e.getX(), e.getY())));
            } else if (absorber.isSelected()) {
                if (startPoint == null) {
                    startPoint = new Point(e.getX(), e.getY());
                } else {
                    components.add(new Absorber(startPoint, new Point(e.getX(), e.getY())));
                }
            } else if (mirror.isSelected()){
                if (startPoint == null) {
                    startPoint = new Point(e.getX(), e.getY());
                } else {
                    components.add(new Mirror(startPoint, new Point(e.getX(), e.getY())));
                }
            } else if (shape.isSelected()) {
                // add new shape
            }
        });


        // testing
        ArrayList<Point> points1 = new ArrayList<>();
        points1.add(new Point(300, 200));
        points1.add(new Point(200, 300));
        points1.add(new Point(400, 250));
        Shape shape1 = new Shape(1.5, points1);

        ArrayList<Point> points2 = new ArrayList<>();
        points2.add(new Point(380, 600));
        points2.add(new Point(340, 350));
        points2.add(new Point(570, 480));
        points2.add(new Point(420, 550));
        Shape shape2 = new Shape(1.6, points2);

        components.add(shape1);
        components.add(shape2);

        Source source = new Source(new Point(240, 400));
        source.setAngle(-160);
        components.add(source);

        Beam beam = source.getBeam();
        beam.generateBeam(components);

        updateCanvas();
    }

    private void updateCanvas() {
        for (Component component: components) {
            if (component instanceof Source) {
                ((Source) component).getBeam().generateBeam(components);
            }
        }

        drawCanvas();
    }

    private void drawCanvas() {
        for (Component component: components) {
            if (component instanceof Shape) {
                graphicsContext.setStroke(Color.rgb(150, 150, 220));
                for (Edge edge : ((Shape) component).getEdges()) {
                    graphicsContext.beginPath();
                    graphicsContext.moveTo(edge.getStart().X(), edge.getStart().Y());
                    graphicsContext.lineTo(edge.getEnd().X(), edge.getEnd().Y());
                    graphicsContext.stroke();
                    graphicsContext.closePath();
                }
            } else if (component instanceof Source source) {
                graphicsContext.setStroke(Color.rgb(220, 150, 150));

                // drawing the source
                Edge edge = source.getEdge();
                graphicsContext.beginPath();
                graphicsContext.moveTo(edge.getStart().X(), edge.getStart().Y());
                graphicsContext.lineTo(edge.getEnd().X(), edge.getEnd().Y());
                graphicsContext.stroke();
                graphicsContext.closePath();

                // drawing the beam
                for (LightComponent lightComponent: source.getBeam().getLightComponents()) {
                    graphicsContext.setStroke(Color.rgb(224, 225, 48));
                    graphicsContext.beginPath();
                    if (lightComponent instanceof LightSegment lightSegment) {
                        graphicsContext.moveTo(lightSegment.getStart().X(), lightSegment.getStart().Y());
                        graphicsContext.lineTo(lightSegment.getEnd().X(), lightSegment.getEnd().Y());
                    } else {
                        LightRay lightRay = (LightRay) lightComponent;
                        graphicsContext.moveTo(lightRay.getStart().X(), lightRay.getStart().Y());
                        graphicsContext.lineTo(lightRay.getStart().X() + 800 * Math.cos(Math.toRadians(lightRay.getAngle())),
                                lightRay.getStart().Y() + 800 * Math.sin(Math.toRadians(lightRay.getAngle())));
                    }
                    graphicsContext.stroke();
                    graphicsContext.closePath();
                }
            }
        }
    }
}