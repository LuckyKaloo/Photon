package application.Model.Light;

import application.Model.Components.Component;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;

import java.util.ArrayList;

public class Beam {
    private final ArrayList<LightComponent> lightComponents;
    private final Ray initialRay;

    public Beam(Ray initialRay) {
        this.lightComponents = new ArrayList<>();
        this.initialRay = initialRay;
    }

    public void generateBeam(ArrayList<Component> components) {
        while (true) {
            ArrayList<Point> intersections = new ArrayList<>();
            ArrayList<Integer> indexes = new ArrayList<>();
            LightRay endRay;

            if (lightComponents.size() == 0) {
                endRay = new LightRay(initialRay, 1);
            } else {
                endRay = (LightRay) lightComponents.get(lightComponents.size()-1);
            }

            for (int i = 0; i < components.size(); i++) {
                Component component = components.get(i);
                Point intersection = component.intersection(endRay);
                if (intersection != null) {
                    intersections.add(intersection);
                    indexes.add(i);
                }
            }

            if (intersections.size() == 0) {
                break;
            } else {
                double minDistance = Point.distance(endRay.getStart(), intersections.get(0));
                int index = indexes.get(0);
                for (int i = 1; i < intersections.size(); i++) {
                    double distance = Point.distance(endRay.getStart(), intersections.get(i));
                    if (distance < minDistance) {
                        minDistance = distance;
                        index = indexes.get(i);
                    }
                }

                LightRay newRay = components.get(index).interact(endRay);
                lightComponents.add(newRay);
            }
        }
    }
}
