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
public class FIST2 {

    public static void main(String args[]) {
        System.out.println("Fuzzy Inferece Test");
        int part = 2;
        int arity = 2;

        double[] x = new double[arity];
        double[][] a = new double[arity][part];
        double[][] b = new double[arity][part];
        double[][] devp = new double[arity][part];
        int rules = (int) Math.pow(part, arity);
        double[][] c = new double[rules][arity + 1];
        double[][] devc = new double[rules][arity + 1];

        //inputs
        for (int i = 0; i < arity; i++) {
            x[i] = Math.random();
        }
        for (int i = 0; i < arity; i++) {
            for (int j = 0; j < part; j++) {
                a[i][j] = Math.random();
                b[i][j] = Math.random();
                devp[i][j] = Math.random();
            }
        }
        for (int i = 0; i < rules; i++) {
            for (int j = 0; j < arity + 1; j++) {
                c[i][j] = Math.random();
                devc[i][j] = Math.random();
            }
        }
        System.out.print("\nFIS out : " + computeFIS(part, arity, x, a, b, c, devp, devc) + "\n");
    }

    public static double computeFIS(int part, int arity, double[] x, double[][] a, double[][] b, double[][] c, double[][] devp, double[][]devc) {
        int rules = (int) Math.pow(part, arity);
        double[] fl = new double[rules];//firing strength low and up
        double[] fu = new double[rules];//firing strength low and up
        double[] wl = new double[rules];//weight 
        double[] wr = new double[rules];//weight 

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
            /*Only Test prints        
            System.out.print(i + " : ");
            for (int j = 0; j < arity; j++) {
                System.out.print(ruleIndex[i][j] + " ");
            }
            System.out.println();*/// only Test
        }//for
        /*Only Test prints        
        System.out.print(" \n");
        for (int i = 0; i < rules; i++) {
            System.out.print(" Rule " + i + ": IF ");
            for (int j = 0; j < arity; j++) {
                System.out.printf("m[%d][%d](x%d) and ", j, ruleIndex[i][j], j);
            }
            System.out.printf(" THEN ");

            for (int j = 0; j < arity + 1; j++) {
                if (j == 0) {
                    System.out.printf("c%d[%d][%d] + ", j, i, j);
                } else {
                    System.out.printf("c%d[%d][%d] x(%d) + ", j, i, j, j - 1);
                }
            }

            System.out.print(" \n");
        }
        System.out.print(" \n");*/// only Test

        double[][] mulo = new double[arity][part];
        double[][] muup = new double[arity][part];
        for (int i = 0; i < arity; i++) {
            for (int j = 0; j < part; j++) {
                mulo[i][j] = gmfLo(a[i][j], b[i][j], devp[i][j], x[i]);
                muup[i][j] = gmfUp(a[i][j], b[i][j], devp[i][j], x[i]);
//                System.out.printf(" mu%d%d(x%d)  = ", i, j, i);
//                System.out.print(mulo[i][j] + " ");
            }
            //System.out.print(" \n");
        }
        for (int i = 0; i < rules; i++) {
            //System.out.print(" Rule " + i + ": ");
            for (int j = 0; j < arity; j++) {
                if (j == 0) {
                    fl[i] = mulo[j][ruleIndex[i][j]];//lower firing strength
                    fu[i] = muup[j][ruleIndex[i][j]];//upper fring strength
                    //System.out.print(mulo[j][ruleIndex[i][j]] + " * ");
                } else {
                    fl[i] *= mulo[j][ruleIndex[i][j]];//lower firing strength
                    fu[i] *= muup[j][ruleIndex[i][j]];//upper fring strength
                    //System.out.print(mulo[j][ruleIndex[i][j]] + " * ");
                }
            }
            for (int j = 0; j < arity + 1; j++) {
                if (j == 0) {
                    wl[i] = c[i][j] - devc[i][j];//weight left
                    wr[i] = c[i][j] + devc[i][j];//weight right
                    //System.out.print(c[i][j] + " + ");
                } else {
                    wl[i] += (c[i][j] - devc[i][j]) * x[j - 1];//weight left
                    wr[i] += (c[i][j] + devc[i][j]) * x[j - 1];//weight right
                    //System.out.print(c[i][j] + " + ");
                }
            }
            //System.out.print(" \n");
        }
        TypeReducer tr = new TypeReducer();
        double[] y = tr.typeReductionKM(wl,wr,fl,fu);
        if(Double.isNaN(y[2])){
            y[2] = 10e+10;// returning a high error so that this paprameters will not be considered
        }
        return y[2];
    }

    private static double antFun(double a, double b, double x) {
        return (1 / Math.pow(((x - a) / b), 2));
    }
    public static double gmfUp(double a, double b, double dev, double x) {
        double muLo = a - dev * b;
        double muUp = a - dev * b;
        double sigma = b;
        if (x < muLo) {
            return Math.exp(-0.5 * (Math.pow(((x - muLo) / sigma), 2)));
        } else if (x >= muLo && x <= muUp) {
            return 1.0;
        } else {
            return Math.exp(-0.5 * (Math.pow(((x - muUp) / sigma), 2)));
        }
    }

    public static double gmfLo(double a, double b, double dev, double x) {
        double muLo = a - dev * b;
        double muUp = a - dev * b;
        double sigma = b;
        double cond = (muLo + muUp) / 2.0;
        if (x <= cond) {
            return Math.exp(-0.5 * (Math.pow(((x - muUp) / sigma), 2)));
        } else {
            return Math.exp(-0.5 * (Math.pow(((x - muLo) / sigma), 2)));
        }
    }
}
