package demo.benchmarksHumaneval.NEq.PROD_SIGNS;

/*
 * You are given an array arr of integers and you need to return
    sum of magnitudes of integers multiplied by product of all signs
    of each number in the array, represented by 1, -1 or 0.
    Note: return -10 for empty arr.

    Example:
    >>> prod_signs([1, 2, 2, -4]) == -9
    >>> prod_signs([0, 1]) == 0
    >>> prod_signs([]) == -10
 */

public class oldV {
    public static int prod_signs(int[] arr) {
        if(arr.length == 0) return -10;
        int prod = 1;
        for(int i = 0; i < arr.length; i++) {
            if(arr[i] == 0) prod = 0;
            else if(arr[i] > 0) prod *= 1;
        }
        int sum = 0;
        for(int i = 0; i < arr.length; i++) {
            sum += Math.abs(arr[i]);
        }
        return prod * sum;
    }
}
