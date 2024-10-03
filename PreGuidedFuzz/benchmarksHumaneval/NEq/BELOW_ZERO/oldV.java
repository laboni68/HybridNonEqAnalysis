package demo.benchmarksHumaneval.NEq.BELOW_ZERO;

import java.util.List;

public class oldV {
    public static boolean below_zero(List<Integer> operations) {
        int balance = 0;
        for (Integer op : operations){
            balance += op;
            if (balance > 0){
                return false;
            }
        }
        return true;
    }
}
