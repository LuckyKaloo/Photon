package application.Model.Light;

import application.Model.Components.Edge;
import application.Model.Geometry.Point;

public record Normal(Edge edge, Point intersection, double angleIncidence, double angleRefraction) {}
