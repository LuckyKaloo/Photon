package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.LightRay;

import java.io.Serializable;

public interface Component extends Serializable {
    void update();

    LightRay interact(LightRay lightRay);

    Point intersection(LightRay lightRay);
}
