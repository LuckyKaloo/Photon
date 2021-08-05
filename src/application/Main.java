package application;

import application.Model.Components.Edge;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
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


        Ray beam = new Ray(116.4, new Point(250, 300));
        Edge edge = new Edge(new Point(100, 200), new Point(300, 700), Edge.REFRACTOR);
        Point intersection = beam.intersection(edge);
        System.out.println(intersection);

        Ray outgoing = edge.interact(beam, 1.5, 1);
        System.out.println(outgoing);

        graphicsContext.beginPath();
        graphicsContext.moveTo(edge.getStart().getX(), edge.getStart().getY());
        graphicsContext.setStroke(Color.rgb(220, 150, 150));
        graphicsContext.lineTo(edge.getEnd().getX(), edge.getEnd().getY());
        graphicsContext.stroke();
        graphicsContext.closePath();

        graphicsContext.beginPath();
        graphicsContext.moveTo(beam.getStart().getX(), beam.getStart().getY());
        graphicsContext.lineTo(intersection.getX(), intersection.getY());
        graphicsContext.stroke();
        graphicsContext.closePath();

        graphicsContext.beginPath();
        graphicsContext.moveTo(intersection.getX(), intersection.getY());
        graphicsContext.lineTo(intersection.getX() + 800 * Math.cos(Math.toRadians(outgoing.getAngle())),
                intersection.getY() + 800 * Math.sin(Math.toRadians(outgoing.getAngle())));
        graphicsContext.stroke();
        graphicsContext.closePath();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
