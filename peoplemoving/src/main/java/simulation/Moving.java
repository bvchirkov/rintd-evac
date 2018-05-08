/*
 Copyright (C) 2016 Kolodkin Vladimir, Chirkov Boris

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
import preparation.BuildingQGis;
import preparation.ZoneElement;

import java.util.StringJoiner;

/**
 * Класс моделирования
 */
public class Moving {
    private static final Logger log = LoggerFactory.getLogger(Moving.class);

    private BuildingQGis bim;
    private double evacuationTime; // Текущее время моделирования эвакуации, c
    private StringJoiner nMessage;

    public Moving(BuildingQGis bim) {
        this.bim = bim;
        this.evacuationTime = 0.0;
    }

    public void run() {
        log.info("Запущено моделирование движения людских потоков");

        // Количество людей в здании, до начала эвакуации
        double numOfPeople0 = bim.getNumOfPeople();
        Traffic traffic = new Traffic(bim);
        bim.addSlice(evacuationTime);

        int nMaxRepeat = 10;
        int repeater = nMaxRepeat;
        int acceptRepeat = 10000; // Максимальное кол-во проходов по циклу (Для избежания зацикливания)
        double nTimeStep = 0;
        double nNumOfPeopleOut = 0;
        double nNumOfPeopleOutCurrent = 0;
        for (int i = 0; i < acceptRepeat; i++) {
            // Блокировка помещений ОФП по времени
            if (bim.isFire()) blockingZone(nTimeStep);

            nTimeStep = traffic.footTraffic();
            nNumOfPeopleOutCurrent = bim.getOutdoorElement().getNumPeople();

            if (nTimeStep < 0) {
                evacuationTime += (-1) * nTimeStep;
                break;
            } else {
                // Смотрим, не повторяется ли количество людей, которые уже вышли в безопасную зону.
                // Если начинает повторяться, начинаем уменьшать счетчик
                if (Double.compare(nNumOfPeopleOut, nNumOfPeopleOutCurrent) == 0) repeater--;
                else {
                    nNumOfPeopleOut = nNumOfPeopleOutCurrent;
                    repeater = nMaxRepeat;
                }
                // Остановка расчета, если количество вышедшых людей не меняется
                // Такое случается при блокировке путей эвакуации ОФП или ошибках
                if (repeater == 0) break;
            }
            evacuationTime += nTimeStep;

            bim.addSlice(evacuationTime);
            // Возвращаем стандартные параметры для некоторых полей элемента
            bim.getZones().values().parallelStream().forEach(e -> {
                e.getPotentials().clear();
                e.setTay(0);
            });
            bim.getExits().values().parallelStream().forEach(e -> e.setTay(0));
            bim.getInternalDoors().values().parallelStream().forEach(e -> e.setTay(0));

            log.debug("In progress: number of people in Safety zone: {}, simulation time: {}", nNumOfPeopleOutCurrent, evacuationTime);
        }

        bim.addSlice(evacuationTime);

        nMessage = new StringJoiner("\n");
        String nEvacTime = String.format("%6.3e", evacuationTime);
        if (repeater != 0) {
            nMessage.add("Эвакуация прошла успешно. Все люди поникули здание");
            log.debug("Successful finish simulation. Total: number of people in Safety zone: {} of {}, simulation "
                    + "time: {}", bim.getOutdoorElement().getNumPeople(), numOfPeople0, nEvacTime);
        } else {
            nMessage.add("При текущих условиях конфигурации и распространения опасных факторов пожара не все люди " +
                    "смогут эвакуаироваться.").add("В здании останутся люди.");
            log.debug("WARNING The program got into a loop. Total: number of people in Safety zone: {} of {}, "
                    + "simulation time: {}", nNumOfPeopleOutCurrent, numOfPeople0, nEvacTime);
            log.debug("Помещения, в которых остались люди:");

            nMessage.add("Помещения, в которых остались люди на момент остановки программы:");
            bim.getZones().values().stream().filter(e -> e.getNumPeople() > 1e-02)
                    .forEach(e -> {
                        nMessage.add("\t" + e.getName() + "\t" + e.getNumPeople() + " чел.");
                        log.debug("{}", e.getId());
                    });
        }
    }

    private void blockingZone(double balance) {
        for (ZoneElement zone : bim.getFireZones()) {
            double nBlockTime = zone.getBlockTime() - bim.getTimeOfBeginEvacuation();
            if (nBlockTime < 0) nBlockTime = 0;
            if (Double.compare(nBlockTime, evacuationTime - balance) >= 0
                    && Double.compare(nBlockTime, evacuationTime + balance) <= 0) {
                zone.setPermeability(0.0);
                log.info("Помещение {} (id: {}) было заблокировано на {} секунде после начала эвакуации",
                        zone.getName(), zone.getId(), nBlockTime);
            }
        }
    }

    public double getEvacuationTime() {
        return evacuationTime;
    }

    public StringJoiner getMessage() {
        return nMessage;
    }
}
