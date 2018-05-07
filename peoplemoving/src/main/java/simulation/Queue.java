/*
 * Copyright (C) 2017 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * Queue is part of jSimulationMoving.
 *
 * jSimulationMoving is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jSimulationMoving is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jSimulationMoving. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------------
 *
 * This code is in BETA; some features are incomplete and the code
 * could be written better.
 */

package simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Класса описывающий очередь
 * <p>
 * Created by boris on 02.06.17.
 */
public class Queue<T> implements Comparable<Queue> {
    /**
     * Идентификатор очереди
     */
    private String     id;
    /**
     * Потенциал очереди
     */
    private double  potential;
    /**
     * Список элементов очереди для обработки
     */
    private List<T> elements;
    /**
     * Список обработанных элементов очереди
     */
    private List<T> deletedElements;

    Queue(String id, double potential, T element) {
        setId(id);
        setPotential(potential);
        setElements(new ArrayList<>());
        addElements(element);
        setDeletedElements(new ArrayList<>());
    }

    String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    double getPotential() {
        return potential;
    }

    void setPotential(double potential) {
        this.potential = potential;
    }

    List<T> getElements() {
        return elements;
    }

    private void setElements(List<T> elements) {
        this.elements = elements;
    }

    private void addElements(T element) {
        this.elements.add(element);
    }

    List<T> getDeletedElements() {
        return deletedElements;
    }

    private void setDeletedElements(List<T> deletedElements) {
        this.deletedElements = deletedElements;
    }

    @Override
    public int compareTo(Queue o) {
        return (potential < o.potential) ? -1 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Queue)) return false;

        Queue<?> queue = (Queue<?>) o;

        return Objects.equals(id, queue.id);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
