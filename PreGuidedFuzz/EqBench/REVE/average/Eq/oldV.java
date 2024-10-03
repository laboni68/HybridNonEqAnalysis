package demo.EqBench.REVE.average.Eq;
public class oldV {
	static double average(int n, int a[]) {
		if (n <= 0) {
			return 0;
		}
		int sum = 0;
		for (int i = 0; (i < n); i++) {
			sum += a[i];
		}
		return (double)sum / n;
	}
}