package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.LightRay;

import java.util.ArrayList;

public class Shape implements Component {
    double refractiveIndex;
    ArrayList<Edge> edges;

    public Shape(double refractiveIndex, ArrayList<Edge> edges) {
        this.refractiveIndex = refractiveIndex;
        this.edges = edges;
    }

    public boolean contains(Point point) {
        return false;
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
