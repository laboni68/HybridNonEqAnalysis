package demo.EqBench.REVE.bug15.Eq;
public class newV {
	static int f(int z) {
		int y = 0;
		int x = 1;
		while (x < 10) {
			y = 2 + x;
			x = y + y;
		}
		return x * 2;
	}
}