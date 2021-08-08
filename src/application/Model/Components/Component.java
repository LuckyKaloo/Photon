package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.LightRay;

import java.util.ArrayList;

public interface Component {
    LightRay interact(LightRay lightRay);

    Point intersection(LightRay lightRay);
}
