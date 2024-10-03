package demo.EqBench.CLEVER.oneN2.Eq;
public class oldV {
    static int client(int x){
      int x1;
      if (x > 10)
        x1= 11;
      else
        x1= x;
      if (x > x1)
        return x;
      else{
        if (x > 10)
          return 11;
        else
          return x;
      }
    }
}