package application.Model.Geometry;

import application.Model.Components.Edge;

import java.util.ArrayList;

public class Segment extends Ray {
    protected final Point end;

    public Segment(Point start, Point end) {
        if (start.X() == end.X() && start.Y() == end.Y()) {
            throw new IllegalArgumentException("Points are the same!" + start);
        } else {
            this.gradient = (end.Y() - start.Y()) / (end.X() - start.X());
            this.angle = Math.toDegrees(Math.atan(this.gradient)) % 180;
            if (this.angle < 0) {
                this.angle += 180;
            }
        }
        this.yIntercept = start.Y() - this.gradient * start.X();

        this.start = start;
        this.end = end;
    }

    public Segment(Segment segment) {
        this(segment.start, segment.end);
    }

    public Point getEnd() {
        return end;
    }

    @Override
    public boolean containsIntersection(Point point) {
        return containsIntersection(point, 1);
    }

    public boolean containsIntersection(Point point, double maxDistance) {
        if (Point.distance(point, this) > maxDistance) {
            return false;
        }

        // check if point is outside bounds of line on X-axis
        if (point.X() < start.X() && point.X() < end.X() ||
                point.X() > start.X() && point.X() > end.X()) {

            return false;
        }

        // check if point is outside bounds of line on Y-axis
        return (!(point.Y() < start.Y()) || !(point.Y() < end.Y())) &&
                (!(point.Y() > start.Y()) || !(point.Y() > end.Y()));
    }

    @Override
    public String toString() {
        return "Start: " + start + " End: " + end;
    }

    public static ArrayList<Segment> pointsToSegments(ArrayList<Point> points, boolean joinStartAndEnd) {
        ArrayList<Segment> segments = new ArrayList<>();
        for (int i = 0; i < points.size()-1; i++) {
            segments.add(new Segment(points.get(i), points.get(i+1)));
        }
        if (joinStartAndEnd) {
            segments.add(new Segment(points.get(0), points.get(points.size() - 1)));
        }

        return segments;
    }

    public static ArrayList<Point> segmentsToPoints(ArrayList<Segment> segments) {
        ArrayList<Point> points = new ArrayList<>();
        points.add(segments.get(0).start);
        points.add(segments.get(0).end);
        for (int i = 1; i < segments.size()-1; i++) {
            if (segments.get(i).start.equals(points.get(points.size()-1))) {
                points.add(segments.get(i).end);
            } else {
                throw new IllegalArgumentException("Segments must form a closed loop in order!");
            }
        }

        return points;
    }
}
