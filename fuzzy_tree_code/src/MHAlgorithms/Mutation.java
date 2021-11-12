/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MHAlgorithms;

import Randoms.JavaRand;

/**
 *
 * @author Varun
 */
class Mutation {

    private static JavaRand randGen;
    private static double[] maxX;
    private static double[] minX;
    private static double MUTATION_PROB;

    static double[] mutation(double[] parent_3, double[] low, double[] high, JavaRand rand, double MUTATION_PROB, String Mutation_TYPE) {
        double[] child = new double[parent_3.length];
        minX = low;
        maxX = high;
        randGen = rand;
        MUTATION_PROB = MUTATION_PROB;
        switch (Mutation_TYPE) {
            case "Simulated Binary Mutation":
                child = mutationSimulatedBinary(parent_3);
                break;
            case "Uniform Mutation":
                child = mutationUniform(parent_3);
                break;
            case "Single Point":
                child = mutationGaussin(parent_3);
                break;
            default:
                break;
        }
        return child;
    }

    private static double[] mutationSimulatedBinary(double[] parent_3) {
        double[] child = new double[parent_3.length];
        for (int j = 0; j < parent_3.length - 1; j++) {
            double u = randGen.nextDouble();
            double delta;
            if (u <= 0.5) {
                delta = Math.pow((2.0 * u), (1.0 / (20 + 1.0))) - 1.0;
            } else {
                delta = 1.0 - Math.pow((2 * (1.0 - u)), (1.0 / (20 + 1.0)));
            }//if  
            //Generate the jth element of first child
            child[j] = checkBounds((parent_3[j] + delta), j);
            //decision space else set it to the appropriate extrema.
            //System.out.print(" "+child_3[j]);
        }//for j
        return child;
    }

    private static double[] mutationUniform(double[] parent_3) {
        int[] mutationVal = mutationVector(parent_3.length);
        double[] child = new double[parent_3.length];
        for (int j = 0; j < parent_3.length; j++) {
            if (mutationVal[j] == 1) {
                child[j] = checkBounds(randGen.nextUniform(), j);
            } else {
                child[j] = checkBounds(parent_3[j], j);
            }
        }
        return child;
    }
    
    private static double[] mutationGaussin(double[] parent_3) {
        int[] mutationVal = mutationVector(parent_3.length);
        double[] child = new double[parent_3.length];
        for (int j = 0; j < parent_3.length; j++) {
            if (mutationVal[j] == 1) {
                child[j] = checkBounds(randGen.nextGaussian(), j);
            } else {
                child[j] = checkBounds(parent_3[j], j);
            }
        }
        return child;
    }
    
    //returns a vector for mutation
    public static int[] mutationVector(int n) {
        int[] mutationVector = new int[n];
        for (int i = 0; i < n; i++) {
            //double r = Math.random();
            //System.out.printf(" %.3f", r);
            if (Math.random() < MUTATION_PROB) {
                mutationVector[i] = 1;
            } else {
                mutationVector[i] = 0;
            }
        }
        return mutationVector;
    }

    // check whether a parameter is out of range
    private static double checkBounds(double param, int i) {
        if (param < minX[i]) {//low[i]) {
            return minX[i];//low[i];
        } else if (param > maxX[i]) {//high[i]) {
            return maxX[i];//high[i];
        } else {
            return param;
        }
    }//check bound
}
