package demo.benchmarksHumaneval.NEq.MAX_ELEMENT;

import java.util.List;

public class oldV {
    public static int max_element(List<Integer> l) {
        int m = 0;
        for (Integer e : l) {
            if (e > m){
                m = e;
            }
        }
        return m;
    }
}
