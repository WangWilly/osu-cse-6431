package lab1.utils;

////////////////////////////////////////////////////////////////////////////////

public class Operation {
    private int type;      // 0 for READ and 1 for WRITE
    private int rowNumber; // which row to read or write
    private int value;     // for read, this is the return value; for write, this is the value to write

    public static int OP_STOP = -1;
    public static int OP_READ = 0;
    public static int OP_WRITE = 1;

    ////////////////////////////////////////////////////////////////////////////

    private Operation(int type, int rowNumber, int value) {
        this.type = type;
        this.rowNumber = rowNumber;
        this.value = value;
    }

    ////////////////////////////////////////////////////////////////////////////

    public static Operation readOp(int rowNumber) {
        return new Operation(OP_READ, rowNumber, 0);
    }

    public static Operation readOpWithTxID(int rowNumber, int txID) {
        Operation op = new Operation(OP_READ, rowNumber, 0);
        op.fromTxIdx = txID;
        return op;
    }

    public static Operation writeOp(int rowNumber, int value) {
        return new Operation(OP_WRITE, rowNumber, value);
    }

    public static Operation writeOpWithTxID(int rowNumber, int value, int txID) {
        Operation op = new Operation(OP_WRITE, rowNumber, value);
        op.fromTxIdx = txID;
        return op;
    }

    public static Operation stopOp() {
        return new Operation(OP_STOP, -1, -1);
    }

    ////////////////////////////////////////////////////////////////////////////

    private int fromTxIdx = -1;

    ////////////////////////////////////////////////////////////////////////////

    public int getType() {
        return this.type;
    }

    public int getRowNumber() {
        return this.rowNumber;
    }

    public int getValue() {
        return this.value;
    }

    public int getFromTxIdx() {
        return this.fromTxIdx;
    }

    ////////////////////////////////////////////////////////////////////////////

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isStop() {
        return this.type == OP_STOP;
    }

    public String toString() {
        if(type == 0)
            return "READ row " + rowNumber + " value " + value + " from transaction " + fromTxIdx;
        else
            return "WRITE row " + rowNumber + " value " + value + " from transaction " + fromTxIdx;
    }
}