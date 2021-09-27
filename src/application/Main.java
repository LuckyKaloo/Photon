package application;

import application.Controller.Editor;
import application.Controller.SplashScreen;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    private static Stage stage;

    private static Editor editor;

    @Override
    public void start(Stage stage) throws Exception {
        Main.stage = stage;
        stage.setTitle("Photon");
        stage.getIcons().add(new Image(new FileInputStream("src/application/Resources/images/logo.png")));
        stage.initStyle(StageStyle.TRANSPARENT);

        // showing the splash screen
        stage.setScene(SplashScreen.generateScene());
        stage.setMaximized(true);
        stage.show();
        SplashScreen.play();
    }

    public static void showOptions() {}

    public static void showEditor() {
        SplashScreen.finish();

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(Main.class.getResource("View/editor.fxml")));

            stage.getScene().setRoot(loader.load());
            stage.setMaximized(true);
            stage.show();

            editor = loader.getController();
            editor.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void minimize() {
        stage.setIconified(true);
    }

    public static void exit() {
        stage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
