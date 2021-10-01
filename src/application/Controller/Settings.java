package application.Controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class Settings implements Initializable {
    @FXML
    private BorderPane borderPane;
    @FXML
    private AnchorPane themeColors;
    @FXML
    private ColorPicker titleColorPicker;
    @FXML
    private ColorPicker backgroundColorPicker;
    @FXML
    private ColorPicker backgroundSelectedColorPicker;
    @FXML
    private ColorPicker textColorPicker;
    @FXML
    private ColorPicker unselectedButtonColorPicker;
    @FXML
    private ColorPicker selectedButtonColorPicker;
    @FXML
    private ColorPicker highlightColorPicker;
    @FXML
    private ColorPicker separatorColorPicker;
    @FXML
    private ColorPicker disabledColorPicker;
    @FXML
    private ColorPicker accentColorPicker;
    @FXML
    private AnchorPane editorColors;
    @FXML
    private ColorPicker canvasColorPicker;
    @FXML
    private ColorPicker mirrorColorPicker;
    @FXML
    private ColorPicker absorberColorPicker;
    @FXML
    private ColorPicker shapeColorPicker;
    @FXML
    private ColorPicker drawingColorPicker;
    @FXML
    private ColorPicker selectedColorPicker;
    @FXML
    private ColorPicker hoveredColorPicker;
    @FXML
    private TreeView<String> treeView;


    private final StringProperty css = new SimpleStringProperty();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        borderPane.styleProperty().bind(css);


        TreeItem<String> root = new TreeItem<>("Color Scheme");
        TreeItem<String> editorTheme = new TreeItem<>("Editor Theme");
        TreeItem<String> canvasColors = new TreeItem<>("Canvas Colors");
        root.getChildren().add(editorTheme);
        root.getChildren().add(canvasColors);

        treeView.setRoot(root);

        treeView.setOnMouseClicked(e -> {
            try {
                themeColors.setVisible(false);
                editorColors.setVisible(false);
                switch (treeView.getSelectionModel().getSelectedItem().getValue()) {
                    case "Editor Theme" -> themeColors.setVisible(true);
                    case "Canvas Colors" -> editorColors.setVisible(true);
                }
            } catch (NullPointerException ignored) {}
        });

        setColorPickers(Main.PROPERTIES);
    }

    public void setCss(String css) {
        this.css.setValue(css);
    }

    private void setColorPickers(Properties properties) {
        titleColorPicker.setValue(Color.web(properties.getProperty("titleColor")));
        backgroundColorPicker.setValue(Color.web(properties.getProperty("backgroundColor")));
        backgroundSelectedColorPicker.setValue(Color.web(properties.getProperty("backgroundSelectedColor")));
        textColorPicker.setValue(Color.web(properties.getProperty("textColor")));
        unselectedButtonColorPicker.setValue(Color.web(properties.getProperty("unselectedButtonColor")));
        selectedButtonColorPicker.setValue(Color.web(properties.getProperty("selectedButtonColor")));
        highlightColorPicker.setValue(Color.web(properties.getProperty("highlightColor")));
        separatorColorPicker.setValue(Color.web(properties.getProperty("separatorColor")));
        disabledColorPicker.setValue(Color.web(properties.getProperty("disabledColor")));
        accentColorPicker.setValue(Color.web(properties.getProperty("accentColor")));

        canvasColorPicker.setValue(Color.web(properties.getProperty("canvasColor")));
        mirrorColorPicker.setValue(Color.web(properties.getProperty("mirrorColor")));
        absorberColorPicker.setValue(Color.web(properties.getProperty("absorberColor")));
        shapeColorPicker.setValue(Color.web(properties.getProperty("shapeColor")));
        drawingColorPicker.setValue(Color.web(properties.getProperty("drawingColor")));
        selectedColorPicker.setValue(Color.web(properties.getProperty("selectedColor")));
        hoveredColorPicker.setValue(Color.web(properties.getProperty("hoveredColor")));
    }

    @FXML
    private void apply() {
        Main.PROPERTIES.setProperty("titleColor", colorToString(titleColorPicker.getValue()));
        Main.PROPERTIES.setProperty("backgroundColor", colorToString(backgroundColorPicker.getValue()));
        Main.PROPERTIES.setProperty("backgroundSelectedColor", colorToString(backgroundSelectedColorPicker.getValue()));
        Main.PROPERTIES.setProperty("textColor", colorToString(textColorPicker.getValue()));
        Main.PROPERTIES.setProperty("unselectedButtonColor", colorToString(unselectedButtonColorPicker.getValue()));
        Main.PROPERTIES.setProperty("selectedButtonColor", colorToString(selectedButtonColorPicker.getValue()));
        Main.PROPERTIES.setProperty("highlightColor", colorToString(highlightColorPicker.getValue()));
        Main.PROPERTIES.setProperty("separatorColor", colorToString(separatorColorPicker.getValue()));
        Main.PROPERTIES.setProperty("disabledColor", colorToString(disabledColorPicker.getValue()));
        Main.PROPERTIES.setProperty("accentColor", colorToString(accentColorPicker.getValue()));

        Main.PROPERTIES.setProperty("canvasColor", colorToString(canvasColorPicker.getValue()));
        Main.PROPERTIES.setProperty("mirrorColor", colorToString(mirrorColorPicker.getValue()));
        Main.PROPERTIES.setProperty("absorberColor", colorToString(absorberColorPicker.getValue()));
        Main.PROPERTIES.setProperty("shapeColor", colorToString(shapeColorPicker.getValue()));
        Main.PROPERTIES.setProperty("drawingColor", colorToString(drawingColorPicker.getValue()));
        Main.PROPERTIES.setProperty("selectedColor", colorToString(selectedColorPicker.getValue()));
        Main.PROPERTIES.setProperty("hoveredColor", colorToString(hoveredColorPicker.getValue()));

        Main.writeProperties();
        Main.updateEditor();
    }

    private String colorToString(Color color) {
        return String.format("rgb(%.0f,%.0f,%.0f)", color.getRed()*255, color.getGreen()*255, color.getBlue()*255);
    }

    @FXML
    private void exit() {
        Main.exitSettings();
    }

    @FXML
    private void resetSettings() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("src/application/Resources/settings/default.properties"));
            setColorPickers(properties);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
