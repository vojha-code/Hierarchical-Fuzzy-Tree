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
public class FIST1 {

    static int counterNaN = 0;
    static int counterGood = 0;

    public static void main(String args[]) {
        System.out.println("Fuzzy Inferece Test");
        int part = 2;
        int arity = 2;

        double[] x = new double[arity];
        double[][] a = new double[arity][part];
        double[][] b = new double[arity][part];
        int rules = (int) Math.pow(part, arity);
        double[][] c = new double[rules][arity + 1];

        //inputs
        for (int i = 0; i < arity; i++) {
            x[i] = Math.random();
        }
        for (int i = 0; i < arity; i++) {
            for (int j = 0; j < part; j++) {
                a[i][j] = Math.random();
                b[i][j] = Math.random();
            }
        }
        for (int i = 0; i < rules; i++) {
            for (int j = 0; j < arity + 1; j++) {
                c[i][j] = Math.random();
            }
        }
        System.out.print("\nFIS out : " + computeFIS(part, arity, x, a, b, c) + "\n");
    }

    public static double computeFIS(int part, int arity, double[] x, double[][] a, double[][] b, double[][] c) {
        int rules = (int) Math.pow(part, arity);
        double[] w = new double[rules];
        double[] z = new double[rules];
        double sum_wz = 0.00000000001;////just in case  NaN possibility
        double sum_w = 0.000000000001;//just in case  NaN possibility
        if (a.length != b.length) {
            System.out.println(arity + " " + part);
            System.out.println("A " + a.length + " " + a[0].length);
            System.out.println("B " + b.length + " " + b[0].length);
            System.exit(0);
        }
        //System.out.println("Total Rules: " + rules);
        int[][] ruleIndex = new int[rules][arity];
        for (int i = 0; i < rules; i++) {
            ruleIndex[i] = BinaryToString.getVector(i, arity);
//            System.out.print(i + " : ");
//            for (int j = 0; j < arity; j++) {
//                System.out.print(ruleIndex[i][j] + " ");
//            }
//            System.out.println();
        }
//Only Test prints        
//        System.out.print(" \n");
//        for (int i = 0; i < rules; i++) {
//            System.out.print(" Rule " + i + ": IF ");
//            for (int j = 0; j < arity; j++) {
//                System.out.printf("m[%d][%d](x%d) and ", j, ruleIndex[i][j], j);
//            }
//            System.out.printf(" THEN ");
//
//            for (int j = 0; j < arity + 1; j++) {
//                if (j == 0) {
//                    System.out.printf("c%d[%d][%d] + ", j, i, j);
//                } else {
//                    System.out.printf("c%d[%d][%d] x(%d) + ", j, i, j, j - 1);
//                }
//            }
//
//            System.out.print(" \n");
//        }
//        System.out.print(" \n");

        double[][] mux = new double[arity][part];
        for (int i = 0; i < arity; i++) {
            for (int j = 0; j < part; j++) {
                mux[i][j] = antFun(a[i][j], b[i][j], x[i]);
                //System.out.printf(" mu%d%d(x%d)  = ", i, j, i);
                //System.out.print(mux[i][j] + " ");
            }
            //System.out.print(" \n");
        }
        for (int i = 0; i < rules; i++) {
            //System.out.print(" Rule " + i + ": ");
            for (int j = 0; j < arity; j++) {
                if (j == 0) {
                    w[i] = mux[j][ruleIndex[i][j]];
                    //System.out.print(mux[j][ruleIndex[i][j]] + " * ");
                } else {
                    w[i] *= mux[j][ruleIndex[i][j]];
                    //System.out.print(mux[j][ruleIndex[i][j]] + " * ");
                }
            }
            for (int j = 0; j < arity + 1; j++) {
                if (j == 0) {
                    z[i] = c[i][j];
                    //System.out.print(c[i][j] + " + ");
                } else {
                    z[i] += c[i][j] * x[j - 1];
                    //System.out.print(c[i][j] + " + ");
                }
            }
            sum_wz += w[i] * z[i];
            sum_w += w[i];
            //System.out.print(" \n");
        }
        double y = sum_wz / sum_w;

        if (Double.isNaN(y)) {
            //System.out.println("NaN: " + counterNaN++);
            y = 10e+10;//returning a high error
        }
        return y;
    }

    private static double antFun(double a, double b, double x) {
        double y = (1 /( 1 + Math.pow(((x - a) / b), 2) ));
//        ActivationFunction fun = new ActivationFunction();
//        y = fun.getOutput(a, b, 1, x);
//        double netn = ((x - a) / b);
//        netn = netn * netn;
//        netn = -netn;
//        double y = Math.pow(2.71828182845904523536, netn);
        return y;
    }
}
