package application.Model.Geometry;

import java.util.regex.Pattern;

public final class Point {
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
        // error is that the distance is calculated as if the line extends to infinity and does not consider the endpoints
        Point projection = segment.infiniteLineIntersection(new Ray(segment.getAngle() + 90, point));
        Point start = segment.start;
        Point end = segment.end;
        if (segment.containsIntersection(projection)) {
            return Point.distance(point, projection);
        } else {
            return Math.min(Point.distance(point, start), Point.distance(point, end));
        }
    }

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f)", x, y);
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

    public String toData() {
        return "Point: (" + x + "," + y + ")";
    }

    public static Point parseData(String data) {
        if (!Pattern.matches("Point:.*", data)) {
            throw new IllegalArgumentException("Data for point is not valid!");
        } else {
            String[] coordinates = data.substring(6).replaceAll("[()]", "").split(",");
            try {
                return new Point(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Data for point is not valid!");
            }
        }
    }
}
