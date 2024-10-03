package demo.EqBench.REVE.limit3.Eq;
public class oldV {
	static int f(int n) {
		int r;
		r = 0;
		if (n <= 1) {
		  r = n;
		} else {
		  r = f(n - 1);
		  r = n + r;
		  if (false) {
			r = n + r;
		  }
		}
		return r;
	} 
}