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

    private ArrayList<Normal> normals;  // list of all the normals that the light beam encounters

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

        while (true) {
            ArrayList<Point> intersections = new ArrayList<>();
            ArrayList<Integer> componentIndexes = new ArrayList<>();
            LightRay endRay;

            if (lightComponents.size() == 0) {
                endRay = new LightRay(initialRay, null, null);
            } else {
                endRay = (LightRay) lightComponents.remove(lightComponents.size()-1);
                if (endRay == null) {
                    break;
                }
            }

            for (int i = 0; i < components.size(); i++) {
                Component component = components.get(i);
                Point intersection = component.intersection(endRay);

                if (intersection != null && !intersection.equals(endRay.getStart())) {
                    intersections.add(intersection);  // intersections that the ray makes with all components
                    componentIndexes.add(i);  // index of the component that got intersected;
                }
            }

            if (intersections.size() == 0) {  // no intersections
                lightComponents.add(endRay);
                break;
            } else {
                // get the component that the ray will interact with first
                double minDistance = Point.distance(endRay.getStart(), intersections.get(0));
                int index = 0;
                for (int i = 1; i < intersections.size(); i++) {
                    double distance = Point.distance(endRay.getStart(), intersections.get(i));
                    if (distance < minDistance) {
                        minDistance = distance;
                        index = i;
                    }
                }

                Component nextComponent = components.get(componentIndexes.get(index));
                LightRay newRay = nextComponent.interact(endRay);

                LightSegment lightSegment = new LightSegment(endRay, intersections.get(index));
                lightComponents.add(lightSegment);
                lightComponents.add(newRay);
            }
        }
    }


    @Override
    public String toString() {
        return "\n" + lightComponents;
    }
}
