package preparation;

import ru.eesystem.parser4builder.json.structure.BuildElement;

/**
 * Класс, обозначающий безопасную зону
 */
public class OutdoorElement implements Element {

    private ZoneElement zoneElement;

    OutdoorElement(BuildElement self) {
        zoneElement = new ZoneElement(self);
    }

    public double getNumPeople() {
        return zoneElement.getNumPeople();
    }

    public void addPeople(double pDeltaPeople) {
        zoneElement.setNumPeople(zoneElement.getNumPeople() + pDeltaPeople);
    }

    @Override
    public double getLevel() {
        return zoneElement.getLevel();
    }

    public void setNumPeople(double numOfPeople) {
        zoneElement.setNumPeople(numOfPeople);
    }

    public String getId() {
        return zoneElement.getId();
    }
}
