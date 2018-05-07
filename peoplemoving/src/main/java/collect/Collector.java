package collect;

/**
 * Интерфейс для получения срезов состояний здания по времени
 */
public interface Collector {
    /**
     * Добавление среза
     *
     * @param pTime момент времени, в который получен срез
     */
    void addSlice(double pTime);
}
