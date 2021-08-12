package application.Model.Light;

import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;

public class LightRay extends Ray implements LightComponent {
    private final double refractiveIndex;

    public LightRay(double angle, Point start, double refractiveIndex) {
        super(angle, start);
        this.refractiveIndex = refractiveIndex;
    }

    public LightRay(Ray ray, double refractiveIndex) {
        super(ray);
        this.refractiveIndex = refractiveIndex;
    }

    @Override
    public double getRefractiveIndex() {
        return refractiveIndex;
    }

    @Override
    public String toString() {
        return start + " angle: " + angle + " refractive index: " + refractiveIndex;
    }
}
