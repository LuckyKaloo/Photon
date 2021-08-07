package application.Model.Geometry;

import application.Model.Components.Component;
import application.Model.Components.Edge;
import application.Model.Light.LightRay;

import java.util.ArrayList;

public class Ray {
    protected double angle;
    protected Point start;

    protected double gradient;
    protected double yIntercept;

    Ray() {}

    public Ray(double angle, Point start) {
        this.angle = angle % 360;
        if (this.angle < 0) {
            this.angle += 360;
        }
        this.start = start;

        gradient = Math.tan(Math.toRadians(angle));
        yIntercept = start.Y() - gradient * start.X();
    }

    public Ray(Ray ray) {
        this.angle = ray.angle;
        this.start = ray.start;
        this.gradient = ray.gradient;
        this.yIntercept = ray.yIntercept;
    }

    public double getAngle() {
        return angle;
    }

    public Point getStart() {
        return start;
    }

    public Point intersection(Ray ray) {
        // calculating intersection of the 2 rays as if they were lines extending to infinity
        if (angle == ray.angle || angle == ray.angle + 180) {
            return null;
        }

        double x, y;
        if (angle == 90 || angle == 270) {
            x = start.X();
            y = ray.gradient * x + ray.yIntercept;
        } else if (ray.angle == 90 || ray.angle == 270) {
            x = ray.start.X();
            y = gradient * x + yIntercept;
        } else {
            x = (ray.yIntercept - yIntercept) / (gradient - ray.gradient);
            y = (gradient * x + yIntercept);
        }

        Point point = new Point(x, y);

        if (this.containsIntersection(point) && ray.containsIntersection(point)) {
            return point;
        }

        return null;
    }

    protected boolean containsIntersection(Point point) {
        if ((angle < 90 || angle > 270) && point.X() < start.X()) {  // ray only goes to the right
            return false;
        } else if ((angle > 90 && angle < 270) && point.X() > start.X()) {  // ray only goes to the left
            return false;
        }

        // ray only goes down
        if (angle < 180 && point.Y() < start.Y()) {  // ray only goes up
            return false;
        } else return !(angle > 180) || !(point.Y() > start.Y());
    }


    @Override
    public String toString() {
        return "Start: " + start + ", angle: " + angle;
    }
}
