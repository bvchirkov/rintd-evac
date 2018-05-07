package detectors;

public class Detector {
    private final String id;
    private final DetectorType type;
    private final double x;
    private final double y;
    private final String belonging;

    public Detector(String id, DetectorType type, double x, double y, String belonging) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.belonging = belonging;
    }

    Detector(Detector pDetector) {
        this(pDetector.getId(), pDetector.getType(), pDetector.getX(), pDetector.getY(), pDetector.getBelonging());
    }

    public static Detector newInstance(Detector pDetector) {
        return new Detector(pDetector);
    }

    public String getId() {
        return id;
    }

    public DetectorType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getBelonging() {
        return belonging;
    }

    @Override
    public String toString() {
        return "Detector{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", x=" + x +
                ", y=" + y +
                ", belonging='" + belonging + '\'' +
                '}';
    }
}
