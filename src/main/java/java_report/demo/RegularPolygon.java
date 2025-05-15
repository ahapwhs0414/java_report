package java_report.demo;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class RegularPolygon extends Shape {
    private int sides;
    private double rotationAngle;
    private List<Point> vertices;

    public RegularPolygon(Point center, double radius, int sides, double rotationAngle) {
        super(center, radius);
        this.sides = sides;
        this.rotationAngle = rotationAngle;
        this.vertices = generateVertices();
    }

    private List<Point> generateVertices() {
        List<Point> points = new ArrayList<>();
        double angleStep = 2 * Math.PI / sides;

        for (int i = 0; i < sides; i++) {
            double angle = angleStep * i + rotationAngle;
            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() + radius * Math.sin(angle);
            points.add(new Point(x, y));
        }

        return points;
    }

    @Override
    public boolean overlaps(Shape other) {
        if (other instanceof Circle) {
            Circle circle = (Circle) other;
            Point c = circle.getCenter();
            double r = circle.getRadius();

            // 1. 정다각형 꼭짓점이 원 안에 있음
            for (Point vertex : vertices) {
                if (vertex.distanceTo(c) <= r) return true;
            }

            // 2. 정다각형의 변이 원과 교차
            int n = vertices.size();
            for (int i = 0; i < n; i++) {
                Point a = vertices.get(i);
                Point b = vertices.get((i + 1) % n);
                if (circleIntersectsLine(c, r, a, b)) return true;
            }

            // 3. 원의 중심이 정다각형 안에 포함
            if (isPointInsidePolygon(c, vertices)) return true;

            return false;
        }

        // Polygon vs Polygon: SAT 적용
        List<Point> verticesA = this.getVertices();
        List<Point> verticesB = other.getVertices();

        List<Point> axes = new ArrayList<>();
        extractAxes(verticesA, axes);
        extractAxes(verticesB, axes);

        for (Point axis : axes) {
            double[] projA = projectOntoAxis(verticesA, axis);
            double[] projB = projectOntoAxis(verticesB, axis);
            if (!overlapsOnAxis(projA, projB)) {
                return false;
            }
        }

        return true;
    }

    // --- 유틸 함수들 ---

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

        if (p.getY() == a.getY() || p.getY() == b.getY()) {
            p = new Point(p.getX(), p.getY() + 1e-10);
        }

        if (p.getY() < a.getY() || p.getY() > b.getY()) return false;
        if (p.getX() >= Math.max(a.getX(), b.getX())) return false;

        double xIntersect = (p.getY() - a.getY()) * (b.getX() - a.getX()) / (b.getY() - a.getY()) + a.getX();
        return p.getX() < xIntersect;
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
        double EPS = 1e-8;
        return !(projA[1] < projB[0] - EPS || projB[1] < projA[0] - EPS);
    }

    private double dot(Point a, Point b) {
        return a.getX() * b.getX() + a.getY() * b.getY();
    }

    private Point normalize(Point p) {
        double len = Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY());
        return (len == 0) ? new Point(0, 0) : new Point(p.getX() / len, p.getY() / len);
    }

    // --- 기타 인터페이스 구현 ---

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "regularPolygon");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
        json.put("sides", sides);
        json.put("rotationAngle", rotationAngle);
        json.put("color", color);

        JSONArray arr = new JSONArray();
        for (Point v : vertices) {
            arr.put(v.toJSON());
        }
        json.put("vertices", arr);
        return json;
    }

    @Override
    public String getShapeType() { return "regularPolygon"; }

    @Override
    public List<Point> getVertices() {
        return new ArrayList<>(vertices);
    }
}
