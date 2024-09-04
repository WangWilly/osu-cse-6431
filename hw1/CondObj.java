package hw1;

public class CondObj {
    private boolean condition;

    public CondObj() {
        this.condition = false;
    }

    public synchronized void setCondition(boolean condition) {
        this.condition = condition;
    }

    public synchronized boolean getCondition() {
        return condition;
    }
}
