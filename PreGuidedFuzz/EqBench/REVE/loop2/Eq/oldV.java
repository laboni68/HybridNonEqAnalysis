package demo.EqBench.REVE.loop2.Eq;
public class oldV {
	static int f(int n) {
		int i = 1;
		int j = 0;
		while (i <= n) {
			j = j + 2;
			i++;
		}
		return j;
	}
}