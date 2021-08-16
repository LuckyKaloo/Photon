package application.Model.Light;

import application.Model.Components.Shape;

public interface LightComponent {
    default double getRefractiveIndex() {
        if (getShape() == null) {
            return 1;
        } else {
            return getShape().getRefractiveIndex();
        }
    }
    Shape getShape();
}
