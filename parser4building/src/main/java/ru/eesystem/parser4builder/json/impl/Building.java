package ru.eesystem.parser4builder.json.impl;

import ru.eesystem.parser4builder.json.structure.BuildElement;

import java.util.HashMap;

public abstract class Building {
    private String pathToFile;

    public Building(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    protected String getPathToFile() {
        return pathToFile;
    }

    abstract public double getArea();

    abstract public double getNumOfPeople();

    abstract public Size getSizeBuilding();

    abstract public HashMap<String, ? extends BuildElement> getZones();

    abstract public HashMap<String, ? extends BuildElement> getInternalDoors();

    abstract public HashMap<String, ? extends BuildElement> getExits();

    abstract public BuildElement getOutdoorElement();
}
