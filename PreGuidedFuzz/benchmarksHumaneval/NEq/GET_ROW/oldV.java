package demo.benchmarksHumaneval.NEq.GET_ROW;

import java.util.ArrayList;

public class GET_ROW {
    public class oldV{
        int x;
        int y;
        public oldV(int x, int y) {
            this.x = x;
            this.y = y;            
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof oldV) {
                return this.x == ((oldV) o).x && this.y == ((oldV) o).y;
            }
            return false;
        }
    } 

    public static ArrayList<oldV> get_row(ArrayList<ArrayList<Integer>> lst, int x) {
        ArrayList<oldV> result = new ArrayList<oldV>();

        for (int i = 0; i < lst.size(); i += 1) {
            for (int j = lst.get(0).size() - 1; j >= 0; j -= 1){
                if (lst.get(i).get(i) == x){
                    result.add(new GET_ROW().new oldV(i, i));
                }
            }
        }
        return result;
    }
}
