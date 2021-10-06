package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Light.LightRay;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.regex.Pattern;

public interface Component {
    void update();  // updates all fields in the component (for cases where a point is updated but edges are not)

    String getName();

    void setName(String name);

    LightRay interact(LightRay lightRay);  // returns the outgoing light ray after interacting with it

    Point intersection(LightRay lightRay);  // returns the first intersection that the light ray makes with the component

    static Component parseData(String data) throws IllegalArgumentException {  // parses data from the toData() method into a component object
        // cleans up the data for easier parsing of it
        data = data.replaceAll("[ \\t\\r\\f]", "");
        String[] lines = data.split("\n");

        switch (lines[0]) {
            case "Shape{" -> {   // data is referring to a shape
                // checking that the data is in a valid format
                if (!Pattern.matches("Name:.*", lines[1]) || !Pattern.matches("Visible:.*", lines[2]) ||
                        !Pattern.matches("RefractiveIndex:.*", lines[3])) {

                    throw new IllegalArgumentException("Data for shape is not valid!");
                }


                // parsing the data for the shape
                try {
                    // data about the shape
                    ArrayList<Point> points = new ArrayList<>();
                    double refractiveIndex = Double.parseDouble(lines[3].substring(16));
                    String name = lines[1].substring(5);
                    boolean visible = Boolean.parseBoolean(lines[2].substring(8));

                    for (int i = 4; i < lines.length - 1; i++) {
                        points.add(Point.parseData(lines[i]));
                    }

                    return new Shape(name, visible, refractiveIndex, points);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Data for shape is not valid!");
                }
            }
            case "Source{" -> {   // data is referring to a source
                // checking that the data is in a valid format
                if (!Pattern.matches("Name:.*", lines[1]) || !Pattern.matches("Visible:.*", lines[2]) ||
                        !Pattern.matches("Angle:.*", lines[4]) || !Pattern.matches("Color:.*", lines[5])) {

                    throw new IllegalArgumentException("Data for source is not valid!");
                }
                try {
                    // data about the source
                    String name = lines[1].substring(5);
                    boolean visible = Boolean.parseBoolean(lines[2].substring(8));
                    Point start = Point.parseData(lines[3]);
                    double angle = Double.parseDouble(lines[4].substring(6));
                    String[] colorInformation = lines[5].substring(6).replaceAll("[()]", "").split(",");
                    int red = (int) (Double.parseDouble(colorInformation[0]) * 255);
                    int green = (int) (Double.parseDouble(colorInformation[1]) * 255);
                    int blue = (int) (Double.parseDouble(colorInformation[2]) * 255);
                    Color color = Color.rgb(red, green, blue);

                    return new Source(name, visible, start, angle, color);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Data for source is not valid!");
                }
            }
            case "Absorber{", "Mirror{" -> {   // data is referring to a line component
                // checking that the data is in a valid format
                if (!Pattern.matches("Name:.*", lines[1]) || !Pattern.matches("Visible:.*", lines[2])) {
                    throw new IllegalArgumentException("Data for Absorber or Mirror is not valid!");
                }

                String name = lines[1].substring(5);
                boolean visible = Boolean.parseBoolean(lines[2].substring(8));
                Point start = Point.parseData(lines[3]);
                Point end = Point.parseData(lines[4]);
                if (lines[0].equals("Absorber{")) {
                    return new LineComponent(name, visible, start, end, Edge.EdgeType.ABSORBER);
                } else {
                    return new LineComponent(name, visible, start, end, Edge.EdgeType.REFLECTOR);
                }
            }
            default -> throw new IllegalArgumentException(
                    "File data is invalid: object must be of type shape, source, absorber or mirror!");
        }
    }

    String toData();  // converts the component into data that can be stored into a file

    boolean getVisibility();  // returns whether the component is visible to the user

    void setVisibility(boolean visibility);

    Component copy();

    Component translate(Point vector);

    void set(Component component);
}
