package demo.benchmarksHumaneval.NEq.SMALLEST_CHANGE;

// Given an array arr of integers, find the minimum number of elements that
// need to be changed to make the array palindromic. A palindromic array is an array that
// is read the same backwards and forwards. In one change, you can change one element to any other element.

// For example:
// smallest_change([1,2,3,5,4,7,9,6]) == 4
// smallest_change([1, 2, 3, 4, 3, 2, 2]) == 1
// smallest_change([1, 2, 3, 2, 1]) == 0

public class oldV {
    public static int smallest_change(int[] arr) {
        int result = 0;
        for (int i = 0; i < arr.length; i += 1) {
            if (arr[i] != arr[arr.length - i])
                result += 1;
        }

        return result;
    }
}
