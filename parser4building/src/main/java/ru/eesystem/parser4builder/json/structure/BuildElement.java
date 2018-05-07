/*
 * Copyright (C) 2017 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * BuildElement is part of jSimulationMoving.
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

package ru.eesystem.parser4builder.json.structure;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by boris on 03.04.17.
 */
public class BuildElement {
    /**
     * Массив идентификаторов соседних элементов
     */
    private ArrayList<String> Output;
    /**
     * Высота элемента
     */
    private double SizeZ;
    /**
     * Геометрия элемента
     */
    private double[][][] XY;
    /**
     * Идентификатор элемента
     */
    private String Id;
    /**
     * Тип элемента
     */
    private Sign Sign;
    /**
     * Количество людей в здании
     */
    private double NumPeople;
    /**
     * Наименование элемента.
     * Для всех типов проемов формируется автоматически
     * Для помещений задается пользователем
     */
    private String Name;
    /**
     * Тип помещения по пожарной нагрузке
     */
    private int Type;

    public static BuildElement getInstance(BuildElement self) {
        return new BuildElement(self);
    }

    public BuildElement(BuildElement self) {
        this(self.getOutput(), self.getSizeZ(), self.getXY(), self.getId(), self.getSign(), self.getNumPeople(), self.getName(), self.getType());
    }

    public BuildElement() {
    }

    private BuildElement(ArrayList<String> output, double sizeZ, double[][][] XY, String id, Sign sign, double numPeople, String name, int type) {
        setOutput(output);
        setSizeZ(sizeZ);
        this.setXY(XY);
        setId(id);
        setSign(sign);
        setNumPeople(numPeople);
        setName(name);
        setType(type);
    }

    @Override
    public String toString() {
        return "BuildElement{ " +
                "Sign=" + getSign() +
                ", Id='" + getId() + '\'' +
                ", Output=" + getOutput().toString() +
                ((getSign() == ru.eesystem.parser4builder.json.structure.Sign.Room) ? (", NumPeople=" + getNumPeople()) : "") +
                ", Name='" + getName() + '\'' +
                ((getSign() == ru.eesystem.parser4builder.json.structure.Sign.Room) ? (", Type=" + getType()) : "") +
                ", SizeZ=" + getSizeZ() +
                " }";
    }

    public ArrayList<String> getOutput() {
        return Output;
    }

    public void setOutput(ArrayList<String> output) {
        Output = output;
    }

    public double getSizeZ() {
        return SizeZ;
    }

    public void setSizeZ(double sizeZ) {
        SizeZ = sizeZ;
    }

    public double[][][] getXY() {
        return XY;
    }

    public void setXY(double[][][] XY) {
        this.XY = XY;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public ru.eesystem.parser4builder.json.structure.Sign getSign() {
        return Sign;
    }

    public void setSign(ru.eesystem.parser4builder.json.structure.Sign sign) {
        Sign = sign;
    }

    public double getNumPeople() {
        return NumPeople;
    }

    public void setNumPeople(double numPeople) {
        NumPeople = numPeople;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

/*    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuildElement)) return false;

        BuildElement that = (BuildElement) o;

        if (Double.compare(that.SizeZ, SizeZ) != 0) return false;
        if (Double.compare(that.NumPeople, NumPeople) != 0) return false;
        if (Type != that.Type) return false;
        if (!Output.equals(that.Output)) return false;
        if (!Arrays.deepEquals(XY, that.XY)) return false;
        if (!Id.equals(that.Id)) return false;
        if (Sign != that.Sign) return false;
        return Name.equals(that.Name);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = Output.hashCode();
        temp = Double.doubleToLongBits(SizeZ);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + Arrays.deepHashCode(XY);
        result = 31 * result + Id.hashCode();
        result = 31 * result + Sign.hashCode();
        temp = Double.doubleToLongBits(NumPeople);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + Name.hashCode();
        result = 31 * result + Type;
        return result;
    }*/
}
