import collect.SummaryCollector;
import distribution.NormativeDistribution;
import preparation.BuildingQGis;
import preparation.ZoneElement;
import simulation.Moving;

public class Main {
    public static void main(String... args) {

//        BuildingQGis nBimQGis = new BuildingQGis("/home/boris/buildings/udsu-v2018.03.12_with_corridors.json");
//        //nBimQGis.setDistribution(new RandomDistribution(0.5, 6, 5, nBimQGis)).apply();
//        nBimQGis.setDistribution(new NormativeDistribution(NormativeDistribution.UNIFORM, 5, 7, nBimQGis)).apply();
//
//        for (ZoneElement nZone : nBimQGis.getZones().values()) {
//            System.out.println(nZone.getId());
//            if (nZone.getId().contains("8009ec1cf364")) {
//                nBimQGis.setFireZone(nZone.getId(), 78);
//                break;
//            }
//        }
//
//        SummaryCollector nSummaryCollector = new SummaryCollector(nBimQGis);
//        nBimQGis.setCollector(nSummaryCollector);
//
//        Moving mov = new Moving(nBimQGis);
//        mov.run();
//
//        System.out.println(nSummaryCollector.getSummaryTable());
//        System.out.println(nSummaryCollector.getDoorsTable());

    }
}
