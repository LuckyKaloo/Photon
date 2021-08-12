package application.Model.Geometry;

import java.io.Serializable;

public final class Point implements Serializable {
    private double x;
    private double y;

    public Point(double X, double Y) {
        this.x = X;
        this.y = Y;
    }

    // distance between 2 points
    public static double distance(Point point1, Point point2) {
        return Math.sqrt((point1.x - point2.x) * (point1.x - point2.x) +
                (point1.y - point2.y) * (point1.y - point2.y));
    }

    public static double distance(Point point, Ray ray) {
        Point start = ray.start;
        Point end = new Point(ray.start.x + 10000 * Math.cos(Math.toRadians(ray.getAngle())),
                ray.start.y + 10000 * Math.sin(Math.toRadians(ray.getAngle())));
        return distance(point, new Segment(start, end));
    }

    // shortest distance between a point and a line -> https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
    public static double distance(Point point, Segment segment) {
        Point start = segment.start;
        Point end = segment.end;
        double numerator = Math.abs((point.x - end.x) * (start.y - point.y) - (point.x - start.x) * (end.y - point.y));
        double denominator = Math.sqrt((end.x - start.x) * (end.x - start.x) + (end.y - start.y) * (end.y - start.y));

        return numerator / denominator;
    }

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f)", x, y);
    }


    // prints the X and Y coordinates to more decimal places
    public String fullToString() {
        return "(" + x + "," + y + ")";
    }


    public boolean equals(Point point) {
        return equals(point, 1);
    }

    public boolean equals(Point point, double marginError) {
        return distance(this, point) < marginError;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
