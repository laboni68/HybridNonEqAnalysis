package demo.benchmarksHumaneval.NEq.DIGITS;

/*
 * Given a positive integer n, return the product of the odd digits.
    Return 0 if all digits are even.
    For example:
    digits(1)  == 1
    digits(4)  == 0
    digits(235) == 15
 */

public class oldV {
    public static int digits(int n) {
        int product = 1;
        boolean hasOdd = false;
        while (n > 0) {
            int digit = n % 10; // Get the last digit
            n /= 10;            // Remove the last digit
            if (digit % 2 != 0) { // Check if the digit is odd
                product *= digit;
                hasOdd = true;
            }
        }
        return hasOdd ? product : 0;
    }
}