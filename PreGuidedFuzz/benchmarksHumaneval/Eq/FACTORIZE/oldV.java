package demo.benchmarksHumaneval.NEq.FACTORIZE;

import java.util.ArrayList;
import java.util.List;

public class oldV {
    public static List<Integer> factorize(int n){
        List<Integer> result = new ArrayList<Integer>();

        int i = 2;
        while (i <= (int)(Math.sqrt(n) + 1)){
            if (n % i == 0){
                result.add(i);
                n = n / i;
            } else{
                i += 1;
            }
        }

        return result;
    }
}
