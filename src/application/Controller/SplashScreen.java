package application.Controller;

import application.Main;
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
    private final static int numPetals = 8;
    private final static double multiplierAngle = 360.0 / numPetals;

    private final static double flowerWidth = 270;
    private final static double petalWidth = 26;
    private final static double initialPetalLength = 87;
    private final static double finalPetalLength = 78;

    private final static double center = flowerWidth / 2;
    private final static double distanceContract = center - initialPetalLength;

    private final static Duration bloomDuration = Duration.millis(700);
    private final static double x1 = 0.503;
    private final static double y1 = 0.148;
    private final static Interpolator interpolator = Interpolator.SPLINE(x1, y1, 1-x1, 1-y1);

    private final static double fontSize = 200;

    private final static double progressBarHeight = 5;
    private final static double progressBarLength = 1000;

    private final static double hboxSpacing = 70;
    private final static double vboxSpacing = 80;

    private final static Color[] colors = new Color[]{
            Color.web("#f47777"),
            Color.web("#ffb660"),
            Color.web("#fbff76"),
            Color.web("#a1ff8e"),
            Color.web("#7bf3ff"),
            Color.web("#79acff"),
            Color.web("#a697ff"),
            Color.web("#f894f8")};
    private final static String backgroundColor = "#0a0012";
    private final static Color textColor = Color.rgb(247, 251, 255);

    private final static Timeline[] contractTimelines = new Timeline[numPetals];

    private final static Duration timedDuration = Duration.millis(2000);


    public static Scene generateScene() {
        // adding the flower rainbow
        AnchorPane flower = new AnchorPane();
        flower.setMinSize(flowerWidth,  flowerWidth);
        flower.setPrefSize(flowerWidth,  flowerWidth);
        flower.setMaxSize(flowerWidth,  flowerWidth);

        for (int i = 0; i < numPetals; i++) {
            double angle = multiplierAngle * i + 180;

            Region region = new Region();
            region.setPrefHeight(petalWidth);
            region.setPrefWidth(initialPetalLength);
            Color color = colors[i];
            region.setStyle("-fx-background-color: rgb(" +
                    (color.getRed() * 255) + ", " + (color.getGreen() * 255) + ", " + (color.getBlue() * 255) + ");" +
                    " -fx-border-radius:" + petalWidth / 2 + ";" +
                    " -fx-background-radius:" + petalWidth / 2 + ";");

            region.setLayoutX(center + distanceContract);
            region.setLayoutY(center - petalWidth / 2);

            Rotate rotate = new Rotate(angle, -distanceContract, petalWidth / 2);
            region.getTransforms().add(rotate);

            Timeline contract = new Timeline(new KeyFrame(bloomDuration,
                    new KeyValue(region.prefWidthProperty(), finalPetalLength, interpolator),
                    new KeyValue(region.layoutXProperty(), center + distanceContract, interpolator),
                    new KeyValue(rotate.pivotXProperty(), -distanceContract, interpolator)));

            contract.setAutoReverse(true);
            contract.setCycleCount(Animation.INDEFINITE);

            contractTimelines[i] = contract;

            flower.getChildren().add(region);
        }

        // adding the text to say the name of the program
        Text text = new Text("Photon");
        try {
            text.setFont(Font.loadFont(new FileInputStream("Resources/VarelaRound-Regular.ttf"), fontSize));
            text.setFill(textColor);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(hboxSpacing);
        hbox.getChildren().addAll(flower, text);


        MFXProgressBar progressBar = new MFXProgressBar(0);
        progressBar.setId("progress-bar");
        progressBar.setPrefSize(progressBarLength, progressBarHeight);


        StringProperty css = new SimpleStringProperty();
        double portion = 1.0 / colors.length;
        ArrayList<Color> colorList = new ArrayList<>(Arrays.asList(colors));
        Animation animation = new Transition() {
            {
                setCycleDuration(timedDuration);
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


                StringBuilder stringBuilder = new StringBuilder("gradient-info: linear-gradient(to bottom right");
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
        vbox.setSpacing(vboxSpacing);
        vbox.setStyle("-fx-background-color: " + backgroundColor);
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
        for (int i = 0; i < numPetals; i++) {
            contractTimelines[i].playFrom(bloomDuration.multiply(1.0 * i / numPetals));
        }
    }

    public static void finish() {
        for (int i = 0; i < numPetals; i++) {
            contractTimelines[i].stop();
        }
    }
}
