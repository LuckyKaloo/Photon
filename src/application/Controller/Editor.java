package application.Controller;

import application.Model.Components.*;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import application.Model.Geometry.Segment;
import application.Model.Light.LightComponent;
import application.Model.Light.LightRay;
import application.Model.Light.LightSegment;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Editor {
    @FXML
    private BorderPane borderPane;
    @FXML
    private MenuBar menuBar;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Canvas canvas;
    @FXML
    private Pane information;
    @FXML
    private HBox nameInformation;
    @FXML
    private MFXTextField nameTextField;
    @FXML
    private AnchorPane shapeInformation;
    @FXML
    private MFXTextField shapeLayoutX;
    @FXML
    private MFXTextField shapeLayoutY;
    @FXML
    private MFXTextField shapeRefractiveIndex;
    @FXML
    private AnchorPane lineComponentInformation;
    @FXML
    private MFXTextField lineComponentLayoutX;
    @FXML
    private MFXTextField lineComponentLayoutY;
    @FXML
    private MFXTextField lineComponentRotation;
    @FXML
    private AnchorPane sourceInformation;
    @FXML
    private MFXTextField sourceLayoutX;
    @FXML
    private MFXTextField sourceLayoutY;
    @FXML
    private MFXTextField sourceRotation;
    @FXML
    private ColorPicker sourceColorPicker;
    @FXML
    private ToggleButton source;
    @FXML
    private ToggleButton mirror;
    @FXML
    private ToggleButton absorber;
    @FXML
    private ToggleButton shape;
    @FXML
    private Accordion accordion;
    @FXML
    private ListView<GridPane> sourceListView;
    @FXML
    private ListView<GridPane> mirrorListView;
    @FXML
    private ListView<GridPane> absorberListView;
    @FXML
    private ListView<GridPane> shapeListView;
    @FXML
    private Label fileName;
    @FXML
    private Region topRegion;
    @FXML
    private Region leftRegion;
    @FXML
    private Region centerRegion;
    @FXML
    private Region rightRegion;
    @FXML
    private Region bottomRegion;
    @FXML
    private StackPane topPanel;
    @FXML
    private StackPane leftPanel;
    @FXML
    private StackPane centerPanel;
    @FXML
    private StackPane rightPanel;
    @FXML
    private StackPane bottomPanel;

    // non FXML stuff
    private GraphicsContext graphicsContext;
    private final ArrayList<Component> components = new ArrayList<>();
    private final ArrayList<Component> visibleComponents = new ArrayList<>();
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

    private boolean translating = false;
    private Component initialTranslateComponent; // the initial position of the component currently being translated
    private Point translatePoint; // the point that the user started translating from

    private final ArrayList<File> recentFiles = new ArrayList<>();
    private File currentFile; // the current file that the user has opened

    private final HashMap<GridPane, Component> gridPaneComponentHashMap = new HashMap<>();
    private final HashMap<Component, GridPane> componentGridPaneHashMap = new HashMap<>();
    private final HashMap<Component, Text> componentTextHashMap = new HashMap<>();

    private final HashMap<String, StringProperty> keybindNamePropertyMap = new HashMap<>();

    private final StringProperty css = new SimpleStringProperty();

    private final Menu openRecent = new Menu();
    private boolean showingTutorial = false;

    private final static int MAX_DISTANCE_SELECT = 6;
    private final static int NORMAL_WIDTH = 5;
    private final static double ROTATE_WHEEL_WIDTH = 50;

    private final static int COMPONENT_BUTTON_SIZE = 60;


    // start method to initialize everything and start the editor
    // cannot use initialize method because the panes are not set to the correct size yet
    // need to wait for the FXML file to be loaded
    public void start() {
        borderPane.styleProperty().bind(css);
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
            source.setGraphic(new ImageView(new Image(new FileInputStream(System.getProperty("user.dir") + "/src/application/Resources/images/Source.png"),
                    COMPONENT_BUTTON_SIZE, COMPONENT_BUTTON_SIZE, false, false)));
            absorber.setGraphic(new ImageView(new Image(new FileInputStream(System.getProperty("user.dir") + "/src/application/Resources/images/Absorber.png"),
                    COMPONENT_BUTTON_SIZE, COMPONENT_BUTTON_SIZE, false, false)));
            mirror.setGraphic(new ImageView(new Image(new FileInputStream(System.getProperty("user.dir") + "/src/application/Resources/images/Mirror.png"),
                    COMPONENT_BUTTON_SIZE, COMPONENT_BUTTON_SIZE, false, false)));
            shape.setGraphic(new ImageView(new Image(new FileInputStream(System.getProperty("user.dir") + "/src/application/Resources/images/Shape.png"),
                    COMPONENT_BUTTON_SIZE, COMPONENT_BUTTON_SIZE, false, false)));
        } catch (FileNotFoundException ignored) {}

        // loading files
        try {
            Scanner scanner = new Scanner(new File(System.getProperty("user.dir") + "/src/application/Resources/recent_files.txt"));
            while (scanner.hasNextLine()) {
                recentFiles.add(new File(scanner.nextLine()));
            }
            scanner.close();
        } catch (FileNotFoundException ignored) {}

        // initialisation methods
        initialiseKeyboardShortcuts();
        initialiseCanvasActions();
        initialiseMenuBar();
        initializeInformationPanel();
        initializeTutorial();

        componentsModified = true;
        updateCanvas();
    }

    public void setCss(String css) {
        this.css.setValue(css);
    }

    /*
    MENU BAR
     */

    private void initialiseMenuBar() {
        menuBar.getMenus().clear();
        int fileWidth = 180;
        Menu file = new Menu("File");
        menuBar.getMenus().add(file);

        MenuItem newFile = new MenuItem();
        keybindNamePropertyMap.put("newFile", new SimpleStringProperty());
        newFile.setGraphic(menuFormatting("New", "newFile", fileWidth));
        newFile.setOnAction(e -> newFile());
        file.getItems().add(newFile);

        MenuItem save = new MenuItem();
        keybindNamePropertyMap.put("save", new SimpleStringProperty());
        save.setGraphic(menuFormatting("Save", "save", fileWidth));
        save.setOnAction(e -> save());
        file.getItems().add(save);

        MenuItem saveAs = new MenuItem();
        keybindNamePropertyMap.put("saveAs", new SimpleStringProperty());
        saveAs.setGraphic(menuFormatting("Save As", "saveAs", fileWidth));
        saveAs.setOnAction(e -> saveAs());
        file.getItems().add(1, saveAs);

        MenuItem open = new MenuItem();
        keybindNamePropertyMap.put("open", new SimpleStringProperty());
        open.setGraphic(menuFormatting("Open", "open", fileWidth));
        open.setOnAction(e -> open());
        file.getItems().add(open);

        keybindNamePropertyMap.put("openRecent", new SimpleStringProperty());
        openRecent.setGraphic(menuFormatting("Open Recent", "openRecent", fileWidth));
        file.getItems().add(openRecent);

        MenuItem settings = new MenuItem();
        keybindNamePropertyMap.put("settings", new SimpleStringProperty());
        settings.setGraphic(menuFormatting("Settings", "settings", fileWidth));
        settings.setOnAction(e -> Main.showSettings());
        file.getItems().add(settings);


        Menu help = new Menu("Help");
        menuBar.getMenus().add(help);

        MenuItem showTutorial = new MenuItem();
        keybindNamePropertyMap.put("showTutorial", new SimpleStringProperty());
        showTutorial.setGraphic(menuFormatting("Show Tutorial", "showTutorial", fileWidth));
        showTutorial.setOnAction(e -> showTutorial());
        help.getItems().add(showTutorial);

        MenuItem closeTutorial = new MenuItem();
        keybindNamePropertyMap.put("closeTutorial", new SimpleStringProperty());
        closeTutorial.setGraphic(menuFormatting("Close Tutorial", "closeTutorial", fileWidth));
        closeTutorial.setOnAction(e -> closeTutorial());
        help.getItems().add(closeTutorial);

        MenuItem about = new MenuItem();
        keybindNamePropertyMap.put("about", new SimpleStringProperty());
        about.setGraphic(menuFormatting("About", "about", fileWidth));
        about.setOnAction(e -> Main.showAbout());
        help.getItems().add(about);

        setMenuBarKeybinds();
        updateRecentFiles();
    }

    void setMenuBarKeybinds() {
        for (String propertyName: keybindNamePropertyMap.keySet()) {
            keybindNamePropertyMap.get(propertyName).setValue(Main.KEYBIND_PROPERTIES.getProperty(propertyName));
        }
    }

    private AnchorPane menuFormatting(String action, String propertyName, int width) {
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPrefWidth(width);
        Label actionLabel = new Label(action);
        Label shortcutLabel = new Label();
        shortcutLabel.textProperty().bind(keybindNamePropertyMap.get(propertyName));
        anchorPane.getChildren().addAll(actionLabel, shortcutLabel);

        AnchorPane.setRightAnchor(shortcutLabel, 0.0);

        return anchorPane;
    }

    private void newFile() {
        currentFile = null;
        fileName.setText("");
        components.clear();
        removedComponents.clear();
        visibleComponents.clear();
        selectedComponent = null;
        selectedPoint = null;

        sourceListView.getItems().clear();
        mirrorListView.getItems().clear();
        absorberListView.getItems().clear();
        shapeListView.getItems().clear();

        updateCanvas();
    }

    private void save() {
        if (currentFile != null) {
            saveFile(currentFile);
        } else {
            FileChooser fileChooser = new FileChooser();
            Stage stage = new Stage();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Photon Files", "*.phn"));
            File selectedFile = fileChooser.showSaveDialog(stage);

            saveFile(selectedFile);
        }
    }

    private void saveAs() {
        FileChooser fileChooser = new FileChooser();
        Stage stage = new Stage();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Photon Files", "*.phn"));
        File selectedFile = fileChooser.showSaveDialog(stage);

        currentFile = selectedFile;
        saveFile(selectedFile);
    }

    private void saveFile(File file) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.append("Canvas {\n\tWidth: ").append(String.valueOf(canvas.getWidth()))
                    .append("\n\tHeight: ").append(String.valueOf(canvas.getHeight())).append("\n}\n");
            for (Component component: components) {
                fileWriter.append(component.toData());
            }
            fileWriter.close();

            addRecentFile(file);
        } catch (Exception ignored) {}
    }

    private void open() {
        FileChooser fileChooser = new FileChooser();
        Stage stage = new Stage();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Photon Files", "*.phn"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            openFile(selectedFile);
        }

        componentsModified = true;
    }

    private void addRecentFile(File file) {
        boolean newFile = true;
        for (File recentFile: recentFiles) {
            if (file.getAbsolutePath().equals(recentFile.getAbsolutePath())) {
                newFile = false;
            }
        }
        if (newFile) {
            recentFiles.add(file);
            if (recentFiles.size() > 10) {
                recentFiles.remove(0);
            }
        }

        try {
            FileWriter fileWriter = new FileWriter(System.getProperty("user.dir") + "/src/application/Resources/recent_files.txt");
            for (File recentFile: recentFiles) {
                fileWriter.append(recentFile.getAbsolutePath()).append("\n");
            }
            fileWriter.close();
        } catch (IOException ignored) {}

        fileName.setText(file.getAbsolutePath());

        updateRecentFiles();
    }

    private void openFile(File file) {
        currentFile = file;

        removedComponents.clear();
        visibleComponents.clear();
        components.clear();
        sourceListView.getItems().clear();
        mirrorListView.getItems().clear();
        absorberListView.getItems().clear();
        shapeListView.getItems().clear();

        try {
            String data = Files.readString(Paths.get(file.toURI()));
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
                        addComponent(Component.parseData(matcher.group()));
                    }
                } catch (IllegalArgumentException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
                    alert.show();
                }
            }
        } catch (NoSuchFileException ex) {
            Main.showDialog(CustomDialog.DialogType.ERROR, "File cannot be found: " + file.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        addRecentFile(file);
    }

    private void updateRecentFiles() {
        openRecent.getItems().clear();
        for (File recentFile: recentFiles) {
            String[] info = recentFile.getAbsolutePath().split("\\\\");
            String name = info[info.length - 1];

            MenuItem fileName = new MenuItem(name.substring(0, name.length() - 4));
            fileName.setOnAction(e -> openFile(recentFile));
            openRecent.getItems().add(fileName);
        }
    }

    @FXML
    private void minimize() {
        Main.minimizeEditor();
    }

    @FXML
    private void exit() {
        Main.exitEditor();
    }

    /*
    TUTORIAL
     */

    private void initializeTutorial() {
        try {
            String left = Files.readString(Paths.get(new File(System.getProperty("user.dir") + "/src/application/Resources/tutorial_information/left.txt").toURI()));
            String center = Files.readString(Paths.get(new File(System.getProperty("user.dir") + "/src/application/Resources/tutorial_information/center.txt").toURI()));
            String right = Files.readString(Paths.get(new File(System.getProperty("user.dir") + "/src/application/Resources/tutorial_information/right.txt").toURI()));
            String bottom = Files.readString(Paths.get(new File(System.getProperty("user.dir") + "/src/application/Resources/tutorial_information/bottom.txt").toURI()));

            leftRegion.setOnMouseClicked(e -> Main.showDialog(CustomDialog.DialogType.INFO, left));
            centerRegion.setOnMouseClicked(e -> Main.showDialog(CustomDialog.DialogType.INFO, center));
            rightRegion.setOnMouseClicked(e -> Main.showDialog(CustomDialog.DialogType.INFO, right));
            bottomRegion.setOnMouseClicked(e -> Main.showDialog(CustomDialog.DialogType.INFO, bottom));
        } catch (IOException ignored) {}
    }

    private void showTutorial() {
        if (!showingTutorial) {
            try {
                String tutorialInfo = Files.readString(Paths.get(new File(
                        "src/application/Resources/tutorial_information/initial.txt").toURI()));
                Main.showDialog(CustomDialog.DialogType.INFO, tutorialInfo);
            } catch (IOException ignored) {}

            leftRegion.setVisible(true);
            centerRegion.setVisible(true);
            rightRegion.setVisible(true);
            bottomRegion.setVisible(true);

            showingTutorial = true;
        }
    }

    private void closeTutorial() {
        if (showingTutorial) {
            leftRegion.setVisible(false);
            centerRegion.setVisible(false);
            rightRegion.setVisible(false);
            bottomRegion.setVisible(false);

            showingTutorial = false;
        }
    }

    /*
    COMPONENT PANEL
     */

    private void addComponent(Component component) {
        components.add(component);
        visibleComponents.add(component);

        // adding the component to the component panel
        GridPane gridPane = new GridPane();
        Text text = new Text(component.getName());
        text.getStyleClass().add("text");

        ToggleButton toggleButton = new ToggleButton();
        toggleButton.getStyleClass().add("visibility-button");
        toggleButton.selectedProperty().addListener((c, o, o1) -> {
            component.setVisibility(!o1);

            if (o1) {
                visibleComponents.remove(component);

                if (component == selectedComponent) {
                    selectedComponent = null;
                    selectedPoint = null;

                    rotatable = false;
                    rotating = false;
                }
            } else {
                visibleComponents.add(component);
            }

            componentsModified = true;
            updateCanvas();
        });

        gridPane.add(text, 0, 0);
        gridPane.add(toggleButton, 1, 0);

        gridPane.setPrefWidth(0);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(80);
        gridPane.getColumnConstraints().add(col1);

        gridPaneComponentHashMap.put(gridPane, component);
        componentGridPaneHashMap.put(component, gridPane);
        componentTextHashMap.put(component, text);

        if (component instanceof Source) {
            sourceListView.getItems().add(gridPane);
            sourceListView.getSelectionModel().select(gridPane);
        } else if (component instanceof LineComponent lineComponent) {
            if (lineComponent.getEdge().getType() == Edge.EdgeType.REFLECTOR) {
                mirrorListView.getItems().add(gridPane);
                mirrorListView.getSelectionModel().select(gridPane);
            } else {
                absorberListView.getItems().add(gridPane);
                absorberListView.getSelectionModel().select(gridPane);
            }
        } else if (component instanceof Shape) {
            shapeListView.getItems().add(gridPane);
            shapeListView.getSelectionModel().select(gridPane);
        }

        // updating the state of the canvas
        removedComponents.clear();
        rotatable = false;
        rotating = false;
        componentsModified = true;
        updateCanvas();
    }

    private void removeComponent(Component component) {
        components.remove(component);
        removedComponents.add(component);
        if (component.getVisibility()) {
            visibleComponents.remove(component);

            if (component == selectedComponent) {
                selectedComponent = null;
                selectedPoint = null;
            }
        }

        if (component instanceof Source) {
            sourceListView.getItems().remove(componentGridPaneHashMap.get(component));
        } else if (component instanceof LineComponent lineComponent) {
            if (lineComponent.getEdge().getType() == Edge.EdgeType.REFLECTOR) {
                mirrorListView.getItems().remove(componentGridPaneHashMap.get(component));
            } else {
                absorberListView.getItems().remove(componentGridPaneHashMap.get(component));
            }
        } else if (component instanceof Shape) {
            shapeListView.getItems().remove(componentGridPaneHashMap.get(component));
        }

        componentsModified = true;
        updateCanvas();
    }


    /*
    INFORMATION PANEL
     */

    private void showInformation() {
        for (Node node: information.getChildren()) {
            node.setVisible(false);
        }

        if (selectedComponent != null) {
            nameInformation.setVisible(true);
            nameTextField.setText(selectedComponent.getName());
        }

        // show relevant information panels and add the information
        if (selectedComponent instanceof Shape shape) {
            shapeInformation.setVisible(true);

            shapeRefractiveIndex.setText(String.format("%.2f", shape.getRefractiveIndex()));
            if (selectedPoint != null) {
                shapeLayoutX.setText(String.format("%.1f", selectedPoint.getX()));
                shapeLayoutY.setText(String.format("%.1f", selectedPoint.getY()));
            } else {
                shapeLayoutX.setText("");
                shapeLayoutY.setText("");
            }
        } else if (selectedComponent instanceof LineComponent lineComponent) {
            lineComponentInformation.setVisible(true);
            lineComponentRotation.setText(String.format("%.2f", lineComponent.getEdge().getAngle()));

            if (selectedPoint != null) {
                lineComponentLayoutX.setText(String.format("%.1f", selectedPoint.getX()));
                lineComponentLayoutY.setText(String.format("%.1f", selectedPoint.getY()));
            } else {
                lineComponentLayoutX.setText("");
                lineComponentLayoutY.setText("");
            }
        } else if (selectedComponent instanceof Source source) {
            sourceInformation.setVisible(true);
            sourceLayoutX.setText(String.format("%.1f", source.getBeam().getInitialRay().getStart().getX()));
            sourceLayoutY.setText(String.format("%.1f", source.getBeam().getInitialRay().getStart().getY()));

            sourceRotation.setText(String.format("%.2f", source.getBeam().getInitialRay().getAngle()));
            sourceColorPicker.setValue(source.getBeam().getColor());
        }
    }

    private void initializeInformationPanel() {
        // setting properties of numeral fields
        setNumberFieldProperties(shapeLayoutX, i -> {
            if (selectedComponent instanceof Shape shape) {
                selectedPoint.setX(Double.parseDouble(shapeLayoutX.getText()));
                shape.update();
            }
        });

        setNumberFieldProperties(shapeLayoutY, i -> {
            if (selectedComponent instanceof Shape shape) {
                selectedPoint.setY(Double.parseDouble(shapeLayoutY.getText()));
                shape.update();
            }
        });

        setNumberFieldProperties(shapeRefractiveIndex, i -> {
            if (selectedComponent instanceof Shape shape) {
                shape.setRefractiveIndex(Double.parseDouble(shapeRefractiveIndex.getText()));
            }
        });

        setNumberFieldProperties(lineComponentLayoutX, i -> {
            if (selectedComponent instanceof LineComponent lineComponent) {
                selectedPoint.setX(Double.parseDouble(lineComponentLayoutX.getText()));
                lineComponent.getEdge().updateSegment();
            }
        });

        setNumberFieldProperties(lineComponentLayoutY, i -> {
            if (selectedComponent instanceof LineComponent lineComponent) {
                selectedPoint.setY(Double.parseDouble(lineComponentLayoutY.getText()));
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
        sourceColorPicker.setOnAction(e -> {
            if (selectedComponent instanceof Source source) {
                source.getBeam().setColor(sourceColorPicker.getValue());

                componentsModified = true;
                updateCanvas();
            }
        });


        // setting the properties of the list views
        setListViewProperties(sourceListView);
        setListViewProperties(mirrorListView);
        setListViewProperties(absorberListView);
        setListViewProperties(shapeListView);

        // setting the properties of the name fields
        nameTextField.focusedProperty().addListener((c, o, o1) -> {
            if (!o1) {
                if (selectedComponent != null) {
                    selectedComponent.setName(nameTextField.getText());
                    componentTextHashMap.get(selectedComponent).setText(nameTextField.getText());
                }

                componentsModified = true;
                updateCanvas();
            }
        });

        nameTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (selectedComponent != null) {
                    selectedComponent.setName(nameTextField.getText());
                    componentTextHashMap.get(selectedComponent).setText(nameTextField.getText());
                }

                componentsModified = true;
                updateCanvas();
            }
        });
    }

    // this function sets the properties for a text field based off a general function as most of them are similar
    private void setNumberFieldProperties(MFXTextField textField, Consumer<Integer> function) {
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

    private void setListViewProperties(ListView<GridPane> listView) {
        listView.setOnMouseClicked(e -> {
            selectedComponent = gridPaneComponentHashMap.get(listView.getSelectionModel().getSelectedItem());
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
            String keyString = Settings.keyEventToString(e);
            if (!keyString.equals("")) {
                if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("newFile"))) newFile();
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("save"))) save();
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("saveAs"))) saveAs();
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("open"))) open();
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("openRecent"))) openRecent();
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("settings"))) Main.showSettings();
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("about"))) Main.showAbout();
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("showTutorial"))) showTutorial();
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("closeTutorial"))) closeTutorial();
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("addSource"))) source.setSelected(true);
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("addMirror"))) mirror.setSelected(true);
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("addAbsorber"))) absorber.setSelected(true);
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("addShape"))) shape.setSelected(true);
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("addPoint"))) addPointToShape();
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("undo"))) undo();
                else if (keyString.equals(Main.KEYBIND_PROPERTIES.getProperty("redo"))) redo();
            } else {
                switch (e.getCode()) {
                    case SHIFT -> scrollPane.setPannable(true);
                    case DELETE -> delete();
                }
            }
        });

        borderPane.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.SHIFT) {
                scrollPane.setPannable(false);
            }
        });
    }

    private void openRecent() {
        openRecent.show();
    }

    private void undo() {
        if (components.size() > 0) {
            removeComponent(components.get(components.size() - 1));
        }
    }

    private void redo() {
        if (removedComponents.size() > 0) {
            addComponent(removedComponents.remove(removedComponents.size() - 1));
        }
    }

    private void delete() {
        if (selectedComponent != null) {
            removeComponent(selectedComponent);
            selectedComponent = null;

            sourceListView.getSelectionModel().clearSelection();
            mirrorListView.getSelectionModel().clearSelection();
            absorberListView.getSelectionModel().clearSelection();
            shapeListView.getSelectionModel().clearSelection();

            rotatable = false;
            rotating = false;
        }
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
                addComponent(selectedComponent);
            } else if (mirror.isSelected() || absorber.isSelected()) {
                selectedPoint = null;
                if (startPoint == null) {
                    startPoint = new Point(e.getX(), e.getY());
                } else {
                    if (mirror.isSelected()) {
                        selectedComponent = new LineComponent(startPoint, new Point(e.getX(), e.getY()), Edge.EdgeType.REFLECTOR);
                    } else {
                        selectedComponent = new LineComponent(startPoint, new Point(e.getX(), e.getY()), Edge.EdgeType.ABSORBER);
                    }

                    addComponent(selectedComponent);
                    startPoint = null;

                    lineComponentLayoutX.setDisable(true);
                    lineComponentLayoutY.setDisable(true);
                }
            } else if (shape.isSelected()) {
                selectedPoint = null;
                Point selectedPoint = new Point(e.getX(), e.getY());
                if (vertices.size() >= 3 && selectedPoint.equals(vertices.get(0), 5)) {
                    selectedComponent = new Shape(1.5, vertices);
                    addComponent(selectedComponent);
                    vertices.clear();

                    shapeLayoutX.setDisable(true);
                    shapeLayoutY.setDisable(true);
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
                        if (Math.abs(distance - ROTATE_WHEEL_WIDTH / 2) < 5) {
                            double relativeAngle = Ray.angleTo(rotatePoint, new Point(e.getX(), e.getY()));
                            if (selectedComponent instanceof Source source) {
                                rotateAngle = source.getBeam().getInitialRay().getAngle() - relativeAngle;
                            } else if (selectedComponent instanceof LineComponent lineComponent) {
                                rotateAngle = lineComponent.getEdge().getAngle() - relativeAngle;
                            } else {
                                throw new IllegalStateException("Rotating but selected component is not a source or line component");
                            }
                            rotating = true;
                            translating = false;
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

                            translating = true;
                            initialTranslateComponent = lineComponent.copy();
                            translatePoint = new Point(e.getX(), e.getY());
                        }
                    } else {
                        rotatable = false;
                        rotating = false;

                        if (selectedComponent instanceof Shape) {
                            translating = true;
                            initialTranslateComponent = selectedComponent.copy();
                            translatePoint = new Point(e.getX(), e.getY());
                        }
                    }

                    // resetting the selected component in the list views
                    sourceListView.getSelectionModel().clearSelection();
                    mirrorListView.getSelectionModel().clearSelection();
                    absorberListView.getSelectionModel().clearSelection();
                    shapeListView.getSelectionModel().clearSelection();

                    if (selectedComponent instanceof Source) {
                        sourceListView.getSelectionModel().select(componentGridPaneHashMap.get(selectedComponent));
                    } else if (selectedComponent instanceof LineComponent lineComponent) {
                        if (lineComponent.getEdge().getType() == Edge.EdgeType.REFLECTOR) {
                            mirrorListView.getSelectionModel().select(componentGridPaneHashMap.get(selectedComponent));
                        } else {
                            absorberListView.getSelectionModel().select(componentGridPaneHashMap.get(selectedComponent));
                        }
                    } else if (selectedComponent instanceof Shape) {
                        shapeListView.getSelectionModel().select(componentGridPaneHashMap.get(selectedComponent));
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
            } else if (translating) {
                Point vector = Point.subtract(new Point(e.getX(), e.getY()), translatePoint);
                selectedComponent.set(initialTranslateComponent.translate(vector));

                componentsModified = true;
            }

            updateCanvas(e);
        });

        canvas.setOnMouseReleased(e -> {
            rotating = false;
            translating = false;

            if (selectedComponent instanceof LineComponent lineComponent && rotatable) {
                rotatePoint = lineComponent.getEdge().midpoint();
            }
        });
    }

    @FXML
    private void addPointToShape() {
        if (selectedComponent instanceof Shape shape) {
            shape.addPoint();
            componentsModified = true;
            updateCanvas();
        } else {
            Main.showDialog(CustomDialog.DialogType.ERROR, "Selected component is not a shape! Cannot add a point to selected component");
        }
    }

    @FXML
    private void deleteSelectedPoint() {
        if (selectedComponent instanceof Shape shape) {
            if (selectedPoint != null) {
                if (shape.getVertices().size() > 3) {
                    shape.removePoint(selectedPoint);
                    selectedPoint = null;
                    componentsModified = true;
                    updateCanvas();
                } else {
                    Main.showDialog(CustomDialog.DialogType.ERROR, "Cannot remove point if shape has less than 4 points!");
                }
            } else {
                Main.showDialog(CustomDialog.DialogType.ERROR, "Cannot remove point if no point is selected!");
            }
        }
    }

    private void copy() {

    }

    private void paste() {

    }

    private void duplicate() {
        copy();
        paste();
    }

    // finding the clicked or hovered component
    private Component findMousedComponent(MouseEvent e) {
        Component mousedComponent = null;
        ArrayList<Component> clickedComponents = new ArrayList<>();
        HashMap<Component, Double> distances = new HashMap<>();
        int highest = 0;  // shape -> 0, line component -> 1, source -> 2

        Point mousedPoint = new Point(e.getX(), e.getY());
        for (Component component: visibleComponents) {
            if (component instanceof Shape shape) {
                if (shape.containsMouse(mousedPoint)) {
                    clickedComponents.add(component);
                }
            } else if (component instanceof LineComponent lineComponent) {
                double distance = Point.distance(mousedPoint, lineComponent.getEdge());
                if (distance < MAX_DISTANCE_SELECT) {
                    clickedComponents.add(component);
                    distances.put(component, distance);
                    highest = Math.max(highest, 1);
                }
            } else if (component instanceof Source source) {
                for (LightComponent lightComponent: source.getBeam().getLightComponents()) {
                    if (lightComponent instanceof LightRay lightRay) {
                        double distance = Point.distance(mousedPoint, lightRay);
                        if (distance < MAX_DISTANCE_SELECT * 0.5) {
                            clickedComponents.add(component);
                            distances.put(component, distance);
                            highest = 2;
                            break;
                        }
                    } else if (lightComponent instanceof LightSegment lightSegment) {
                        double distance = Point.distance(mousedPoint, lightSegment);
                        if (distance < MAX_DISTANCE_SELECT * 0.5) {
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
                for (Component component: clickedComponents) {
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
                for (Component component: clickedComponents) {
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
                for (Component component: clickedComponents) {
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
            for (Point point: shape.getVertices()) {
                if (Point.distance(point, clickedPoint) < MAX_DISTANCE_SELECT) {
                    shapeLayoutX.setDisable(false);
                    shapeLayoutY.setDisable(false);
                    lineComponentLayoutX.setDisable(true);
                    lineComponentLayoutY.setDisable(true);
                    return point;
                }
            }
        } else if (clickedComponent instanceof LineComponent lineComponent) {
            Point point = null;
            if (Point.distance(lineComponent.getEdge().getStart(), clickedPoint) < MAX_DISTANCE_SELECT) {
                point = lineComponent.getEdge().getStart();
            } else if (Point.distance(lineComponent.getEdge().getEnd(), clickedPoint) < MAX_DISTANCE_SELECT) {
                point = lineComponent.getEdge().getEnd();
            }

            if (point != null) {
                shapeLayoutX.setDisable(true);
                shapeLayoutY.setDisable(true);
                lineComponentLayoutX.setDisable(false);
                lineComponentLayoutY.setDisable(false);
                return point;
            }
        } else if (clickedComponent instanceof Source source) {
            if (Point.distance(source.getBeam().getInitialRay().getStart(), clickedPoint) < MAX_DISTANCE_SELECT) {
                shapeLayoutX.setDisable(true);
                shapeLayoutY.setDisable(true);
                lineComponentLayoutX.setDisable(true);
                lineComponentLayoutY.setDisable(true);
                return source.getBeam().getInitialRay().getStart();
            }
        }

        shapeLayoutX.setDisable(true);
        shapeLayoutY.setDisable(true);
        lineComponentLayoutX.setDisable(true);
        lineComponentLayoutY.setDisable(true);
        return null;
    }

    // drawing fully formed components
    private void updateCanvas() {
        showInformation();

        if (componentsModified) {
            for (Component component: visibleComponents) {
                if (component instanceof Source source) {
                    source.getBeam().generateBeam(visibleComponents);
                }
            }
        }

        drawCanvas();
    }

    void drawCanvas() {
        graphicsContext.setFill(Color.web(Main.COLOR_PROPERTIES.getProperty("canvasColor")));
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Component component: visibleComponents) {  // draw components
            // drawing the shape of the component
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

            // setting the color of the component to be drawn
            if (component == selectedComponent) {
                graphicsContext.setStroke(Color.web(Main.COLOR_PROPERTIES.getProperty("selectedColor")));
            } else if (component == hoveredComponent) {
                graphicsContext.setStroke(Color.web(Main.COLOR_PROPERTIES.getProperty("hoveredColor")));
            } else {
                if (component instanceof Shape) {
                    graphicsContext.setStroke(Color.web(Main.COLOR_PROPERTIES.getProperty("shapeColor")));
                } else if (component instanceof Source source) {
                    graphicsContext.setStroke(source.getBeam().getColor());
                } else if (component instanceof LineComponent lineComponent) {
                    if (lineComponent.getEdge().getType() == Edge.EdgeType.ABSORBER) {
                        graphicsContext.setStroke(Color.web(Main.COLOR_PROPERTIES.getProperty("absorberColor")));
                    } else if (lineComponent.getEdge().getType() == Edge.EdgeType.REFLECTOR) {
                        graphicsContext.setStroke(Color.web(Main.COLOR_PROPERTIES.getProperty("mirrorColor")));
                    }
                }
            }

            graphicsContext.stroke();
            graphicsContext.closePath();

            // drawing the normals if the component is a source
//            if (component instanceof Source source) {
//                graphicsContext.setStroke(Color.web(Main.PROPERTIES.getProperty("shapeColor")));
//                graphicsContext.setLineDashes(2);
//                for (LightComponent lightComponent: source.getBeam().getLightComponents()) {
//                    Normal normal = lightComponent.getNormal();
//                    if (normal != null) {
//                        graphicsContext.beginPath();
//                        drawNormal(normal.edge(), normal.intersection());
//                        graphicsContext.stroke();
//                    }
//                }
//                graphicsContext.closePath();
//                graphicsContext.setLineDashes(0);
//            }
        }

        // drawing the rotate-wheel if the selected component is a source or a line component
        if (rotatable) {
            graphicsContext.setStroke(Color.web(Main.COLOR_PROPERTIES.getProperty("shapeColor")));
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
            graphicsContext.strokeArc(point.getX() - ROTATE_WHEEL_WIDTH / 2, point.getY() - ROTATE_WHEEL_WIDTH / 2,
                    ROTATE_WHEEL_WIDTH, ROTATE_WHEEL_WIDTH, 0, 360, ArcType.OPEN);
            graphicsContext.closePath();
            graphicsContext.setLineWidth(1);
        }
    }

    // draw components that are being added
    private void updateCanvas(MouseEvent e) {
        updateCanvas();

        // if a component is being drawn, shows the component before it is confirmed
        graphicsContext.beginPath();
        Point newPoint = new Point(e.getX(), e.getY());
        if (absorber.isSelected() && startPoint != null) {
            graphicsContext.setStroke(Color.web(Main.COLOR_PROPERTIES.getProperty("absorberColor")));

            if (!startPoint.equals(newPoint)) {
                drawSegment(new Segment(startPoint, newPoint));
            }
        } else if (mirror.isSelected() && startPoint != null) {
            graphicsContext.setStroke(Color.web(Main.COLOR_PROPERTIES.getProperty("mirrorColor")));

            if (!startPoint.equals(newPoint)) {
                drawSegment(new Segment(startPoint, newPoint));
            }
        } else if (shape.isSelected() && vertices.size() != 0) {
            graphicsContext.setStroke(Color.web(Main.COLOR_PROPERTIES.getProperty("drawingColor")));

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

    private void drawNormal(Segment edge, Point intersection) {
        // the coordinates of the start and end points of the normal displayed
        double startX = intersection.getX() + NORMAL_WIDTH * Math.cos(Math.toRadians(edge.getAngle() + 90));
        double startY = intersection.getY() + NORMAL_WIDTH * Math.sin(Math.toRadians(edge.getAngle() + 90));
        double endX = intersection.getX() - NORMAL_WIDTH * Math.cos(Math.toRadians(edge.getAngle() + 90));
        double endY = intersection.getY() - NORMAL_WIDTH * Math.sin(Math.toRadians(edge.getAngle() + 90));

        // drawing a dashed line to represent the normal
        graphicsContext.moveTo(startX, startY);
        graphicsContext.lineTo(endX, endY);
    }
}