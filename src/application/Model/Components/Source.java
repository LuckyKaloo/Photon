package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import application.Model.Light.Beam;
import application.Model.Light.LightRay;

public class Source implements Component {
    private Edge edge;
    private Beam beam;

    public Source(Ray ray) {
        beam = new Beam(ray);
        double startX = ray.getStart().X() + 5 * Math.cos(Math.toRadians(ray.getAngle()));
        double startY = ray.getStart().Y() + 5 * Math.sin(Math.toRadians(ray.getAngle()));
        double endX = ray.getStart().X() - 5 * Math.cos(Math.toRadians(ray.getAngle()));
        double endY = ray.getStart().Y() - 5 * Math.sin(Math.toRadians(ray.getAngle()));
        edge = new Edge(new Point(startX, startY), new Point(endX, endY), Edge.ABSORBER);
    }

    public Source(double angle, Point start) {
        this(new Ray(angle, start));
    }

    @Override
    public LightRay interact(LightRay lightRay) {
        return edge.interact(lightRay);
    }

    @Override
    public Point intersection(LightRay lightRay) {
        return edge.intersection(lightRay);
    }
}
