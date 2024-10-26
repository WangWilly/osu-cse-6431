package lab1;

import java.util.ArrayList;
import java.lang.Thread;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lab1.utils.Operation;
import lab1.utils.Row;
import lab1.utils.Transaction;
import lab1.utils.Graph;
import lab1.utils.LoggerHelper;
import lab1.utils.OpHist;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

////////////////////////////////////////////////////////////////////////////////

public class Database1 {
    ////////////////////////////////////////////////////////////////////////////
    // Operation History
    
    private OpHist opHist = new OpHist();
    private ReadWriteLock historyLock = new ReentrantReadWriteLock();

    ////////////////////////////////////////////////////////////////////////////
    // Data & Locks

    private Row rows[] = new Row[100];
    private ReadWriteLock locks[] = new ReentrantReadWriteLock[100];

    ////////////////////////////////////////////////////////////////////////////
    // Transaction Execution Thread

    private Thread issueTx(Transaction tx, int txIdx) {
        Thread thread = new Thread(() -> {
            // Lock all rows that this transaction is going to read or write
            LoggerHelper.log("Transaction " + txIdx + " is waiting for locks", LoggerHelper.LOG_LEVEL_DEBUG);
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
                    /** TODO: remove this block
                    Lock histLock = historyLock.writeLock();
                    histLock.lock();
                    LoggerHelper.log("Transaction " + txIdx + " reads row " + op.getRowNumber() + " = " + op.getValue(), LoggerHelper.LOG_LEVEL_DEBUG);
                    opHist.add(op);
                    histLock.unlock();
                    */
                } else {
                    rows[op.getRowNumber()].setValue(op.getValue());
                    /** TODO: remove this block
                    Lock histLock = historyLock.writeLock();
                    histLock.lock();
                    LoggerHelper.log("Transaction " + txIdx + " writes row " + op.getRowNumber() + " = " + op.getValue(), LoggerHelper.LOG_LEVEL_DEBUG);
                    opHist.add(op);
                    histLock.unlock();
                    */
                }
                Lock histLock = historyLock.writeLock();
                histLock.lock();
                LoggerHelper.log(op.toString(), LoggerHelper.LOG_LEVEL_DEBUG);
                opHist.add(op);
                histLock.unlock();
            }

            // Unlock all rows
            LoggerHelper.log("Transaction " + txIdx + " is releasing locks", LoggerHelper.LOG_LEVEL_DEBUG);
            for(Lock lock : acquiredLocks) {
                lock.unlock();
            }
        });
        thread.start();

        return thread;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructor

    public Database1() {
        for(int i = 0; i < 100; i++) {
            rows[i] = new Row(i);
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Execute Transactions

    public void executeTransactions(List<Transaction> transactions) {
        Thread threads[] = new Thread[transactions.size()];

        for(int i = 0; i < transactions.size(); i++) {
            threads[i] = issueTx(transactions.get(i), i + 1);
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
    // Get Operation History

    public String getOperationHistoryString() {
        String res = "";

        Lock histLock = historyLock.readLock();
        histLock.lock();
        for(Operation op : opHist) {
            res += op.toString() + "\n";
        }
        histLock.unlock();

        return res;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Main

    public static void main(String []args) {
        Transaction t1 = new Transaction();
        t1.addOperation(Operation.readOpWithTxID(3, 1));
        t1.addOperation(Operation.writeOpWithTxID(4, 5, 1));
        t1.addOperation(Operation.readOpWithTxID(5, 1));
        t1.addOperation(Operation.writeOpWithTxID(6, 7, 1));
        t1.addOperation(Operation.readOpWithTxID(7, 1));
        
        Transaction t2 = new Transaction();
        t2.addOperation(Operation.writeOpWithTxID(1, 99, 2));
        t2.addOperation(Operation.readOpWithTxID(2, 2));
        t2.addOperation(Operation.writeOpWithTxID(8, 9, 2));
        t2.addOperation(Operation.readOpWithTxID(3, 2));
        t2.addOperation(Operation.writeOpWithTxID(10, 11, 2));
        t2.addOperation(Operation.readOpWithTxID(4, 2));
        t2.addOperation(Operation.writeOpWithTxID(12, 13, 2));
        t2.addOperation(Operation.readOpWithTxID(5, 2));

        LinkedList<Transaction> batch = new LinkedList<Transaction>();
        batch.add(t1);
        batch.add(t2);
        
        Database1 db = new Database1();
        db.executeTransactions(batch);

        LoggerHelper.log(db.getOperationHistoryString());

        Graph graph = Graph.fromOpHist(db.opHist);
        LoggerHelper.log(graph.toString(), LoggerHelper.LOG_LEVEL_DEBUG);
        ArrayList<Integer> res = Graph.topologicalSort(graph);

        LoggerHelper.log("All Transactions Orderings: ");
        LoggerHelper.log(db.opHist.getAllTxOrderings().toString());
        LoggerHelper.log("Current Ordering: ");
        LoggerHelper.log(res.toString());
    }
}
