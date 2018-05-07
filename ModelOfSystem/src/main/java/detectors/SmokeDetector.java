package detectors;

public class SmokeDetector {
    private final Detector detector;

    public SmokeDetector(Detector pDetector) {
        this.detector = Detector.newInstance(pDetector);
    }

    public String getId() {
        return detector.getId();
    }

    public DetectorType getType() {
        return detector.getType();
    }
}
