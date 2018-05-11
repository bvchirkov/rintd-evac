import collect.SummaryCollector;
import distribution.Distribution;
import distribution.NormativeDistribution;
import distribution.RandomDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import out.FileHandler;
import preparation.BuildingQGis;
import preparation.ZoneElement;
import properties.EvacPropertiesLoader;
import ru.rintd.cfast.files.CfastFileN;
import simulation.Moving;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {
        String nSeparator = System.getProperty("file.separator");
        String nWorkspace = System.getProperty("user.dir");

        String nConfigName = "config.properties";
        File nConfigFile = new File(nWorkspace + nSeparator + "config" + nSeparator + nConfigName);
        EvacPropertiesLoader eProp = new EvacPropertiesLoader(selectFileOrDefault(nConfigFile, nConfigName));

        String nBuildingName = eProp.getFileName().concat(".json");
        File nBuildingFile = new File(nWorkspace + nSeparator + "buildings" + nSeparator + nBuildingName);
        BuildingQGis nBimQGis = new BuildingQGis(selectFileOrDefault(nBuildingFile, nBuildingName));
        nBimQGis.setDistribution(selectDistribution(eProp, nBimQGis)).apply();
        nBimQGis.setTimeOfBeginEvacuation(eProp.getBeginEvacuation());

        if (eProp.isFire()) {
            String nCfastName = eProp.getCfastFile();
            File nCfastFile = new File(nWorkspace + nSeparator + "cfast" + nSeparator + nCfastName);
            CfastFileN file_n = new CfastFileN(selectFileOrDefault(nCfastFile, nCfastName));

            for (ZoneElement zone : nBimQGis.getZones().values()) {
                String[] nZoneNameSegments = zone.getName().split(" ");
                String nZoneName = nZoneNameSegments[0] + " " + nZoneNameSegments[1];

                HashMap<String, Double> nBlockedZones = file_n.getBlockedZones(60, 1.6);
                if (nBlockedZones.containsKey(nZoneName))
                    nBimQGis.setFireZone(zone.getId(), nBlockedZones.get(nZoneName));
            }
        }

        SummaryCollector nSummaryCollector = new SummaryCollector(nBimQGis);
        nBimQGis.setCollector(nSummaryCollector);

        Moving mov = new Moving(nBimQGis);
        mov.run();

        log.info(mov.getMessage() + "\n" + nSummaryCollector.getSummaryTable());

        FileHandler nFileHandler = new FileHandler();
        String nPath = selectPathToResultsFolder(nWorkspace, eProp, nBuildingFile) + nSeparator + eProp.getFileName();
        nFileHandler.save(nPath + "_z.csv", nSummaryCollector.getZonesTable());
        nFileHandler.save(nPath + "_d.csv", nSummaryCollector.getDoorsTable());
        nFileHandler.save(nPath + "_out.txt", mov.getMessage() + "\n" + nSummaryCollector.getSummaryTable());
    }

    private static String selectPathToResultsFolder(String nWorkspace, EvacPropertiesLoader eProp, File nBuildingFile) {
        String nPath;
        if (nBuildingFile.exists()) {
            File nResultsFolder = new File(nWorkspace, eProp.getResultsFolder());
            if (nResultsFolder.mkdir()) {
                nPath = nResultsFolder.getPath();
                log.info("Папка для результатов успешно создана: {}", nPath);
            } else if (nResultsFolder.exists()) {
                nPath = nResultsFolder.getPath();
                log.error("Папка для результатов не создана. Результаты сохранены в дирректории: {}", nPath);
            } else {
                nPath = nWorkspace;
                log.error("Папка для результатов не создана. Результаты сохранены корневой в дирректории: {}", nPath);
            }
        } else nPath = "/tmp";

        return nPath;
    }

    private static Distribution selectDistribution(EvacPropertiesLoader eProp, BuildingQGis nBimQGis) {
        Distribution nDistribution = null;
        switch (eProp.getBimDistribution()) {
            case NORMATIVE:
                nDistribution = new NormativeDistribution(eProp.getNormativeDistributionType(), eProp.getDistributionSeed(), 7, nBimQGis);
                break;
            case RANDOM:
                nDistribution = new RandomDistribution(eProp.getD0(), eProp.getK(), eProp.getDistributionSeed(), nBimQGis);
                break;
        }
        return nDistribution;
    }

    private static String selectFileOrDefault(File pFile, String pDefault) {
        String nResultPath;
        if (pFile.exists()) nResultPath = pFile.getPath();
        else nResultPath = Objects.requireNonNull(Main.class.getClassLoader().getResource(pDefault)).getPath();

        return nResultPath;
    }
}
