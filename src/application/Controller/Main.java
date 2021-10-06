package application.Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class Main extends Application {
    private static Stage editorStage;
    private static Stage settingsStage;
    private static Stage aboutStage;
    private static Stage dialogStage;

    private static Editor editor;
    private static Settings settings;
    private static About about;
    private static CustomDialog dialog;

    public final static Properties COLOR_PROPERTIES = new Properties();
    public final static Properties KEYBIND_PROPERTIES = new Properties();

    @Override
    public void start(Stage stage) throws Exception {
        loadProperties();

        FXMLLoader settingsLoader = new FXMLLoader(Objects.requireNonNull(Main.class.getResource("/application/View/settings.fxml")));
        settingsStage = new Stage();
        settingsStage.setScene(new Scene(settingsLoader.load()));
        settingsStage.initStyle(StageStyle.TRANSPARENT);
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settings = settingsLoader.getController();

        FXMLLoader aboutLoader = new FXMLLoader(Objects.requireNonNull(Main.class.getResource("/application/View/about.fxml")));
        aboutStage = new Stage();
        aboutStage.setScene(new Scene(aboutLoader.load()));
        aboutStage.initStyle(StageStyle.TRANSPARENT);
        aboutStage.initModality(Modality.APPLICATION_MODAL);
        about = aboutLoader.getController();

        FXMLLoader dialogLoader = new FXMLLoader(Objects.requireNonNull(Main.class.getResource("/application/View/dialog.fxml")));
        dialogStage = new Stage();
        dialogStage.setScene(new Scene(dialogLoader.load()));
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialog = dialogLoader.getController();

        writeToCss();


        Main.editorStage = stage;
        stage.setTitle("Photon");
        stage.getIcons().add(new Image(new FileInputStream(System.getProperty("user.dir") + "/src/application/Resources/images/logo.png")));
        stage.initStyle(StageStyle.TRANSPARENT);

        // showing the splash screen
        stage.setScene(SplashScreen.generateScene());
        stage.setMaximized(true);
        stage.show();
        SplashScreen.play();
    }

    private static void loadProperties() {
        try {
            COLOR_PROPERTIES.load(new FileInputStream(System.getProperty("user.dir") + "/src/application/Resources/settings/colors.properties"));
            KEYBIND_PROPERTIES.load(new FileInputStream(System.getProperty("user.dir") + "/src/application/Resources/settings/keybinds.properties"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void writeProperties() {
        try {
            FileWriter fileWriter = new FileWriter(System.getProperty("user.dir") + "/src/application/Resources/settings/colors.properties");
            Main.COLOR_PROPERTIES.store(fileWriter, "");
            fileWriter.close();

            fileWriter = new FileWriter(System.getProperty("user.dir") + "/src/application/Resources/settings/keybinds.properties");
            Main.KEYBIND_PROPERTIES.store(fileWriter, "");
            fileWriter.close();

            writeToCss();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void writeToCss() {
        String string = "-title-color: " + COLOR_PROPERTIES.getProperty("titleColor") + ";\n" +
            "-background-color: " + COLOR_PROPERTIES.getProperty("backgroundColor") + ";\n" +
            "-text-color: " + COLOR_PROPERTIES.getProperty("textColor") + ";\n" +
            "-background-selected-color: " + COLOR_PROPERTIES.getProperty("backgroundSelectedColor") + ";\n" +
            "-button-color: " + COLOR_PROPERTIES.getProperty("unselectedButtonColor") + ";\n" +
            "-disabled-color: " + COLOR_PROPERTIES.getProperty("disabledColor") + ";\n" +
            "-selected-button-color: " + COLOR_PROPERTIES.getProperty("selectedButtonColor") + ";\n" +
            "-highlight-color: " + COLOR_PROPERTIES.getProperty("highlightColor") + ";\n" +
            "-separator-color: " + COLOR_PROPERTIES.getProperty("separatorColor") + ";\n" +
            "-accent-color: " + COLOR_PROPERTIES.getProperty("accentColor") + ";";

        if (editor != null) {
            editor.setCss(string);
        }
        settings.setCss(string);
        about.setCss(string);
        dialog.setCss(string);
    }

    public static void showEditor() {
        SplashScreen.finish();

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(Main.class.getResource("/application/View/editor.fxml")));

            editorStage.getScene().setRoot(loader.load());
            editorStage.setMaximized(true);
            editorStage.show();

            editor = loader.getController();
            editor.start();

            writeToCss();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void resetMenuBar() {
        editor.setMenuBarKeybinds();
    }

    public static void minimizeEditor() {
        editorStage.setIconified(true);
    }

    public static void exitEditor() {
        editorStage.close();
    }

    public static void updateEditor() {
        editor.drawCanvas();
    }

    public static void showSettings() {
        settingsStage.show();
    }

    public static void exitSettings() {
        settingsStage.close();
    }

    public static void showAbout() {
        aboutStage.show();
    }

    public static void exitAbout() {
        aboutStage.close();
    }

    public static void showDialog(CustomDialog.DialogType type, String text) {
        dialog.setType(type);
        dialog.setInformation(text);
        dialogStage.show();
    }

    public static void exitDialog() {
        dialogStage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
