package application.Model.Geometry;

import java.util.ArrayList;

public class Segment extends Ray {
    protected Point end;

    public Segment(Point start, Point end) {
        if (start.getX() == end.getX() && start.getY() == end.getY()) {
            throw new IllegalArgumentException("Points are the same!" + start);
        } else {
            this.gradient = (end.getY() - start.getY()) / (end.getX() - start.getX());
            this.angle = Math.toDegrees(Math.atan(this.gradient)) % 180;
            if (this.angle < 0) {
                this.angle += 180;
            }
        }
        this.yIntercept = start.getY() - this.gradient * start.getX();

        this.start = start;
        this.end = end;
    }

    public Segment(Segment segment) {
        this(segment.start, segment.end);
    }

    @Override
    public void setAngle(double angle) {
        Point midPoint = new Point((start.getX() + end.getX()) / 2, (start.getY() + end.getY()) / 2);
        double width = Math.sqrt((start.getX() - end.getX()) * (start.getX() - end.getX()) +
                (start.getY() - end.getY()) * (start.getY() - end.getY()));
        double radius = width / 2;

        double actualAngle = angle % 180;
        if (actualAngle < 0) {
            actualAngle += 180;
        }
        actualAngle = Math.toRadians(actualAngle);

        start = new Point(midPoint.getX() - radius * Math.cos(actualAngle), midPoint.getY() - radius * Math.sin(actualAngle));
        end = new Point(midPoint.getX() + radius * Math.cos(actualAngle), midPoint.getY() + radius * Math.sin(actualAngle));

        updateSegment();
    }

    public Point getEnd() {
        return end;
    }

    public void updateSegment() {
        this.gradient = (end.getY() - start.getY()) / (end.getX() - start.getX());
        this.angle = Math.toDegrees(Math.atan(this.gradient)) % 180;
        if (this.angle < 0) {
            this.angle += 180;
        }
        this.yIntercept = start.getY() - this.gradient * start.getX();
    }

    @Override
    public boolean containsIntersection(Point point) {
        if (point == null) {
            return false;
        }

        // check if point is outside bounds of line on X-axis
        if (point.getX() < start.getX() && point.getX() < end.getX() ||
                point.getX() > start.getX() && point.getX() > end.getX()) {

            return false;
        }

        // check if point is outside bounds of line on Y-axis
        return (!(point.getY() < start.getY()) || !(point.getY() < end.getY())) &&
                (!(point.getY() > start.getY()) || !(point.getY() > end.getY()));
    }

    public boolean containsPoint(Point point) {
        return containsPoint(point, 1);
    }

    public boolean containsPoint(Point point, double maxDistance) {
        if (Point.distance(point, this) > maxDistance) {
            return false;
        }

        // check if point is outside bounds of line on X-axis
        if (point.getX() < start.getX() && point.getX() < end.getX() ||
                point.getX() > start.getX() && point.getX() > end.getX()) {

            return false;
        }

        // check if point is outside bounds of line on Y-axis
        return (!(point.getY() < start.getY()) || !(point.getY() < end.getY())) &&
                (!(point.getY() > start.getY()) || !(point.getY() > end.getY()));
    }

    @Override
    public String toString() {
        return "Start: " + start + " End: " + end;
    }

    public static ArrayList<Segment> pointsToSegments(ArrayList<Point> points, boolean joinStartAndEnd) {
        ArrayList<Segment> segments = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            segments.add(new Segment(points.get(i), points.get(i + 1)));
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
        for (int i = 1; i < segments.size() - 1; i++) {
            if (segments.get(i).start.equals(points.get(points.size() - 1))) {
                points.add(segments.get(i).end);
            } else {
                throw new IllegalArgumentException("Segments must form a closed loop in order!");
            }
        }

        return points;
    }

    public Point midpoint() {
        double x = (start.getX() + end.getX()) / 2;
        double y = (start.getY() + end.getY()) / 2;
        return new Point(x, y);
    }
}
