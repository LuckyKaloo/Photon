package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import application.Model.Geometry.Segment;

public class Edge extends Segment {
    public final static int REFLECTOR = 0;
    public final static int REFRACTOR = 1;
    public final static int ABSORBER = 2;

    private final int type;
    public Edge(Point start, Point end, int type) {
        super(start, end);
        if (type < 0 || type > 2) {
            throw new IllegalArgumentException("Invalid type for edge!");
        }
        this.type = type;
    }

    public Ray interact(Ray ray) {
        if (type == ABSORBER) {
            return null;
        } else if (type == REFLECTOR) {
            return interact(ray, 0, 0);
        }

        throw new IllegalCallerException("Cannot call method with only ray for refractor!");
    }

    public Ray interact(Ray ray, double startIndex, double endIndex) {
        if (type == ABSORBER) {
            return null;
        }

        Point intersection = intersection(ray);
        if (intersection == null) {
            return null;
        }

        double initialAngle = ray.getAngle() - angle;  // angle of the ray relative to the edge
        double normalisedAngle = Math.abs(initialAngle % 180);
        double angleIncidence = Math.abs(90 - normalisedAngle);

        if (type == REFLECTOR || endIndex == 0 || startIndex == 0) {
            double finalAngle = (reflect(initialAngle, normalisedAngle, angleIncidence) + angle) % 360;
            return new Ray(finalAngle, intersection);
        } else if (type == REFRACTOR) {
            double finalAngle =
                    (refract(initialAngle, normalisedAngle, angleIncidence, startIndex, endIndex) + angle) % 360;

            return new Ray(finalAngle, intersection);
        }

        throw new IllegalStateException("Edge type is not valid!");
    }

    // methods to return the relative angle of the outgoing ray to the edge
    private double refract(double initialAngle, double normalisedAngle, double angleIncidence,
                           double startIndex, double endIndex) {

        double angleRefraction;
        double relativeFinalAngle;
        if (endIndex == 0) {  // always reflection
            angleRefraction = 180;  // more than 90 -> goes into reflection case
        } else {  // n1 * sin(angle1) = n2 * sin(angle2)
            angleRefraction = Math.toDegrees(Math.asin(
                    Math.sin(Math.toRadians(angleIncidence)) * startIndex / endIndex));
        }

        if (angleRefraction <= 90) {  // normal refraction
            if (initialAngle > 180) {
                if (normalisedAngle > 90) {
                    return 270 + angleRefraction;
                } else {
                    return 270 - angleRefraction;
                }
            } else {
                if (normalisedAngle > 90) {
                    return 90 + angleRefraction;
                } else {
                    return 90 - angleRefraction;
                }
            }
        } else {
            return reflect(initialAngle, normalisedAngle, angleIncidence);
        }
    }

    private double reflect(double relativeInitialAngle, double normalisedAngle, double angleIncidence) {
        if (relativeInitialAngle > 180) {
            if (normalisedAngle > 90) {
                return 90 - angleIncidence;
            } else {
                return 90 + angleIncidence;
            }
        } else {
            if (normalisedAngle > 90) {
                return 270 - angleIncidence;
            } else {
                return 270 + angleIncidence;
            }
        }
    }
}
