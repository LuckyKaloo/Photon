package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.Beam;
import application.Model.Light.LightRay;

public class Source implements Component {
    private final Beam beam;

    public Source(Point start) {
        beam = new Beam(new LightRay(0, start, 1));
    }

    public Beam getBeam() {
        return beam;
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
