package detectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;

public class DetectorsLoader {
    private final ArrayList<Detector> detectors;
    private final String pathToFile;

    public DetectorsLoader(String pPathToFile) {
        this.pathToFile = pPathToFile;
        this.detectors = new ArrayList<>();
    }

    public void load() {
        Logger log = LoggerFactory.getLogger(DetectorsLoader.class);
        InputStream is = null;
        try {
            is = new FileInputStream(new File(getPathToFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assert is != null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String s;
            while ((s = br.readLine()) != null) {
                if (s.startsWith("#")) continue;
                String[] nSplitedLine = s.split(",");
                String uuid = nSplitedLine[0];
                double x = Double.parseDouble(nSplitedLine[1]);
                double y = Double.parseDouble(nSplitedLine[2]);
                DetectorType type = null;
                switch (nSplitedLine[3]) {
                    case "smoke":
                        type = DetectorType.SMOKE;
                        break;
                    case "temp":
                        type = DetectorType.TEMP;
                        break;
                    case "gas":
                        type = DetectorType.GAS;
                        break;
                    default:
                        log.error("Неопознанный тип детектора");
                }
                String belonging = nSplitedLine[4];

                detectors.add(new Detector(uuid, type, x, y, belonging));
            }


            log.debug("Successful reading and parse csv");
        } catch (final IOException e) {
            log.error("Fail: parse csv to Detectors structure. Any problems: ", e);
        }

        log.info("BuildingQGis successful loaded from {}", getPathToFile());
    }

    public ArrayList<Detector> getDetectors() {
        return detectors;
    }

    private String getPathToFile() {
        return pathToFile;
    }
}
