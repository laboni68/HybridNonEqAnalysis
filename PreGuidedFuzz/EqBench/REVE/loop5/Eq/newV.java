package demo.EqBench.REVE.loop5.Eq;
public class newV {
	static int f(int n) {
		int i = n + n;
		int j = 0;
		while (i > 0) {
		  j++;
		  i = i - 1;
		}
		return j;
	}
}