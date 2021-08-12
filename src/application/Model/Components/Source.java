package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.Beam;
import application.Model.Light.LightRay;

public class Source implements Component {
    private final Beam beam;

    public Source(Point start) {
        beam = new Beam(new LightRay(0, start, 1));
    }

    public Point getStart() {
        return beam.getInitialRay().getStart();
    }

    public Beam getBeam() {
        return beam;
    }

    public void setAngle(double angle) {
        beam.setAngle(angle);
    }

    @Override
    public LightRay interact(LightRay lightRay) {
        return null;
    }

    @Override
    public Point intersection(LightRay lightRay) {
        return null;
    }
}
