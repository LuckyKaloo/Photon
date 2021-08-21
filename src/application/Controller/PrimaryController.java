package application.Controller;

import application.Model.Components.*;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import application.Model.Geometry.Segment;
import application.Model.Light.LightComponent;
import application.Model.Light.LightRay;
import application.Model.Light.LightSegment;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import javafx.scene.paint.Color;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PrimaryController {
    @FXML
    private BorderPane borderPane;
    @FXML
    private MenuBar menuBar;
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

    // information panel
    @FXML
    private Pane information;
    @FXML
    private AnchorPane shapeInformation;
    @FXML
    private TextField shapePointLayoutX;
    @FXML
    private TextField shapePointLayoutY;
    @FXML
    private TextField shapeRefractiveIndex;
    @FXML
    private AnchorPane lineComponentInformation;
    @FXML
    private TextField lineComponentPointLayoutX;
    @FXML
    private TextField lineComponentPointLayoutY;
    @FXML
    private TextField lineComponentRotation;
    @FXML
    private AnchorPane sourceInformation;
    @FXML
    private TextField sourceLayoutX;
    @FXML
    private TextField sourceLayoutY;
    @FXML
    private TextField sourceRotation;
    @FXML
    private ColorPicker sourceColor;

    // non FXML stuff
    private GraphicsContext graphicsContext;
    private ArrayList<Component> components;
    private ArrayList<Component> removedComponents;

    private Point startPoint;  // if user selected mirror or absorber, this is the first point of the component (line)
    private final ArrayList<Point> vertices = new ArrayList<>();  // if user selected shape, more than 1 point will be used

    private Component hoveredComponent;  // the component that the user is hovering over
    private Component selectedComponent;  // the component that the user has selected
    private Point selectedPoint;  // the point that the user has selected


    private final static int maxDistanceSelect = 6;

    // colors
    private final static Color backgroundColor = Color.rgb(2, 6, 12);
    private final static Color shapeColor = Color.rgb(199, 111, 243);
    private final static Color absorberColor = Color.rgb(126, 252, 137);
    private final static Color mirrorColor = Color.rgb(70, 141, 246);

    private final static Color shapeDrawingColor = Color.rgb(239, 84, 84);

    private final static Color selectedColor = Color.WHITE;
    private final static Color hoveredColor = Color.rgb(210, 210, 210);


    public void start() {
        graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setLineWidth(1.3);
        components = new ArrayList<>();
        removedComponents = new ArrayList<>();

        // initialising canvas
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

        // initialisation methods
        initialiseKeyboardShortcuts();
        initialiseCanvasActions();
        initialiseMenuBar();
        initializeInformationPanel();

        updateCanvas();
    }

    /*
    MENU BAR
     */

    private void initialiseMenuBar() {
        MenuItem save = new MenuItem("Save");
        save.setOnAction(e -> {
            try {
                FileWriter fileWriter = new FileWriter("testing.txt");
                for (Component component: components) {
                    fileWriter.append(component.toData());
                }
                fileWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        menuBar.getMenus().get(0).getItems().add(1, save);

        MenuItem load = new MenuItem("Load");
        load.setOnAction(e -> {
            components.clear();

            try {
                String data = Files.readString(Paths.get("testing.txt"));
                Pattern pattern = Pattern.compile(".*\\{([^}]|\\n)*}");
                Matcher matcher = pattern.matcher(data);

                while (matcher.find()) {
                    try {
                        components.add(Component.parseData(matcher.group()));
                    } catch (IllegalArgumentException ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
                        alert.show();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        menuBar.getMenus().get(0).getItems().add(load);
    }

    /*
    INFORMATION PANEL
     */

    private void showInformation() {
        for (Node node: information.getChildren()) {
            node.setVisible(false);
        }

        // show relevant information panels and add the information
        if (selectedComponent instanceof Shape shape) {
            shapeInformation.setVisible(true);
            shapeRefractiveIndex.setText(String.format("%.2f", shape.getRefractiveIndex()));

            if (selectedPoint != null) {
                shapePointLayoutX.setText(String.format("%.1f", selectedPoint.getX()));
                shapePointLayoutY.setText(String.format("%.1f", selectedPoint.getY()));
            } else {
                shapePointLayoutX.setText("");
                shapePointLayoutY.setText("");
            }
        } else if (selectedComponent instanceof LineComponent lineComponent) {
            lineComponentInformation.setVisible(true);
            lineComponentRotation.setText(String.format("%.2f", lineComponent.getEdge().getAngle()));

            if (selectedPoint != null) {
                lineComponentPointLayoutX.setText(String.format("%.1f", selectedPoint.getX()));
                lineComponentPointLayoutY.setText(String.format("%.1f", selectedPoint.getY()));
            } else {
                lineComponentPointLayoutX.setText("");
                lineComponentPointLayoutY.setText("");
            }
        } else if (selectedComponent instanceof Source source) {
            sourceInformation.setVisible(true);
            sourceLayoutX.setText(String.format("%.1f", source.getBeam().getInitialRay().getStart().getX()));
            sourceLayoutY.setText(String.format("%.1f", source.getBeam().getInitialRay().getStart().getY()));
            sourceRotation.setText(String.format("%.2f", source.getBeam().getInitialRay().getAngle()));
            sourceColor.setValue(source.getBeam().getColor());
        }
    }

    private void initializeInformationPanel() {
        // shape information
        shapePointLayoutX.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    if (selectedComponent instanceof Shape shape) {
                        selectedPoint.setX(Double.parseDouble(shapePointLayoutX.getText()));
                        shape.update();
                        updateCanvas();
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });

        shapePointLayoutY.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    if (selectedComponent instanceof Shape shape) {
                        selectedPoint.setY(Double.parseDouble(shapePointLayoutY.getText()));
                        shape.update();
                        updateCanvas();
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });

        shapeRefractiveIndex.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    if (selectedComponent instanceof Shape shape) {
                        shape.setRefractiveIndex(Double.parseDouble(shapeRefractiveIndex.getText()));
                        updateCanvas();
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });


        // line component information
        lineComponentPointLayoutX.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    if (selectedComponent instanceof LineComponent lineComponent) {
                        selectedPoint.setX(Double.parseDouble(lineComponentPointLayoutX.getText()));
                        lineComponent.getEdge().updateSegment();
                        updateCanvas();
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });

        lineComponentPointLayoutY.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    if (selectedComponent instanceof LineComponent lineComponent) {
                        selectedPoint.setY(Double.parseDouble(lineComponentPointLayoutY.getText()));
                        lineComponent.getEdge().updateSegment();
                        updateCanvas();
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });

        lineComponentRotation.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    if (selectedComponent instanceof LineComponent lineComponent) {
                        lineComponent.getEdge().setAngle(Double.parseDouble(lineComponentRotation.getText()));
                        updateCanvas();
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });


        // source information
        sourceLayoutX.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    if (selectedComponent instanceof Source source) {
                        source.getBeam().getInitialRay().getStart().setX(Double.parseDouble(sourceLayoutX.getText()));
                        source.getBeam().getInitialRay().updateRay();
                        updateCanvas();
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });

        sourceLayoutY.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    if (selectedComponent instanceof Source source) {
                        source.getBeam().getInitialRay().getStart().setY(Double.parseDouble(sourceLayoutY.getText()));
                        source.getBeam().getInitialRay().updateRay();
                        updateCanvas();
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });

        sourceRotation.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    if (selectedComponent instanceof Source source) {
                        source.getBeam().getInitialRay().setAngle(Double.parseDouble(sourceRotation.getText()));
                        source.getBeam().getInitialRay().updateRay();
                        updateCanvas();
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });

        sourceColor.setOnAction(e -> {
            if (selectedComponent instanceof Source source) {
                source.getBeam().setColor(sourceColor.getValue());
                updateCanvas();
            }
        });
    }

    /*
    KEYBOARD SHORTCUTS
     */

    private void initialiseKeyboardShortcuts() {
        borderPane.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case Z -> {
                    if (components.size() > 0) {
                        removedComponents.add(components.remove(components.size() - 1));
                        updateCanvas();
                    }
                }
                case Y -> {
                    if (removedComponents.size() > 0) {
                        components.add(removedComponents.remove(removedComponents.size()-1));
                        updateCanvas();
                    }
                }
                case DELETE -> {
                    if (selectedComponent != null) {
                        components.remove(selectedComponent);
                        selectedComponent = null;
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
        canvas.setOnMousePressed(e -> {
            if (source.isSelected()) {
                selectedPoint = null;
                selectedComponent = new Source(new Point(e.getX(), e.getY()));
                components.add(selectedComponent);
                removedComponents.clear();
            } else if (absorber.isSelected()) {
                selectedPoint = null;
                if (startPoint == null) {
                    startPoint = new Point(e.getX(), e.getY());
                } else {
                    selectedComponent = new LineComponent(startPoint, new Point(e.getX(), e.getY()), Edge.ABSORBER);
                    components.add(selectedComponent);
                    removedComponents.clear();
                    startPoint = null;

                    lineComponentPointLayoutX.setDisable(true);
                    lineComponentPointLayoutY.setDisable(true);
                }
            } else if (mirror.isSelected()){
                selectedPoint = null;
                if (startPoint == null) {
                    startPoint = new Point(e.getX(), e.getY());
                } else {
                    selectedComponent = new LineComponent(startPoint, new Point(e.getX(), e.getY()), Edge.REFLECTOR);
                    components.add(selectedComponent);
                    removedComponents.clear();
                    startPoint = null;

                    lineComponentPointLayoutX.setDisable(true);
                    lineComponentPointLayoutY.setDisable(true);
                }
            } else if (shape.isSelected()) {
                selectedPoint = null;
                Point selectedPoint = new Point(e.getX(), e.getY());
                if (vertices.size() != 0 && selectedPoint.equals(vertices.get(0), 5)) {
                    selectedComponent = new Shape(1.5, vertices);
                    components.add(selectedComponent);
                    removedComponents.clear();
                    vertices.clear();

                    shapePointLayoutX.setDisable(true);
                    shapePointLayoutY.setDisable(true);
                } else {
                    vertices.add(selectedPoint);
                }
            } else {
                selectedComponent = findMousedComponent(e);
                selectedPoint = findClickedPoint(e);
                showInformation();
            }

            updateCanvas();
        });

        canvas.setOnMouseMoved(e -> {
            hoveredComponent = findMousedComponent(e);
            updateCanvas(e);
        });

        canvas.setOnMouseDragged(e -> {
            if (selectedPoint != null) {
                selectedPoint.setX(e.getX());
                selectedPoint.setY(e.getY());
                selectedComponent.update();
            }
            updateCanvas(e);
        });
    }

    // finding the clicked component
    private Component findMousedComponent(MouseEvent e) {
        Component mousedComponent = null;
        ArrayList<Component> clickedComponents = new ArrayList<>();
        HashMap<Component, Double> distances = new HashMap<>();
        int highest = 0;  // shape -> 0, line component -> 1, source -> 2

        Point mousedPoint = new Point(e.getX(), e.getY());
        for (Component component: components) {
            if (component instanceof Shape shape) {
                if (shape.containsMouse(mousedPoint)) {
                    clickedComponents.add(component);
                }
            } else if (component instanceof LineComponent lineComponent) {
                double distance = Point.distance(mousedPoint, lineComponent.getEdge());
                if (distance < maxDistanceSelect) {
                    clickedComponents.add(component);
                    distances.put(component, distance);
                    highest = Math.max(highest, 1);
                }
            } else if (component instanceof Source source) {
                for (LightComponent lightComponent: source.getBeam().getLightComponents()) {
                    if (lightComponent instanceof LightRay lightRay) {
                        double distance = Point.distance(mousedPoint, lightRay);
                        if (distance < maxDistanceSelect * 0.5) {
                            clickedComponents.add(component);
                            distances.put(component, distance);
                            highest = 2;
                            break;
                        }
                    } else if (lightComponent instanceof LightSegment lightSegment) {
                        double distance = Point.distance(mousedPoint, lightSegment);
                        if (distance < maxDistanceSelect * 0.5) {
                            clickedComponents.add(component);
                            distances.put(component, distance);
                            highest = 2;
                            break;
                        }
                    }
                }
            }
        }

        switch (highest) {
            case 0 -> {
                int highestLayer = -1;
                for (Component component: clickedComponents) {
                    if (component instanceof Shape shape) {
                        if (shape.getLayer() > highestLayer) {
                            mousedComponent = shape;
                            highestLayer = shape.getLayer();
                        }
                    }
                }
            }
            case 1 -> {
                double minDistance = 0;
                for (Component component: clickedComponents) {
                    if (component instanceof LineComponent) {
                        if (minDistance == 0 || distances.get(component) < minDistance) {
                            mousedComponent = component;
                            minDistance = distances.get(component);
                        }
                    }
                }
            } case 2 -> {
                double minDistance = 0;
                for (Component component: clickedComponents) {
                    if (component instanceof Source) {
                        if (minDistance == 0 || distances.get(component) < minDistance) {
                            mousedComponent = component;
                            minDistance = distances.get(component);
                        }
                    }
                }
            }
        }

        return mousedComponent;
    }

    private Point findClickedPoint(MouseEvent e) {
        Point clickedPoint = new Point(e.getX(), e.getY());
        if (selectedComponent instanceof Shape shape) {
            for (Point point: shape.getVertexes()) {
                if (Point.distance(point, clickedPoint) < maxDistanceSelect) {
                    shapePointLayoutX.setDisable(false);
                    shapePointLayoutY.setDisable(false);
                    lineComponentPointLayoutX.setDisable(true);
                    lineComponentPointLayoutY.setDisable(true);
                    return point;
                }
            }
        } else if (selectedComponent instanceof LineComponent lineComponent) {
            Point point = null;
            if (Point.distance(lineComponent.getEdge().getStart(), clickedPoint) < maxDistanceSelect) {
                point = lineComponent.getEdge().getStart();
            } else if (Point.distance(lineComponent.getEdge().getEnd(), clickedPoint) < maxDistanceSelect) {
                point = lineComponent.getEdge().getEnd();
            }

            if (point != null) {
                shapePointLayoutX.setDisable(true);
                shapePointLayoutY.setDisable(true);
                lineComponentPointLayoutX.setDisable(false);
                lineComponentPointLayoutY.setDisable(false);
                return point;
            }
        } else if (selectedComponent instanceof Source source) {
            if (Point.distance(source.getBeam().getInitialRay().getStart(), clickedPoint) < maxDistanceSelect) {
                shapePointLayoutX.setDisable(true);
                shapePointLayoutY.setDisable(true);
                lineComponentPointLayoutX.setDisable(true);
                lineComponentPointLayoutY.setDisable(true);
                return source.getBeam().getInitialRay().getStart();
            }
        }

        shapePointLayoutX.setDisable(true);
        shapePointLayoutY.setDisable(true);
        lineComponentPointLayoutX.setDisable(true);
        lineComponentPointLayoutY.setDisable(true);
        return null;
    }

    // drawing fully formed components
    private void updateCanvas() {
        showInformation();

        for (Component component: components) {
            if (component instanceof Source source) {
                source.getBeam().generateBeam(components);
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
                    graphicsContext.setStroke(source.getBeam().getColor());
                } else if (component instanceof LineComponent lineComponent) {
                    if (lineComponent.getEdge().getType() == Edge.ABSORBER) {
                        graphicsContext.setStroke(absorberColor);
                    } else if (lineComponent.getEdge().getType() == Edge.REFLECTOR) {
                        graphicsContext.setStroke(mirrorColor);
                    }
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
        graphicsContext.moveTo(segment.getStart().getX(), segment.getStart().getY());
        graphicsContext.lineTo(segment.getEnd().getX(), segment.getEnd().getY());
    }

    private void drawRay(Ray ray) {
        graphicsContext.moveTo(ray.getStart().getX(), ray.getStart().getY());
        double endX = ray.getStart().getX() + 2000 * Math.cos(Math.toRadians(ray.getAngle()));
        double endY = ray.getStart().getY() + 2000 * Math.sin(Math.toRadians(ray.getAngle()));
        graphicsContext.lineTo(endX, endY);
    }
}