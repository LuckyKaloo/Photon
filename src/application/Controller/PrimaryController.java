package application.Controller;

import application.Model.Components.*;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import application.Model.Geometry.Segment;
import application.Model.Light.LightComponent;
import application.Model.Light.LightRay;
import application.Model.Light.LightSegment;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.*;
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

    // non FXML stuff
    private GraphicsContext graphicsContext;
    private ArrayList<Component> components;

    private Point startPoint;  // if user selected mirror or absorber, this is the first point of the component (line)
    private final ArrayList<Point> vertices = new ArrayList<>();  // if user selected shape, more than 1 point will be used

    private Component hoveredComponent;  // the component that the user is hovering over
    private Component selectedComponent;  // the component that the user has selected

    private ToggleGroup group;


    private final static int maxDistanceSelect = 3;

    // colors
    private final static Color backgroundColor = Color.rgb(2, 6, 12);
    private final static Color shapeColor = Color.rgb(150, 150, 220);
    private final static Color beamColor = Color.rgb(224, 225, 48);
    private final static Color absorberColor = Color.rgb(93, 231, 48);
    private final static Color mirrorColor = Color.rgb(192, 215, 231);

    private final static Color shapeDrawingColor = Color.rgb(239, 84, 84);

    private final static Color selectedColor = Color.WHITE;
    private final static Color hoveredColor = Color.rgb(210, 210, 210);


    public void start() {
        graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setLineWidth(1.3);
        components = new ArrayList<>();

        // initialising canvas
        borderPane.getStylesheets().add(Objects.requireNonNull(
                getClass().getClassLoader().getResource("application/View/primary.css")).toExternalForm());
        canvas.heightProperty().bind(pane.heightProperty());
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.setScaleY(-1);


        // initialising buttons
        group = new ToggleGroup();
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

        // initialisation methods
        initialiseKeyboardShortcuts();
        initialiseCanvasActions();
        initialiseMenuBar();

        updateCanvas();
    }

    /*
    MENU BAR
     */

    private void initialiseMenuBar() {
        MenuItem save = new MenuItem("Save");
        save.setOnAction(e -> {
            File file = new File("testing.txt");
            try {
                file.delete();
                file.createNewFile();

                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                outputStream.writeObject(components);

                outputStream.flush();
                outputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        menuBar.getMenus().get(0).getItems().add(1, save);

        MenuItem load = new MenuItem("Load");
        load.setOnAction(e -> {
            File file = new File("testing.txt");
            try {
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
                //noinspection unchecked
                components = (ArrayList<Component>) inputStream.readObject();
                updateCanvas();
                inputStream.close();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });
        menuBar.getMenus().get(0).getItems().add(load);
    }

    /*
    KEYBOARD SHORTCUTS
     */

    private void initialiseKeyboardShortcuts() {
        borderPane.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case P -> {
                    System.out.println("P");
                    for (Component component: components) {
                        if (component instanceof Source) {
                            System.out.println(((Source) component).getBeam());
                        }
                    }
                }
                case Z -> {
                    if (components.size() > 0) {
                        components.remove(components.size() - 1);
                        updateCanvas();
                    }
                }
            }
        });
    }

    /*
    CANVAS ACTIONS
     */

    private void initialiseCanvasActions() {
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
            } else {
                selectedComponent = null;

                Point selectedPoint = new Point(e.getX(), e.getY());
                for (Component component : components) {
                    if (component instanceof Shape) {
                        if (((Shape) component).contains(selectedPoint)) {
                            selectedComponent = component;
                        }
                    } else if (component instanceof LineComponent) {
                        if (Point.distance(selectedPoint, ((LineComponent) component).getEdge()) < maxDistanceSelect) {
                            selectedComponent = component;
                        }
                    } else if (component instanceof Source source) {
                        for (LightComponent lightComponent: source.getBeam().getLightComponents()) {
                            if (lightComponent instanceof LightRay lightRay) {
                                if (Point.distance(selectedPoint, lightRay) < maxDistanceSelect * 0.5) {
                                    selectedComponent = component;
                                }
                            } else if (lightComponent instanceof LightSegment lightSegment) {
                                if (Point.distance(selectedPoint, lightSegment) < maxDistanceSelect * 0.5) {
                                    selectedComponent = component;
                                }
                            }
                        }
                    }
                }
            }

            updateCanvas();
        });

        canvas.setOnMouseMoved(e -> {
            hoveredComponent = null;
            if (group.getSelectedToggle() == null) {
                Point hoveredPoint = new Point(e.getX(), e.getY());
                for (Component component : components) {
                    if (component instanceof Shape) {
                        if (((Shape) component).contains(hoveredPoint)) {
                            hoveredComponent = component;
                        }
                    } else if (component instanceof LineComponent) {
                        if (Point.distance(hoveredPoint, ((LineComponent) component).getEdge()) < maxDistanceSelect) {
                            hoveredComponent = component;
                        }
                    } else if (component instanceof Source source) {
                        for (LightComponent lightComponent: source.getBeam().getLightComponents()) {
                            if (lightComponent instanceof LightRay lightRay) {
                                if (Point.distance(hoveredPoint, lightRay) < maxDistanceSelect * 0.5) {
                                    hoveredComponent = component;
                                }
                            } else if (lightComponent instanceof LightSegment lightSegment) {
                                if (Point.distance(hoveredPoint, lightSegment) < maxDistanceSelect * 0.5) {
                                    hoveredComponent = component;
                                }
                            }
                        }
                    }
                }
            }

            updateCanvas(e);
        });
    }

    // drawing fully formed components
    private void updateCanvas() {
        for (Component component: components) {
            if (component instanceof Source) {
                ((Source) component).getBeam().generateBeam(components);
            }
        }

        graphicsContext.setFill(backgroundColor);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Component component: components) {  // draw components
            graphicsContext.beginPath();
            if (component instanceof Shape shape) {
                for (Edge edge: shape.getEdges()) {
                    drawSegment(edge);
                }
            } else if (component instanceof LineComponent lineComponent) {
                drawSegment(lineComponent.getEdge());
            } else if (component instanceof Source source) {
                for (LightComponent lightComponent: source.getBeam().getLightComponents()) {
                    if (lightComponent instanceof LightSegment lightSegment) {
                        drawSegment(lightSegment);
                    } else if (lightComponent instanceof LightRay lightRay) {
                        drawRay(lightRay);
                    }
                }
            }

            if (component == selectedComponent) {
                graphicsContext.setStroke(selectedColor);
            } else if (component == hoveredComponent){
                graphicsContext.setStroke(hoveredColor);
            } else {
                if (component instanceof Shape) {
                    graphicsContext.setStroke(shapeColor);
                } else if (component instanceof Source source) {
                    graphicsContext.setStroke(beamColor);
                } else if (component instanceof Absorber) {
                    graphicsContext.setStroke(absorberColor);
                } else if (component instanceof Mirror) {
                    graphicsContext.setStroke(mirrorColor);
                }
            }

            graphicsContext.stroke();
            graphicsContext.closePath();
        }
    }

    // draw components that are being added
    private void updateCanvas(MouseEvent e) {
        updateCanvas();

        graphicsContext.beginPath();
        Point newPoint = new Point(e.getX(), e.getY());
        if (absorber.isSelected() && startPoint != null) {
            graphicsContext.setStroke(absorberColor);

            if (!startPoint.equals(newPoint)) {
                drawSegment(new Segment(startPoint, newPoint));
            }
        } else if (mirror.isSelected() && startPoint != null){
            graphicsContext.setStroke(mirrorColor);

            if (!startPoint.equals(newPoint)) {
                drawSegment(new Segment(startPoint, newPoint));
            }
        } else if (shape.isSelected() && vertices.size() != 0) {
            graphicsContext.setStroke(shapeDrawingColor);

            ArrayList<Point> points = new ArrayList<>(vertices);
            boolean contains = false;
            for (Point point: points) {
                if (point.equals(newPoint)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                points.add(newPoint);
            }
            ArrayList<Segment> segments = Segment.pointsToSegments(points, false);
            for (Segment segment: segments) {
                drawSegment(segment);
            }
        }
        graphicsContext.stroke();
        graphicsContext.closePath();
    }


    // helper methods to draw the canvas
    private void drawSegment(Segment segment) {
        graphicsContext.moveTo(segment.getStart().X(), segment.getStart().Y());
        graphicsContext.lineTo(segment.getEnd().X(), segment.getEnd().Y());
    }

    private void drawRay(Ray ray) {
        graphicsContext.moveTo(ray.getStart().X(), ray.getStart().Y());
        double endX = ray.getStart().X() + 800 * Math.cos(Math.toRadians(ray.getAngle()));
        double endY = ray.getStart().Y() + 800 * Math.sin(Math.toRadians(ray.getAngle()));
        graphicsContext.lineTo(endX, endY);
    }
}