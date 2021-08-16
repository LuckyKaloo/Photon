package application.Model.Light;

import application.Model.Components.Shape;
import application.Model.Geometry.Point;
import application.Model.Geometry.Segment;

public class LightSegment extends Segment implements LightComponent {
    private final Shape shape;

    public LightSegment(Point start, Point end, Shape shape) {
        super(start, end);
        this.shape = shape;
    }

    public LightSegment(LightRay lightRay, Point end) {
        this(lightRay.getStart(), end, lightRay.getShape());
    }

    @Override
    public Shape getShape() {
        return shape;
    }

    @Override
    public String toString() {
        return start + " " + end + " refractive index: " + shape.getRefractiveIndex();
    }
}
