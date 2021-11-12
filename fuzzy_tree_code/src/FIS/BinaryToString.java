package FIS;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Varun
 */
public class BinaryToString {

    public static void main(String[] args) {
        int arity = 3;
        int part = 2;
        int rules = (int) Math.pow(part, arity);
        System.out.println("Total Rules: " + rules);
        int[][] ruleIndex = new int[rules][arity];
        for (int i = 0; i < rules; i++) {
            ruleIndex[i] = getVector(i, arity);
        }

    }
    /**
     * 
     * @param i 
     * @param arity
     * @return 
     */
    public static int[] getVector(int i, int arity) {
        int[] binary = new int[arity];
        String vector = intToString(i, arity - 1);
        //System.out.println(vector);
        String[] tokens = vector.split(",");
        for (int j = 0; j < tokens.length; j++) {
            int val = Integer.parseInt(tokens[j]);
            binary[j] = val;
            //System.out.print(binary[j] + " ");
        }
        //System.out.println();
        return binary;
    }
    
    /**
     * 
     * @param number
     * @param groupSize
     * @return 
     */
    public static String intToString(int number, int groupSize) {
        StringBuilder result = new StringBuilder();

        //for (int i = 31; i >= 0; i--) {
        for (int i = groupSize; i >= 0; i--) {
            int mask = 1 << i;
            result.append((number & mask) != 0 ? "1," : "0,");

            if (i % groupSize == 0) {
                result.append("");
            }
        }
        result.replace(result.length() - 1, result.length(), "");

        return result.toString();
    }
}
