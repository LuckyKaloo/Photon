package application.Model.Light;

import javafx.scene.paint.Color;

import java.io.Serializable;

public class SerializableColor implements Serializable {
    private final double red;
    private final double green;
    private final double blue;
    private final double opacity;

    public SerializableColor(Color color) {
        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
        opacity = color.getOpacity();
    }

    public Color getColor() {
        return new Color(red, green, blue, opacity);
    }
}
