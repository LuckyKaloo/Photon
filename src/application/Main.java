package application;

import application.Model.Components.Component;
import application.Model.Components.Edge;
import application.Model.Components.Shape;
import application.Model.Geometry.Point;
import application.Model.Light.Beam;
import application.Model.Light.LightComponent;
import application.Model.Light.LightRay;
import application.Model.Light.LightSegment;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        BorderPane pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("View/primary.fxml")));
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        Canvas canvas = (Canvas)((Pane)pane.getCenter()).getChildren().get(0);
        canvas.setScaleY(-1);
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();


        ArrayList<Point> points1 = new ArrayList<>();
        points1.add(new Point(300, 200));
        points1.add(new Point(200, 300));
        points1.add(new Point(400, 250));
        Shape shape1 = new Shape(1.5, points1);

        ArrayList<Point> points2 = new ArrayList<>();
        points2.add(new Point(380, 600));
        points2.add(new Point(340, 350));
        points2.add(new Point(570, 480));
        points2.add(new Point(420, 550));
        Shape shape2 = new Shape(1.6, points2);

        ArrayList<Component> components = new ArrayList<>();
        components.add(shape1);
        components.add(shape2);

        // draw shape
        graphicsContext.setStroke(Color.rgb(150, 150, 220));
        for (Component component: components) {
            if (component instanceof Shape) {
                for (Edge edge : ((Shape) component).getEdges()) {
                    graphicsContext.beginPath();
                    graphicsContext.moveTo(edge.getStart().X(), edge.getStart().Y());
                    graphicsContext.lineTo(edge.getEnd().X(), edge.getEnd().Y());
                    graphicsContext.stroke();
                    graphicsContext.closePath();
                }
            }
        }

        Beam beam = new Beam(new LightRay(-70, new Point(240, 400), 1.5));
        beam.generateBeam(components);

        Random random = new Random();
        for (LightComponent lightComponent: beam.getLightComponents()) {
            graphicsContext.setStroke(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            graphicsContext.beginPath();
            if (lightComponent instanceof LightSegment lightSegment) {
                graphicsContext.moveTo(lightSegment.getStart().X(), lightSegment.getStart().Y());
                graphicsContext.lineTo(lightSegment.getEnd().X(), lightSegment.getEnd().Y());
            } else {
                LightRay lightRay = (LightRay) lightComponent;
                graphicsContext.moveTo(lightRay.getStart().X(), lightRay.getStart().Y());
                graphicsContext.lineTo(lightRay.getStart().X() + 800 * Math.cos(Math.toRadians(lightRay.getAngle())),
                        lightRay.getStart().Y() + 800 * Math.sin(Math.toRadians(lightRay.getAngle())));
            }
            graphicsContext.stroke();
            graphicsContext.closePath();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
