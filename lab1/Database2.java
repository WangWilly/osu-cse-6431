package lab1;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lab1.utils.Operation;
import lab1.utils.Row;
import lab1.utils.Transaction;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

////////////////////////////////////////////////////////////////////////////////

public class Database2 {
    ////////////////////////////////////////////////////////////////////////////
    // Operation History
    
    private ArrayList<Operation> opHist = new ArrayList<Operation>();
    private ReadWriteLock historyLock = new ReentrantReadWriteLock();

    ////////////////////////////////////////////////////////////////////////////
    // Data

    private Row rows[] = new Row[100];

    @SuppressWarnings("unchecked")
    private LinkedBlockingQueue<Operation> stageQueues[] = new LinkedBlockingQueue[10];

    ////////////////////////////////////////////////////////////////////////////
    // Stage Processing

    private Thread issueStageProc(int stageIdx) {
        Thread thread = new Thread(() -> {
            System.out.println("Stage " + stageIdx + " is running");
            while(true) {
                try {
                    Operation op = stageQueues[stageIdx].take();
                    if (op.isStop()) {
                        System.out.println("Stage " + stageIdx + " is done");
                        if (stageIdx == 9) {
                            return;
                        }
                        stageQueues[stageIdx + 1].put(op);
                        return;
                    }

                    int rowNumber = op.getRowNumber();
                    if (rowNumber < stageIdx * 10 || rowNumber >= (stageIdx + 1) * 10) {
                        // the row is not in this stage
                        stageQueues[stageIdx + 1].put(op);
                        continue;
                    }

                    if(op.getType() == Operation.OP_READ) {
                        op.setValue(rows[op.getRowNumber()].getValue());
                        // System.out.println("Transaction " + op.fromTxIdx + " reads row " + op.getRowNumber() + " = " + op.getValue() + "(Stage " + stageIdx + ")");
                    } else {
                        rows[rowNumber].setValue(op.getValue());
                        // System.out.println("Transaction " + op.fromTxIdx + " writes row " + op.getRowNumber() + " = " + op.getValue() + "(Stage " + stageIdx + ")");
                    }
                    Lock histLock = historyLock.writeLock();
                    histLock.lock();
                    System.out.println("(Stage " + stageIdx + ") " + op);
                    opHist.add(op);
                    histLock.unlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return thread;
    }

    private Thread[] startStageProc() {
        Thread threads[] = new Thread[10];

        for(int i = 0; i < 10; i++) {
            threads[i] = issueStageProc(i);
        }

        return threads;
    }

    public void stopStageProc(Thread[] threads) {
        try {
            stageQueues[0].put(Operation.stopOp());
        } catch (InterruptedException e) {
            e.printStackTrace();
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
    
    public Database2() {
        for(int i = 0; i < 100; i++) {
            rows[i] = new Row(i);
        }

        for(int i = 0; i < 10; i++) {
            stageQueues[i] = new LinkedBlockingQueue<Operation>();
        }

    }

    ////////////////////////////////////////////////////////////////////////////

    public void executeTransactions(List<Transaction> transactions) {
        for(int i = 0; i < transactions.size(); i++) {
            Transaction tx = transactions.get(i);
            for(Operation op : tx.getOperations()) {
                // op.fromTxIdx = i;
                try {
                    stageQueues[0].put(op);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Thread[] threads = startStageProc();
        stopStageProc(threads);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Get Operation History

    public void printOperationHistory() {
        Lock histLock = historyLock.readLock();
        histLock.lock();
        for(Operation op : opHist) {
            System.out.println(op);
        }
        histLock.unlock();
    }

    ////////////////////////////////////////////////////////////////////////////

    public static void main(String []args) {
        Transaction t1 = new Transaction();
        t1.addOperation(Operation.readOpWithTxID(31, 1));
        t1.addOperation(Operation.writeOpWithTxID(41, 5, 1));
        t1.addOperation(Operation.readOpWithTxID(51, 1));
        t1.addOperation(Operation.writeOpWithTxID(61, 7, 1));
        t1.addOperation(Operation.readOpWithTxID(71, 1));
        
        Transaction t2 = new Transaction();
        t2.addOperation(Operation.writeOpWithTxID(11, 99, 2));
        t2.addOperation(Operation.readOpWithTxID(12, 2));
        t2.addOperation(Operation.writeOpWithTxID(18, 9, 2));
        t2.addOperation(Operation.readOpWithTxID(13, 2));
        t2.addOperation(Operation.writeOpWithTxID(10, 11, 2));
        t2.addOperation(Operation.readOpWithTxID(42, 2));
        t2.addOperation(Operation.writeOpWithTxID(12, 13, 2));
        t2.addOperation(Operation.readOpWithTxID(50, 2));

        LinkedList<Transaction> batch = new LinkedList<Transaction>();
        batch.add(t1);
        batch.add(t2);
        
        Database2 db = new Database2();
        db.executeTransactions(batch);

        // db.printOperationHistory();
    }
}
