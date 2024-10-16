package lab1.utils;
import java.util.List;
import java.util.LinkedList;
import java.util.TreeMap;

////////////////////////////////////////////////////////////////////////////////

public class Transaction {
    private LinkedList<Operation> operations;
    private TreeMap<Integer, Integer> requestingRows;

    ////////////////////////////////////////////////////////////////////////////

    public Transaction() {
        operations = new LinkedList<Operation>();
        requestingRows = new TreeMap<Integer, Integer>();
    }
    
    ////////////////////////////////////////////////////////////////////////////

    public void addOperation(Operation o) {
        operations.add(o);

        // Keep the information of the rows that this transaction is going to read or write
        Integer originalValue = requestingRows.get(o.getRowNumber());
        int oriVal = originalValue == null ? 0 : originalValue.intValue();
        requestingRows.put(
            Integer.valueOf(o.getRowNumber()),
            Integer.valueOf(Math.max(oriVal, o.getType()))
        );
    }

    public List<Operation> getOperations() {
        return this.operations;
    }

    ////////////////////////////////////////////////////////////////////////////

    public TreeMap<Integer, Integer> getRequestingRows() {
        return this.requestingRows;
    }
}
