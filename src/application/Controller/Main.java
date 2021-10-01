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

    private static Settings settings;
    private static Editor editor;

    public final static Properties PROPERTIES = new Properties();

    @Override
    public void start(Stage stage) throws Exception {
        loadProperties();
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(Main.class.getResource("/application/View/settings.fxml")));
        settingsStage = new Stage();
        settingsStage.setScene(new Scene(loader.load()));
        settingsStage.initStyle(StageStyle.TRANSPARENT);
        settingsStage.initModality(Modality.APPLICATION_MODAL);

        settings = loader.getController();
        writeToCss();


        Main.editorStage = stage;
        stage.setTitle("Photon");
        stage.getIcons().add(new Image(new FileInputStream("src/application/Resources/images/logo.png")));
        stage.initStyle(StageStyle.TRANSPARENT);

        // showing the splash screen
        stage.setScene(SplashScreen.generateScene());
        stage.setMaximized(true);
        stage.show();
        SplashScreen.play();
    }

    private static void loadProperties() {
        try {
            PROPERTIES.load(new FileInputStream("src/application/Resources/settings/color_settings.properties"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void writeProperties() {
        try {
            FileWriter fileWriter = new FileWriter("src/application/Resources/settings/color_settings.properties");
            Main.PROPERTIES.store(fileWriter, "");
            fileWriter.close();

            writeToCss();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void writeToCss() {
        String string = "-title-color: " + PROPERTIES.getProperty("titleColor") + ";\n" +
            "-background-color: " + PROPERTIES.getProperty("backgroundColor") + ";\n" +
            "-text-color: " + PROPERTIES.getProperty("textColor") + ";\n" +
            "-background-selected-color: " + PROPERTIES.getProperty("backgroundSelectedColor") + ";\n" +
            "-button-color: " + PROPERTIES.getProperty("unselectedButtonColor") + ";\n" +
            "-disabled-color: " + PROPERTIES.getProperty("disabledColor") + ";\n" +
            "-selected-button-color: " + PROPERTIES.getProperty("selectedButtonColor") + ";\n" +
            "-highlight-color: " + PROPERTIES.getProperty("highlightColor") + ";\n" +
            "-separator-color: " + PROPERTIES.getProperty("separatorColor") + ";\n" +
            "-accent-color: " + PROPERTIES.getProperty("accentColor") + ";";

        System.out.println(string);

        if (editor != null) {
            editor.setCss(string);
        }
        settings.setCss(string);
    }

    public static void showOptions() {}

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

    public static void main(String[] args) {
        launch(args);
    }
}
