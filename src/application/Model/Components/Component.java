package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.LightRay;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.regex.Pattern;

public interface Component {
    void update();  // updates all fields in the component (for cases where a point is updated but edges are not)

    LightRay interact(LightRay lightRay);  // returns the outgoing light ray after interacting with it

    Point intersection(LightRay lightRay);  // returns the first intersection that the light ray makes with the component

    static Component parseData(String data) throws IllegalArgumentException {  // parses data from the toData() method into a component object
        // cleans up the data for easier parsing of it
        data = data.replaceAll("[ \\t\\r\\f]", "");
        String[] lines = data.split("\n");

        switch (lines[0]) {
            case "Shape{" -> {   // data is referring to a shape
                // checking that the data is in a valid format
                if (!Pattern.matches("RefractiveIndex:.*", lines[1])) {
                    throw new IllegalArgumentException("Data for shape is not valid!");
                }


                // parsing the data for the shape
                try {
                    // data about the shape
                    ArrayList<Point> points = new ArrayList<>();
                    double refractiveIndex = Double.parseDouble(lines[1].substring(16));

                    for (int i = 2; i < lines.length-1; i++) {
                        points.add(Point.parseData(lines[i]));
                    }

                    return new Shape(refractiveIndex, points);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Data for shape is not valid!");
                }
            }
            case "Source{" -> {   // data is referring to a source
                // checking that the data is in a valid format
                if (!Pattern.matches("Angle:.*", lines[2]) || !Pattern.matches("Color:.*", lines[3])) {
                    throw new IllegalArgumentException("Data for source is not valid!");
                }
                try {
                    // data about the source
                    Point start = Point.parseData(lines[1]);
                    double angle = Double.parseDouble(lines[2].substring(6));
                    String[] colorInformation = lines[3].substring(6).replaceAll("[()]", "").split(",");
                    int red = (int) (Double.parseDouble(colorInformation[0]) * 255);
                    int green = (int) (Double.parseDouble(colorInformation[1]) * 255);
                    int blue = (int) (Double.parseDouble(colorInformation[2]) * 255);
                    Color color = Color.rgb(red, green, blue);

                    return new Source(start, angle, color);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Data for source is not valid!");
                }
            }
            case "Absorber{", "Mirror{" -> {   // data is referring to a line component
                Point start = Point.parseData(lines[1]);
                Point end = Point.parseData(lines[2]);
                if (lines[0].equals("Absorber{")) {
                    return new LineComponent(start, end, Edge.ABSORBER);
                } else {
                    return new LineComponent(start, end, Edge.REFLECTOR);
                }
            }
            default -> throw new IllegalArgumentException(
                    "File data is invalid: object must be of type shape, source, absorber or mirror!");
        }
    }

    String toData();  // converts the component into data that can be stored into a file
}
