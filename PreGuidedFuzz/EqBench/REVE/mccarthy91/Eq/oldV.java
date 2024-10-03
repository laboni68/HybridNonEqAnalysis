package demo.EqBench.REVE.mccarthy91.Eq;
public class oldV {
	static int f(int x) {
		int r = 0;
		if (x > 100) {
		  r = x - 10;
		} else {
		  r = f(x + 11);
		  r = f(r);
		}
		return r;
	}  
}