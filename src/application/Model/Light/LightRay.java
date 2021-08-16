package application.Model.Light;

import application.Model.Components.Shape;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;

import java.util.ArrayList;

public class LightRay extends Ray implements LightComponent {
    private final Shape shape;

    public LightRay(double angle, Point start, Shape shape) {
        super(angle, start);
        this.shape = shape;
    }

    public LightRay(Ray ray, Shape shape) {
        super(ray);
        this.shape = shape;
    }

    @Override
    public Shape getShape() {
        return shape;
    }

    @Override
    public String toString() {
        return start + " angle: " + angle + " refractive index: " + shape.getRefractiveIndex();
    }
}
