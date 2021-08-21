package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.Beam;
import application.Model.Light.LightRay;
import javafx.scene.paint.Color;

public class Source implements Component {
    private final Beam beam;

    public Source(Point start) {
        beam = new Beam(new LightRay(0, start, null));
    }

    public Source(Point start, double angle, Color color) {
        beam = new Beam(new LightRay(angle, start, null));
        beam.setColor(color);
    }

    public Beam getBeam() {
        return beam;
    }

    @Override
    public void update() {
        beam.getInitialRay().updateRay();
    }

    @Override
    public LightRay interact(LightRay lightRay) {
        return null;
    }

    @Override
    public Point intersection(LightRay lightRay) {
        return null;
    }

    @Override
    public String toData() {
        return "Source{\n" +
                beam.getInitialRay().getStart().toData() + "\n" +
                "Angle:" + beam.getInitialRay().getAngle() + "\n" +

                // adding the color data
                "Color:(" + beam.getColor().getRed() + "," +
                beam.getColor().getGreen() + "," +
                beam.getColor().getBlue() + ")\n" +
                "}\n";
    }
}
