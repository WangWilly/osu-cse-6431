package lab1;

import java.lang.Thread;
import java.util.LinkedList;
import java.util.List;

import lab1.utils.Operation;
import lab1.utils.Row;
import lab1.utils.Transaction;

import java.util.concurrent.LinkedBlockingQueue;

////////////////////////////////////////////////////////////////////////////////

public class Database2 {
    private Row rows[] = new Row[100];
    private LinkedBlockingQueue<Operation> stageQueues[] = new LinkedBlockingQueue[10];

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
                        System.out.println("Transaction " + op.fromTxIdx + " reads row " + op.getRowNumber() + " = " + op.getValue() + "(Stage " + stageIdx + ")");
                    } else {
                        rows[rowNumber].setValue(op.getValue());
                        System.out.println("Transaction " + op.fromTxIdx + " writes row " + op.getRowNumber() + " = " + op.getValue() + "(Stage " + stageIdx + ")");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return thread;
    }

    ////////////////////////////////////////////////////////////////////////////


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
                op.fromTxIdx = i;
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

    public static void main(String []args) {
	    Transaction t1 = new Transaction();
        t1.addOperation(Operation.readOp(31));
        t1.addOperation(Operation.writeOp(41, 5));
        t1.addOperation(Operation.readOp(51));
        t1.addOperation(Operation.writeOp(61, 7));
        t1.addOperation(Operation.readOp(71));
        
        Transaction t2 = new Transaction();
        t2.addOperation(Operation.writeOp(11, 99));
        t2.addOperation(Operation.readOp(12));
        t2.addOperation(Operation.writeOp(18, 9));
        t2.addOperation(Operation.readOp(13));
        t2.addOperation(Operation.writeOp(10, 11));
        t2.addOperation(Operation.readOp(42));
        t2.addOperation(Operation.writeOp(12, 13));
        t2.addOperation(Operation.readOp(50));

        LinkedList<Transaction> batch = new LinkedList<Transaction>();
        batch.add(t1);
        batch.add(t2);
        
        Database2 db = new Database2();
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
