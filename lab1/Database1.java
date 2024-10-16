package lab1;

import java.util.ArrayList;
import java.lang.Thread;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lab1.utils.Operation;
import lab1.utils.Row;
import lab1.utils.Transaction;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

////////////////////////////////////////////////////////////////////////////////

public class Database1 {
    private Row rows[] = new Row[100];
    private ReadWriteLock locks[] = new ReentrantReadWriteLock[100];

    private Thread issueTx(Transaction tx, int txIdx) {
        Thread thread = new Thread(() -> {
            // Lock all rows that this transaction is going to read or write
            System.out.println("Transaction " + txIdx + " is waiting for locks");
            ArrayList<Lock> acquiredLocks = new ArrayList<Lock>();
            for(Map.Entry<Integer,Integer> entry : tx.getRequestingRows().entrySet()) {
                int rowNumber = entry.getKey();
                int type = entry.getValue();

                if(type == Operation.OP_READ) {
                    acquiredLocks.add(locks[rowNumber].readLock());
                } else {
                    acquiredLocks.add(locks[rowNumber].writeLock());
                }

                acquiredLocks.get(acquiredLocks.size() - 1).lock();
            }

            // Execute the transaction
            for(Operation op : tx.getOperations()) {
                // System.out.println("Transaction " + txIdx + " executing operation " + op);
                if(op.getType() == Operation.OP_READ) {
                    op.setValue(rows[op.getRowNumber()].getValue());
                    System.out.println("Transaction " + txIdx + " reads row " + op.getRowNumber() + " = " + op.getValue());
                } else {
                    rows[op.getRowNumber()].setValue(op.getValue());
                    System.out.println("Transaction " + txIdx + " writes row " + op.getRowNumber() + " = " + op.getValue());
                }
            }

            // Unlock all rows
            System.out.println("Transaction " + txIdx + " is releasing locks");
            for(Lock lock : acquiredLocks) {
                lock.unlock();
            }
        });
        thread.start();

        return thread;
    }

    ////////////////////////////////////////////////////////////////////////////

    public Database1() {
        for(int i = 0; i < 100; i++) {
            rows[i] = new Row(i);
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public void executeTransactions(List<Transaction> transactions) {
        Thread threads[] = new Thread[transactions.size()];

        for(int i = 0; i < transactions.size(); i++) {
            threads[i] = issueTx(transactions.get(i), i);
        }

        for(Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public static void main(String []args) {
	    Transaction t1 = new Transaction();
        t1.addOperation(Operation.readOp(3));
        t1.addOperation(Operation.writeOp(4, 5));
        t1.addOperation(Operation.readOp(5));
        t1.addOperation(Operation.writeOp(6, 7));
        t1.addOperation(Operation.readOp(7));
        
        Transaction t2 = new Transaction();
        t2.addOperation(Operation.writeOp(1, 99));
        t2.addOperation(Operation.readOp(2));
        t2.addOperation(Operation.writeOp(8, 9));
        t2.addOperation(Operation.readOp(3));
        t2.addOperation(Operation.writeOp(10, 11));
        t2.addOperation(Operation.readOp(4));
        t2.addOperation(Operation.writeOp(12, 13));
        t2.addOperation(Operation.readOp(5));

        LinkedList<Transaction> batch = new LinkedList<Transaction>();
        batch.add(t1);
        batch.add(t2);
        
        Database1 db = new Database1();
        db.executeTransactions(batch);
    }
}
