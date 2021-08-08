package application.Model.Light;

import application.Model.Components.Component;
import application.Model.Components.Shape;
import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;

import java.util.ArrayList;

public class LightRay extends Ray implements LightComponent {
    private final double refractiveIndex;

    public LightRay(double angle, Point start, double refractiveIndex) {
        super(angle, start);
        this.refractiveIndex = refractiveIndex;
    }

    public LightRay(Ray ray, double refractiveIndex) {
        super(ray);
        this.refractiveIndex = refractiveIndex;
    }

    @Override
    public double getRefractiveIndex() {
        return refractiveIndex;
    }

    public Component nextComponent(ArrayList<Component> components) {
        // get the point a very small distance from start
        Point nextPoint = new Point(start.X() + Math.cos(Math.toRadians(angle)),
                start.Y() + Math.sin(Math.toRadians(angle)));

        for (Component component: components) {
            if (component instanceof Shape) {
                if (((Shape) component).contains(nextPoint)) {
                    return component;
                }
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return start + " angle: " + angle + " refractive index: " + refractiveIndex;
    }
}
