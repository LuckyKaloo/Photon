package application.Controller;

import io.github.palexdev.materialfx.controls.MFXLabel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
    private ColorPicker accentColorPicker;
    @FXML
    private ColorPicker disabledColorPicker;
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
    private AnchorPane canvasKeyBinds;
    @FXML
    private MFXLabel addSource;
    @FXML
    private MFXLabel addMirror;
    @FXML
    private MFXLabel addAbsorber;
    @FXML
    private MFXLabel addShape;
    @FXML
    private MFXLabel addPoint;
    @FXML
    private MFXLabel undo;
    @FXML
    private MFXLabel redo;
    @FXML
    private AnchorPane editorKeyBinds;
    @FXML
    private MFXLabel newFile;
    @FXML
    private MFXLabel save;
    @FXML
    private MFXLabel saveAs;
    @FXML
    private MFXLabel open;
    @FXML
    private MFXLabel openRecent;
    @FXML
    private MFXLabel settings;
    @FXML
    private MFXLabel about;
    @FXML
    private MFXLabel showTutorial;
    @FXML
    private MFXLabel closeTutorial;
    @FXML
    private TreeView<String> treeView;


    private final StringProperty css = new SimpleStringProperty();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        borderPane.styleProperty().bind(css);

        TreeItem<String> root = new TreeItem<>("Settings");

        TreeItem<String> colorScheme = new TreeItem<>("Color Scheme");
        TreeItem<String> editorTheme = new TreeItem<>("Editor Theme");
        TreeItem<String> canvasColors = new TreeItem<>("Canvas Colors");
        colorScheme.getChildren().add(editorTheme);
        colorScheme.getChildren().add(canvasColors);
        root.getChildren().add(colorScheme);

        TreeItem<String> keybinds = new TreeItem<>("Keybinds");
        TreeItem<String> canvasShortcuts = new TreeItem<>("Canvas Keybinds");
        TreeItem<String> editorShortcuts = new TreeItem<>("Editor Keybinds");
        keybinds.getChildren().add(canvasShortcuts);
        keybinds.getChildren().add(editorShortcuts);
        root.getChildren().add(keybinds);

        treeView.setRoot(root);

        treeView.setOnMouseClicked(e -> {
            try {
                themeColors.setVisible(false);
                editorColors.setVisible(false);
                canvasKeyBinds.setVisible(false);
                editorKeyBinds.setVisible(false);
                switch (treeView.getSelectionModel().getSelectedItem().getValue()) {
                    case "Editor Theme" -> themeColors.setVisible(true);
                    case "Canvas Colors" -> editorColors.setVisible(true);
                    case "Canvas Keybinds" -> canvasKeyBinds.setVisible(true);
                    case "Editor Keybinds" -> editorKeyBinds.setVisible(true);
                }
            } catch (NullPointerException ignored) {}
        });

        setColorPickers(Main.COLOR_PROPERTIES);
        setKeybinds(Main.KEYBIND_PROPERTIES);
    }

    public void setCss(String css) {
        this.css.setValue(css);
    }

    @FXML
    private void setKeyBind(KeyEvent keyEvent) {
        MFXLabel source = ((MFXLabel) keyEvent.getSource());
        if (source.isFocused()) {
            source.setText(keyEventToString(keyEvent));
        }
    }

    public static String keyEventToString(KeyEvent keyEvent) {
        if (keyEvent.getCode().isLetterKey()) {
            StringBuilder stringBuilder = new StringBuilder();
            if (keyEvent.isControlDown()) {
                stringBuilder.append("Ctrl+");
            }
            if (keyEvent.isShiftDown()) {
                stringBuilder.append("Shift+");
            }
            if (keyEvent.isAltDown()) {
                stringBuilder.append("Alt+");
            }
            stringBuilder.append(keyEvent.getCode().getChar());

            return stringBuilder.toString();
        }

        return "";
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

    private void setKeybinds(Properties properties) {
        newFile.setText(properties.getProperty("newFile"));
        open.setText(properties.getProperty("open"));
        openRecent.setText(properties.getProperty("openRecent"));
        save.setText(properties.getProperty("save"));
        saveAs.setText(properties.getProperty("saveAs"));
        settings.setText(properties.getProperty("settings"));
        about.setText(properties.getProperty("about"));
        showTutorial.setText(properties.getProperty("showTutorial"));
        closeTutorial.setText(properties.getProperty("closeTutorial"));

        addSource.setText(properties.getProperty("addSource"));
        addMirror.setText(properties.getProperty("addMirror"));
        addAbsorber.setText(properties.getProperty("addAbsorber"));
        addShape.setText(properties.getProperty("addShape"));
        addPoint.setText(properties.getProperty("addPoint"));
        undo.setText(properties.getProperty("undo"));
        redo.setText(properties.getProperty("redo"));
    }

    @FXML
    private void apply() {
        Main.COLOR_PROPERTIES.setProperty("titleColor", colorToString(titleColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("backgroundColor", colorToString(backgroundColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("backgroundSelectedColor", colorToString(backgroundSelectedColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("textColor", colorToString(textColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("unselectedButtonColor", colorToString(unselectedButtonColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("selectedButtonColor", colorToString(selectedButtonColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("highlightColor", colorToString(highlightColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("separatorColor", colorToString(separatorColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("disabledColor", colorToString(disabledColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("accentColor", colorToString(accentColorPicker.getValue()));

        Main.COLOR_PROPERTIES.setProperty("canvasColor", colorToString(canvasColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("mirrorColor", colorToString(mirrorColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("absorberColor", colorToString(absorberColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("shapeColor", colorToString(shapeColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("drawingColor", colorToString(drawingColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("selectedColor", colorToString(selectedColorPicker.getValue()));
        Main.COLOR_PROPERTIES.setProperty("hoveredColor", colorToString(hoveredColorPicker.getValue()));


        Main.KEYBIND_PROPERTIES.setProperty("newFile", newFile.getText());
        Main.KEYBIND_PROPERTIES.setProperty("open", open.getText());
        Main.KEYBIND_PROPERTIES.setProperty("openRecent", openRecent.getText());
        Main.KEYBIND_PROPERTIES.setProperty("save", save.getText());
        Main.KEYBIND_PROPERTIES.setProperty("saveAs", saveAs.getText());
        Main.KEYBIND_PROPERTIES.setProperty("settings", settings.getText());
        Main.KEYBIND_PROPERTIES.setProperty("about", about.getText());
        Main.KEYBIND_PROPERTIES.setProperty("showTutorial", showTutorial.getText());
        Main.KEYBIND_PROPERTIES.setProperty("closeTutorial", closeTutorial.getText());

        Main.KEYBIND_PROPERTIES.setProperty("addSource", addSource.getText());
        Main.KEYBIND_PROPERTIES.setProperty("addMirror", addMirror.getText());
        Main.KEYBIND_PROPERTIES.setProperty("addAbsorber", addAbsorber.getText());
        Main.KEYBIND_PROPERTIES.setProperty("addShape", addShape.getText());
        Main.KEYBIND_PROPERTIES.setProperty("addPoint", addPoint.getText());
        Main.KEYBIND_PROPERTIES.setProperty("undo", undo.getText());
        Main.KEYBIND_PROPERTIES.setProperty("redo", redo.getText());


        Main.resetMenuBar();
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
            Properties colorProperties = new Properties();
            colorProperties.load(new FileInputStream(System.getProperty("user.dir") + "/src/application/Resources/settings/default_colors.properties"));
            setColorPickers(colorProperties);

            Properties keybindProperties = new Properties();
            keybindProperties.load(new FileInputStream(System.getProperty("user.dir") + "/src/application/Resources/settings/default_keybinds.properties"));
            setKeybinds(keybindProperties);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
