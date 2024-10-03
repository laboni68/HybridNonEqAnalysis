package demo.EqBench.CLEVER.pos.Eq;
public class oldV {
    static int client(int x){
      if (x > 0) {
        return -lib(-x);
      }else{
        return lib(x);
      }
    }
    private static int lib(int x) {
      int counter = 0;
      while (x < 0) {
        x++;
        counter++;
      }
      return counter;
    }
    
}