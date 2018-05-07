import detectors.DetectorsLoader;

public class Main {
    public static void main(String... args) {
        DetectorsLoader nDetectors = new DetectorsLoader("/tmp/detectors_csv/detectors.csv");
        nDetectors.load();
//        for (Detector detector : nDetectors.getDetectors()) {
//            System.out.println(detector);
//        }

    }
}
