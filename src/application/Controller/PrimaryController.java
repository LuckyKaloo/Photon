package application.Controller;

import application.Model.Components.*;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import application.Model.Geometry.Segment;
import application.Model.Light.LightComponent;
import application.Model.Light.LightRay;
import application.Model.Light.LightSegment;
import application.Model.Light.Normal;
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
import javafx.scene.shape.ArcType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrimaryController {
    // main buttons and controls
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
    private Canvas canvas;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ListView<Source> sourceListView;
    @FXML
    private ListView<LineComponent> mirrorListView;
    @FXML
    private ListView<LineComponent> absorberListView;
    @FXML
    private ListView<Shape> shapeListView;

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
    @FXML
    private CheckBox shapeVisibility;
    @FXML
    private CheckBox lineComponentVisibility;
    @FXML
    private CheckBox sourceVisibility;
    @FXML
    private TextField shapeName;
    @FXML
    private TextField lineComponentName;
    @FXML
    private TextField sourceName;

    // non FXML stuff
    private GraphicsContext graphicsContext;
    private final ArrayList<Component> components = new ArrayList<>();
    private final ArrayList<Component> removedComponents = new ArrayList<>();
    private boolean componentsModified = false;  // whether a component has been modified -> if has been modified then need to recalculate the beam

    private Point startPoint;  // if user selected mirror or absorber, this is the first point of the component (line)
    private final ArrayList<Point> vertices = new ArrayList<>();  // if user selected shape, more than 1 point will be used

    private Component hoveredComponent;  // the component that the user is hovering over
    private Component selectedComponent;  // the component that the user has selected
    private Point selectedPoint;  // the point that the user has selected

    private boolean rotating = false;  // whether the user is rotating a component using rotate wheel on the canvas
    private boolean rotatable = false;  // whether a component can be rotated now
    private Point rotatePoint;
    private double rotateAngle;  // if the user is rotating a component, the initial angle that they started at


    private final static int maxDistanceSelect = 6;
    private final static int normalWidth = 5;
    private final static double rotateWheelWidth = 50;

    // colors
    private final static Color backgroundColor = Color.rgb(2, 6, 12);
    private final static Color shapeColor = Color.rgb(199, 111, 243);
    private final static Color absorberColor = Color.rgb(126, 252, 137);
    private final static Color mirrorColor = Color.rgb(70, 141, 246);

    private final static Color shapeDrawingColor = Color.rgb(239, 84, 84);
    private final static Color normalColor = shapeColor;

    private final static Color selectedColor = Color.WHITE;
    private final static Color hoveredColor = Color.rgb(210, 210, 210);


    // start method to initialize everything and start the editor
    // cannot use initialize method because the panes are not set to the correct size yet
    // need to wait for the FXML file to be loaded
    public void start() {
        graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setLineWidth(1);

        // initialising canvas
        canvas.heightProperty().set(4000);  // maximum is 8192
        canvas.widthProperty().set(4000);
        canvas.scaleYProperty().set(-1);  // flips the canvas so that 0,0 starts from the bottom :)


        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

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
                FileWriter fileWriter = new FileWriter("testing.phn");
                fileWriter.append("Canvas {\nWidth: ").append(String.valueOf(canvas.getWidth()))
                        .append("\nHeight: ").append(String.valueOf(canvas.getHeight())).append("\n}");
                for (Component component : components) {
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
            FileChooser fileChooser = new FileChooser();
            Stage stage = new Stage();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Photon Files", "*.phn"));
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                components.clear();

                try {
                    String data = Files.readString(Paths.get(selectedFile.toURI()));
                    Pattern pattern = Pattern.compile(".*\\{([^}]|\\n)*}");
                    Matcher matcher = pattern.matcher(data);

                    while (matcher.find()) {
                        try {
                            String line = matcher.group().replaceAll("[ \\t\\r\\f]", "");
                            String[] lines = line.split("\n");
                            if (lines[0].equals("Canvas{")) {
                                if (!Pattern.matches("Width:.*", lines[1]) || !Pattern.matches("Height:.*", lines[2])) {
                                    throw new IllegalArgumentException("Data for canvas is not valid!");
                                }

                                try {
                                    canvas.setWidth(Double.parseDouble(lines[1].substring(6)));
                                    canvas.setHeight(Double.parseDouble(lines[2].substring(7)));
                                } catch (NumberFormatException ex) {
                                    Alert alert = new Alert(Alert.AlertType.ERROR, "Data for canvas is not valid!");
                                    alert.show();
                                }
                            } else {
                                components.add(Component.parseData(matcher.group()));
                                showComponents();
                            }
                        } catch (IllegalArgumentException ex) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
                            alert.show();
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            componentsModified = true;
        });
        menuBar.getMenus().get(0).getItems().add(load);
    }

    /*
    COMPONENT PANEL
     */

    private void showComponents() {
        sourceListView.getItems().clear();
        mirrorListView.getItems().clear();
        absorberListView.getItems().clear();
        shapeListView.getItems().clear();

        for (Component component: components) {
            if (component instanceof Source source) {
                sourceListView.getItems().add(source);
            } else if (component instanceof LineComponent lineComponent) {
                if (lineComponent.getEdge().getType() == Edge.REFLECTOR) {
                    mirrorListView.getItems().add(lineComponent);
                } else {
                    absorberListView.getItems().add(lineComponent);
                }
            } else if (component instanceof Shape shape) {
                shapeListView.getItems().add(shape);
            }
        }
    }


    /*
    INFORMATION PANEL
     */

    private void showInformation() {
        for (Node node : information.getChildren()) {
            node.setVisible(false);
        }

        // show relevant information panels and add the information
        if (selectedComponent instanceof Shape shape) {
            shapeInformation.setVisible(true);
            shapeName.setText(shape.getName());
            shapeRefractiveIndex.setText(String.format("%.2f", shape.getRefractiveIndex()));
            shapeVisibility.setSelected(shape.getVisibility());

            if (selectedPoint != null) {
                shapePointLayoutX.setText(String.format("%.1f", selectedPoint.getX()));
                shapePointLayoutY.setText(String.format("%.1f", selectedPoint.getY()));
            } else {
                shapePointLayoutX.setText("");
                shapePointLayoutY.setText("");
            }
        } else if (selectedComponent instanceof LineComponent lineComponent) {
            lineComponentInformation.setVisible(true);
            lineComponentName.setText(lineComponent.getName());
            lineComponentRotation.setText(String.format("%.2f", lineComponent.getEdge().getAngle()));
            lineComponentVisibility.setSelected(lineComponent.getVisibility());

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

            sourceName.setText(source.getName());
            sourceRotation.setText(String.format("%.2f", source.getBeam().getInitialRay().getAngle()));
            sourceVisibility.setSelected(source.getVisibility());
            sourceColor.setValue(source.getBeam().getColor());
        }
    }

    private void initializeInformationPanel() {
        // setting properties of numeral fields
        setNumberFieldProperties(shapePointLayoutX, i -> {
            if (selectedComponent instanceof Shape shape) {
                selectedPoint.setX(Double.parseDouble(shapePointLayoutX.getText()));
                shape.update();
            }
        });

        setNumberFieldProperties(shapePointLayoutY, i -> {
            if (selectedComponent instanceof Shape shape) {
                selectedPoint.setY(Double.parseDouble(shapePointLayoutY.getText()));
                shape.update();
            }
        });

        setNumberFieldProperties(shapeRefractiveIndex, i -> {
            if (selectedComponent instanceof Shape shape) {
                shape.setRefractiveIndex(Double.parseDouble(shapeRefractiveIndex.getText()));
            }
        });

        setNumberFieldProperties(lineComponentPointLayoutX, i -> {
            if (selectedComponent instanceof LineComponent lineComponent) {
                selectedPoint.setX(Double.parseDouble(lineComponentPointLayoutX.getText()));
                lineComponent.getEdge().updateSegment();
            }
        });

        setNumberFieldProperties(lineComponentPointLayoutY, i -> {
            if (selectedComponent instanceof LineComponent lineComponent) {
                selectedPoint.setY(Double.parseDouble(lineComponentPointLayoutY.getText()));
                lineComponent.getEdge().updateSegment();
            }
        });

        setNumberFieldProperties(lineComponentRotation, i -> {
            if (selectedComponent instanceof LineComponent lineComponent) {
                lineComponent.getEdge().setAngle(Double.parseDouble(lineComponentRotation.getText()));
            }
        });

        setNumberFieldProperties(sourceLayoutX, i -> {
            if (selectedComponent instanceof Source source) {
                source.getBeam().getInitialRay().getStart().setX(Double.parseDouble(sourceLayoutX.getText()));
                source.getBeam().getInitialRay().updateRay();
            }
        });

        setNumberFieldProperties(sourceLayoutY, i -> {
            if (selectedComponent instanceof Source source) {
                source.getBeam().getInitialRay().getStart().setY(Double.parseDouble(sourceLayoutY.getText()));
                source.getBeam().getInitialRay().updateRay();
            }
        });

        setNumberFieldProperties(sourceRotation, i -> {
            if (selectedComponent instanceof Source source) {
                source.getBeam().getInitialRay().setAngle(Double.parseDouble(sourceRotation.getText()));
                source.getBeam().getInitialRay().updateRay();
            }
        });


        // this is a color picker, not a text field so cannot use setTextFieldProperties
        sourceColor.setOnAction(e -> {
            if (selectedComponent instanceof Source source) {
                source.getBeam().setColor(sourceColor.getValue());

                componentsModified = true;
                updateCanvas();
            }
        });

        // setting properties of checkboxes which are similar
        sourceVisibility.selectedProperty().addListener((c, o, o1) -> {
            if (selectedComponent instanceof Source) {
                selectedComponent.setVisibility(o1);
                updateCanvas();
            }
        });

        lineComponentVisibility.selectedProperty().addListener((c, o, o1) -> {
            if (selectedComponent instanceof LineComponent) {
                selectedComponent.setVisibility(o1);
                updateCanvas();
            }
        });

        shapeVisibility.selectedProperty().addListener((c, o, o1) -> {
            if (selectedComponent instanceof Shape) {
                selectedComponent.setVisibility(o1);
                updateCanvas();
            }
        });
        
        // setting the properties of the list views
        setListViewProperties(sourceListView);
        setListViewProperties(mirrorListView);
        setListViewProperties(absorberListView);
        setListViewProperties(shapeListView);

        // setting the properties of the name fields
        setTextFieldProperties(sourceName, i -> {
            if (selectedComponent instanceof Source) {
                selectedComponent.setName(sourceName.getText());
                sourceListView.refresh();
            }
        });

        setTextFieldProperties(lineComponentName, i -> {
            if (selectedComponent instanceof LineComponent) {
                selectedComponent.setName(lineComponentName.getText());
                mirrorListView.refresh();
                absorberListView.refresh();
            }
        });

        setTextFieldProperties(shapeName, i -> {
            if (selectedComponent instanceof Shape) {
                selectedComponent.setName(shapeName.getText());
                shapeListView.refresh();
            }
        });
    }

    // this function sets the properties for a text field based off a general function as most of them are similar
    private void setNumberFieldProperties(TextField textField, Consumer<Integer> function) {
        textField.textProperty().addListener((c, o, o1) -> {
            if (!Pattern.matches("\\d*\\.?\\d*", o1)) {
                textField.setText(o);
            }
        });

        setTextFieldProperties(textField, function);
    }

    private void setTextFieldProperties(TextField textField, Consumer<Integer> function) {
        textField.focusedProperty().addListener((c, o, o1) -> {
            if (!o1) {
                function.accept(0);

                componentsModified = true;
                updateCanvas();
            }
        });

        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                function.accept(0);

                componentsModified = true;
                updateCanvas();
            }
        });
    }

    private <T extends Component> void setListViewProperties(ListView<T> listView) {
        listView.setOnMouseClicked(e -> {
            selectedComponent = listView.getSelectionModel().getSelectedItem();
            selectedPoint = null;
            rotating = false;
            rotatable = false;

            updateCanvas();
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
                        showComponents();
                        componentsModified = true;
                        updateCanvas();
                    }
                }
                case Y -> {
                    if (removedComponents.size() > 0) {
                        components.add(removedComponents.remove(removedComponents.size() - 1));
                        showComponents();
                        componentsModified = true;
                        updateCanvas();
                    }
                }
                case DELETE -> {
                    if (selectedComponent != null) {
                        components.remove(selectedComponent);
                        showComponents();
                        selectedComponent = null;

                        sourceListView.getSelectionModel().clearSelection();
                        mirrorListView.getSelectionModel().clearSelection();
                        absorberListView.getSelectionModel().clearSelection();
                        shapeListView.getSelectionModel().clearSelection();

                        rotatable = false;
                        rotating = false;
                        componentsModified = true;
                        updateCanvas();
                    }
                }
                case SHIFT -> scrollPane.setPannable(true);
            }
        });

        borderPane.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.SHIFT) {
                scrollPane.setPannable(false);
            }
        });
    }

    /*
    CANVAS ACTIONS
     */

    private void initialiseCanvasActions() {
        /*
        mouse pressed can do quite a few things:
        1. add a new component (depending on which button is selected)
        2. select a point of a component to be modified or moved
        3. select a component to be translated or rotated (rotated only if it is a line component or source)
         */
        canvas.setOnMousePressed(e -> {
            // adding new components
            if (source.isSelected()) {
                selectedPoint = null;
                selectedComponent = new Source(new Point(e.getX(), e.getY()));
                components.add(selectedComponent);
                showComponents();
                sourceListView.getSelectionModel().select((Source) selectedComponent);

                removedComponents.clear();

                rotatable = false;
                rotating = false;
                componentsModified = true;
            } else if (mirror.isSelected() || absorber.isSelected()) {
                selectedPoint = null;
                if (startPoint == null) {
                    startPoint = new Point(e.getX(), e.getY());
                } else {
                    if (mirror.isSelected()) {
                        selectedComponent = new LineComponent(startPoint, new Point(e.getX(), e.getY()), Edge.REFLECTOR);
                    } else {
                        selectedComponent = new LineComponent(startPoint, new Point(e.getX(), e.getY()), Edge.ABSORBER);
                    }
                    components.add(selectedComponent);
                    showComponents();
                    if (mirror.isSelected()) {
                        mirrorListView.getSelectionModel().select((LineComponent) selectedComponent);
                    } else {
                        absorberListView.getSelectionModel().select((LineComponent) selectedComponent);
                    }

                    removedComponents.clear();
                    startPoint = null;

                    lineComponentPointLayoutX.setDisable(true);
                    lineComponentPointLayoutY.setDisable(true);

                    rotatable = false;
                    rotating = false;
                    componentsModified = true;
                }
            } else if (shape.isSelected()) {
                selectedPoint = null;
                Point selectedPoint = new Point(e.getX(), e.getY());
                if (vertices.size() != 0 && selectedPoint.equals(vertices.get(0), 5)) {
                    selectedComponent = new Shape(1.5, vertices);
                    components.add(selectedComponent);
                    showComponents();
                    shapeListView.getSelectionModel().select((Shape) selectedComponent);

                    removedComponents.clear();
                    vertices.clear();

                    shapePointLayoutX.setDisable(true);
                    shapePointLayoutY.setDisable(true);

                    componentsModified = true;
                } else {
                    vertices.add(selectedPoint);
                }

                rotatable = false;
                rotating = false;
            // selecting components and making them modifiable
            } else {
                selectedPoint = findClickedPoint(e);

                // if a component is rotatable, check if the user has pressed on the wheel to begin rotating it
                if (rotatable) {
                    if (selectedPoint == null) {
                        double dx = e.getX() - rotatePoint.getX();
                        double dy = e.getY() - rotatePoint.getY();
                        double distance = Math.sqrt(dx * dx + dy * dy);

                        // checking that the user has pressed inside the wheel
                        if (Math.abs(distance - rotateWheelWidth / 2) < 5) {
                            double relativeAngle = Ray.angleTo(rotatePoint, new Point(e.getX(), e.getY()));
                            if (selectedComponent instanceof Source source) {
                                rotateAngle = source.getBeam().getInitialRay().getAngle() - relativeAngle;
                            } else if (selectedComponent instanceof LineComponent lineComponent) {
                                rotateAngle = lineComponent.getEdge().getAngle() - relativeAngle;
                            } else {
                                throw new IllegalStateException("Rotating but selected component is not a source or line component");
                            }
                            rotating = true;
                            componentsModified = true;
                        }
                    // user needs to not have selected a point in order to rotate something
                    } else {
                        rotatable = false;
                        rotating = false;
                    }
                }

                // user is not rotating a component -> might have selected a new component
                if (!rotating) {
                    selectedComponent = findMousedComponent(e);
                    if (selectedComponent instanceof Source || selectedComponent instanceof LineComponent) {  // component is selected for rotation
                        if (selectedComponent instanceof Source && selectedPoint != null) {
                            rotatable = true;
                            rotatePoint = selectedPoint;
                        } else if (selectedComponent instanceof LineComponent lineComponent && selectedPoint == null) {
                            rotatable = true;
                            rotatePoint = lineComponent.getEdge().midpoint();
                        }
                    } else {
                        rotatable = false;
                        rotating = false;
                    }

                    // resetting the selected component in the list views
                    sourceListView.getSelectionModel().clearSelection();
                    mirrorListView.getSelectionModel().clearSelection();
                    absorberListView.getSelectionModel().clearSelection();
                    shapeListView.getSelectionModel().clearSelection();

                    if (selectedComponent instanceof Source source) {
                        sourceListView.getSelectionModel().select(source);
                    } else if (selectedComponent instanceof LineComponent lineComponent) {
                        if (lineComponent.getEdge().getType() == Edge.REFLECTOR) {
                            mirrorListView.getSelectionModel().select(lineComponent);
                        } else {
                            absorberListView.getSelectionModel().select(lineComponent);
                        }
                    } else if (selectedComponent instanceof Shape shape) {
                        shapeListView.getSelectionModel().select(shape);
                    }
                }
                showInformation();
            }

            updateCanvas();
        });

        canvas.setOnMouseMoved(e -> {
            hoveredComponent = findMousedComponent(e);
            updateCanvas(e);
        });

        /*
        mouse dragged can either:
        1. drag a point of a component
        2. rotate a component
        3. translate a component
         */
        canvas.setOnMouseDragged(e -> {
            if (selectedPoint != null) {
                selectedPoint.setX(e.getX());
                selectedPoint.setY(e.getY());
                selectedComponent.update();
                componentsModified = true;
            } else if (rotating) {
                // change the angle of the selected component relative to where the user's start angle was
                double changeAngle = Ray.angleTo(rotatePoint, new Point(e.getX(), e.getY()));
                if (selectedComponent instanceof Source source) {
                    source.getBeam().getInitialRay().setAngle(rotateAngle + changeAngle);
                    source.update();
                } else if (selectedComponent instanceof LineComponent lineComponent) {
                    lineComponent.getEdge().setAngle(rotateAngle + changeAngle);
                    lineComponent.update();
                } else {
                    throw new IllegalStateException("Rotating a component that is not a source or line component");
                }
                componentsModified = true;
            }
            updateCanvas(e);
        });

        canvas.setOnMouseReleased(e -> rotating = false);
    }

    // finding the clicked or hovered component
    private Component findMousedComponent(MouseEvent e) {
        Component mousedComponent = null;
        ArrayList<Component> clickedComponents = new ArrayList<>();
        HashMap<Component, Double> distances = new HashMap<>();
        int highest = 0;  // shape -> 0, line component -> 1, source -> 2

        Point mousedPoint = new Point(e.getX(), e.getY());
        for (Component component : components) {
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
                for (LightComponent lightComponent : source.getBeam().getLightComponents()) {
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

        // components have a hierarchy to them because it's better to select a beam than a shape on the same point
        switch (highest) {
            case 0 -> {  // component is a shape
                int highestLayer = -1;
                for (Component component : clickedComponents) {
                    // finding the shape with the highest layer
                    if (component instanceof Shape shape) {
                        if (shape.getLayer() > highestLayer) {
                            mousedComponent = shape;
                            highestLayer = shape.getLayer();
                        }
                    }
                }
            }
            case 1 -> {  // component is a line component
                double minDistance = 0;
                // finding the closest line component to the mouse
                for (Component component : clickedComponents) {
                    if (component instanceof LineComponent) {
                        if (minDistance == 0 || distances.get(component) < minDistance) {
                            mousedComponent = component;
                            minDistance = distances.get(component);
                        }
                    }
                }
            }
            case 2 -> {  // component is a source
                double minDistance = 0;
                for (Component component : clickedComponents) {
                    // finding the closest source to the mouse
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
        Component clickedComponent = findMousedComponent(e);
        Point clickedPoint = new Point(e.getX(), e.getY());
        if (clickedComponent instanceof Shape shape) {
            for (Point point : shape.getVertexes()) {
                if (Point.distance(point, clickedPoint) < maxDistanceSelect) {
                    shapePointLayoutX.setDisable(false);
                    shapePointLayoutY.setDisable(false);
                    lineComponentPointLayoutX.setDisable(true);
                    lineComponentPointLayoutY.setDisable(true);
                    return point;
                }
            }
        } else if (clickedComponent instanceof LineComponent lineComponent) {
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
        } else if (clickedComponent instanceof Source source) {
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

        if (componentsModified) {
            for (Component component : components) {
                if (component instanceof Source source) {
                    if (source.getVisibility()) {
                        source.getBeam().generateBeam(components);
                    }
                }
            }
            componentsModified = false;
        }

        graphicsContext.setFill(backgroundColor);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Component component: components) {  // draw components
            // do not draw the component if it is not visible
            if (component.getVisibility()) {
                // drawing the shape of the component
                graphicsContext.beginPath();
                if (component instanceof Shape shape) {
                    for (Edge edge : shape.getEdges()) {
                        drawSegment(edge);
                    }
                } else if (component instanceof LineComponent lineComponent) {
                    drawSegment(lineComponent.getEdge());
                } else if (component instanceof Source source) {
                    for (LightComponent lightComponent : source.getBeam().getLightComponents()) {
                        if (lightComponent instanceof LightSegment lightSegment) {
                            drawSegment(lightSegment);
                        } else if (lightComponent instanceof LightRay lightRay) {
                            drawRay(lightRay);
                        }
                    }
                }

                // setting the color of the component to be drawn
                if (component == selectedComponent) {
                    graphicsContext.setStroke(selectedColor);
                } else if (component == hoveredComponent) {
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

                // drawing the normals if the component is a source
                if (component instanceof Source source) {
                    graphicsContext.setStroke(normalColor);
                    graphicsContext.setLineDashes(2);
                    for (LightComponent lightComponent : source.getBeam().getLightComponents()) {
                        Normal normal = lightComponent.getNormal();
                        if (normal != null) {
                            graphicsContext.beginPath();
                            drawNormal(normal.edge(), normal.intersection());
                            graphicsContext.stroke();
                        }
                    }
                    graphicsContext.closePath();
                    graphicsContext.setLineDashes(0);
                }
            }

            // drawing the rotate-wheel if the selected component is a source or a line component
            if (rotatable) {
                graphicsContext.setStroke(shapeColor);
                graphicsContext.beginPath();

                Point point;
                if (selectedComponent instanceof Source source) {
                    point = source.getBeam().getInitialRay().getStart();
                } else if (selectedComponent instanceof LineComponent lineComponent) {
                    point = lineComponent.getEdge().midpoint();
                } else {
                    throw new IllegalArgumentException("Selected component for rotation is not a source or a line component");
                }

                graphicsContext.setLineWidth(2);
                graphicsContext.strokeArc(point.getX() - rotateWheelWidth / 2, point.getY() - rotateWheelWidth / 2,
                        rotateWheelWidth, rotateWheelWidth, 0, 360, ArcType.OPEN);
                graphicsContext.closePath();
                graphicsContext.setLineWidth(1);
            }
        }
    }

    // draw components that are being added
    private void updateCanvas(MouseEvent e) {
        updateCanvas();

        // if a component is being drawn, shows the component before it is confirmed
        graphicsContext.beginPath();
        Point newPoint = new Point(e.getX(), e.getY());
        if (absorber.isSelected() && startPoint != null) {
            graphicsContext.setStroke(absorberColor);

            if (!startPoint.equals(newPoint)) {
                drawSegment(new Segment(startPoint, newPoint));
            }
        } else if (mirror.isSelected() && startPoint != null) {
            graphicsContext.setStroke(mirrorColor);

            if (!startPoint.equals(newPoint)) {
                drawSegment(new Segment(startPoint, newPoint));
            }
        } else if (shape.isSelected() && vertices.size() != 0) {
            graphicsContext.setStroke(shapeDrawingColor);

            ArrayList<Point> points = new ArrayList<>(vertices);
            boolean contains = false;
            for (Point point : points) {
                if (point.equals(newPoint)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                points.add(newPoint);
            }
            ArrayList<Segment> segments = Segment.pointsToSegments(points, false);
            for (Segment segment : segments) {
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

    private void drawNormal(Segment edge, Point intersection) {
        // the coordinates of the start and end points of the normal displayed
        double startX = intersection.getX() + normalWidth * Math.cos(Math.toRadians(edge.getAngle() + 90));
        double startY = intersection.getY() + normalWidth * Math.sin(Math.toRadians(edge.getAngle() + 90));
        double endX = intersection.getX() - normalWidth * Math.cos(Math.toRadians(edge.getAngle() + 90));
        double endY = intersection.getY() - normalWidth * Math.sin(Math.toRadians(edge.getAngle() + 90));

        // drawing a dashed line to represent the normal
        graphicsContext.moveTo(startX, startY);
        graphicsContext.lineTo(endX, endY);
    }
}