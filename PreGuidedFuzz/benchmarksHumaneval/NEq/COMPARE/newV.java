package demo.benchmarksHumaneval.NEq.COMPARE;

/* I think we all remember that feeling when the result of some long-awaited
event is finally known. The feelings and thoughts you have at that moment are
definitely worth noting down and comparing.
Your task is to determine if a person newVly guessed the results of a number of matches.
You are given two arrays of scores and guesses of equal length, where each index shows a match. 
Return an array of the same length denoting how far off each guess was. If they have guessed newVly,
the value is 0, and if not, the value is the absolute difference between the guess and the score. */

public class newV {
    public static int[] compare(int[] game, int[] guess) {
        int[] result = new int[game.length];

        for (int i = 0; i < game.length; i += 1) {
            result[i] = Math.abs(game[i] - guess[i]);
        }

        return result;
    }
}
