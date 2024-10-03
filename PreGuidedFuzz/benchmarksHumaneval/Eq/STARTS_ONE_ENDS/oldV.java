package demo.benchmarksHumaneval.NEq.STARTS_ONE_ENDS;

/* Given a positive integer n, return the count of the numbers of n-digit
positive integers that start or end with 1. */

public class oldV {
    public static int starts_one_ends(int n) {
        if (n == 1)
            return 1;
        return (int) ((10 + 9) * Math.pow(10, n - 2));
    }
}
