package demo.benchmarksHumaneval.NEq.SEARCH;

/* You are given a non-empty list of positive integers. Return the greatest integer that is greater than 
zero, and has a frequency greater than or equal to the value of the integer itself. 
The frequency of an integer is the number of times it appears in the list.
If no such a value exist, return -1.
Examples:
    search([4, 1, 2, 2, 3, 1]) == 2
    search([1, 2, 2, 3, 3, 3, 4, 4, 4]) == 3
    search([5, 5, 4, 4, 4]) == -1 */

public class newV {
    public static int search(int[] lst) {
        int max = -1;
        for (Integer i : lst) {
            if (i > max)
                max = i;
        }

        int result = -1;
        int[] frq = new int[max + 1];
        for (int i = 0; i < lst.length; i += 1)  frq[lst[i]] += 1;
        for (int i = 0; i < frq.length; i += 1) if (frq[i] >= i && frq[i] > 0) result = i;
        
        return result;
    }
}
