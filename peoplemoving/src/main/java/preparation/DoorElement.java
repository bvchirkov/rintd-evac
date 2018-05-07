package preparation;

import com.vividsolutions.jts.geom.Polygon;
import ru.eesystem.parser4builder.json.structure.BuildElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static ru.eesystem.parser4builder.json.structure.Sign.*;

public class DoorElement {

    private int tay;
    /**
     * Ширина двери
     */
    private double width;
    /**
     * Количество людей, прошедших через дверь
     */
    private double transitPeople;
    /**
     * Уровень, на котором находится дверь
     */
    private double level;
    /**
     * Идентификатор помещения, в которое перемещяются люди
     */
    private String direction;
    /**
     * Список соседних элеметов (должно быть 1 или 2)
     */
    private Set<ZoneElement> roommates;
    private BuildElement self;

    DoorElement(BuildElement self) {
        this.self = self;
        this.transitPeople = 0.0;
        this.roommates = new HashSet<>();
    }

    private DoorElement(double width, double transitPeople, BuildElement self, int tay, double level,
                        Set<ZoneElement> roommates, String direction) {
        this.width = width;
        this.transitPeople = transitPeople;
        this.self = BuildElement.getInstance(self);
        this.tay = tay;
        this.level = level;
        this.direction = direction;

        this.roommates = new HashSet<>(roommates);
    }

    private DoorElement(DoorElement self) {
        this(self.width, self.transitPeople, self.self, self.tay, self.level, self.roommates, self.direction);
    }

    public static DoorElement getInstance(DoorElement self) {
        return new DoorElement(self);
    }

    /**
     * Вычисление ширины проема
     */
    void calculationWidth() {
        ArrayList<ZoneElement> roommate = getRoommates();
        ZoneElement nZoneA = roommate.get(0);
        Polygon pZa = ElementGeometry.getPolygon(nZoneA.getXY());

        Polygon pD = ElementGeometry.getPolygon(self.getXY());
        double width;

        switch (roommate.size()) {
            case 1: // Эвакуационные выходы
                width = extractionWidth(pD, pZa);
                break;
            case 2: // Все остальные проемы
                ZoneElement nZoneB = roommate.get(1);
                width = min(extractionWidth(pD, pZa), extractionWidth(pD, ElementGeometry.getPolygon(nZoneB.getXY())));
                // Блок опроделения ширины для проемов между лестницами. В него попадем только при определенном условии
                if (nZoneA.isStaircase() && nZoneB.isStaircase() && isDoorWay())
                    width = sqrt(min(nZoneA.getArea(), nZoneB.getArea()));
                break;
            default:
                throw new Error("Ну дела! У двери больше 2 соседей. Это недопустимо. Исправляйте, уважаемый.");
        }

        /*width = roundingValue(width);*/
        width = new BigDecimal(width).setScale(2, BigDecimal.ROUND_UP).doubleValue();
        if (width == 0d) throw new Error("Ширина проема определена не верно " + width);

        this.width = width;
    }

    /**
     * Функция вычисления ширины проема
     *
     * @param pD полигон, который образует дверь
     * @param pR полигон, который образует помещение
     * @return ширина двери
     */
    private double extractionWidth(Polygon pD, Polygon pR) {
        // Находим длину отрезка внешнего контура, который пересекается с полигоном двери
        return pR.getExteriorRing().intersection(pD).getLength();
    }

    /**
     * Увеличение количества людей, прошедших через дверь
     *
     * @param pNumOfPeople количество людей
     */
    public void addTransitPeople(double pNumOfPeople) {
        transitPeople += pNumOfPeople;
    }

    /**
     * @return количество людей, прошедших через дверь
     */
    public double getTransitPeople() {
        return transitPeople;
    }

    /**
     * @return ширина двери
     */
    public double getWidth() {
        return width;
    }

    /**
     * @return иднетификатор двери
     */
    public String getId() {
        return self.getId();
    }

    /**
     * @return список идентификаторов сосдених элементов
     */
    ArrayList<String> getOutput() {
        return self.getOutput();
    }

    /**
     * @return true, если дверь является виртуальным проемом
     */
    public boolean isDoorWay() {
        return self.getSign() == DoorWay;
    }

    /**
     * @return true, если дверь является внутренней дверью
     */
    public boolean isDoorWayInt() {
        return self.getSign() == DoorWayInt;
    }

    /**
     * @return true, если дверь является эвакуационным выходом
     */
    public boolean isDoorWayOut() {
        return self.getSign() == DoorWayOut;
    }

    /**
     * @return список соседних элементов (size: 1 или 2)
     */
    public ArrayList<ZoneElement> getRoommates() {
        return new ArrayList<>(roommates);
    }

    /**
     * Метод добавления соседнего элемента
     *
     * @param pRoommate соседний элемент, объект класса {@link ZoneElement}
     */
    void addRoommate(ZoneElement pRoommate) {
        roommates.add(pRoommate);
    }

    /**
     * @return уровень, на котором находится дверь
     */
    public double getLevel() {
        return level;
    }

    /**
     * Метод изменения уровня, на котором находится дверь
     *
     * @param level уровень, м.
     */
    void setLevel(double level) {
        this.level = level;
    }

    /**
     * Метод изменения признака обработки двери
     *
     * @param pTay числовое значение
     */
    public void setTay(int pTay) {
        tay = pTay;
    }

    /**
     * @return числовое значение - признак, что эту дверь обрабатывали на текущем шаге
     */
    public int getTay() {
        return tay;
    }

    /**
     * Увеличение значения tay
     *
     * @return значение tay
     */
    public int addTay() {
        tay++;
        return tay;
    }

    /**
     * Метод изменения идентификатора помещения, в которое идут люди через текущую дверь
     *
     * @param pUuidZone идентификатор помещения
     */
    public void setDirection(String pUuidZone) {
        direction = pUuidZone;
    }

    /**
     * @return идентификатор помещения, в которое люди перемещаются через текущую дверь
     */
    public String getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoorElement)) return false;

        DoorElement that = (DoorElement) o;

        if (tay != that.tay) return false;
        if (Double.compare(that.width, width) != 0) return false;
        if (Double.compare(that.transitPeople, transitPeople) != 0) return false;
        if (Double.compare(that.level, level) != 0) return false;
        return self.equals(that.self);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = tay;
        temp = Double.doubleToLongBits(width);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(transitPeople);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(level);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + self.hashCode();
        return result;
    }
}
