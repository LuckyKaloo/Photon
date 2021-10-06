package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import application.Model.Geometry.Segment;
import application.Model.Light.LightRay;

import java.util.ArrayList;

public class Shape implements Component {
    private String name;
    private boolean visible;

    private double refractiveIndex;
    private final ArrayList<Edge> edges;
    private final ArrayList<Point> vertices;

    private int layer;  // if shapes are stacked on each other, then this determines which one is clicked by the user

    public <T> Shape(double refractiveIndex, ArrayList<T> objects) {
        this.refractiveIndex = refractiveIndex;
        if (objects.get(0) instanceof Edge) {  // inputted as ArrayList<Edge>
            this.edges = new ArrayList<>();
            for (T object: objects) {
                this.edges.add((Edge) object);
            }

            ArrayList<Segment> segments = new ArrayList<>(this.edges);
            this.vertices = Segment.segmentsToPoints(segments);
        } else if (objects.get(0) instanceof Point) {  // inputted as ArrayList<Point>
            this.vertices = new ArrayList<>();
            for (T object: objects) {
                this.vertices.add((Point) object);
            }

            ArrayList<Segment> segments = Segment.pointsToSegments(this.vertices, true);
            this.edges = new ArrayList<>();
            for (Segment segment : segments) {
                this.edges.add(new Edge(segment, Edge.EdgeType.REFRACTOR));
            }
        } else {
            throw new IllegalArgumentException("Input an ArrayList of Point or Edge objects only");
        }

        name = "Shape";
        visible = true;
    }

    public <T> Shape(String name, boolean visible, double refractiveIndex, ArrayList<T> objects) {
        this(refractiveIndex, objects);
        this.name = name;
        this.visible = visible;
    }

    @Override
    public void update() {
        ArrayList<Segment> segments = Segment.pointsToSegments(this.vertices, true);
        edges.clear();
        for (Segment segment: segments) {
            edges.add(new Edge(segment, Edge.EdgeType.REFRACTOR));
        }
    }

    public void addPoint() {
        double totalX = 0;
        double totalY = 0;
        for (Point point: vertices) {
            totalX += point.getX();
            totalY += point.getY();
        }
        Point point2 = new Point(totalX / vertices.size(), totalY / vertices.size());

        // ensure that point2 does not equal any other points which would cause errors
        while (true) {
            boolean isEqual = false;
            for (Point point: vertices) {
                if (point.equals(point2)) {
                    isEqual = true;
                    break;
                }
            }
            if (isEqual) {
                point2.setX(point2.getX() + 1);
            } else {
                break;
            }
        }
        vertices.add(point2);

        Edge lastEdge = edges.remove(edges.size() - 1);
        Point point1 = lastEdge.getStart();
        Point point3 = lastEdge.getEnd();

        Edge edge1 = new Edge(new Segment(point1, point2), Edge.EdgeType.REFRACTOR);
        Edge edge2 = new Edge(new Segment(point2, point3), Edge.EdgeType.REFRACTOR);

        edges.add(edge1);
        edges.add(edge2);
    }

    public void removePoint(Point point) {
        if (vertices.contains(point) && vertices.size() > 3) {
            vertices.remove(point);
            ArrayList<Segment> segments = Segment.pointsToSegments(vertices, true);
            edges.clear();
            for (Segment segment: segments) {
                edges.add(new Edge(segment, Edge.EdgeType.REFRACTOR));
            }
        } else {
            throw new IllegalArgumentException("Shape does not contain point, cannot remove point");
        }
    }


    public double getRefractiveIndex() {
        return refractiveIndex;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public ArrayList<Point> getVertices() {
        return vertices;
    }

    public void setRefractiveIndex(double refractiveIndex) {
        this.refractiveIndex = refractiveIndex;
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
    public Shape copy() {
        ArrayList<Point> vertices = new ArrayList<>();
        for (Point vertex: this.vertices) {
            vertices.add(vertex.copy());
        }
        return new Shape(name, visible, refractiveIndex, vertices);
    }

    @Override
    public Shape translate(Point vector) {
        Shape output = copy();
        for (Point vertex: output.vertices) {
            vertex.translate(vector);
        }
        output.update();
        return output;
    }

    @Override
    public void set(Component component) {
        if (component instanceof Shape shape) {
            vertices.clear();
            vertices.addAll(shape.vertices);
            update();
        }
    }

    /* crossing number algorithm
    if the ray intersects with an even number of edges, it is outside the polygon
    if the ray intersects with an odd number of edges, it is inside the polygon
     */
    public boolean contains(Point point) {
        Ray ray = new Ray(0, point);
        int count = 0;
        for (Edge edge : edges) {
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

        for (Point vertex : vertices) {
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
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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
        return name;
    }

    @Override
    public String toData() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Shape {\n");
        stringBuilder.append("\tName: ").append(name).append("\n");
        stringBuilder.append("\tVisible: ").append(visible).append("\n");
        stringBuilder.append("\tRefractive Index: ").append(refractiveIndex).append("\n");
        for (Point point : vertices) {
            stringBuilder.append("\t").append(point.toData()).append("\n");
        }
        stringBuilder.append("}\n");

        return stringBuilder.toString();
    }
}
