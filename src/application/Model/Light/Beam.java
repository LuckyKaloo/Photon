package application.Model.Light;

import application.Model.Components.Component;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Beam {
    private final ArrayList<LightComponent> lightComponents;
    private final Ray initialRay;

    private Color color;

    private final ArrayList<Normal> normals;  // list of all the normals that the light beam encounters

    public Beam(Ray initialRay) {
        this.lightComponents = new ArrayList<>();
        this.initialRay = initialRay;
        this.color = Color.WHITE;

        this.normals = new ArrayList<>();
    }


    public Ray getInitialRay() {
        return initialRay;
    }

    public Color getColor() {
        return color;
    }

    public ArrayList<LightComponent> getLightComponents() {
        return lightComponents;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void generateBeam(ArrayList<Component> components) {
        lightComponents.clear();

        // only goes to 1000 to prevent the program from crashing if there are infinite loops
        // e.g. mirror parallel to a mirror which is perpendicular to a source between them -> infinite reflections
        for (int i = 0; i < 1000; i++) {
            ArrayList<Point> intersections = new ArrayList<>();
            ArrayList<Integer> componentIndexes = new ArrayList<>();
            LightRay endRay;

            if (lightComponents.size() == 0) {
                endRay = new LightRay(initialRay, null, null);
            } else {
                endRay = (LightRay) lightComponents.remove(lightComponents.size() - 1);
                if (endRay == null) {
                    return;
                }
            }

            for (int j = 0; j < components.size(); j++) {
                Component component = components.get(j);
                Point intersection = component.intersection(endRay);

                if (intersection != null && !intersection.equals(endRay.getStart())) {
                    intersections.add(intersection);  // intersections that the ray makes with all components
                    componentIndexes.add(j);  // index of the component that got intersected;
                }
            }

            if (intersections.size() == 0) {  // no intersections
                lightComponents.add(endRay);
                return;
            } else {
                // get the component that the ray will interact with first
                double minDistance = Point.distance(endRay.getStart(), intersections.get(0));
                int index = 0;
                for (int j = 1; j < intersections.size(); j++) {
                    double distance = Point.distance(endRay.getStart(), intersections.get(j));
                    if (distance < minDistance) {
                        minDistance = distance;
                        index = j;
                    }
                }

                Component nextComponent = components.get(componentIndexes.get(index));
                LightRay newRay = nextComponent.interact(endRay);

                LightSegment lightSegment = new LightSegment(endRay, intersections.get(index));
                lightComponents.add(lightSegment);
                lightComponents.add(newRay);
            }
        }

        lightComponents.remove(lightComponents.size()-1);
    }


    @Override
    public String toString() {
        return "\n" + lightComponents;
    }
}
