package demo.benchmarksHumaneval.NEq.LARGEST_DIVISOR;

public class oldV {
    public static int largest_divisor(int n) {
        for (int i = n; i >= 0; i -= 1){
            if (n % i == 0)
                return i;
        }
        return 1;
    }
}
