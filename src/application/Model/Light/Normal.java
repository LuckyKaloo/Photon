package application.Model.Light;

import application.Model.Components.Edge;
import application.Model.Geometry.Point;
import application.Model.Geometry.Segment;

public class Normal {
    private final Edge edge;
    private final Point intersection;
    private final double angleIncidence;
    private final double angleRefraction;

    private final static int NORMAL_WIDTH = 5;
    private final Segment normal;

    public Normal(Edge edge, Point intersection, double angleIncidence, double angleRefraction) {
        this.edge = edge;
        this.intersection = intersection;
        this.angleIncidence = angleIncidence;
        this.angleRefraction = angleRefraction;

        double startX = intersection.getX() + NORMAL_WIDTH * Math.cos(Math.toRadians(edge.getAngle() + 90));
        double startY = intersection.getY() + NORMAL_WIDTH * Math.sin(Math.toRadians(edge.getAngle() + 90));
        double endX = intersection.getX() - NORMAL_WIDTH * Math.cos(Math.toRadians(edge.getAngle() + 90));
        double endY = intersection.getY() - NORMAL_WIDTH * Math.sin(Math.toRadians(edge.getAngle() + 90));
        normal = new Segment(new Point(startX, startY), new Point(endX, endY));
    }

    public Edge getEdge() {
        return edge;
    }

    public Point getIntersection() {
        return intersection;
    }

    public double getAngleIncidence() {
        return angleIncidence;
    }

    public double getAngleRefraction() {
        return angleRefraction;
    }

    public Segment getNormal() {
        return normal;
    }
}
