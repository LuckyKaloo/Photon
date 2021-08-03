package application.Model.Geometry;

public class Line extends Ray {
    private final Point end;

    public Line(Point start, Point end) {
        if (start.getX() == end.getX() && start.getY() == end.getY()) {
            throw new IllegalArgumentException("Points are the same!");
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

    public Point getEnd() {
        return end;
    }

    @Override
    protected boolean containsIntersection(Point point) {
        // check if point is outside bounds of line on x-axis
        if (point.getX() < start.getX() && point.getX() < end.getX() ||
                point.getX() > start.getX() && point.getX() > end.getX()) {

            return false;
        }

        // check if point is outside bounds of line on y-axis
        return (!(point.getY() < start.getY()) || !(point.getY() < end.getY())) &&
                (!(point.getY() > start.getY()) || !(point.getY() > end.getY()));
    }
}
