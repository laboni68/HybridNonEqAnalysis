package demo.benchmarksHumaneval.NEq.GET_ROW;

import java.util.ArrayList;

public class GET_ROW {
    public class newV{
        int x;
        int y;
        public newV(int x, int y) {
            this.x = x;
            this.y = y;            
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof newV) {
                return this.x == ((newV) o).x && this.y == ((newV) o).y;
            }
            return false;
        }
    } 

    public static ArrayList<newV> get_row(ArrayList<ArrayList<Integer>> lst, int x) {
        ArrayList<newV> result = new ArrayList<newV>();

        for (int i = 0; i < lst.size(); i += 1) {
            for (int j = lst.get(i).size() - 1; j >= 0; j -= 1){
                if (lst.get(i).get(j) == x){
                    result.add(new GET_ROW().new newV(i, j));
                }
            }
        }
        return result;
    }
}
