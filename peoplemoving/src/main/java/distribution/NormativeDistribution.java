package distribution;

import preparation.BuildingQGis;
import preparation.ZoneElement;

import java.util.Random;

/**
 * Класс распределения людей согласно СНиП II-Л.6-67
 */
public class NormativeDistribution implements Distribution {

    /**
     * Ключ равномерного распределения людей по всем помещениям кроме корридоров
     */
    public static final int UNIFORM = 0;
    /**
     * Случайное распределения людей по помещениям, исключая корридоры.
     * <p>
     * Некоторые помещения окажутся пустыми, потому что заполнение помещения
     * происходит в том случае, если случайное число из диапазона [0;6] больше 1.
     */
    public static final int RANDOM = 1;

    private final int type; // Тип распределения
    private final int seed; // Стартовое положение случайного распределения
    private final int emptyZoneType; // Номер типа зон, которые будут приниматься за коридор
    private final BuildingQGis bim;

    /**
     * Распределение людей согласно СНиП II-Л.6-67 - 0.5 чел/м^2
     *
     * @param pType          тип распределения. {@link NormativeDistribution#UNIFORM} (равномерное)
     *                       или {@link NormativeDistribution#RANDOM} (случайное)
     * @param pSeed          начальное число для генератора случайных чисел
     * @param pEmptyZoneType тип помещения (по пожарной нагрузке), помещения с которым
     *                       являются корридором (люди в него не помещаются)
     * @param bim            здание
     */
    public NormativeDistribution(int pType, int pSeed, int pEmptyZoneType, BuildingQGis bim) {
        this.type = pType;
        this.seed = pSeed;
        this.emptyZoneType = pEmptyZoneType;
        this.bim = bim;
    }

    /**
     * Распределение людей согласно СНиП II-Л.6-67 - 0.5 чел/м^2
     *
     * @param pType          тип распределения. {@link NormativeDistribution#UNIFORM} (равномерное)
     *                       или {@link NormativeDistribution#RANDOM} (случайное)
     * @param pEmptyZoneType тип помещения (по пожарной нагрузке), помещения с которым
     *                       являются корридором (люди в него не помещаются)
     * @param bim            здание
     */
    public NormativeDistribution(int pType, int pEmptyZoneType, BuildingQGis bim) {
        this.type = pType;
        this.seed = -1;
        this.emptyZoneType = pEmptyZoneType;
        this.bim = bim;
    }

    @Override
    public void apply() {
        setDistribution(type, seed, emptyZoneType, bim);
    }

    /**
     * Реализация нормативного распределения
     */
    private void setDistribution(int pType, int pSeed, int pEmptyZoneType, BuildingQGis bim) {
        switch (pType) {
            case UNIFORM: // Нормативное значение, согласно СНиП II-Л.6-67
                for (ZoneElement nRoom : bim.getZones().values()) {
                    if (nRoom.getType() == pEmptyZoneType) // Подсобные и бытовые помещения, лестничная клетка, корридор
                        nRoom.setNumPeople(0.0);
                    else nRoom.setDensity(1.0 / 2.0); // 1 чел. на 2 кв.м.
                }
                break;
            case RANDOM:
                Random nRndVal = pSeed > 0 ? new Random(pSeed) : new Random();
                for (ZoneElement nRoom : bim.getZones().values()) {
                    nRoom.setNumPeople(0.0);
                    // В помещение устанавливаем плотность, если это не коридор
                    // или слуйно сгенерированное число больше 1 и меньше 5
                    int nNextVal = nRndVal.nextInt(5);
                    boolean nIsGoodVal = nNextVal > 1;
                    if (nRoom.getType() != pEmptyZoneType && nIsGoodVal)
                        nRoom.setDensity(1.0 / 2.0); // 1 чел. на 2 кв.м.
                }
                break;
        }
    }

    @Override
    public int getSeed() {
        return seed;
    }

    @Override
    public double getDensity() {
        return 0.5;
    }

    @Override
    public String[] getOtherParams() {
        return new String[]{"тип распределения:\t" + type, "тип коридоров:\t" + emptyZoneType};
    }
}
