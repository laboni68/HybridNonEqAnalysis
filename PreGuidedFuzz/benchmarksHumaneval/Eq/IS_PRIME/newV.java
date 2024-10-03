package demo.benchmarksHumaneval.NEq.IS_PRIME;

public class newV {
    public static boolean is_prime(int n){
        if (n < 2)
            return false;
        for (int k = 2; k < n - 1; k += 1){
            if (n % k == 0)
                return false;
        }
        return true;
    }
}
