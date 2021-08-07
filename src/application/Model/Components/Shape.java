package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import application.Model.Light.LightRay;

import java.util.ArrayList;

public class Shape implements Component {
    private double refractiveIndex;
    private final ArrayList<Edge> edges;

    public <T> Shape(double refractiveIndex, ArrayList<T> objects) {
        this.refractiveIndex = refractiveIndex;
        if (objects.get(0) instanceof Edge) {  // inputted as ArrayList<Edge>
            this.edges = new ArrayList<>();
            for (T object: objects) {
                this.edges.add((Edge) object);
            }
        } else if (objects.get(0) instanceof Point) {  // inputted as ArrayList<Point>
            ArrayList<Point> points = new ArrayList<>();
            for (T object: objects) {
                points.add((Point) object);
            }

            this.edges = new ArrayList<>();
            for (int i = 0; i < points.size()-1; i++) {
                this.edges.add(new Edge(points.get(i), points.get(i+1), Edge.REFRACTOR));
            }
            this.edges.add(new Edge(points.get(0), points.get(points.size()-1), Edge.REFRACTOR));
        } else {
            throw new IllegalArgumentException("Input an ArrayList of Point or Edge objects only");
        }
    }



    public double getRefractiveIndex() {
        return refractiveIndex;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void setRefractiveIndex(double refractiveIndex) {
        this.refractiveIndex = refractiveIndex;
    }

    /* crossing number algorithm
    if the ray intersects with an even number of edges, it is outside the polygon
    if the ray intersects with an odd number of edges, it is inside the polygon
     */
    public boolean contains(Point point) {
        Ray ray = new Ray(0, point);
        int count = 0;
        for (Edge edge: edges) {
            if (edge.intersection(ray) != null) {
                count++;
            }
        }

        return count % 2 == 1;
    }

    @Override
    public LightRay interact(LightRay lightRay) {
        return null;
    }

    @Override
    public Point intersection(LightRay lightRay) {
        ArrayList<Point> intersections = new ArrayList<>();

        if (edges.size() == 0) {
            return null;
        }

        for (Edge edge : edges) {
            Point intersection = edge.intersection(lightRay);
            if (intersection != null && !intersection.equals(lightRay.getStart())) {
                intersections.add(intersection);
            }
        }

        if (intersections.size() == 0) {
            return null;
        } else {
            double minDistance = Point.distance(lightRay.getStart(), intersections.get(0));
            Point closestIntersection = intersections.get(0);
            for (int i = 1; i < intersections.size(); i++) {
                double distance = Point.distance(lightRay.getStart(), intersections.get(i));
                if (distance < minDistance) {
                    minDistance = distance;
                    closestIntersection = intersections.get(i);
                }
            }

            return closestIntersection;
        }
    }

    public Edge intersectionEdge(LightRay lightRay) {
        Point closestIntersection = intersection(lightRay);

        if (edges.size() == 0 || closestIntersection == null) {
            return null;
        }

        for (Edge edge : edges) {
            Point intersection = edge.intersection(lightRay);
            if (intersection != null && intersection.equals(closestIntersection)) {
                return edge;
            }
        }

        return null;
    }
}
