package application.Controller;

import application.Model.Components.*;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import application.Model.Geometry.Segment;
import application.Model.Light.Beam;
import application.Model.Light.LightComponent;
import application.Model.Light.LightRay;
import application.Model.Light.LightSegment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
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
    private final ArrayList<Point> vertices = new ArrayList<>();  // if user selected shape, more than 1 point will be used

    // colors
    private final static Color backgroundColor = Color.rgb(2, 6, 12);
    private final static Color shapeColor = Color.rgb(150, 150, 220);
    private final static Color sourceColor = Color.rgb(203, 48, 255);
    private final static Color beamColor = Color.rgb(224, 225, 48);
    private final static Color absorberColor = Color.rgb(93, 231, 48);
    private final static Color mirrorColor = Color.rgb(192, 215, 231);


    public void start() {
        graphicsContext = canvas.getGraphicsContext2D();
        components = new ArrayList<>();

        // initialising canvas
        borderPane.getStylesheets().add(Objects.requireNonNull(
                getClass().getClassLoader().getResource("application/View/primary.css")).toExternalForm());
        canvas.heightProperty().bind(pane.heightProperty());
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.setScaleY(-1);


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
                    startPoint = null;
                }
            } else if (mirror.isSelected()){
                if (startPoint == null) {
                    startPoint = new Point(e.getX(), e.getY());
                } else {
                    components.add(new Mirror(startPoint, new Point(e.getX(), e.getY())));
                    startPoint = null;
                }
            } else if (shape.isSelected()) {
                Point selectedPoint = new Point(e.getX(), e.getY());
                if (vertices.size() != 0 && selectedPoint.equals(vertices.get(0), 5)) {
                    components.add(new Shape(1.5, vertices));
                    vertices.clear();
                } else {
                    vertices.add(selectedPoint);
                }
            }

            updateCanvas(e);
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

    // drawing fully formed components
    private void updateCanvas() {
        for (Component component: components) {
            if (component instanceof Source) {
                ((Source) component).getBeam().generateBeam(components);
            }
        }

        drawCanvas();
    }

    private void drawCanvas() {
        graphicsContext.setFill(backgroundColor);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Component component: components) {  // draw components
            if (component instanceof Shape) {
                graphicsContext.setStroke(shapeColor);
                for (Edge edge : ((Shape) component).getEdges()) {
                    drawSegment(edge);
                }
            } else if (component instanceof Source source) {
                graphicsContext.setStroke(sourceColor);
                drawSegment(source.getEdge());

                // drawing the beam
                graphicsContext.setStroke(beamColor);
                for (LightComponent lightComponent: source.getBeam().getLightComponents()) {
                    if (lightComponent instanceof LightSegment lightSegment) {
                        drawSegment(lightSegment);
                    } else if (lightComponent instanceof LightRay lightRay) {
                        drawRay(lightRay);
                    }
                }
            } else if (component instanceof Absorber absorber) {
                graphicsContext.setStroke(absorberColor);
                drawSegment(absorber.getEdge());
            } else if (component instanceof Mirror mirror) {
                graphicsContext.setStroke(mirrorColor);
                drawSegment(mirror.getEdge());
            }
        }
    }

    // draw components that are being added
    private void updateCanvas(MouseEvent e) {
        updateCanvas();

        if (absorber.isSelected() && startPoint != null) {
            graphicsContext.setStroke(absorberColor);
            drawSegment(new Segment(startPoint, new Point(e.getX(), e.getY())));
        } else if (mirror.isSelected() && startPoint != null){
            graphicsContext.setStroke(mirrorColor);
            drawSegment(new Segment(startPoint, new Point(e.getX(), e.getY())));
        } else if (shape.isSelected() && vertices.size() != 0) {
            graphicsContext.setStroke(shapeColor);

            ArrayList<Point> points = new ArrayList<>(vertices);
            ArrayList<Segment> segments = Segment.pointsToSegments(points);
            for (Segment segment: segments) {
                drawSegment(segment);
            }
        }
    }


    // helper methods to draw the canvas
    private void drawSegment(Segment segment) {
        graphicsContext.beginPath();
        graphicsContext.moveTo(segment.getStart().X(), segment.getStart().Y());
        graphicsContext.lineTo(segment.getEnd().X(), segment.getEnd().Y());
        graphicsContext.stroke();
        graphicsContext.closePath();
    }

    private void drawRay(Ray ray) {
        graphicsContext.beginPath();
        graphicsContext.moveTo(ray.getStart().X(), ray.getStart().Y());
        double endX = ray.getStart().X() + 800 * Math.cos(Math.toRadians(ray.getAngle()));
        double endY = ray.getStart().Y() + 800 * Math.sin(Math.toRadians(ray.getAngle()));
        graphicsContext.lineTo(endX, endY);
        graphicsContext.stroke();
        graphicsContext.closePath();
    }
}