package application.Model.Light;

import application.Model.Geometry.Point;
import application.Model.Geometry.Segment;

public class LightSegment extends Segment implements LightComponent {
    private final double refractiveIndex;

    public LightSegment(Point start, Point end, double refractiveIndex) {
        super(start, end);
        this.refractiveIndex = refractiveIndex;
    }

    @Override
    public double getRefractiveIndex() {
        return refractiveIndex;
    }
}
