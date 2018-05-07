package ru.rintd.cfast.io;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Csv2Map {

    private String pPathToFile;
    private ArrayList<ArrayList<Double>> bodyTable;
    private ArrayList<ArrayList<String>> headerTable;

    public Csv2Map(String pPathToFile) {
        this.pPathToFile = pPathToFile;
        unwrap();
    }

    private void unwrap() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(pPathToFile)), "UTF-8"))) {
            headerTable = new ArrayList<>();

            String s;
            for (int i = 0; i < 4; i++) {
                if ((s = br.readLine()) == null) return;
                String[] keys = s.split(",");
                if (headerTable.isEmpty()) {
                    for (String key : keys) {
                        ArrayList<String> headerVals = new ArrayList<>();
                        headerVals.add(key);
                        headerTable.add(headerVals);
                    }
                } else {
                    Iterator valsIter = Arrays.asList(keys).iterator();
                    for (ArrayList<String> v : headerTable)
                        v.add((String) valsIter.next());
                }
            }

            bodyTable = new ArrayList<>();
            while ((s = br.readLine()) != null) {
                String[] keys = s.split(",");
                if (bodyTable.isEmpty()) {
                    for (String key : keys) {
                        ArrayList<Double> headerVals = new ArrayList<>();
                        headerVals.add(Double.parseDouble(key));
                        bodyTable.add(headerVals);
                    }
                } else {
                    Iterator valsIter = Arrays.asList(keys).iterator();
                    for (ArrayList<Double> v : bodyTable)
                        v.add(Double.parseDouble((String) valsIter.next()));
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ArrayList<Double>> getBodyTable() {
        return bodyTable;
    }

    public ArrayList<ArrayList<String>> getHeaderTable() {
        return headerTable;
    }
}
