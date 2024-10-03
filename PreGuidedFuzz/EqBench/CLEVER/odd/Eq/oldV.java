package demo.EqBench.CLEVER.odd.Eq;
public class oldV {
   static int lib(int x) {
        int counter = 0;
        while (x % 2 == 0){
            x = x/2;
            counter++;
        }
        return counter;
    }
}