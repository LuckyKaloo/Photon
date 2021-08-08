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

    public Beam(Ray initialRay) {
        this.lightComponents = new ArrayList<>();
        this.initialRay = initialRay;
    }

    public void setAngle(double angle) {
        initialRay.setAngle(angle);
    }

    public ArrayList<LightComponent> getLightComponents() {
        return lightComponents;
    }

    public void generateBeam(ArrayList<Component> components) {
        while (true) {
            ArrayList<Point> intersections = new ArrayList<>();
            ArrayList<Integer> indexes = new ArrayList<>();
            LightRay endRay;

            if (lightComponents.size() == 0) {
                endRay = new LightRay(initialRay, 1);
            } else {
                endRay = (LightRay) lightComponents.remove(lightComponents.size()-1);
            }

            for (int i = 0; i < components.size(); i++) {
                Component component = components.get(i);
                Point intersection = component.intersection(endRay);
                if (intersection != null && !intersection.equals(endRay.getStart())) {
                    intersections.add(intersection);  // intersections that the ray makes with all components
                    indexes.add(i);  // index of the component that got intersected
                }
            }

            if (intersections.size() == 0) {  // no intersections
                lightComponents.add(endRay);
                break;
            } else {
                // get the component that the ray will interact with first
                double minDistance = Point.distance(endRay.getStart(), intersections.get(0));
                int componentIndex = indexes.get(0);
                int intersectionIndex = 0;
                for (int i = 1; i < intersections.size(); i++) {
                    double distance = Point.distance(endRay.getStart(), intersections.get(i));
                    if (distance < minDistance) {
                        minDistance = distance;
                        componentIndex = indexes.get(i);
                        intersectionIndex = i;
                    }
                }

                Component nextComponent = components.get(componentIndex);
                LightRay newRay = nextComponent.interact(endRay, components);

                LightSegment lightSegment = new LightSegment(endRay, intersections.get(intersectionIndex));
                lightComponents.add(lightSegment);
                lightComponents.add(newRay);
            }
        }
    }


    @Override
    public String toString() {
        return initialRay + "\n" + lightComponents;
    }
}
