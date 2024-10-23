package lab1;

import java.util.LinkedList;
import java.util.List;
import lab1.utils.Operation;
import lab1.utils.Row;
import lab1.utils.Transaction;

////////////////////////////////////////////////////////////////////////////////

public class Database {
    private Row rows[] = new Row[100];

    public Database() {
        for(int i = 0; i < 100; i++) {
            rows[i] = new Row(i);
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public void executeTransactions(List<Transaction> transactions) {
        // Here I provide a serial implementation. You need to change it to a concurrent execution.
        for(Transaction t : transactions) {
            for(Operation o : t.getOperations()) {
                System.out.println("Transaction " + t + " executing operation " + o);

                if(o.getType() == 0) {
                   o.setValue(rows[o.getRowNumber()].getValue());
                } else {
                   rows[o.getRowNumber()].setValue(o.getValue());
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public static void main(String []args) {
        Transaction t1 = new Transaction();
        t1.addOperation(Operation.readOp(3));
        t1.addOperation(Operation.writeOp(4, 5));
        
        Transaction t2 = new Transaction();
        t2.addOperation(Operation.writeOp(3, 99));
        t2.addOperation(Operation.readOp(4));

        LinkedList<Transaction> batch = new LinkedList<Transaction>();
        batch.add(t1);
        batch.add(t2);
        
        Database db = new Database();
        db.executeTransactions(batch);
    }
}

////////////////////////////////////////////////////////////////////////////////

/**
 * TODO: Expected output: The program should output to screen a log like:
 * 
 * ```
 * Transaction 1 reads row100 = 3; Transaction 2 writes row 99 = 6; …
 * ```
 * 
 * and
 * 
 * ```
 * This execution is equivalent to a serial execution of Transaction 2 -> Transaction 1-> …
 * ```
 * 
 */
