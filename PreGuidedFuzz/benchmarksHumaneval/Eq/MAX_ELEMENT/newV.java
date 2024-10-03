package demo.benchmarksHumaneval.NEq.MAX_ELEMENT;

import java.util.List;

public class newV {
    public static int max_element(List<Integer> l) {
        int m = l.get(0);
        for (Integer e : l) {
            if (e > m){
                m = e;
            }
        }
        return m;
    }
}
