package demo.EqBench.REVE.inlining.Eq;
public class oldV {
	static int f(int x) {
		if (x > 0) {
		  x = f(x-1);
		  x = x + 1;
		}
		if (x < 0) {
		  x = 0;
		}
		return x;
	  }
}