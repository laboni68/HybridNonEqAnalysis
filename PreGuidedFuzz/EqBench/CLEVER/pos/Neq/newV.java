package demo.EqBench.CLEVER.pos.Neq;
public class newV { 
    static int lib(int x) {
      int counter = 1;
      while (x < 0) {
        x++;
        counter++;
      }
      return counter;
    }
}