package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.LightRay;

public interface Component {
    LightRay interact(LightRay lightRay);

    Point intersection(LightRay lightRay);
}
