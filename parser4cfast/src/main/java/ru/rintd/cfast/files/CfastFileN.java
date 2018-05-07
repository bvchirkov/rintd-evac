package ru.rintd.cfast.files;

import ru.rintd.cfast.io.Csv2Map;

import java.util.*;

public class CfastFileN {

    private ArrayList<ArrayList<String>> nHeader;
    private ArrayList<ArrayList<Double>> nBody;

    public CfastFileN(String pPathToCsv) {
        Csv2Map csv = new Csv2Map(pPathToCsv);

        nHeader = csv.getHeaderTable();
        nBody = csv.getBodyTable();
    }

    public LinkedHashMap<Double, Double> getColumnData(String pRoomName, FieldsNFile pColumnType) {
        LinkedHashMap<Double, Double> nResultTable = new LinkedHashMap<>();

        for (int i = 1; i < nHeader.size(); i++) {
            ArrayList<String> nColHeader = nHeader.get(i);
            String nColumnType = nColHeader.get(0).split("_")[0];
            String nRoomName = nColHeader.get(2);

            if (nColumnType.equals(pColumnType.name()) && nRoomName.equals(pRoomName)) {
                for (int j = 0; j < nBody.get(0).size(); j++) {
                    Double nTime = nBody.get(0).get(j);
                    Double nValue = nBody.get(i).get(j);
                    nResultTable.put(nTime, nValue);
                }
            }
        }

        return nResultTable;
    }

    public HashMap<String, Double> getBlockedZones(double pUlt, double pHgt) {
        HashMap<String, Double> nResultMap = new HashMap<>();

        Set<String> nZonesName = new HashSet<>();
        for (int i = 1; i < nHeader.size(); i++) {
            ArrayList<String> nColHeader = nHeader.get(i);
            String nRoomName = nColHeader.get(2);
            nZonesName.add(nRoomName);
        }

        for (String name : nZonesName) {
            LinkedHashMap<Double, Double> nUlt = getColumnData(name, FieldsNFile.ULT);
            LinkedHashMap<Double, Double> nHgt = getColumnData(name, FieldsNFile.HGT);

            for (Map.Entry<Double, Double> e : nHgt.entrySet()) {
                boolean isDefineBlock = false;
                double time = e.getKey();
                double valHgt = e.getValue();

                if (Double.compare(valHgt, pHgt) > 0) {
                    double valUlt = nUlt.get(time);
                    if (Double.compare(valUlt, pUlt) > 0) {
                        nResultMap.put(name, time);
                        isDefineBlock = true;
                    }
                }

                if (isDefineBlock) break;
            }
        }

        return nResultMap;
    }
}
