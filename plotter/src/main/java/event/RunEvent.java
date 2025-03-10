package event;

public class RunEvent {
    public enum Side {
        Right, Left, Unknown
    }

    public enum Type {
        Contact, Lift
    }

    private Type type;
    private Side side;
    private int frame;
    private double value;

    public RunEvent(Type type, Side side, int frame, double value) {
        this.type = type;
        this.side = side;
        this.frame = frame;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
