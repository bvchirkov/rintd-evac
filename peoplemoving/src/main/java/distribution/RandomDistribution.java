package distribution;

import preparation.BuildingQGis;
import preparation.ZoneElement;

import java.util.ArrayList;
import java.util.Random;


/**
 * Реализовано псевдо случайное равномерно распределение.
 * <p>
 * Здание делится на 2 части с отношением k. Плотность в частях здания определяется как
 * d2 = 2*d0/(K+1), d1 = 2*K*d0/(K+1), где d0 - плотность при равномерном распределении во всем здании (заданная
 * величина)
 */
public class RandomDistribution implements Distribution {

    /**
     * Средняя плотность по зданию
     */
    private final double d0;
    /**
     * Коэффициент различия плотности
     */
    private final double k;
    /**
     * Начальное число для генератора случайных чисел
     */
    private final int seed;

    private BuildingQGis bim;

    /**
     * Псевдо случайное равномерно распределение.
     * <p>
     * Здание делится на 2 части с отношением k. Плотность в частях здания определяется как
     * d2 = 2*d0/(K+1), d1 = 2*K*d0/(K+1), где d0 - плотность при равномерном распределении во всем здании (заданная
     * величина)
     *
     * @param seed начальное число для генератора случайных чисел
     * @param d0   средняя плотность людей, с которой необходим заполнить здание
     * @param k    коэффициент отношения частей
     * @param bim  здание целиком
     */
    public RandomDistribution(double d0, double k, int seed, BuildingQGis bim) {
        this.d0 = d0;
        this.k = k;
        this.seed = seed;
        this.bim = bim;
    }

    /**
     * Псевдо случайное равномерно распределение.
     * <p>
     * Здание делится на 2 части с отношением k. Плотность в частях здания определяется как
     * d2 = 2*d0/(K+1), d1 = 2*K*d0/(K+1), где d0 - плотность при равномерном распределении во всем здании (заданная
     * величина)
     *
     * @param d0  средняя плотность людей, с которой необходим заполнить здание
     * @param k   коэффициент отношения частей
     * @param bim здание целиком
     */
    public RandomDistribution(double d0, double k, BuildingQGis bim) {
        this.d0 = d0;
        this.k = k;
        this.seed = -1;
        this.bim = bim;
    }

    @Override
    public void apply() {
        setUniformDistribution(d0, k, seed, bim);
    }

    @Override
    public int getSeed() {
        return seed;
    }

    @Override
    public double getDensity() {
        return d0;
    }

    @Override
    public String[] getOtherParams() {
        return new String[]{"коэффициен различи:\t" + k};
    }

    /**
     * Реализация "равномерного" распределения людей в здании.
     */
    private void setUniformDistribution(double d0, double k, int seed, BuildingQGis bim) {
        removePeople();
        //double d0 = numOfPeople / getBuildArea(); // Начальная плотность
        double d1 = 2 * k * d0 / (k + 1); // Плотность в первой половине здания
        double d2 = 2 * d0 / (k + 1); // Плотность во второй половине здания
        double halfArea = bim.getArea() / 2.0; // Половина площади здания
        ArrayList<ZoneElement> nZones = new ArrayList<>(bim.getZones().values()); // Список несещнных помещений
        Random random = seed > 0 ? new Random(seed) : new Random();

        double sumArea = 0d; // Суммарная площадь обработанных элементов
        while (!nZones.isEmpty()) {
            // Получаем случайное помещение из здания
            // bound для nextInt каждый раз меняется, чтобы не попадать на уже просмотренные
            ZoneElement nZone = nZones.get(random.nextInt(nZones.size()));
            double elementArea = nZone.getArea(); // Площадь текущего элемента
            double d;

            // Если суммарная площадь обработанных элементов меньше половины здания, то размещаем плотсность d2,
            // иначе d1
            if (sumArea + elementArea <= halfArea + halfArea / 1000.0) {
                d = d2;
                // и увеличиваем использованную долю
                sumArea += elementArea;
            } else d = d1;
            // то размещаем в помещении полученную плотность
            nZone.setDensity(d);
            // Удаляем обработанный элемент из списка
            nZones.remove(nZone);
        }
    }

    /**
     * Очистка здания от людей
     */
    private void removePeople() {
        bim.getZones().values().forEach(v -> v.setNumPeople(0.0));
    }
}

