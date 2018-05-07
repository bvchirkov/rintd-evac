package preparation;

import com.vividsolutions.jts.geom.Polygon;
import ru.eesystem.parser4builder.json.structure.BuildElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static ru.eesystem.parser4builder.json.structure.Sign.Room;
import static ru.eesystem.parser4builder.json.structure.Sign.Staircase;

/**
 * Класс, описывающий помещения и лестницы
 */
public class ZoneElement implements Element {
    private int tay; // признак посещения помещения на шаге моделирования

    private double area; // Площадь, м^2
    private double density; // Плотность людей, чел/м^2
    private double permeability; // Коэффициент проходимости
    private double level; // уровень (этаж), м^2
    // Идентификатор очереди
    // (по факту uuid эвак. выхода, через которую люди из этого помещения выходят из здания)
    private String queueId;
    // Хеш карта потенциалов, ключ=uuid эвак. выхода, значение=число
    // значением является время достижения эвак выхода в мин. (вроде)
    // TODO проверить единицы измерения потенциала
    private HashMap<String, Double> potentials;
    // Хеш карта, слежения за выходом людей
    // ключ=uuid двери, значение=число (количество вышедших)
    private HashMap<String, Double> importPeople;
    private BuildElement self; // Элемент здания
    private Set<DoorElement> roommates;
    private double blockTime; // Время блокирования помещения, сек

    /**
     * Класс, описывающий помещения и лестницы
     *
     * @param self объект класса {@link BuildElement}
     */
    ZoneElement(BuildElement self) {
        this.self = self;
        this.permeability = 1.0;
        this.blockTime = -1.0;
        this.potentials = new HashMap<>();
        this.roommates = new HashSet<>();

        calculateArea();
        calculateDensity();
    }

    /**
     *
     */
    private ZoneElement(double area, double density, String queueId, double permeability,
                        HashMap<String, Double> potentials, BuildElement self,
                        int tay, double level, Set<DoorElement> roommates, double blockTime) {
        this.area = area;
        this.density = density;
        this.queueId = queueId;
        this.permeability = permeability;
        this.self = BuildElement.getInstance(self);
        this.tay = tay;
        this.level = level;

        this.potentials = new HashMap<>(potentials);
        this.blockTime = blockTime;

        this.roommates = new HashSet<>();
        for (DoorElement nDoor : roommates) this.roommates.add(DoorElement.getInstance(nDoor));
    }

    private ZoneElement(ZoneElement self) {
        this(self.area, self.density, self.queueId, self.permeability, self.potentials, self.self,
                self.tay, self.level, self.roommates, self.blockTime);
    }

    public static ZoneElement getInstance(ZoneElement pZoneElement) {
        return new ZoneElement(pZoneElement);
    }

    /**
     * Вычисление площади зоны
     */
    private void calculateArea() {
        Polygon mP = ElementGeometry.getPolygon(self.getXY());
        if (mP != null) area = mP.getArea();
        else area = 0;
    }

    /**
     * Вычисление плотности в зоне
     */
    private void calculateDensity() {
        density = self.getNumPeople() / getArea();
    }

    /**
     * @return площадь зоны
     */
    public double getArea() {
        return area;
    }

    /**
     * @return плотность в зоне, чел./м^2
     */
    public double getDensity() {
        calculateDensity();
        return density;
    }

    /**
     * Метод установки плотности людей в зоне
     *
     * @param density плотность, чел./м^2
     */
    public void setDensity(double density) {
        this.density = density;
        self.setNumPeople(density * getArea());
    }

    /**
     * Увеличение количества людей в зоне
     *
     * @param pDeltaPeople величина, на которую увеличивается количество людей в зоне
     */
    public void addPeople(double pDeltaPeople) {
        self.setNumPeople(self.getNumPeople() + pDeltaPeople);
    }

    /**
     * Уменьшение количества людей в зоне
     *
     * @param pDeltaPeople величина, на которую уменьшаяется количество людей в зоне
     */
    public void removePeople(double pDeltaPeople) {
        self.setNumPeople(self.getNumPeople() - pDeltaPeople);
    }

    /**
     * Метод установки идентификатора эвакуационного выхода, в который выходят люди из текущего помещения
     *
     * @param pExitUuid идентификатор эвакуационного выхода
     */
    public void setQueueId(String pExitUuid) {
        queueId = pExitUuid;
    }

    /**
     * @return коэффициент замедления движения людей в зоне (должен быть связан с пожаром).
     * Изменяется от 0 до 1.
     */
    public double getSlownessFactor() {
        return 1.0;
    }

    /**
     * @return коэффициент проходимости помещения.
     * Изменяется от 0 до 1.
     */
    public double getPermeability() {
        return permeability;
    }

    /**
     * Метод изменения проницаемости зоны
     *
     * @param pPermeabilityValue - проницаемость зоны (от 0 до 1)
     */
    public void setPermeability(double pPermeabilityValue) {
        permeability = pPermeabilityValue;
    }

    /**
     * @return список потенциалов (время достижения эвакуационного выхода) в формате <br>
     * идентификатор эвакуационного выхода:числовое значение
     */
    public HashMap<String, Double> getPotentials() {
        return potentials;
    }

    /**
     * Метод записывающий откуда и сколько людей поступило в данное помещение
     *
     * @param pDoorUuid    идентификатор двери, через которую вошли люди
     * @param pNumOfPeople количество людей
     */
    @Deprecated
    public void leaveVia(String pDoorUuid, double pNumOfPeople) {
        importPeople.put(pDoorUuid, pNumOfPeople);
    }

    @Deprecated
    private void setImportPeople(HashMap<String, Double> importPeople) {
        this.importPeople = importPeople;
    }

    /**
     * Метод установки метки, которая показывает, что в этом помещении уже были на текущем шаге моделирования
     *
     * @param pTay занчение
     */
    public void setTay(int pTay) {
        tay = pTay;
    }

    /**
     * @return значение метки, которая показвает, что в этом помещении уже были на текущем шаге
     */
    public int getTay() {
        return tay;
    }

    /**
     * @return координаты точек помещения
     */
    double[][][] getXY() {
        return self.getXY();
    }

    @Override
    public double getLevel() {
        return level;
    }

    /**
     * Метод установки уровня, на котором располагается зона
     *
     * @param level уровень, м.
     */
    void setLevel(double level) {
        this.level = level;
    }

    /**
     * @return список дверей, которыми помещение связано с другими зонами
     */
    public ArrayList<DoorElement> getRoommates() {
        return new ArrayList<>(roommates);
    }

    /**
     * Метод добавления двери в список соседей
     *
     * @param pRoommate объект класса {@link DoorElement}
     */
    void addRoommate(DoorElement pRoommate) {
        roommates.add(pRoommate);
    }

    /**
     * Метод изменения количества людей в зоне
     *
     * @param pPeople количесто людей
     */
    public void setNumPeople(double pPeople) {
        self.setNumPeople(pPeople);
    }

    /**
     * @return количество людей в зоне
     */
    public double getNumPeople() {
        return self.getNumPeople();
    }

    /**
     * @return true, если текущая зона является помещением
     */
    public boolean isRoom() {
        return self.getSign() == Room;
    }

    /**
     * @return true, если текущая зона является лестничной площадкой
     */
    public boolean isStaircase() {
        return self.getSign() == Staircase;
    }

    /**
     * @return иднетификатор зоны
     */
    public String getId() {
        return self.getId();
    }

    /**
     * @return тип пожарной нагрузки помещения
     */
    public int getType() {
        return self.getType();
    }

    /**
     * @return название помещения, заданное при создании плана здания в QGis
     */
    public String getName() {
        return self.getName();
    }

    /**
     * Метод изменения времения блокирования помещения
     *
     * @param pTime время блокирования, сек.
     */
    public void setBlockTime(double pTime) {
        blockTime = pTime;
    }

    /**
     * @return время блокирования помещения, если <0, значит значение не установлено
     */
    public double getBlockTime() {
        return blockTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ZoneElement)) return false;

        ZoneElement that = (ZoneElement) o;

        if (Double.compare(that.area, area) != 0) return false;
        return Double.compare(that.level, level) != 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;

        temp = Double.doubleToLongBits(area);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(level);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
