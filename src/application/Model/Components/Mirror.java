package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.LightRay;

import java.util.ArrayList;

public class Mirror implements Component {
    private final Edge edge;

    public Mirror(Point start, Point end) {
        edge = new Edge(start, end, Edge.REFLECTOR);
    }

    public Edge getEdge() {
        return edge;
    }

    @Override
    public LightRay interact(LightRay lightRay, ArrayList<Component> components) {
        return edge.interact(lightRay);
    }

    @Override
    public Point intersection(LightRay lightRay) {
        return edge.intersection(lightRay);
    }
}
