package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.LightRay;

public class Mirror implements Component {
    private final Edge edge;

    public Mirror(Point start, Point end) {
        edge = new Edge(start, end, Edge.REFLECTOR);
    }


    @Override
    public LightRay interact(LightRay lightRay) {
        return edge.interact(lightRay);
    }

    @Override
    public Point intersection(LightRay lightRay) {
        return edge.intersection(lightRay);
    }
}
