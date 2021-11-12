package Function;

public class Function {

    public static double computeteRMSE(double x[], double[] t, double[][] predictors, int ensembls, int patLength) {
        double rmse = 10e10;
        // System.out.println("Computing RMSE");
        double[] p = new double[patLength];
        for (int i = 0; i < patLength; i++) {
            double sum = 0.0;
            //System.out.print(i+" "+t[i]+",");
            for (int j = 0; j < ensembls; j++) {
                sum = sum + x[j] * predictors[i][j];
                //System.out.print(predictors[i][j]+",");
            }
            // System.out.println();
            p[i] = sum;
        }//for
        double mse = 0.0;
        for (int i = 0; i < patLength; i++) {
            mse = mse + (t[i] - p[i]) * (t[i] - p[i]);
        }//for
        mse = mse / (double) patLength;
        rmse = Math.sqrt(mse);
       // for (int j = 0; j < ensembls; j++) {
        //   System.out.print(x[j] + " ");
        //}
        // System.out.println("RMSE:" + rmse);
        //System.exit(0);
        return rmse;
    }//RMSE

    public static double computeteAccuracy(double x[], double[] t, double[][] predictors, int ensembls, int patLength) {
        double[] p = new double[patLength];
        for (int i = 0; i < patLength; i++) {
            double countONE = 0.0;
            double countZERO = 0.0;
            //System.out.print(i+" "+t[i]+",");
            for (int j = 0; j < ensembls; j++) {
                if (predictors[i][j] == 1.0) {//ONE
                    countONE = x[j] * 1.0;
                } else {//ZERO
                    countZERO = x[j] * 1.0;
                }//if
                //System.out.print(predictors[i][j]+",");
            }
            // System.out.println();
            p[i] = (countONE < countZERO) ? 0.0 : 1.0;
        }//for
        int countTrue = 0;
        for (int i = 0; i < patLength; i++) {
            if (t[i] == p[i]) {
                //System.out.println(t[i]+" - "+p[i]);
                countTrue++;
            }
        }
        //System.out.print( " "+ countTrue);
        double errorRate = (1.0 - (double)countTrue / (double)patLength) + 0.0000000000000001;
        //System.out.print( "  "+ errorRate);
       // for (int j = 0; j < ensembls; j++) {
        //   System.out.print(x[j] + " ");
        //}
        // System.out.println("RMSE:" + rmse);
        return errorRate;
    }//accurayc

    public static double computeteFunction(double x[], String fun) {
        double dRet = 0.0;
        double pi = 3.1415926535;
        int Dimensions = x.length;
        FunName currFun = FunName.valueOf(fun);

        switch (currFun) {
            case Beales:
                dRet = Math.pow((2.8125 - x[0] + x[0] * Math.pow(x[1], 4)), 2)
                        + Math.pow((2.25 - x[0] + x[0] * Math.pow(x[1], 2)), 2)
                        + Math.pow((1.5 - x[0] + x[0] * x[1]), 2);
                break;

            case Booth:
                dRet = Math.pow((x[0] + 2 * x[1] - 7), 2) + Math.pow((2 * x[0] + x[1] - 5), 2);
                break;

            case BF:
                dRet = (Math.pow(x[0], 2) - 10 * Math.cos(2 * 3.14 * x[0])) + (Math.pow(x[1], 2) - 10 * Math.cos(2 * 3.14 * x[1])) + 20;
                break;

            case Sphere:
                for (int i = 0; i < Dimensions; i++) {
                    dRet += x[i] * x[i];
                }
                break;

            case Rosenbrock:
                for (int i = 1; i < Dimensions; i++) {
                    dRet += 100.0 * (x[i] - x[i - 1] * x[i - 1]) * (x[i] - x[i - 1] * x[i - 1]) + (x[i - 1] - 1) * (x[i - 1] - 1);
                }
                dRet = Math.abs(dRet);
                break;

            case Rastrigrin:
                for (int i = 0; i < Dimensions; i++) {
                    dRet += x[i] * x[i] - 10.0 * Math.cos(2.0 * pi * x[i]) + 10.0;
                }
                break;

            case Griewank:
                double result_s,
                 result_p;
                result_s = 0.0;
                result_p = 1.0;
                for (int i = 0; i < Dimensions; i++) {
                    result_s += x[i] * x[i];
                    result_p *= Math.cos(x[i] / Math.sqrt((double) i + 1));
                }
                dRet = result_s / 4000.0 - result_p + 1;
                break;

            case Ackley:
                double a = 20,
                 b = 0.2,
                 c = 2 * pi;
                double s1 = 0,
                 s2 = 0;
                for (int i = 0; i < Dimensions; i++) {
                    s1 = s1 + x[i] * x[i];
                    s2 = s2 + Math.cos(c * x[i]);
                }
                dRet = -a * Math.exp(-b * Math.sqrt(1.0 / Dimensions * s1)) - Math.exp(1.0 / Dimensions * s2) + a + Math.exp(1.0);
                break;

            case Zakharov:
                s1 = 0;
                s2 = 0;
                for (int i = 0; i < Dimensions; i++) {
                    s1 = s1 + x[i] * x[i];
                    s2 = s2 + 0.5 * i * x[i];
                }
                dRet = s1 + Math.pow(s2, 2) + Math.pow(s2, 4);
                break;

            case Levy:
                for (int i = 0; i < Dimensions; i++) {
                    x[i] = 1.0 + (x[i] - 1.0) / 4.0;
                }

                double s = Math.pow(Math.sin(pi * x[0]), 2);

                for (int i = 1; i < Dimensions - 1; i++) {
                    s = s + Math.pow((x[i] - 1.0), 2) * (1.0 + 10.0 * (Math.pow(Math.sin(pi * x[i] + 1), 2)));
                }

                dRet = s + Math.pow((x[Dimensions - 1] - 1), 2) * (1 + Math.pow((Math.sin(2 * pi * x[Dimensions - 1])), 2));
                break;

            case Schwefel:
                s = 0.0;
                for (int i = 0; i < Dimensions; i++) {
                    s = s + (x[i] * Math.sin(Math.sqrt(Math.abs(x[i]))));
                }

                dRet = 418.9829 * Dimensions - s;
                break;

            case SumSquares:
                s = 0.0;
                for (int i = 0; i < Dimensions; i++) {
                    s = s + i * Math.pow(x[i], 2);
                }

                dRet = s;
                break;

            case DixonPrice:
                s1 = Math.pow((x[0] - 1), 2);
                s2 = 0;
                s = 0;
                for (int i = 1; i < Dimensions; i++) {
                    s2 += i * Math.pow((2 * Math.pow(x[i], 2) - x[i - 1]), 2);
                }

                s = s1 + s2;

                dRet = s;
                break;

            case Perm:
                s2 = 0;
                s = 0;
                double bta = 0.5;
                for (int i = 0; i < Dimensions; i++) {
                    s1 = 0;
                    for (int j = 0; j < Dimensions; j++) {
                        s1 = s1 + (Math.pow(j, i) + bta) * (Math.pow((x[j] / j), i) - 1);
                    }
                    s2 = s2 + Math.pow(s1, 2);
                }
                s = s2;
                dRet = s;
                break;

            case Trid:
                s1 = 0;
                s2 = 0;
                s = 0;
                for (int i = 0; i < Dimensions; i++) {
                    s1 += s1 + Math.pow((x[i] - 1), 2);
                }

                for (int i = 1; i < Dimensions; i++) {
                    s2 += s2 + x[i] * x[i - 1];
                }

                s = s1 - s2;

                dRet = s;
                break;

            case Powell:
                int m = Dimensions / 4;
                double fvec[] = new double[Dimensions];

                for (int i = 1; i < m; i++) {
                    fvec[4 * i - 3] = x[4 * i - 3] + 10 * (x[4 * i - 2]);
                    fvec[4 * i - 2] = Math.sqrt(5) * (x[4 * i - 1] - x[4 * i]);
                    fvec[4 * i - 1] = Math.pow((x[4 * i - 2] - 2 * (x[4 * i - 1])), 2);
                    fvec[4 * i] = Math.sqrt(10) * Math.pow((x[4 * i - 3] - x[4 * i]), 2);
                }
                s1 = 0.0;
                for (int i = 1; i < m; i++) {
                    s1 = s1 + fvec[i] * fvec[i];;
                }
                s = Math.pow(Math.sqrt(s1), 2);

                dRet = s;
                break;
        }
        return dRet;
    }

    public enum FunName {
        Beales,
        Booth,
        BF,
        Sphere,
        Rosenbrock,
        Rastrigrin,
        Griewank,
        Ackley,
        Levy,
        Schwefel,
        SumSquares,
        Zakharov,
        DixonPrice,
        Perm,
        Trid,
        Powell
    }
}
