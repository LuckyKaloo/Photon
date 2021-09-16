package application.Model.Geometry;

public class Ray {
    protected double angle;
    protected Point start;

    protected double gradient;
    protected double yIntercept;

    Ray() {
    }

    public Ray(double angle, Point start) {
        this.angle = angle % 360;
        if (this.angle < 0) {
            this.angle += 360;
        }
        this.start = start;

        gradient = Math.tan(Math.toRadians(angle));
        yIntercept = start.getY() - gradient * start.getX();
    }

    public Ray(Ray ray) {
        this.angle = ray.angle;
        this.start = ray.start;
        this.gradient = ray.gradient;
        this.yIntercept = ray.yIntercept;
    }

    public void setAngle(double angle) {
        this.angle = angle % 360;
        if (this.angle < 0) {
            this.angle += 360;
        }
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public void updateRay() {
        gradient = Math.tan(Math.toRadians(angle));
        yIntercept = start.getY() - gradient * start.getX();
    }

    public double getAngle() {
        return angle;
    }

    public Point getStart() {
        return start;
    }

    public Point infiniteLineIntersection(Ray ray) {
        // calculating intersection of the 2 rays as if they were lines extending to infinity
        if ((angle - ray.angle) % 180 == 0) {
            return null;
        }

        double x, y;
        if (angle == 90 || angle == 270) {
            x = start.getX();
            y = ray.gradient * x + ray.yIntercept;
        } else if (ray.angle == 90 || ray.angle == 270) {
            x = ray.start.getX();
            y = gradient * x + yIntercept;
        } else {
            x = (ray.yIntercept - yIntercept) / (gradient - ray.gradient);
            y = (gradient * x + yIntercept);
        }

        return new Point(x, y);
    }

    public Point intersection(Ray ray) {
        Point intersection = infiniteLineIntersection(ray);

        if (this.containsIntersection(intersection) && ray.containsIntersection(intersection)) {
            return intersection;
        }

        return null;
    }

    public boolean containsIntersection(Point point) {
        if ((angle < 90 || angle > 270) && point.getX() < start.getX()) {  // ray only goes to the right
            return false;
        } else if ((angle > 90 && angle < 270) && point.getX() > start.getX()) {  // ray only goes to the left
            return false;
        }

        // ray only goes down
        if (angle > 0 && angle < 180 && point.getY() < start.getY()) {  // ray only goes up
            return false;
        } else return !(angle > 180) || !(point.getY() > start.getY());
    }

    public static double angleTo(Point start, Point end) {
        if (start.equals(end)) {
            throw new IllegalArgumentException("Points are the same, cannot calculate angle between them");
        }

        if (start.getX() ==  end.getX()) {
            if (start.getY() < end.getY()) {
                return 90;
            } else {
                return 270;
            }
        }

        double basicAngle = Math.toDegrees(Math.atan((end.getY() - start.getY()) / (end.getX() - start.getX())));
        if (end.getX() > start.getX()) {  // quadrant 1 or 4
            return basicAngle;
        } else {  // quadrant 2 or 3
            return 180 + basicAngle;
        }
    }

    @Override
    public String toString() {
        return "Start: " + start + ", angle: " + angle;
    }
}
