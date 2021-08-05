package application.Model.Components;

import application.Model.Geometry.Segment;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;

import java.util.ArrayList;

public class Shape implements Component {
    double refractiveIndex;
    ArrayList<Edge> edges;

    public Shape(double refractiveIndex, ArrayList<Edge> edges) {
        this.refractiveIndex = refractiveIndex;
        this.edges = edges;
    }
}
