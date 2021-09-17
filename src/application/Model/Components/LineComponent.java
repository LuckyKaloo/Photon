package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.LightRay;

// this class refers to both mirror and absorber components as they function very similarly
public class LineComponent implements Component {
    private String name;
    private boolean visible;

    private final int type;
    private final Edge edge;

    public LineComponent(Point start, Point end, int type) {
        if (type == Edge.REFLECTOR || type == Edge.ABSORBER) {
            this.type = type;
            edge = new Edge(start, end, type);

            if (type == Edge.REFLECTOR) {
                name = "Mirror";
            } else {
                name = "Absorber";
            }

            visible = true;
        } else {
            throw new IllegalArgumentException("Type must be either absorber or reflector!");
        }
    }

    public LineComponent(String name, boolean visible, Point start, Point end, int type) {
        this(start, end, type);
        this.name = name;
        this.visible = visible;
    }

    public Edge getEdge() {
        return edge;
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
    public boolean getVisibility() {
        return visible;
    }

    @Override
    public void setVisibility(boolean visible) {
        this.visible = visible;
    }

    @Override
    public LightRay interact(LightRay lightRay) {
        return edge.interact(lightRay);
    }

    @Override
    public Point intersection(LightRay lightRay) {
        return edge.intersection(lightRay);
    }

    @Override
    public void update() {
        getEdge().updateSegment();
    }

    @Override
    public String toData() {
        StringBuilder stringBuilder = new StringBuilder();
        if (type == Edge.REFLECTOR) {
            stringBuilder.append("Mirror {\n");
        } else if (type == Edge.ABSORBER) {
            stringBuilder.append("Absorber {\n");
        }
        stringBuilder.append("\tName: ").append(name).append("\n");
        stringBuilder.append("\tVisible: ").append(visible).append("\n");
        stringBuilder.append("\t").append(edge.getStart().toData()).append("\n");
        stringBuilder.append("\t").append(edge.getEnd().toData()).append("\n");
        stringBuilder.append("}\n");

        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
