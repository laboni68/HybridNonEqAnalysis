package demo.EqBench.REVE.nestedwhile.Neq;
public class newV {
	static int f(int x, int g) {
		int i = 0;
		while (i < x) {
		  i = i + 1;
		  g = g - 2;
		  while(x < i) {
			x = x + 1;
			g = g + 2;
		  }
		}
		return g;
	}
}