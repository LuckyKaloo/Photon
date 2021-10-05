package application.Controller;

import io.github.palexdev.materialfx.controls.MFXProgressBar;
import javafx.animation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class SplashScreen {
    private final static int NUM_PETALS = 8;
    private final static double MULTIPLIER_ANGLE = 360.0 / NUM_PETALS;

    private final static double FLOWER_WIDTH = 270;
    private final static double PETAL_WIDTH = 26;
    private final static double INITIAL_PETAL_LENGTH = 87;
    private final static double FINAL_PETAL_LENGTH = 78;

    private final static double CENTER = FLOWER_WIDTH / 2;
    private final static double DISTANCE_CONTRACT = CENTER - INITIAL_PETAL_LENGTH;

    private final static Duration BLOOM_DURATION = Duration.millis(700);
    private final static double X1 = 0.503;
    private final static double Y1 = 0.148;
    private final static Interpolator INTERPOLATOR = Interpolator.SPLINE(X1, Y1, 1- X1, 1- Y1);

    private final static double FONT_SIZE = 200;

    private final static double PROGRESS_BAR_HEIGHT = 5;
    private final static double PROGRESS_BAR_LENGTH = 1000;

    private final static double HBOX_SPACING = 70;
    private final static double VBOX_SPACING = 80;

    private final static Color[] COLORS = new Color[]{
            Color.web("#f47777"),
            Color.web("#ffb660"),
            Color.web("#fbff76"),
            Color.web("#a1ff8e"),
            Color.web("#7bf3ff"),
            Color.web("#79acff"),
            Color.web("#a697ff"),
            Color.web("#f894f8")};
    private final static String BACKGROUND_COLOR = "#0a0012";
    private final static Color TEXT_COLOR = Color.rgb(247, 251, 255);

    private final static Timeline[] CONTRACT_TIMELINES = new Timeline[NUM_PETALS];

    private final static Duration TIMED_DURATION = Duration.millis(1);


    public static Scene generateScene() {
        // adding the flower rainbow
        AnchorPane flower = new AnchorPane();
        flower.setMinSize(FLOWER_WIDTH, FLOWER_WIDTH);
        flower.setPrefSize(FLOWER_WIDTH, FLOWER_WIDTH);
        flower.setMaxSize(FLOWER_WIDTH, FLOWER_WIDTH);

        for (int i = 0; i < NUM_PETALS; i++) {
            double angle = MULTIPLIER_ANGLE * i + 180;

            Region region = new Region();
            region.setPrefHeight(PETAL_WIDTH);
            region.setPrefWidth(INITIAL_PETAL_LENGTH);
            Color color = COLORS[i];
            region.setStyle("-fx-background-color: rgb(" +
                    (color.getRed() * 255) + ", " + (color.getGreen() * 255) + ", " + (color.getBlue() * 255) + ");" +
                    " -fx-border-radius:" + PETAL_WIDTH / 2 + ";" +
                    " -fx-background-radius:" + PETAL_WIDTH / 2 + ";");

            region.setLayoutX(CENTER + DISTANCE_CONTRACT);
            region.setLayoutY(CENTER - PETAL_WIDTH / 2);

            Rotate rotate = new Rotate(angle, -DISTANCE_CONTRACT, PETAL_WIDTH / 2);
            region.getTransforms().add(rotate);

            Timeline contract = new Timeline(new KeyFrame(BLOOM_DURATION,
                    new KeyValue(region.prefWidthProperty(), FINAL_PETAL_LENGTH, INTERPOLATOR),
                    new KeyValue(region.layoutXProperty(), CENTER + DISTANCE_CONTRACT, INTERPOLATOR),
                    new KeyValue(rotate.pivotXProperty(), -DISTANCE_CONTRACT, INTERPOLATOR)));

            contract.setAutoReverse(true);
            contract.setCycleCount(Animation.INDEFINITE);

            CONTRACT_TIMELINES[i] = contract;

            flower.getChildren().add(region);
        }

        // adding the text to say the name of the program
        Text text = new Text("Photon");
        try {
            text.setFont(Font.loadFont(new FileInputStream("src/application/Resources/VarelaRound-Regular.ttf"), FONT_SIZE));
            text.setFill(TEXT_COLOR);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(HBOX_SPACING);
        hbox.getChildren().addAll(flower, text);


        MFXProgressBar progressBar = new MFXProgressBar(0);
        progressBar.setId("progress-bar");
        progressBar.setPrefSize(PROGRESS_BAR_LENGTH, PROGRESS_BAR_HEIGHT);


        StringProperty css = new SimpleStringProperty();
        double portion = 1.0 / COLORS.length;
        ArrayList<Color> colorList = new ArrayList<>(Arrays.asList(COLORS));
        Animation animation = new Transition() {
            {
                setCycleDuration(TIMED_DURATION);
            }

            @Override
            protected void interpolate(double v) {
                progressBar.setProgress(v);

                int numColors = (int) (v / portion);
                double percentage = v / portion - numColors;

                ArrayList<Color> includedColors;
                if (numColors < colorList.size() - 1) {
                    Color startColor = colorList.get(numColors);
                    Color endColor = colorList.get(numColors + 1);
                    Color interpolatedColor = interpolatePreMultiplied(percentage, startColor, endColor);

                    includedColors = new ArrayList<>(colorList.subList(0, numColors + 1));
                    includedColors.add(interpolatedColor);
                } else {
                    includedColors = colorList;
                }


                StringBuilder stringBuilder = new StringBuilder("-gradient-info: linear-gradient(to bottom right");
                double newPortion = 100.0 / (includedColors.size() - 1);
                for (int i = 0; i < includedColors.size(); i++) {
                    Color color = includedColors.get(i);
                    stringBuilder.append(",\nderive(rgb(")
                            .append(color.getRed() * 255).append(", ")
                            .append(color.getGreen() * 255).append(", ")
                            .append(color.getBlue() * 255).append("), -20%) ").append(newPortion * i).append("%");
                }
                stringBuilder.append(");");

                css.set(stringBuilder.toString());
            }
        };
        animation.setOnFinished(e -> Main.showEditor());

        progressBar.styleProperty().bind(css);
        progressBar.getStylesheets().add(Objects.requireNonNull(
                SplashScreen.class.getResource("/application/View/progressBar.css")).toExternalForm());

        animation.play();


        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(VBOX_SPACING);
        vbox.setStyle("-fx-background-color: " + BACKGROUND_COLOR);
        vbox.getChildren().addAll(hbox, progressBar);


        return new Scene(vbox);
    }

    public static Color interpolatePreMultiplied(double ratio, Color origin, Color target) {
        var po = toPreMultiplied(origin);
        var pt = toPreMultiplied(target);
        return toStraightAlpha(po.interpolate(pt, ratio));
    }

    public static Color toPreMultiplied(Color color) {
        return Color.color(
                color.getRed() * color.getOpacity(),
                color.getGreen() * color.getOpacity(),
                color.getBlue() * color.getOpacity(),
                color.getOpacity()
        );
    }

    public static Color toStraightAlpha(Color color) {
        return color.getOpacity() == 0.0 ? Color.TRANSPARENT : Color.color(
                color.getRed() / color.getOpacity(),
                color.getGreen() / color.getOpacity(),
                color.getBlue() / color.getOpacity(),
                color.getOpacity()
        );
    }

    public static void play() {
        for (int i = 0; i < NUM_PETALS; i++) {
            CONTRACT_TIMELINES[i].playFrom(BLOOM_DURATION.multiply(1.0 * i / NUM_PETALS));
        }
    }

    public static void finish() {
        for (int i = 0; i < NUM_PETALS; i++) {
            CONTRACT_TIMELINES[i].stop();
        }
    }
}
