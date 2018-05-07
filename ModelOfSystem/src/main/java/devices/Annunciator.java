package devices;

public class Annunciator {
    private int id;
    private double status;
    private AnnunciatorType type;

    public Annunciator(int id, double status, AnnunciatorType type) {
        this.id = id;
        this.status = status;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getStatus() {
        return status;
    }

    public void setStatus(double status) {
        this.status = status;
    }

    public AnnunciatorType getType() {
        return type;
    }

    public void setType(AnnunciatorType type) {
        this.type = type;
    }
}
