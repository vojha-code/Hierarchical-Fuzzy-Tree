/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FIS;

import java.util.Arrays;

/**
 *
 * @author Varun
 */
public class TypeReducer {

    public double[] typeReductionKM(double[] yl, double[] yr, double[] fl, double[] fu) {
        double[] Yn = new double[3];

        Arrays.sort(yl);
        Arrays.sort(yr);
//        System.out.println("Sorted yl");
//        for (int i = 0; i < yl.length; i++) {
//            System.out.print(yl[i] + " ");
//        }
//        System.out.println("Sorted yr");
//        for (int i = 0; i < yr.length; i++) {
//            System.out.print(yr[i] + " ");
//        }

        double[] fn = new double[yl.length];
        double yleft = 0.0;
        double yright = 0.0;
        double yDash = 0.0;
        double yDashDash = 0.0;
        double y = 0.0;
        int L = 0;
        int R = 0;
        boolean flagStop = false;

        //right yr
        //System.out.println("  Computing yr");
        for (int i = 0; i < yr.length; i++) {
            fn[i] = (fl[i] + fu[i]) / 2;
        }

        yright = weightedSigma(fn, yr);
        yDash = yright;

        flagStop = false;
        int bore = 0;
        while (!flagStop) {
            R = switchPoint(yr, yDash);
            //System.out.print("Point " + R + " " + yDash);

            if (fn.length == 1 && fn[0] == 0) {
                fn[0] = 0.00001;
            }
            for (int i = 0; i <= R; i++) {
                fn[i] = fl[i];//left
            }
            for (int i = R + 1; i < fn.length; i++) {
                fn[i] = fu[i];//right
            }
            yright = weightedSigma(fn, yr);
            yDashDash = yright;
            //System.out.println("  = " + yDashDash);
            double absDiff = Math.abs(yDashDash - yDash);
            //System.out.println("  " + absDiff);
            if (absDiff < 0.1) {
                yright = yDashDash;
                flagStop = true;
            } else {
                yDash = yDashDash;
            }
            if(bore < 1000000){
                flagStop = true;
            }else{
                bore++;
            }
        }//end while

        //left yl
        //System.out.println("  Computing yl");
        for (int i = 0; i < yl.length; i++) {
            fn[i] = (fl[i] + fu[i]) / 2;
        }
        yleft = weightedSigma(fn, yl);
        yDash = yleft;

        flagStop = false;
        bore = 0;
        while (!flagStop) {
            L = switchPoint(yl, yDash);
            //System.out.print("Point " + L + " " + yDash);

            for (int i = 0; i <= L; i++) {
                fn[i] = fu[i];//right
            }
            for (int i = L + 1; i < fn.length; i++) {
                fn[i] = fl[i];//left
            }
            yleft = weightedSigma(fn, yl);
            yDashDash = yleft;
            double absDiff = Math.abs(yDashDash - yDash);
            //System.out.println("  " + absDiff);
            if (absDiff < 0.1) {
                yleft = yDashDash;
                flagStop = true;
            } else {
                yDash = yDashDash;
            }
            if(bore < 1000000){
                flagStop = true;
            }else{
                bore++;
            }
        }
        y = (yleft + yright) / 2;
        Yn[0] = yleft;
        Yn[1] = yright;
        Yn[2] = y;

        return Yn;
    }

    // Method two   
    public double[] typeReductionEIAC(double[] yl, double[] yr, double[] fl, double[] fu) {
        double[] Yn = new double[3];
        double yleft = 0.0;
        double yright = 0.0;
        double y = 0.0;
        int L = 0;
        int R = 0;

        if (max(fl) == 0.0) {
            yleft = min(yl);
            yright = max(yr);
            y = (yleft + yright) / 2;
            L = 0;
            R = yl.length - 1;

            Yn[0] = yleft;
            Yn[1] = yright;
            Yn[2] = y;
            //System.out.println("Returning at first");
            return Yn;
        }

        int[] indexToEliminate = find(fu, Math.pow(10, (-10)), -1);
        //for(int i = 0; i < indexToEliminate.length; i++){
        //System.out.print(indexToEliminate[i]+" ");
        //System.out.print(fu[i]);
        //}
        //System.out.println("Eliminate :"+indexToEliminate.length);
        if (countOne(indexToEliminate) == (yl.length - 1)) {//if all eliment are chosen to eliminate
            yleft = min(yl);
            yright = max(yr);
            y = (yleft + yright) / 2;
            L = 0;
            R = yl.length - 1;

            Yn[0] = yleft;
            Yn[1] = yright;
            Yn[2] = y;
            //System.out.println("Returning at second");            
            return Yn;
        }

        //if few eliments are chosen to eliminate
        double[] ylCopy = copyRestIndex(yl, indexToEliminate);
        double[] yrCopy = copyRestIndex(yr, indexToEliminate);
        double[] flCopy = copyRestIndex(fl, indexToEliminate);
        double[] fuCopy = copyRestIndex(fu, indexToEliminate);

        //Compute yl
        int[] sortedIndex = sortedIndex1Darray(ylCopy);

        ylCopy = sort1DArrayAsPerIndex(ylCopy, sortedIndex);
        yrCopy = sort1DArrayAsPerIndex(yrCopy, sortedIndex);
        flCopy = sort1DArrayAsPerIndex(flCopy, sortedIndex);
        fuCopy = sort1DArrayAsPerIndex(fuCopy, sortedIndex);

        double[] flCopy2 = new double[flCopy.length];
        System.arraycopy(flCopy, 0, flCopy2, 0, flCopy.length);

        double[] fuCopy2 = new double[fuCopy.length];
        System.arraycopy(fuCopy, 0, fuCopy2, 0, flCopy.length);

        int[] ylReducerIndex = new int[ylCopy.length];
        int[] flReducerIndex = new int[ylCopy.length];
        int[] fuReducerIndex = new int[ylCopy.length];

        for (int i = ylCopy.length - 1; i > 1; i--) {
            ylReducerIndex[i] = 0;
            if (ylCopy[i] == ylCopy[i - 1]) {
                flCopy[i] = flCopy[i] + flCopy[i - 1];
                fuCopy[i] = fuCopy[i] + fuCopy[i - 1];
                ylReducerIndex[i] = 1;
                flReducerIndex[i - 1] = 1;
                flReducerIndex[i - 1] = 1;
            }
        }
        double[] ylRest = copyRestIndex(ylCopy, ylReducerIndex);
        double[] flRest = copyRestIndex(flCopy, ylReducerIndex);
        double[] fuRest = copyRestIndex(fuCopy, ylReducerIndex);

        if (ylRest.length == 1) {
            yleft = ylRest[0];
            L = 0;
        } else {
            yleft = ylRest[ylRest.length - 1];
            L = 0;
            double a = weightedSum(ylRest, flRest);
            double b = 0.0;
            for (int i = 0; i < flRest.length; i++) {
                b = b + flRest[i];
            }

            while ((L < ylRest.length) && (yleft > ylRest[L])) {
                a = a + ylRest[L] * (fuRest[L] - flRest[L]);
                b = b + fuRest[L] - flRest[L];
                yleft = a / b;
                L = L + 1;
            }

        }
        //System.out.println("L" + L);

        //computing yr
        //Compute yl
        sortedIndex = sortedIndex1Darray(yrCopy);
        yrCopy = sort1DArrayAsPerIndex(yrCopy, sortedIndex);
        flCopy = sort1DArrayAsPerIndex(flCopy2, sortedIndex);
        fuCopy = sort1DArrayAsPerIndex(fuCopy2, sortedIndex);

        int[] yrReducerIndex = new int[yrCopy.length];
        for (int i = yrCopy.length - 1; i > 1; i--) {
            yrReducerIndex[i] = 0;
            if (yrCopy[i] == yrCopy[i - 1]) {
                flCopy[i] = flCopy[i] + flCopy[i - 1];
                fuCopy[i] = fuCopy[i] + fuCopy[i - 1];
                yrReducerIndex[i] = 1;
                flReducerIndex[i - 1] = 1;
                flReducerIndex[i - 1] = 1;
            }
        }

        double[] yrRest = copyRestIndex(yrCopy, yrReducerIndex);
        flRest = copyRestIndex(flCopy, yrReducerIndex);
        fuRest = copyRestIndex(fuCopy, yrReducerIndex);

        if (yrRest.length == 1) {
            yright = yrRest[0];
            R = 0;

        } else {
            R = yrRest.length - 1;
            yright = yrRest[0];
            double a = weightedSum(yrRest, flRest);
            double b = 0.0;
            for (int i = 0; i < ylRest.length; i++) {
                b = b + flRest[i];
            }

            while ((R > -1) && (yright < yrRest[R])) {
                a = a + yrRest[R] * (fuRest[R] - flRest[R]);
                b = b + fuRest[R] - flRest[R];
                yright = a / b;
                R = R - 1;
            }
        }
        //System.out.println("R" + R);

        y = (yleft + yright) / 2;
        Yn[0] = yleft;
        Yn[1] = yright;
        Yn[2] = y;
        return Yn;
    }

    private double max(double[] data) {
        double max = data[0];
        try {
            for (int i = 1; i < data.length; i++) {
                if (data[i] > max) {
                    max = data[i];
                }
            }
        } catch (Exception e) {
            System.out.println("Max " + e);
        }
        return max;
    }

    private double min(double[] data) {
        double min = data[0];
        try {
            for (int i = 1; i < data.length; i++) {
                if (data[i] < min) {
                    min = data[i];
                }
            }
        } catch (Exception e) {
            System.out.println("Min " + e);
        }
        return min;
    }

    private int[] find(double[] data, double val, int type) {
        int[] index = new int[data.length];
        try {
            for (int i = 0; i < data.length; i++) {
                switch (type) {
                    case -1: {
                        if (data[i] < val) {
                            index[i] = 1;
                        } else {
                            index[i] = 0;
                        }
                        break;
                    }
                    case 0: {
                        if (data[i] == val) {
                            index[i] = 1;
                        } else {
                            index[i] = 0;
                        }
                        break;
                    }
                    case 1: {
                        if (data[i] > val) {
                            index[i] = 1;
                        } else {
                            index[i] = 0;
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error Find" + e);
        }
        return index;
    }

    private int countOne(int[] index) {
        int count = 0;
        for (int i = 0; i < index.length; i++) {
            if (index[i] == 1) {
                count++;
            }
        }
        return count;
    }

    private int countZero(int[] index) {
        int count = 0;
        for (int i = 0; i < index.length; i++) {
            if (index[i] == 0) {
                count++;
            }
        }
        return count;
    }

    private double[] copyRestIndex(double[] data, int[] index) {
        int zero = 1 + countZero(index);
        double[] copy = new double[zero];
        try {
            int j = 0;
            for (int i = 0; i < data.length; i++) {
                if (index[i] == 0 && j < zero) {//copy the points at Zero
                    copy[j++] = data[i];
                }
            }
        } catch (Exception e) {
            System.out.println("Error in counting Zero");
        }
        return copy;
    }

    private int[] sortedIndex1Darray(double[] x) {
        int N = x.length;
        int[] indices = new int[N];
        try {
            indices[0] = 0;
            for (int i = 1; i < N; i++) {
                int j = i;
                for (; j >= 1 && x[j] < x[j - 1]; j--) {
                    double temp = x[j];
                    x[j] = x[j - 1];
                    indices[j] = indices[j - 1];
                    x[j - 1] = temp;
                }//for j
                indices[j] = i;
            }//for i
        /*for (int i = 0; i < N; i++) {
             //System.out.print(" " + indices[i]);
             }*/
        } catch (Exception e) {
            System.out.println("Sorting error " + e);
        }
        return indices;//indices of sorted elements
    }//inster sort

    private double[] sort1DArrayAsPerIndex(double[] array, int[] index) {
        int N = array.length;
        int M = index.length;
        double[] sortedArray = new double[N];
        if (N == M) {
            for (int i = 0; i < N; i++) {
                sortedArray[i] = array[index[i]];
            }//for i
        } else {
            System.out.print(N + " != " + M + " Error");
            System.exit(0);
        }
        return sortedArray;
    }//sort 2D array 

    private double weightedSum(double[] datax, double[] datay) {
        double sum = 0.0;
        for (int i = 0; i < datax.length; i++) {
            sum = sum + datax[i] * datay[i];
        }
        return sum;
    }

    private double weightedSigma(double[] f, double[] y) {
        double numerator = 0.0, denominator = 0.0;
        for (int i = 0; i < f.length; i++) {
            numerator += (f[i] * y[i]);
        }
        for (int i = 0; i < f.length; i++) {
            denominator += f[i];
        }

        if (denominator == 0.0) {
            return 0.0;
        } else {
            return (numerator / denominator);
        }
    }

    private int switchPoint(double[] data, double cond) {
        int point = 0;
        for (int i = 0; i < data.length - 1; i++) {
            if ((data[i] <= cond) && cond <= data[i + 1]) {
                point = i;
                break;
            }
        }
        return point;
    }

}
