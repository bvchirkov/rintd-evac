package detectors;

public class TempDetector {

    private final Detector detector;

    public TempDetector(Detector pDetector) {
        this.detector = Detector.newInstance(pDetector);
    }

    public String getId() {
        return detector.getId();
    }

    public DetectorType getType() {
        return detector.getType();
    }
}
