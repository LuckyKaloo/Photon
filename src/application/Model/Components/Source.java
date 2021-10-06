package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.Beam;
import application.Model.Light.LightRay;
import javafx.scene.paint.Color;

public class Source implements Component {
    private String name;
    private boolean visible;

    private final Beam beam;

    public Source(Point start) {
        beam = new Beam(new LightRay(0, start, null, null));
        name = "Source";
        visible = true;
    }

    public Source(String name, boolean visible, Point start, double angle, Color color) {
        this.name = name;
        this.visible = visible;

        beam = new Beam(new LightRay(angle, start, null, null));
        beam.setColor(color);
    }

    public Beam getBeam() {
        return beam;
    }

    @Override
    public boolean getVisibility() {
        return visible;
    }

    @Override
    public void setVisibility(boolean visible) {
        this.visible = visible;
    }

    @Override
    public Source copy() {
        return new Source(name, visible, beam.getInitialRay().getStart(), beam.getInitialRay().getAngle(), beam.getColor());
    }

    @Override
    public Source translate(Point vector) {
        Source output = copy();
        output.beam.getInitialRay().getStart().translate(vector);
        output.update();
        return output;
    }

    @Override
    public void set(Component component) {
        if (component instanceof Source source) {
            beam.getInitialRay().setStart(source.beam.getInitialRay().getStart().copy());
            beam.getInitialRay().setAngle(source.beam.getInitialRay().getAngle());
            update();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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
        return "Source {\n" +
                "\tName: " + name + "\n" +
                "\tVisible: " + visible + "\n" +
                "\t" + beam.getInitialRay().getStart().toData() + "\n" +
                "\tAngle: " + beam.getInitialRay().getAngle() + "\n" +

                // adding the color data
                "\tColor: (" + beam.getColor().getRed() + "," +
                beam.getColor().getGreen() + "," +
                beam.getColor().getBlue() + ")\n" +
                "}\n";
    }

    @Override
    public String toString() {
        return name;
    }
}
