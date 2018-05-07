package preparation;

/**
 * Интерфейс понадобился для унификации работы с зонами и безопасной зоной
 * в классе поделирования
 */
public interface Element {
    /**
     * @return уровень, на котором расположен элемент
     */
    double getLevel();
}
