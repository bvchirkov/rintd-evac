package devices;

public class SoundAnnunciator implements IDevice {

    private final Annunciator annunciator;

    public SoundAnnunciator(int id, double status) {
        this.annunciator = new Annunciator(id, status, AnnunciatorType.SOUND);
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
