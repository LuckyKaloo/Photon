package application.Model.Light;

import application.Model.Geometry.Point;
import application.Model.Geometry.Segment;

public class LightSegment extends Segment implements LightComponent {
    private final double refractiveIndex;

    public LightSegment(Point start, Point end, double refractiveIndex) {
        super(start, end);
        this.refractiveIndex = refractiveIndex;
    }

    public LightSegment(LightRay lightRay, Point end) {
        this(lightRay.getStart(), end, lightRay.getRefractiveIndex());
    }

    @Override
    public double getRefractiveIndex() {
        return refractiveIndex;
    }

    @Override
    public String toString() {
        return start + " " + end + " refractive index: " + refractiveIndex;
    }
}
