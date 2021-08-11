package application.Model.Geometry;

import java.io.Serializable;

public record Point(double X, double Y) implements Serializable {
    // distance between 2 points
    public static double distance(Point point1, Point point2) {
        return Math.sqrt((point1.X - point2.X) * (point1.X - point2.X) +
                (point1.Y - point2.Y) * (point1.Y - point2.Y));
    }

    public static double distance(Point point, Ray ray) {
        Point start = ray.start;
        Point end = new Point(ray.start.X + 10000 * Math.cos(Math.toRadians(ray.getAngle())),
                ray.start.Y + 10000 * Math.sin(Math.toRadians(ray.getAngle())));
        return distance(point, new Segment(start, end));
    }

    // shortest distance between a point and a line -> https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
    public static double distance(Point point, Segment segment) {
        Point start = segment.start;
        Point end = segment.end;
        double numerator = Math.abs((end.X - start.X) * (start.Y - point.Y) - (start.X - point.X) * (end.Y - start.Y));
        double denominator = Math.sqrt((end.X - start.X) * (end.X - start.X) + (end.Y - start.Y) * (end.Y - start.Y));
        return numerator / denominator;
    }

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f)", X, Y);
    }


    // prints the X and Y coordinates to more decimal places
    public String fullToString() {
        return "(" + X + "," + Y + ")";
    }


    public boolean equals(Point point) {
        return equals(point, 1);
    }

    public boolean equals(Point point, double marginError) {
        return distance(this, point) < marginError;
    }
}
