package lab1.utils;

import java.util.ArrayList;
import java.util.HashSet;

public class OpHist extends ArrayList<Operation> {
    private void permutation(ArrayList<ArrayList<Integer>> res, ArrayList<Integer> cur, HashSet<Integer> txs) {
        if(cur.size() == txs.size()) {
            res.add(new ArrayList<Integer>(cur));
            return;
        }

        for(int tx : txs) {
            if(cur.contains(tx)) {
                continue;
            }

            cur.add(tx);
            permutation(res, cur, txs);
            cur.remove(cur.size() - 1);
        }
    }

    public ArrayList<ArrayList<Integer>> getAllTxOrderings() {
        ArrayList<ArrayList<Integer>> res = new ArrayList<ArrayList<Integer>>();

        HashSet<Integer> txs = new HashSet<Integer>();
        for(Operation op : this) {
            txs.add(op.getFromTxIdx());
        }

        permutation(res, new ArrayList<Integer>(), txs);

        return res;
    }
        
}
