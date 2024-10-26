package lab1.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Common {
    public static LinkedList<Transaction> readFromStdin() {
        LinkedList<Transaction> res = new LinkedList<Transaction>();

        try {
            if (System.in.available() == 0) {
                throw new IllegalArgumentException("No input");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to read from stdin");
        }

        Scanner scanner = new Scanner(System.in);
        HashMap<Integer, Transaction> txMap = new HashMap<Integer, Transaction>();
        while(scanner.hasNext()) {
            String line = scanner.nextLine();
            if(line.length() == 0) {
                continue;
            }

            if (!meetsFormat(line)) {
                scanner.close();
                throw new IllegalArgumentException("Invalid input format");
            }
            
            String[] parts = line.split(" ");

            int txId = Integer.parseInt(parts[0]);
            if(!txMap.containsKey(txId)) {
                txMap.put(txId, new Transaction());
            }

            Transaction tx = txMap.get(txId);
            if(parts[1].equals("R")) {
                int rowId = Integer.parseInt(parts[2]);
                tx.addOperation(Operation.readOpWithTxID(rowId, txId));
                continue;
            }

            int rowId = Integer.parseInt(parts[2]);
            int value = Integer.parseInt(parts[3]);
            tx.addOperation(Operation.writeOpWithTxID(rowId, value, txId));
        }
        scanner.close();

        for(Transaction tx : txMap.values()) {
            res.add(tx);
        }

        return res;
    }

    public static boolean meetsFormat(String line) {
        // Format:
        // <TxId> W <RowId> <Value>
        // <TxId> R <RowId>
        Pattern pattern = Pattern.compile("^\\d+ (W \\d+ \\d+|R \\d+)$");
        return pattern.matcher(line).matches();
    }
}
