package java_report.demo;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class IrregularPolygon extends Shape {
    private List<Point> vertices;

    public IrregularPolygon(Point center, double radius, int numVertices) {
        super(center, radius);
        this.vertices = generateIrregularVertices(numVertices);
    }

    private List<Point> generateIrregularVertices(int numVertices) {
        List<Point> points = new ArrayList<>();
        List<Double> angles = new ArrayList<>();

        for (int i = 0; i < numVertices; i++) {
            angles.add(Math.random() * 2 * Math.PI);
        }
        Collections.sort(angles);

        for (int i = 0; i < numVertices; i++) {
            double angle = angles.get(i);
            double r = radius * (0.6 + Math.random() * 0.4); // 최소 60% 보장
            double x = center.getX() + r * Math.cos(angle);
            double y = center.getY() + r * Math.sin(angle);
            points.add(new Point(x, y));
        }

        return createConvexHull(points);
    }

    private List<Point> createConvexHull(List<Point> points) {
        if (points.size() <= 3) return points;

        points.sort(Comparator.comparingDouble(Point::getX).thenComparingDouble(Point::getY));

        List<Point> lower = new ArrayList<>();
        for (Point p : points) {
            while (lower.size() >= 2 && orientation(lower.get(lower.size()-2), lower.get(lower.size()-1), p) <= 0)
                lower.remove(lower.size() - 1);
            lower.add(p);
        }

        List<Point> upper = new ArrayList<>();
        for (int i = points.size() - 1; i >= 0; i--) {
            Point p = points.get(i);
            while (upper.size() >= 2 && orientation(upper.get(upper.size()-2), upper.get(upper.size()-1), p) <= 0)
                upper.remove(upper.size() - 1);
            upper.add(p);
        }

        lower.remove(lower.size() - 1);
        upper.remove(upper.size() - 1);
        lower.addAll(upper);

        return lower;
    }

    private double orientation(Point p, Point q, Point r) {
        return (q.getX() - p.getX()) * (r.getY() - p.getY()) -
                (q.getY() - p.getY()) * (r.getX() - p.getX());
    }

    @Override
    public boolean overlaps(Shape other) {
        List<Point> verticesA = this.getVertices();
        List<Point> verticesB = other.getVertices();

        List<Point> axes = new ArrayList<>();
        extractAxes(verticesA, axes);
        extractAxes(verticesB, axes);

        for (Point axis : axes) {
            double[] projA = projectOntoAxis(verticesA, axis);
            double[] projB = projectOntoAxis(verticesB, axis);
            if (!overlapsOnAxis(projA, projB)) return false;
        }

        return true;
    }

    private void extractAxes(List<Point> vertices, List<Point> axes) {
        int size = vertices.size();
        for (int i = 0; i < size; i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % size);
            double dx = p2.getX() - p1.getX();
            double dy = p2.getY() - p1.getY();
            Point normal = new Point(-dy, dx);
            axes.add(normalize(normal));
        }
    }

    private Point normalize(Point p) {
        double len = Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY());
        return len == 0 ? new Point(0, 0) : new Point(p.getX() / len, p.getY() / len);
    }

    private double dot(Point a, Point b) {
        return a.getX() * b.getX() + a.getY() * b.getY();
    }

    private double[] projectOntoAxis(List<Point> vertices, Point axis) {
        double min = dot(vertices.get(0), axis);
        double max = min;
        for (Point p : vertices) {
            double proj = dot(p, axis);
            if (proj < min) min = proj;
            if (proj > max) max = proj;
        }
        return new double[]{min, max};
    }

    private boolean overlapsOnAxis(double[] projA, double[] projB) {
        double EPS = 1e-6;
        return !(projA[1] < projB[0] - EPS || projB[1] < projA[0] - EPS);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "irregularPolygon");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
        json.put("color", color);

        JSONArray arr = new JSONArray();
        for (Point v : vertices) arr.put(v.toJSON());
        json.put("vertices", arr);

        return json;
    }

    @Override
    public String getShapeType() { return "irregularPolygon"; }

    @Override
    public List<Point> getVertices() {
        return new ArrayList<>(vertices);
    }
}
