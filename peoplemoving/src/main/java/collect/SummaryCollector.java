package collect;

import preparation.BuildingQGis;
import preparation.DoorElement;
import preparation.ZoneElement;

import java.util.*;

/**
 * Класс, реализующий интерфейс получения срезов
 * <p>
 * Позволяет получить информацию о всех помещения и дверях в формате csv
 */
public class SummaryCollector implements Collector {

    private BuildingQGis bim;
    /**
     * Таблица срезов состояний помещений во времени
     */
    private LinkedHashMap<Double, HashMap<String, ZoneElement>> slicesZones;
    /**
     * Таблица срезов состояний дверей во времени
     */
    private LinkedHashMap<Double, HashMap<String, DoorElement>> slicesDoors;
    /**
     * Количество людей в здании
     */
    private double numOfPeople;
    /**
     * Количество людей в здании на момент начала эвакуации
     */
    private double basicNumOfPeople;
    /**
     * Длительность эвакуации
     */
    private double timeEvacuation;

    /**
     * @param bim объект класса {@link BuildingQGis},
     *            необходим для работы с его полями (списками помещений, дверей и др.)
     */
    public SummaryCollector(BuildingQGis bim) {
        this.bim = bim;
        this.basicNumOfPeople = bim.getNumOfPeople();
        this.slicesZones = new LinkedHashMap<>();
        this.slicesDoors = new LinkedHashMap<>();
    }

    @Override
    public void addSlice(double pTime) {
        // Наполенение таблицы срезов помещений
        HashMap<String, ZoneElement> zones = new HashMap<>();
        for (ZoneElement nZone : bim.getZones().values()) {
            zones.put(nZone.getId(), ZoneElement.getInstance(nZone));
        }
        slicesZones.put(pTime, zones);

        // Наполенение таблицы срезов дверей
        HashMap<String, DoorElement> doors = new HashMap<>();
        for (DoorElement nDoor : bim.getInternalDoors().values()) {
            doors.put(nDoor.getId(), DoorElement.getInstance(nDoor));
        }
        for (DoorElement nDoor : bim.getExits().values()) {
            doors.put(nDoor.getId(), DoorElement.getInstance(nDoor));
        }
        slicesDoors.put(pTime, doors);

        timeEvacuation = pTime;
    }

    /**
     * Сводная таблица данных по зданию
     *
     * @return общая информация о здании
     */
    public String getSummaryTable() {
        String LF = "\n";
        StringBuilder nSummary = new StringBuilder();

        nSummary.append("Справка по зданию").append(LF).append(LF);
        nSummary.append("Путь к файлу здания json: \t").append(bim.getPathToFile()).append(LF);
        nSummary.append("Площадь здания: \t").append(scienceFmt(bim.getArea())).append(LF);
        nSummary.append("Количество людей в здании:\t").append(scienceFmt(bim.getNumOfPeople())).append(LF);
        nSummary.append("Количество людей в безопасной зоне:\t").append(scienceFmt(bim.getOutdoorElement().getNumPeople())).append(LF);
        nSummary.append("Длительность эвакуации:\t").append(scienceFmt(timeEvacuation)).append(LF);
        nSummary.append("Время начала эвакуации:\t").append(bim.getTimeOfBeginEvacuation()).append(LF);
        nSummary.append("Количество эвакуационных выходов:\t").append(bim.getExits().size()).append(LF);
        nSummary.append("Количество помещений:\t").append(bim.getZones().size()).append(LF);
        nSummary.append("Количество внутренних дверей:\t").append(bim.getInternalDoors().size()).append(LF);
        nSummary.append("Распределение людей в здании:\t").append(bim.getDistribution().getClass().getSimpleName()).append(LF);
        nSummary.append("Параметры распределения:").append(LF);
        nSummary.append("\tseed:\t").append(bim.getDistribution().getSeed()).append(LF);
        nSummary.append("\tсредняя плотность:\t").append(bim.getDistribution().getDensity()).append(LF);
        for (int i = 0; i < bim.getDistribution().getOtherParams().length; i++) {
            String p = bim.getDistribution().getOtherParams()[i];
            nSummary.append("\t").append(p).append(LF);
        }

        if (bim.isFire()) {
            nSummary.append("Блокируемые помещения: ").append(bim.getFireZones().size()).append(LF);
            for (ZoneElement nZone : bim.getFireZones())
                nSummary.append("\t").append(nZone.getId()).append("\t").append(nZone.getBlockTime()).append(LF);
        }

        return nSummary.toString();
    }

    /**
     * Таблица данных о состоянии дверей в процессе моделирования.
     * Информация получена из срезов
     *
     * @return таблица в формате csv <br>
     * первый столбец - время (<b>столбец не повторяется</b>)<br>
     * <p>
     * <b>следующий столбцы повторяются для каждой двери</b><br>
     * первый столбец - количество людей, прошедших через дверь<br>
     * второй столбец - идентификатор помещения, в который прошли люди в текущий момент времени<br>
     */
    public String getDoorsTable() {
        String delimiter = ";";
        boolean isHeaderInstalled = false;

        String[][] nRowsNames = {
                {"Time", "сек."},
                {"Flow", "чел."},
                {"To", ""},
        };

        StringJoiner nColumnsNames = new StringJoiner(delimiter, "", "\n"); // Названия столбцов
        nColumnsNames.add(nRowsNames[0][0]);

        StringJoiner nUnits = new StringJoiner(delimiter, "", "\n"); // Единицы измерения
        nUnits.add(nRowsNames[0][1]);

        StringJoiner nIdDoor = new StringJoiner(delimiter, "", "\n"); // ID дверей
        nIdDoor.add("");

        StringJoiner nRowsTable = new StringJoiner("\n"); // Строки данных по времени
        // Обход всех срезов
        for (Map.Entry<Double, HashMap<String, DoorElement>> slices : slicesDoors.entrySet()) {
            StringBuilder nRow = new StringBuilder(); // Текущая строка
            double nTime = slices.getKey(); // Текущий момент времени
            nRow.append(scienceFmt(nTime)).append(delimiter);

            // Обход всех дверей из среза
            for (DoorElement nDoor : slices.getValue().values()) {
                nRow.append(scienceFmt(nDoor.getTransitPeople())).append(delimiter); //Количество прошедших ч/з дверь людей
                // Направление движения
                // null возникает, потому что до начала моделирования движения нет
                if (nDoor.getDirection() == null) nRow.append("NULL").append(delimiter);
                else
                    nRow.append(nDoor.getDirection().split("-")[4].replace("}", "")).append(delimiter);

                // Заполнение шапки таблицы
                if (!isHeaderInstalled)
                    for (int i = 1; i < nRowsNames.length; i++) {
                        nColumnsNames.add(nRowsNames[i][0]);
                        nUnits.add(nRowsNames[i][1]);
                        nIdDoor.add(nDoor.getId().split("-")[4].replace("}", ""));
                    }
            }
            nRow.deleteCharAt(nRow.length() - 1);
            nRowsTable.add(nRow);
            if (!isHeaderInstalled) isHeaderInstalled = true;
        }

        return nColumnsNames.toString() + nIdDoor.toString() + nUnits.toString() + nRowsTable.toString();
    }

    /**
     * Таблица данных о состоянии зон (помещений и лестничных площадок) в процессе моделирования.
     * Информация получена из срезов
     *
     * @return таблица в формате csv <br>
     * неповторящиеся столбцы: <br>
     * первый столбец - время<br>
     * второй столбец - общее количество людей, оставшихся в здании<br><br>
     * повторяющиеся столбцы: <br>
     * первый столбец - количество людей<br>
     * второй столбец - плотность, чел./м^2<br>
     */
    public String getZonesTable() {
        String delimiter = ";";
        boolean isHeaderInstalled = false;

        String[][] nRowsNames = {
                {"Time", "сек."},
                {"Total number of people", "чел."},
                {"Number of people", "чел."},
                {"Density", "чел./м^2"}
        };

        StringJoiner nColumnsNames = new StringJoiner(delimiter, "", "\n");
        nColumnsNames.add("#" + nRowsNames[0][0]).add(nRowsNames[1][0]);

        StringJoiner nIdsZones = new StringJoiner(delimiter, "", "\n");
        nIdsZones.add("#").add(""); // Пропуск двух полей, чтоб повтрояющиеся столбцы встали на место

        StringJoiner nNamesZones = new StringJoiner(delimiter, "", "\n");
        nNamesZones.add("#").add("");

        StringJoiner nUnits = new StringJoiner(delimiter, "", "\n");
        nUnits.add("#" + nRowsNames[0][1]).add(nRowsNames[1][1]);

        StringJoiner nRowsTable = new StringJoiner("\n");
        // Обход срезов
        for (Map.Entry<Double, HashMap<String, ZoneElement>> slices : slicesZones.entrySet()) {
            StringBuilder nRow = new StringBuilder();
            double nTime = slices.getKey();
            nRow.append(scienceFmt(nTime)).append(delimiter);

            ArrayList<ZoneElement> nZones = new ArrayList<>(slices.getValue().values());
            numOfPeople = 0.0;
            nZones.forEach(v -> numOfPeople += v.getNumPeople());
            nRow.append(scienceFmt(basicNumOfPeople - numOfPeople)).append(delimiter);

            for (ZoneElement nZone : nZones) {
                double numPeople = nZone.getNumPeople();
                double density = nZone.getDensity();

                nRow.append(scienceFmt(numPeople)).append(delimiter);
                nRow.append(scienceFmt(density)).append(delimiter);

                if (!isHeaderInstalled)
                    for (int i = 2; i < nRowsNames.length; i++) {
                        nColumnsNames.add(nRowsNames[i][0]);
                        nIdsZones.add(nZone.getId().split("-")[4].replace("}", ""));
                        nNamesZones.add(nZone.getName());
                        nUnits.add(nRowsNames[i][1]);
                    }
            }
            nRow.deleteCharAt(nRow.length() - 1);
            nRowsTable.add(nRow);
            if (!isHeaderInstalled) isHeaderInstalled = true;
        }

        return nColumnsNames.toString() + nIdsZones.toString() + nNamesZones.toString() + nUnits.toString()
                + nRowsTable.toString();
    }

    /**
     * Научный формат данных
     *
     * @param v значение
     * @return форматированная строка
     */
    private String scienceFmt(double v) {
        return String.format("%6.3e", v);
    }
}
