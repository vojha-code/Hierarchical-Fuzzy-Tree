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
class Crossover {
    private static double[] minX;
    private static double[] maxX;
    private static JavaRand randGen;

    static double[][] crossover(double[] parent_1, double[] parent_2, double[] low, double[] high, JavaRand rand,String crossOverType) {
        double[][] child = new double[2][parent_1.length];
        minX = low;
        maxX = high;
        randGen = rand;        
        switch (crossOverType) {
            case "Simulated Binary Crossover":
                child = crossOverSimulatedBinary(parent_1, parent_2);
                break;
            case "Arithmatic Crossover":
                child = crossOverArithmaticMean(parent_1, parent_2);
                break;
            case "Single Point":
                child = crossOverSinglePoint(parent_1, parent_2);
                break;
            case "Two Point":
                break;
            default:
                break;
        }
        return child;
    }

    private static double[][] crossOverSimulatedBinary(double[] parent_1, double[] parent_2) {
        double[][] child = new double[2][parent_1.length];
        for (int j = 0; j < parent_1.length - 1 - 1; j++) {
            //SBX (Simulated Binary Crossover).
            //For more information about SBX refer the enclosed pdf file.
            //Generate a random number
            double u = randGen.nextDouble();
            double bq;
            if (u <= 0.5) {
                bq = Math.pow((2.0 * u), (1.0 / (20 + 1.0)));
            } else {
                bq = Math.pow((1.0 / (2.0 * (1.0 - u))), (1.0 / (20 + 1.0)));
            }//if  
            //Generate the jth element of first child
            child[0][j] = checkBounds((0.5 * (((1.0 + bq) * parent_1[j]) + (1.0 - bq) * parent_2[j])),j);
            //Generate the jth element of second child
            child[1][j] = checkBounds((0.5 * (((1.0 - bq) * parent_1[j]) + (1.0 + bq) * parent_2[j])),j);
            //Make sure that the generated element is within the specified
            //decision space else set it to the appropriate extrema.
            //System.out.print(" ["+child_1[j]+" "+child_2[j]+"]");
        }//for j
        return child;
    }

    private static double[][] crossOverArithmaticMean(double[] parent_1, double[] parent_2) {
        double[][] child = new double[2][parent_1.length];
        double alpha = randGen.random(false);
        for (int j = 0; j < parent_1.length - 1 - 1; j++) {
            child[0][j] = checkBounds((alpha * parent_1[j] + (1.0 - alpha) * parent_2[j]),j);
            child[1][j] = checkBounds((alpha * parent_2[j] + (1.0 - alpha) * parent_1[j]),j);
            
        }
        return child;
    }

    private static double[][] crossOverSinglePoint(double[] parent_1, double[] parent_2) {
        double index = randGen.random(parent_1.length - 1,false);
        double[][] child = new double[2][parent_1.length];
        for (int j = 0; j < parent_1.length - 1; j++) {
            if (j < index) {
                child[0][j] = checkBounds(parent_1[j],j);
                child[1][j] = checkBounds(parent_2[j],j);
            } else {
                child[0][j] = checkBounds(parent_2[j],j);
                child[1][j] = checkBounds(parent_1[j],j);
            }
        }
        return child;
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
