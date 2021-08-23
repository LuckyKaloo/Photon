package application.Model.Light;

import application.Model.Components.Shape;
import application.Model.Geometry.Point;
import application.Model.Geometry.Segment;

public class LightSegment extends Segment implements LightComponent {
    private final Shape shape;
    private final Normal normal;  // the normal at the light segment's start

    public LightSegment(LightRay lightRay, Point end) {
        super(lightRay.getStart(), end);
        this.shape = lightRay.getShape();
        this.normal = lightRay.getNormal();
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
        return start + " " + end + " refractive index: " + shape.getRefractiveIndex();
    }
}
