package devices;

public class LightAnnunciator implements IDevice {

    private final Annunciator annunciator;

    public LightAnnunciator(int id, double status) {
        this.annunciator = new Annunciator(id, status, AnnunciatorType.LIGHT);
    }

    public int getId() {
        return annunciator.getId();
    }

    public double getStatus() {
        return annunciator.getStatus();
    }

    public AnnunciatorType getType() {
        return annunciator.getType();
    }
}
