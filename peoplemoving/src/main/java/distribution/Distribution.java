package distribution;

/**
 * Интерфейс распределения людей
 */
public interface Distribution {

    /**
     * Применение распределения
     */
    void apply();

    /**
     * @return стартовое положение случайного распеределения, которое было задано
     */
    int getSeed();

    /**
     * @return средняя плотность
     */
    double getDensity();

    /**
     * @return список других параметров
     * формат записи: "название параметра\t"+значение параметра
     */
    String[] getOtherParams();
}
