/*
 Copyright (C) 2016 Kolodkin Vladimir <kolodkin@rintd.ru>   Version of 27.12.2016
                                                            Version of 02.04.2018
 Project website:       http://eesystem.ru
 Organization website:  http://rintd.ru

 --------------------- DO NOT REMOVE THIS NOTICE ------------------------------
 This file is part of SimulationMoving.

 SimulationMoving is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 SimulationMoving is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with SimulationMoving. If not, see <http://www.gnu.org/licenses/>.
*/

package simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import preparation.*;

import java.util.*;

import static java.lang.Math.*;
import static simulation.Traffic.Direction.DOWN;
import static simulation.Traffic.Direction.UP;

class Traffic {
    private final static Logger log = LoggerFactory.getLogger(Traffic.class);

    /**
     * Коэффициент проходимости зоны. Если значение проходимости зоны ниже этого
     * значения, зона не проходима
     */
    private final static double CR_PERMEABILITY = 0.1;

    /**
     * Максимальная скорость движения людского потока, м/мин
     */
    private final double V_MAX;
    /**
     * Максимальная плотность людского потока
     */
    private final double D_MAX;
    /**
     * Минимальная плотность людского потока
     */
    private final double D_MIN;
    /**
     * Средняя площадь помещений
     */
    private final double averageWidth;
    /**
     * Безопасная зона
     */
    private OutdoorElement safetyZone;
    /**
     * Количество людей в здании. Если поставить число <=0, то распределение
     * людей берется из ПИМ здания.
     */
    private double numOfPeople;
    /**
     * Шаг по времени
     */
    private double tay;
    /**
     * Индексированный списко зон, где индекс - uuid зона
     */
    private HashMap<String, ZoneElement> zones;
    /**
     * Список эвакуационных выходов
     */
    private HashMap<String, DoorElement> exits;

    /**
     * D_MIN и K_TAY выведено из экспериментов от 18.05.17
     *
     * @param bim объект модели здания
     * @see <a href="https://drive.google.com/open?id=0B62fOieUTaVxZl8yTTU1b0lXM00">Результаты исследования</a>
     */
    Traffic(BuildingQGis bim) {
        this.zones = bim.getZones();
        this.exits = bim.getExits();
        this.safetyZone = bim.getOutdoorElement();
        this.numOfPeople = bim.getNumOfPeople();
        this.averageWidth = bim.getArea() / zones.size();

        this.D_MAX = 5.0;//MODEL_D_MAX; // чел/м2
        this.D_MIN = 1.9;//0.2 * 0.95;//MODEL_D_MIN; // чел/м2
        this.V_MAX = 100.0; // м/мин
        this.tay = getTay();

    }

    /**
     * @param l     ширина проема, м
     * @param dElem плотность в элементе
     * @return Скорость потока в проеме в зависимости от плотности, м/мин
     */
    private static double vElem(final double l, final double dElem) {
        double v0 = 100; // м/мин
        double d0 = 0.65;
        double a = 0.295;
        double v0k;
        if (dElem >= 9) v0k = 10 * (2.5 + 3.75 * l) / d0; // 07.12.2016
        else if (dElem > d0) {
            double m = dElem >= 5 ? 1.25 - 0.05 * dElem : 1;
            v0k = v0 * (1.0 - a * log(dElem / d0)) * m;
        } else v0k = v0;
        return v0k;
    }

    /**
     * @param dElem плотность в элементе
     * @return Скорость потока по горизонтальному пути, м/мин
     */
    private static double vElem(final double dElem) {
        double v0 = 100; // м/мин
        double d0 = 0.51;
        double a = 0.295;
        return dElem > d0 ? v0 * (1.0 - a * log(dElem / d0)) : v0;
    }

    double footTraffic() {
        ArrayList<Queue<ZoneElement>> queueList = new ArrayList<>();
        for (DoorElement exit : exits.values()) {
            // Определяем зону, которая находится в здании и зону, которая граничит с эвакуационным выходом
            ZoneElement zone = exit.getRoommates().get(0);
            double dPeopleZone = zone.getNumPeople(); // Количество человек в зоне, чел
            double sZone = zone.getArea(); // Площадь зоны, м2
            double dZone = zone.getDensity(); // Определяем плотность людей в зоне, рядом с выходом чел/м2
            double vZone = vElem(zone, safetyZone); // Скорость движения в зоне
            double wTransition = exit.getWidth(); // Ширина проема на улицу
            double vTransition = vElem(wTransition, dZone); // Скорость движения в дверях на выходе из здания
            double vAtExit = zone.getSlownessFactor() * min(vZone, vTransition); // Скорость на выходе из здания
            // Изменение численности людей в помещении рядом с выходом
            double dPeople = changeNumOfPeople1(sZone, dZone, wTransition, vAtExit); // чел
            double ddPeople = (dPeopleZone - dPeople > 0) ? dPeople : dPeopleZone;
            if (ddPeople < 0) {
                log.error("");
            }

            safetyZone.addPeople(ddPeople);// Увеличение количества людей в безопасной зоне
            zone.removePeople(ddPeople); // Уменьшение количества людей в обрабатываемой зоне
            exit.addTransitPeople(ddPeople); // Увеличение счетчика людей, прошедших через дверь ii
            exit.setDirection(safetyZone.getId()); // Зона, в которую пошли люди через дверь
            zone.setQueueId(exit.getId()); // Помещение освобождается через выход i
            /*zone.leaveVia(exit.getId(), ddPeople);*/
            zone.setTay(exit.addTay());

            // Потенциал времени в первом помещении. Время достижения эвакуационного выхода из зоны
            double potential = (vAtExit > 0) ? sqrt(sZone) / vAtExit : 0.0;

            // Убираем очередь из списка, если она там уже есть, потому что ее потенциал выше
            /*if (!zone.getPotentials().isEmpty()) {
                queueList.parallelStream().filter(e -> zone.equals(e.getElements().get(0))).findFirst()
                        .ifPresent(queueList::remove);
                zone.getPotentials().clear();
            }*/

            zone.getPotentials().put(exit.getId(), potential);
            queueList.add(new Queue<>(exit.getId(), potential, zone)); // Формирование списка очередей
        }
        queueList.sort(simulation.Queue::compareTo); // Сортировка списка очередей

        ArrayList<ZoneElement> rooms = new ArrayList<>(zones.values());
        ArrayList<ZoneElement> hostRooms = new ArrayList<>(zones.size());

        // Обходим очереди до тех пор, пока в есть не обработанные помещения
        while (!rooms.isEmpty()) {
            // Итератор очередей
            for (Iterator<Queue<ZoneElement>> queueIterator = queueList.iterator(); queueIterator.hasNext(); ) {
                Queue<ZoneElement> currentQueue = queueIterator.next(); // Очередь из списка - Рабочая очередь
                String numQueue = currentQueue.getId(); // Номер очереди
                List<ZoneElement> curQueueElem = currentQueue.getElements(); // Список элементов очереди
                List<ZoneElement> curQueueDelElem = currentQueue.getDeletedElements(); // Список элементов очереди

                for (ListIterator<ZoneElement> elementIter = curQueueElem.listIterator(); elementIter.hasNext(); ) {
                    ZoneElement hostElement = elementIter.next(); // Принимающая зона
                    ArrayList<ZoneElement> tmpPartQueue = new ArrayList<>();

                    if (hostElement.getPermeability() >= CR_PERMEABILITY) {
                        for (DoorElement door : hostElement.getRoommates()) {
                            // Переходим к другой двери, если эта является выходом из здания
                            if (door.isDoorWayOut()) continue;
                            // Tay - число, означающее глубину по дереву элементов
                            // Если Tay помещения, в которое планируется переместить людей,
                            // меньше или равно Tay двери, через которую планируется переместить людей
                            // в выбранное помещение, значит эта дверь обрабатывается другой очередью
                            // и на этом шаге обработке не подлежит
                            if (hostElement.getTay() <= door.getTay()) continue;

                            // Определяем соседа обрабатываемой двери, который является отдающей зоной
                            ArrayList<ZoneElement> doorRoommates = door.getRoommates();
                            ZoneElement givingElement = doorRoommates.get(0); // Потенциально отдающая зона
                            if (givingElement.equals(hostElement)) givingElement = doorRoommates.get(1);

                            // Если отдающая зона содержится в списке элементов на обработку или списке удаленных
                            // элементов, то выполняется переход к следующей зоне
                            if (curQueueElem.contains(givingElement) || curQueueDelElem.contains(givingElement))
                                continue;

                            double sZone = givingElement.getArea();
                            double vZone = vElem(givingElement, hostElement);
                            double dZone = givingElement.getDensity();
                            double wTransition = door.getWidth();
                            double vTransition = vElem(wTransition, dZone);
                            double vAtExit = givingElement.getSlownessFactor() * min(vZone, vTransition);
                            // Доля вышедших  людей
                            double dPeople = changeNumOfPeople1(sZone, dZone, wTransition, vAtExit); // чел
                            double numOfPeopleInZone = givingElement.getNumPeople();
                            double delta = numOfPeopleInZone - dPeople;
                            // Кол. людей, которые могут покинуть помещение
                            double ddPeople = (delta > 0) ? dPeople : numOfPeopleInZone;
                            double capacityZone = D_MAX * hostElement.getArea() - hostElement.getNumPeople();
                            double changePeople = (capacityZone > ddPeople) ? ddPeople : capacityZone;
                            // Подсчет потенциала
                            double potentialZone = (vAtExit > 0) ?
                                    hostElement.getPotentials().get(numQueue) + sqrt(sZone) / vAtExit :
                                    hostElement.getPotentials().get(numQueue);

                            givingElement.removePeople(changePeople); // Уменьшение людей
                            hostElement.addPeople(changePeople); // Увелич. людей в принимающей
                            door.addTransitPeople(changePeople); // Увелич. людей через дверь
                            door.setDirection(hostElement.getId()); // Зона, в которую пошли люди через дверь
                            givingElement.setTay(door.addTay());
                            /*givingElement.leaveVia(door.getId(), changePeople);*/

                            // Запоминаем потенциал данного помещения для текущей очереди
                            setElementPotential(numQueue, givingElement, potentialZone);

                            // Кладем помещение во временный список элементов на обработку,
                            // если помещение имеет более одной связи,
                            // иначе удаляем помещение из списка обрабатываемых на текущем шаге
                            // Потому что имея одну дверь, нельзя быть принимающей зоной
                            if (givingElement.getRoommates().size() == 1) rooms.remove(givingElement);
                            else tmpPartQueue.add(givingElement);
                        }
                    }

                    elementIter.remove(); // Удаляем текущий элемент из очереди на обработку
                    rooms.remove(hostElement); // Удаляем текущий элемент из списка всех помещений
                    // Добавляем элемент в список удаленных, чтобы не обрабатать его повторно
                    curQueueDelElem.add(hostElement);
                    hostRooms.add(hostElement); // Добавляем элемент в общий список обработанных

                    // Если список обработанных соседей текущего элемента не пуст
                    if (!tmpPartQueue.isEmpty()) {
                        // Обходим список с целью добавить элементы в список на обработку очереди
                        for (ZoneElement e : tmpPartQueue) {
                            // Не добавляем, если элемент уже находится в одном из списков
                            if (curQueueElem.contains(e) || hostRooms.contains(e)) continue;
                            curQueueElem.add(e);
                        }
                        // Если удалось добавить элементы или в текущем списке на обработку очереди еще есть элементы
                        if (!curQueueElem.isEmpty()) {
                            // Сортируем помещения очереди по возрастанию потенциала
                            curQueueElem.sort(new PotentialComparator(numQueue, PotentialComparator.SORTING_UP));
                            // Меняем потенциал очереди
                            currentQueue.setPotential(curQueueElem.get(0).getPotentials().get(numQueue));
                        }
                        // Фактически переписываем индекс, на который указывает итератор
                        // Потому что после сортировки он сбивается
                        elementIter = curQueueElem.listIterator();
                    }

                    // Если после сортировки выясняется, что впереди больше нет элементов
                    // Очередь следует удалить
                    if (!elementIter.hasNext()) queueIterator.remove();
                    else {
                        // Иначе сравниваем потенциал очереди со всеми остальными.
                        // И если он окажется меньше, то переходим к обработке следующего элемента
                        boolean t = true;
                        for (Queue<ZoneElement> otherQueue : queueList) {
                            if (currentQueue.equals(otherQueue)) continue;
                            t &= currentQueue.getPotential() < otherQueue.getPotential();
                        }
                        // В противном случае прекращаем обработку очереди
                        if (!t) break;
                    }
                } // end WHILE elementIter
                // После обработки каждой очереди, сортируем очереди по возрастанию потенциала
                queueList.sort(Queue::compareTo);
                // Устанвливам итератор в новое значение
                queueIterator = queueList.iterator();
            } // end WHILE queueIterator
            double lastNumOfPeople = rooms.parallelStream().mapToDouble(ZoneElement::getNumPeople).sum();
            // Если в здании осталось меньше 0.001 человека, то прекращаем работу
            double limit = 1E-3;
            if (lastNumOfPeople <= limit) break;
            if (queueList.isEmpty()) break;
        }

        return isEnded() ? calcDivTime(-1) : calcDivTime(1);
    }

    private void setElementPotential(String numQueue, ZoneElement givingElement, double potentialZone) {
        HashMap<String, Double> elementPotentials = givingElement.getPotentials();
        if (!elementPotentials.containsKey(numQueue)) elementPotentials.put(numQueue, potentialZone);
        else {
            if (elementPotentials.get(numQueue) < potentialZone) {
                elementPotentials.replace(numQueue, potentialZone);
            }
        }
    }

    /**
     * @param s площадь зоны
     * @param d плотность в зоне
     * @param l ширина проема, через которую происходит высасывание
     * @param v время достижения эвакуационного выхода
     * @return Дельта изменения численности людей (Количество людей, которое осталось в помещении)
     */
    private double changeNumOfPeople0(double s, double d, double l, double v) {
        double dN = 0;
        if (d <= D_MIN) {
            double A = sqrt(s) - v * tay;
            if (A > 0) dN = d * sqrt(s) * A;
            else dN = d * s;
        } else if (d > D_MIN) {
            double B = s - v * tay * l;
            if (B > 0) dN = d * (s - v * tay * l);
            else dN = d * s;
        }
        return dN;
    }

    /**
     * @param s площадь зоны
     * @param d плотность в зоне
     * @param l ширина проема, через которую происходит высасывание
     * @param v время достижения эвакуационного выхода
     * @return Дельта изменения численности людей
     */
    private double changeNumOfPeople1(double s, double d, double l, double v) {
        double dN = 0;
        int compareRes = Double.compare(d, D_MIN);
        if (compareRes == 1) {
            dN = d * v * tay * l;
        } else if (compareRes <= 0) {
            dN = d * v * tay * sqrt(s);
        }
        return dN;
    }

    /**
     * @param sZone       площадь зоны
     * @param dZone       плотность потока в зоне
     * @param wTransition ширина выхода с зоны
     * @param vAtExit     скорость движения через выход
     * @return коэффициент изменения плотности в помещении
     */
    private double rateOfChangeDensity(double sZone, double dZone, double wTransition, double vAtExit) {
        double dd_ = (dZone <= D_MIN) ? (2 * vAtExit * tay / pow(sZone, 0.5)) : (wTransition * vAtExit * tay / sZone);
        return dd_ > 1 ? 1 : dd_;
    }

    private double calcDivTime(int numberCycle) {
        return numberCycle * tay * 60;
    }

    /**
     * @return true, если в здании нет людей
     */
    private boolean isEnded() {
        if (numOfPeople - safetyZone.getNumPeople() <= 1) {
            zones.forEach((k, v) -> v.setNumPeople(0.0)); // Чистка помещений
            safetyZone.setNumPeople(numOfPeople);
            return true;
        }
        return false;
    }

    /**
     * @return шаг моделирования процесса эвакуации,мин
     */
    private double getTay() {
        double hxy = sqrt(averageWidth); // характерный размер области, м
        return (hxy / V_MAX) * 0.1/*MODEL_K_TAY*/; // Шаг моделирования, мин
    }

    /**
     * @param direct направление движения (direct = 3 - вверх ({@link
     *               Direction#UP}), = -3 - вниз ({@link Direction#DOWN})
     * @param dElem  плотность в элементе
     * @return Скорость потока при движении по лестнице в зависимости от
     * плотности, м/мин
     */
    private double vElemZ(final Direction direct, final double dElem) {
        double d0 = 0, v0 = 0, a = 0;

        switch (direct) {
            case UP:
                d0 = 0.67;
                v0 = 50;
                a = 0.305;
                break;
            case DOWN:
                d0 = 0.89;
                v0 = 80;
                a = 0.4;
                break;
            default:
                log.error("Fail! Direction unknown - '{}'", direct);
                break;
        }
        return dElem > d0 ? v0 * (1.0 - a * log(dElem / d0)) : v0;
    }

    /**
     * Метод определения скорости движения людского потока по разным зонам
     *
     * @param outZone зона, из которой высасываются люди
     * @param toZone  зона, в которую засасываются люди
     * @return Скорость людского потока в зоне
     */
    private double vElem(ZoneElement outZone, Element toZone) {
        double vZone = Double.NaN;

        // Определяем как выходим из зоны в здании - по лестнице или по прямой
        if (outZone.isRoom()) {
            vZone = vElem(outZone.getDensity());
        } else if (outZone.isStaircase()) {// Оценка точности представления координат, метры
            double dxyz = 0.1 * tay * V_MAX;
            // У безопасной зоны нет геометрических параметров, но есть уровень, на котором она находится относительно
            // каждого из эвакуационных выходов
            // Проверяем, является ли toZone экземпляром Безопасной зоны
            // если является, то используем другой метод определения высоты,
            // на которой расположена зона
            double dh = outZone.getLevel() - ((toZone instanceof OutdoorElement) ?
                    outZone.getLevel() :
                    toZone.getLevel()); // Разница высот зон
            // Если перепад высот незначительный, то скорость движения
            // принимается как при типе зоны FLOOR
            if (abs(dh) < dxyz) {
                vZone = vElem(outZone.getDensity());
            } else {
                // Иначе определяем направление движения по лестнице
                Direction direction = (dh > 0) ? DOWN : UP;
                vZone = vElemZ(direction, outZone.getDensity());
            }
        } else {
            log.error("Unknown zone type zone");
        }

        return vZone;
    }

    /**
     * Сортировка элементов очереди по потенциалу
     */
    private class PotentialComparator implements Comparator<ZoneElement> {

        static final int SORTING_UP = 1;
        static final int SORTING_DOWN = -1;
        /**
         * Номер очереди
         */
        private String numQueue;
        /**
         * Направление сортировки
         */
        private int directionSort;

        private PotentialComparator(String numQueue, int directionSort) {
            this.numQueue = numQueue;
            this.directionSort = directionSort;
        }

        /**
         * Сортировка по потенциалу
         * Для смены направления сортировки умножить на -1
         *
         * @param e1 уже существующий элмент в списке
         * @param e2 приходящий элемент
         * @return int
         */
        @Override
        public int compare(ZoneElement e1, ZoneElement e2) {
            return this.directionSort * Double
                    .compare(e1.getPotentials().get(this.numQueue), e2.getPotentials().get(this.numQueue));
        }
    }

    protected enum Direction {
        UP,
        DOWN
    }
}
