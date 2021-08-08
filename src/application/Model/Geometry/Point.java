package application.Model.Geometry;

public record Point(double X, double Y) {
    public static double distance(Point point1, Point point2) {
        return Math.sqrt((point1.X - point2.X) * (point1.X - point2.X) +
                (point1.Y - point2.Y) * (point1.Y - point2.Y));
    }

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f)", X, Y);
    }


    public boolean equals(Point point) {
        return equals(point, 0.8);
    }

    public boolean equals(Point point, double marginError) {
        return Math.abs(X - point.X) <= marginError  &&  Math.abs(Y - point.Y) <= marginError;
    }
}
