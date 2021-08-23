package application.Model.Light;

import application.Model.Components.Shape;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;

public class LightRay extends Ray implements LightComponent {
    private final Shape shape;
    private final Normal normal;  // the normal that the light ray comes from

    public LightRay(double angle, Point start, Shape shape, Normal normal) {
        super(angle, start);
        this.normal = normal;
        this.shape = shape;
    }

    public LightRay(Ray ray, Shape shape, Normal normal) {
        super(ray);
        this.normal = normal;
        this.shape = shape;
    }

    @Override
    public Shape getShape() {
        return shape;
    }

    @Override
    public Normal getNormal() {
        return normal;
    }

    @Override
    public String toString() {
        return start + " angle: " + angle + " refractive index: " + shape.getRefractiveIndex();
    }
}
