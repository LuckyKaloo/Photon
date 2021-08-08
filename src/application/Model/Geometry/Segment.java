package application.Model.Geometry;

public class Segment extends Ray {
    protected final Point end;

    public Segment(Point start, Point end) {
        if (start.X() == end.X() && start.Y() == end.Y()) {
            throw new IllegalArgumentException("Points are the same!");
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

    public Point getEnd() {
        return end;
    }

    @Override
    protected boolean containsIntersection(Point point) {
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
}
