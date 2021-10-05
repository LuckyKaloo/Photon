package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Geometry.Segment;
import application.Model.Light.LightRay;
import application.Model.Light.Normal;

public class Edge extends Segment {
    public enum EdgeType {REFLECTOR, REFRACTOR, ABSORBER}

    private final EdgeType type;


    public Edge(Point start, Point end, EdgeType type) {
        super(start, end);
        this.type = type;
    }

    public Edge(Segment segment, EdgeType type) {
        super(segment);
        this.type = type;
    }

    public EdgeType getType() {
        return type;
    }

    public LightRay interact(LightRay lightRay) {
        return switch(type) {
            case ABSORBER -> null;
            case REFLECTOR -> interact(lightRay, null, true);
            default -> throw new IllegalCallerException("Cannot call method with only ray for refractor!");
        };
    }

    public LightRay interact(LightRay lightRay, Shape endShape, boolean reflect) {
        if (type == EdgeType.ABSORBER) {
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

        if (type == EdgeType.REFLECTOR || reflect || lightRay.getRefractiveIndex() == 0) {
            return reflect(initialAngle, normalisedAngle, angleIncidence,
                    lightRay.getShape(), intersection);
        } else if (type == EdgeType.REFRACTOR) {
            return refract(initialAngle, normalisedAngle, angleIncidence,
                    lightRay.getShape(), endShape, intersection);
        }

        throw new IllegalStateException("Edge type is not valid!");
    }

    // helper methods to return the relative angle of the outgoing ray to the edge
    private LightRay refract(double relativeInitialAngle, double normalisedAngle, double angleIncidence,
                             Shape startShape, Shape endShape, Point intersection) {

        double startIndex = startShape == null ? 1 : startShape.getRefractiveIndex();
        double endIndex = endShape == null ? 1 : endShape.getRefractiveIndex();

        double angleRefraction;
        if (endIndex == 0) {  // always reflection
            angleRefraction = 180;  // more than 90 -> goes into reflection case
        } else {  // n1 * sin(angle1) = n2 * sin(angle2)
            angleRefraction = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(angleIncidence)) *
                    startIndex) / endIndex);
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
            return new LightRay(finalAngle, intersection, endShape,
                    new Normal(this, intersection, angleIncidence, angleRefraction));
        } else {  // total internal reflection
            return reflect(relativeInitialAngle, normalisedAngle, angleIncidence, startShape, intersection);
        }
    }

    private LightRay reflect(double relativeInitialAngle, double normalisedAngle, double angleIncidence,
                             Shape startShape, Point intersection) {

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

        return new LightRay(finalAngle, intersection, startShape,
                new Normal(this, intersection, angleIncidence, angleIncidence));
    }

    public String getReference() {
        return ((Object) this).toString();
    }
}
