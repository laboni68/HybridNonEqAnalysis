package demo.EqBench.CLEVER.odd.Neq;
public class newV {
   static int lib(int x) {
        int counter = 0;
        while (x % 2 == 0){
            x = x/2;
            counter++;
        }
        return counter+1;
    }
}