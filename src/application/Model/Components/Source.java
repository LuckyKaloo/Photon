package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import application.Model.Light.Beam;
import application.Model.Light.LightRay;

import java.util.ArrayList;

public class Source implements Component {
    private final int WIDTH = 5;

    private final Point start;
    private Edge edge;
    private final Beam beam;

    public Source(Point start) {
        this.start = start;

        double startX = start.X() + WIDTH;
        double startY = start.Y();
        double endX = start.X() - WIDTH;
        double endY = start.Y();
        edge = new Edge(new Point(startX, startY), new Point(endX, endY), Edge.ABSORBER);

        beam = new Beam(new LightRay(90, start, 1));
    }

    public Edge getEdge() {
        return edge;
    }

    public Beam getBeam() {
        return beam;
    }

    public void setAngle(double angle) {
        double angleRadians = Math.toRadians(angle);
        double startX = start.X() + WIDTH * Math.cos(angleRadians);
        double startY = start.Y() + WIDTH * Math.sin(angleRadians);
        double endX = start.X() - WIDTH * Math.cos(angleRadians);
        double endY = start.Y() - WIDTH * Math.sin(angleRadians);
        edge = new Edge(new Point(startX, startY), new Point(endX, endY), Edge.ABSORBER);

        beam.setAngle(angle + 90);
    }

    @Override
    public LightRay interact(LightRay lightRay, ArrayList<Component> components) {
        return null;
    }

    @Override
    public Point intersection(LightRay lightRay) {
        return edge.intersection(lightRay);
    }
}
