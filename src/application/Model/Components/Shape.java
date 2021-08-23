package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import application.Model.Geometry.Segment;
import application.Model.Light.LightRay;

import java.util.ArrayList;

public class Shape implements Component {
    private double refractiveIndex;
    private final ArrayList<Edge> edges;
    private final ArrayList<Point> vertexes;

    private int layer;  // if shapes are stacked on each other, then this determines which one is clicked by the user

    public <T> Shape(double refractiveIndex, ArrayList<T> objects) {
        this.refractiveIndex = refractiveIndex;
        if (objects.get(0) instanceof Edge) {  // inputted as ArrayList<Edge>
            this.edges = new ArrayList<>();
            for (T object: objects) {
                this.edges.add((Edge) object);
            }

            ArrayList<Segment> segments = new ArrayList<>(this.edges);
            this.vertexes = Segment.segmentsToPoints(segments);
        } else if (objects.get(0) instanceof Point) {  // inputted as ArrayList<Point>
            this.vertexes = new ArrayList<>();
            for (T object: objects) {
                this.vertexes.add((Point) object);
            }

            ArrayList<Segment> segments = Segment.pointsToSegments(this.vertexes, true);
            this.edges = new ArrayList<>();
            for (Segment segment: segments) {
                this.edges.add(new Edge(segment, Edge.REFRACTOR));
            }
        } else {
            throw new IllegalArgumentException("Input an ArrayList of Point or Edge objects only");
        }
    }

    @Override
    public void update() {
        for (Edge edge: edges) {
            edge.updateSegment();
        }
    }


    public double getRefractiveIndex() {
        return refractiveIndex;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public ArrayList<Point> getVertexes() {
        return vertexes;
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

    public boolean containsMouse(Point point) {
        if (contains(point)) {
            return true;
        }

        for (Point vertex: vertexes) {
            if (Point.distance(vertex, point) < 5) {
                return true;
            }
        }

        return false;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public int getLayer() {
        return layer;
    }

    @Override
    public LightRay interact(LightRay lightRay) {
        Edge intersectionEdge = intersectionEdge(lightRay);
        boolean entering = this != lightRay.getShape();  // whether the ray is entering the shape

        if (entering) {
            return intersectionEdge.interact(lightRay, this, false);
        } else {
            return intersectionEdge.interact(lightRay, null, false);
        }
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


    @Override
    public String toString() {
        return edges.toString();
    }

    @Override
    public String toData() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Shape {\n");
        stringBuilder.append("\tRefractive Index: ").append(refractiveIndex).append("\n");
        for (Point point: vertexes) {
            stringBuilder.append("\t").append(point.toData()).append("\n");
        }
        stringBuilder.append("}\n");

        return stringBuilder.toString();
    }
}
