package demo.benchmarksHumaneval.NEq.LARGEST_DIVISOR;

public class oldV {
    public static int largest_divisor(int n) {
        for (int i = n / 2; i >= 1; i--) {
            if (n % i == 0)
                return i; // Return the first divisor found
        }
        return 1;
    }
}
