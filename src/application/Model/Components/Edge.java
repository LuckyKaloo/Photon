package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Geometry.Segment;
import application.Model.Light.LightRay;

public class Edge extends Segment {
    public final static int REFLECTOR = 0;
    public final static int REFRACTOR = 1;
    public final static int ABSORBER = 2;

    private int type;


    public Edge(Point start, Point end, int type) {
        super(start, end);
        setType(type);
    }

    public Edge(Segment segment, int type) {
        super(segment);
        setType(type);
    }

    public void setType(int type) {
        if (type == REFLECTOR || type == REFRACTOR || type == ABSORBER) {
            this.type = type;
        } else {
            throw new IllegalArgumentException("Invalid type for edge!");
        }
    }

    public LightRay interact(LightRay lightRay) {
        if (type == ABSORBER) {
            return null;
        } else if (type == REFLECTOR) {
            return interact(lightRay, 0);
        }

        throw new IllegalCallerException("Cannot call method with only ray for refractor!");
    }

    public LightRay interact(LightRay lightRay, double endIndex) {
        if (type == ABSORBER) {
            return null;
        }

        Point intersection = intersection(lightRay);
        if (intersection == null) {
            return null;
        }

        double initialAngle = lightRay.getAngle() - angle;  // angle of the ray relative to the edge
        if (initialAngle < 0) {
            initialAngle += 360;
        }

        double normalisedAngle = Math.abs(initialAngle % 180);
        double angleIncidence = Math.abs(90 - normalisedAngle);

        if (type == REFLECTOR || endIndex == 0 || lightRay.getRefractiveIndex() == 0) {
            return reflect(initialAngle, normalisedAngle, angleIncidence,
                    lightRay.getRefractiveIndex(), intersection);
        } else if (type == REFRACTOR) {
            return refract(initialAngle, normalisedAngle, angleIncidence,
                    lightRay.getRefractiveIndex(), endIndex, intersection);
        }

        throw new IllegalStateException("Edge type is not valid!");
    }

    // helper methods to return the relative angle of the outgoing ray to the edge
    private LightRay refract(double relativeInitialAngle, double normalisedAngle, double angleIncidence,
                           double startIndex, double endIndex, Point intersection) {

        double angleRefraction;
        if (endIndex == 0) {  // always reflection
            angleRefraction = 180;  // more than 90 -> goes into reflection case
        } else {  // n1 * sin(angle1) = n2 * sin(angle2)
            angleRefraction = Math.toDegrees(Math.asin(
                    Math.sin(Math.toRadians(angleIncidence)) * startIndex / endIndex));
        }

        double relativeFinalAngle;
        if (angleRefraction <= 90) {  // normal refraction
            if (relativeInitialAngle > 180) {
                if (normalisedAngle > 90) {
                    relativeFinalAngle = 270 + angleRefraction;
                } else {
                    relativeFinalAngle = 270 - angleRefraction;
                }
            } else {
                if (normalisedAngle > 90) {
                    relativeFinalAngle = 90 + angleRefraction;
                } else {
                    relativeFinalAngle = 90 - angleRefraction;
                }
            }

            double finalAngle = (relativeFinalAngle + angle) % 360;
            return new LightRay(finalAngle, intersection, endIndex);
        } else {  // total internal reflection
            return reflect(relativeInitialAngle, normalisedAngle, angleIncidence, startIndex, intersection);
        }
    }

    private LightRay reflect(double relativeInitialAngle, double normalisedAngle, double angleIncidence,
                             double startIndex, Point intersection) {

        double relativeFinalAngle;
        if (relativeInitialAngle > 180) {
            if (normalisedAngle > 90) {
                relativeFinalAngle = 90 - angleIncidence;
            } else {
                relativeFinalAngle = 90 + angleIncidence;
            }
        } else {
            if (normalisedAngle > 90) {
                relativeFinalAngle = 270 - angleIncidence;
            } else {
                relativeFinalAngle = 270 + angleIncidence;
            }
        }
        double finalAngle = (relativeFinalAngle + angle) % 360;

        return new LightRay(finalAngle, intersection, startIndex);
    }
}
