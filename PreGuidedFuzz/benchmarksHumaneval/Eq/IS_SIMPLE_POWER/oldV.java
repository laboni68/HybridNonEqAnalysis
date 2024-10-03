package demo.benchmarksHumaneval.NEq.IS_SIMPLE_POWER;

// Your task is to write a function that returns true if a number x is a simple
// power of n and false in other cases.
// x is a simple power of n if n**int=x
// For example:
// is_simple_power(1, 4) => true
// is_simple_power(2, 2) => true
// is_simple_power(8, 2) => true
// is_simple_power(3, 2) => false
// is_simple_power(3, 1) => false
// is_simple_power(5, 3) => false

public class oldV {
    public static int is_simple_power(int x, int n) {
        if (n == 1)
            return (x == 1)?1:0;
        int currentPower = 1;
        while (currentPower < x) {
            currentPower *= n;
        }

        return (currentPower == x)?1:0;
    }
}
