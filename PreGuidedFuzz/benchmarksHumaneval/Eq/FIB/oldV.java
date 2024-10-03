package demo.benchmarksHumaneval.NEq.FIB;

/* Return n-th Fibonacci number.
>>> fib(10)
55
>>> fib(1)
1
>>> fib(8)
21 */

public class oldV {
    public static int fib(int n) {
        return fib(n - 1) + fib(n - 2);
    }
}
