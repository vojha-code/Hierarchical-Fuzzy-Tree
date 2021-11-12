/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Function;

/**
 *
 * @author Varun
 */
public class FunctionValues {
    public static double[] defaultFunctionValues(String FunName){
        double[] values = new double[4];
        double minX = 0.0;
        double maxX = 0.0;
        double dimension = 0.0;
        double d = 20;
        int isFunFound = 1;
        switch (FunName) {
            case "Ackley":
                minX = -15;
                maxX = 30.0;
                dimension = d;
                break;
            case "Levy":
                minX = -100;
                maxX = 100;
                dimension = d;
                break;
            case "Sphere":
                minX = -50;
                maxX = 100.0;
                dimension = d;
                break;
            case "SumSquares":
                minX = -10;
                maxX = 10.00;
                dimension = d;
                break;
            case "Rosenbrock":
                minX = -5;
                maxX = 10.0;
                dimension = d;
                break;
            case "Rastrigrin":
                minX = -2.56;
                maxX = 5.12;
                dimension = d;
                break;
            case "Griewank":
                minX = -300;
                maxX = 600.00;
                dimension = d;
                break;
            case "Zakharov":
                minX = -5;
                maxX = 10.0;
                dimension = d;
                break;
            case "DixonPrice":
                minX = -10;
                maxX = 10.00;
                dimension = d;
                break;
            case "Powell":
                minX = -4;
                maxX = 5.00;
                dimension = d;
                break;
            case "Perm":
                minX = -10;
                maxX = 10.00;
                dimension = d;
                break;
            case "Trid":
                minX = -10;
                maxX = 10.00;
                dimension = 10;
                break;
            case "Beales":
                minX = -10;
                maxX = 10;
                dimension = 2;
                break;
            case "Booth":
                minX = -10;
                maxX = 10.00;
                dimension = 2;
                break;
            case "BF":
                minX = -5.12;
                maxX = 5.12;
                dimension = 2;
                break;
            default:
                isFunFound = 0;
                break;
        }
        values[0] = dimension;
        values[1] = minX;
        values[2] = maxX;
        values[3] = isFunFound;
        return values;
    }
    
}
