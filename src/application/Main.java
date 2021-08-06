package application;

import application.Model.Components.Edge;
import application.Model.Geometry.Point;
import application.Model.Light.LightRay;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        BorderPane pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("View/primary.fxml")));
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        Canvas canvas = (Canvas)((Pane)pane.getCenter()).getChildren().get(0);
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();


        LightRay beam = new LightRay(116.4, new Point(250, 300), 1.5);
        Edge edge = new Edge(new Point(100, 200), new Point(300, 700), Edge.REFRACTOR);
        Point intersection = beam.intersection(edge);
        System.out.println(intersection);

        LightRay outgoing = edge.interact(beam, 1);
        System.out.println(outgoing);

        graphicsContext.beginPath();
        graphicsContext.moveTo(edge.getStart().X(), edge.getStart().Y());
        graphicsContext.setStroke(Color.rgb(220, 150, 150));
        graphicsContext.lineTo(edge.getEnd().X(), edge.getEnd().Y());
        graphicsContext.stroke();
        graphicsContext.closePath();

        graphicsContext.beginPath();
        graphicsContext.moveTo(beam.getStart().X(), beam.getStart().Y());
        graphicsContext.lineTo(intersection.X(), intersection.Y());
        graphicsContext.stroke();
        graphicsContext.closePath();

        graphicsContext.beginPath();
        graphicsContext.moveTo(intersection.X(), intersection.Y());
        graphicsContext.lineTo(intersection.X() + 800 * Math.cos(Math.toRadians(outgoing.getAngle())),
                intersection.Y() + 800 * Math.sin(Math.toRadians(outgoing.getAngle())));
        graphicsContext.stroke();
        graphicsContext.closePath();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
