package application.Model.Components;

import application.Model.Geometry.Line;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;

public class Material {
    private final double refractiveIndex;

    public Material(double refractiveIndex) {
        this.refractiveIndex = refractiveIndex;
    }

    public double getRefractiveIndex() {
        return refractiveIndex;
    }

    public static Ray refract(Ray ray, Line edge, Material start, Material end) {
        Point intersection = edge.intersection(ray);
        if (intersection == null) {
            return null;
        }

        double relativeInitialAngle = ray.getAngle() - edge.getAngle();
        double normalisedAngle = Math.abs(relativeInitialAngle % 180);
        double angleIncidence = Math.abs(90 - normalisedAngle);

        // n1 * sin(angle1) = n2 * sin(angle2)
        double angleRefraction = Math.toDegrees(Math.asin(
                Math.sin(Math.toRadians(angleIncidence)) * start.refractiveIndex / end.refractiveIndex));
        double relativeFinalAngle;

        if (angleRefraction <= 90) {
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
        } else {
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
        }

        double finalAngle = (relativeFinalAngle + edge.getAngle()) % 360;

        return new Ray(finalAngle, intersection);
    }
}
