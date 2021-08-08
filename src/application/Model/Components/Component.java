package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.LightRay;

import java.util.ArrayList;

public interface Component {
    LightRay interact(LightRay lightRay, ArrayList<Component> components);

    Point intersection(LightRay lightRay);
}
