package application.Model.Components;

import application.Model.Geometry.Point;
import application.Model.Geometry.Ray;

public abstract class Shape extends Component {
    Material material;

    public abstract Point intersection(Ray ray);
}
