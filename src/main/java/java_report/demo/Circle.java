package java_report.demo;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Circle extends Shape {

    public Circle(Point center, double radius) {
        super(center, radius);
    }

    @Override
    public boolean overlaps(Shape other) {
        if (other instanceof Circle) {
            double dist = center.distanceTo(other.getCenter());
            return dist < (this.radius + other.getRadius());
        }

        // Circle vs Polygon (Regular or Irregular)
        List<Point> vertices = other.getVertices();

        for (Point vertex : vertices) {
            if (vertex.distanceTo(this.center) <= this.radius) return true;
        }

        int n = vertices.size();
        for (int i = 0; i < n; i++) {
            Point a = vertices.get(i);
            Point b = vertices.get((i + 1) % n);
            if (circleIntersectsLine(this.center, this.radius, a, b)) return true;
        }

        if (isPointInsidePolygon(this.center, vertices)) return true;

        return false;
    }

    private boolean circleIntersectsLine(Point c, double r, Point a, Point b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double fx = a.getX() - c.getX();
        double fy = a.getY() - c.getY();

        double A = dx * dx + dy * dy;
        double B = 2 * (fx * dx + fy * dy);
        double C = fx * fx + fy * fy - r * r;

        double discriminant = B * B - 4 * A * C;
        if (discriminant < -1e-8) return false;
        if (discriminant < 0) discriminant = 0;

        double sqrtD = Math.sqrt(discriminant);
        double t1 = (-B - sqrtD) / (2 * A);
        double t2 = (-B + sqrtD) / (2 * A);

        return (t1 >= -1e-6 && t1 <= 1 + 1e-6) || (t2 >= -1e-6 && t2 <= 1 + 1e-6);
    }

    private boolean isPointInsidePolygon(Point p, List<Point> polygon) {
        int count = 0;
        int n = polygon.size();
        for (int i = 0; i < n; i++) {
            Point a = polygon.get(i);
            Point b = polygon.get((i + 1) % n);
            if (rayIntersectsSegment(p, a, b)) count++;
        }
        return count % 2 == 1;
    }

    private boolean rayIntersectsSegment(Point p, Point a, Point b) {
        if (a.getY() > b.getY()) {
            Point tmp = a; a = b; b = tmp;
        }

        // 수평선분은 교차하지 않음
        if (Math.abs(a.getY() - b.getY()) < 1e-12) {
            return false;
        }

        if (p.getY() == a.getY() || p.getY() == b.getY()) {
            p = new Point(p.getX(), p.getY() + 1e-10);
        }

        if (p.getY() < a.getY() || p.getY() > b.getY()) return false;
        if (p.getX() >= Math.max(a.getX(), b.getX())) return false;

        double xIntersect = (p.getY() - a.getY()) * (b.getX() - a.getX()) / (b.getY() - a.getY()) + a.getX();
        return p.getX() < xIntersect;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "circle");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
        json.put("color", color);
        return json;
    }

    @Override
    public String getShapeType() { return "circle"; }

    @Override
    public List<Point> getVertices() {
        List<Point> vertices = new ArrayList<>();
        int numPoints = 32;
        for (int i = 0; i < numPoints; i++) {
            double angle = 2 * Math.PI * i / numPoints;
            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() + radius * Math.sin(angle);
            vertices.add(new Point(x, y));
        }
        return vertices;
    }
}
