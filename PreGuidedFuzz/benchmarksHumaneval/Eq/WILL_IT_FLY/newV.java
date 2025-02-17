package demo.benchmarksHumaneval.NEq.WILL_IT_FLY;

/* Write a function that returns True if the object q will fly, and False otherwise.
The object q will fly if it's balanced (it is a palindromic list) and the sum of its elements is less than or equal the maximum possible weight w.

Example:
will_it_fly([1, 2], 5) ➞ False 
# 1+2 is less than the maximum possible weight, but it's unbalanced.

will_it_fly([3, 2, 3], 1) ➞ False
# it's balanced, but 3+2+3 is more than the maximum possible weight.

will_it_fly([3, 2, 3], 9) ➞ True
# 3+2+3 is less than the maximum possible weight, and it's balanced.

will_it_fly([3], 5) ➞ True
# 3 is less than the maximum possible weight, and it's balanced. */

public class newV {
    public static boolean will_it_fly(int[] q, int w) {
        int sum = 0;
        for (int i = 0; i < q.length; i += 1)
            sum += q[i];
        
        if (sum > w)
            return false;
        
        int i = 0;
        int j = q.length - 1;
        while (i < j) {
            if (q[i] != q[j])   return false;
            i += 1;
            j -= 1;
        }
        return true;
    }
}
