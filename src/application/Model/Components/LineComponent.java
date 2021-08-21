package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.LightRay;

// this class refers to both mirror and absorber components as they function very similarly
public class LineComponent implements Component {
    private final int type;
    private final Edge edge;

    public LineComponent(Point start, Point end, int type) {
        if (type == Edge.REFLECTOR || type == Edge.ABSORBER) {
            this.type = type;
            edge = new Edge(start, end, type);
        } else {
            throw new IllegalArgumentException("Type must be either absorber or reflector!");
        }
    }

    public Edge getEdge() {
        return edge;
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
            stringBuilder.append("Mirror{\n");
        } else if (type == Edge.ABSORBER) {
            stringBuilder.append("Absorber{\n");
        }
        stringBuilder.append(edge.getStart().toData()).append("\n");
        stringBuilder.append(edge.getEnd().toData()).append("\n");
        stringBuilder.append("}\n");

        return stringBuilder.toString();
    }
}
