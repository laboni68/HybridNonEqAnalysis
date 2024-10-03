package demo.EqBench.REVE.limit2.Neq;
public class oldV {
	static int f(int n) {
		int r;
		r = 0;
		if (n <= 0) {
		  r = n;
		} 
		else {
		  r = f(n - 1);
		  r = n + r;
		  if (false) {
			r = 10;
		  }
		}
		return r;
	}  
}