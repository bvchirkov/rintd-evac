package preparation;

import collect.Collector;
import distribution.Distribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.eesystem.parser4builder.json.structure.BIM;
import ru.eesystem.parser4builder.json.structure.BuildElement;
import ru.eesystem.parser4builder.json.structure.Level;
import ru.eesystem.parser4builder.json.structure.Sign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Класс перевода структуры данных из json в угодную для моделирования
 */
public class BuildingQGis {
    private static final Logger log = LoggerFactory.getLogger(BuildingQGis.class);

    /**
     * Список зон
     */
    private HashMap<String, ZoneElement> zones;
    /**
     * Список внутренних дверей
     */
    private HashMap<String, DoorElement> internalDoors;
    /**
     * Список эвакуационных выходов
     */
    private HashMap<String, DoorElement> exits;
    /**
     * Безопасная зона
     */
    private OutdoorElement outdoorElement;
    /**
     * Список зон, блокируемых пожаром
     */
    private Set<ZoneElement> blockedZones;
    /**
     * Площадь здания
     */
    private double area;
    /**
     * Количество людей в здании
     */
    private double numOfPeople;
    /**
     * наличие пожара в здании
     */
    private boolean fire;
    /**
     * Путь к файлу здания в формате json
     */
    private String nFilePath;
    /**
     * Обработчик срезов
     */
    private Collector collector;
    /**
     * Распределение
     */
    private Distribution distribution;
    private double timeOfBeginEvacuation;

    public BuildingQGis(String nFilePath) {
        this.nFilePath = nFilePath;
        unwrap();
        linked();
        doorWidthInit();
    }

    /**
     * Заполненеие списков
     */
    private void unwrap() {
        BIM nBim = new BIMLoader().getBim(getPathToFile());
        zones = new HashMap<>();
        internalDoors = new HashMap<>();
        exits = new HashMap<>();
        area = 0.0;

        for (Level level : nBim.getLevel()) {
            for (BuildElement bElem : level.BuildElement) {
                String uuid = bElem.getId();
                switch (bElem.getSign()) {
                    case Room:
                    case Staircase:
                        ZoneElement zone = new ZoneElement(bElem);
                        zone.setLevel(level.ZLevel);
                        zones.put(uuid, zone);
                        area += zone.getArea();
                        continue;
                    case DoorWay:
                    case DoorWayInt:
                        DoorElement internalDoor = new DoorElement(bElem);
                        internalDoor.setLevel(level.ZLevel);
                        internalDoors.put(uuid, internalDoor);
                        continue;
                    case DoorWayOut:
                        DoorElement exit = new DoorElement(bElem);
                        exit.setLevel(level.ZLevel);
                        exits.put(uuid, exit);
                }
            }
        }

        BuildElement nElem = new BuildElement();
        nElem.setSign(Sign.Outdoor);
        nElem.setNumPeople(0);
        nElem.setName("Safety Zone");
        nElem.setId("{00000000-0000-0000-0000-out_space001}");
        nElem.setSizeZ(0);
        nElem.setXY(null);
        nElem.setType(-1);
        ArrayList<String> Output = new ArrayList<>(exits.keySet());
        nElem.setOutput(Output);
        outdoorElement = new OutdoorElement(nElem);
    }

    /**
     * Связывание элементов здания в граф
     */
    private void linked() {
        for (DoorElement nDoor : internalDoors.values()) {
            String uuid1 = nDoor.getOutput().get(0);
            String uuid2 = nDoor.getOutput().get(1);

            ZoneElement nZone1 = zones.get(uuid1);
            ZoneElement nZone2 = zones.get(uuid2);

            nZone1.addRoommate(nDoor);
            nZone2.addRoommate(nDoor);
            nDoor.addRoommate(nZone1);
            nDoor.addRoommate(nZone2);
        }

        for (DoorElement nDoor : exits.values()) {
            String uuid1 = nDoor.getOutput().get(0);

            ZoneElement nZone1 = zones.get(uuid1);

            nZone1.addRoommate(nDoor);
            nDoor.addRoommate(nZone1);
        }
    }

    /**
     * Запуск вычисления ширины дверей
     */
    private void doorWidthInit() {
        for (DoorElement d : internalDoors.values()) {
            d.calculationWidth();
        }
        for (DoorElement d : exits.values()) {
            d.calculationWidth();
        }
    }

    /**
     * @return список зон
     */
    public HashMap<String, ZoneElement> getZones() {
        return zones;
    }

    /**
     * @return список внутренних дверей
     */
    public HashMap<String, DoorElement> getInternalDoors() {
        return internalDoors;
    }

    /**
     * @return список эвакуационных выходов
     */
    public HashMap<String, DoorElement> getExits() {
        return exits;
    }

    /**
     * @return безопасная зона
     */
    public OutdoorElement getOutdoorElement() {
        return outdoorElement;
    }

    /**
     * @return площадь здания, м^2
     */
    public double getArea() {
        return area;
    }

    /**
     * @return количество людей в здании
     */
    public double getNumOfPeople() {
        numOfPeople = 0.0;
        zones.values().forEach(v -> numOfPeople += v.getNumPeople());
        return numOfPeople;
    }

    /**
     * @return интерфейс {@link Distribution}
     */
    public Distribution getDistribution() {
        return distribution;
    }

    /**
     * Метод установки распределения людей в здании
     *
     * @param pDistribution класс, реализующий интерфейс {@link Distribution}
     * @return {@link Distribution}
     */
    public Distribution setDistribution(Distribution pDistribution) {
        distribution = pDistribution;
        return distribution;
    }

    /**
     * Метод установки обработчика срезов
     *
     * @param pCollector обработчик срезов, должен реализовать интерфейс {@link Collector}
     */
    public void setCollector(Collector pCollector) {
        this.collector = pCollector;
    }

    /**
     * Метод добавления нового среза
     *
     * @param pTime время эвакуации, в которое был сделан срез
     */
    public void addSlice(double pTime) {
        collector.addSlice(pTime);
    }

    /**
     * @return путь до файла со зданием в формате json
     */
    public String getPathToFile() {
        return nFilePath;
    }

    /**
     * Метод добавления зоны и времени ее блокирования
     *
     * @param pId   иднетификатор зона
     * @param pTime время блокирования зоны
     */
    public void setFireZone(String pId, double pTime) {
        if (!zones.containsKey(pId)) {
            log.error("Zone with UUID: {} not found. Program stopped", pId);
            return;
        }
        if (!fire) fire = true;
        ZoneElement nZone = zones.get(pId);
        nZone.setBlockTime(pTime);

        if (blockedZones == null) blockedZones = new HashSet<>();
        blockedZones.add(nZone);

    }

    /**
     * @return список зон, блокируемых пожаром
     */
    public ArrayList<ZoneElement> getFireZones() {
        return new ArrayList<>(blockedZones);
    }

    /**
     * @return true, если в одном из помещений здания пожар
     */
    public boolean isFire() {
        return fire;
    }

    public void setTimeOfBeginEvacuation(double timeOfBeginEvacuation) {
        this.timeOfBeginEvacuation = timeOfBeginEvacuation;
    }

    public double getTimeOfBeginEvacuation() {
        return timeOfBeginEvacuation;
    }
}
