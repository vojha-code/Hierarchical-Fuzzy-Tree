/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MISC;

import AdditionalFrames.InitiatorFrame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JOptionPane;

/**
 *
 * @author Varun Ojha
 */
public class Statistics {
    public String matriPrint;

    public double[] statistics(String fileName, int indexPointer, boolean isClassification) {
        if (isClassification) {
            return confusionMatrix(fileName, indexPointer);
        } else {
            return statisticsReg(fileName, indexPointer);
        }
    }//end correlation
    
    public double[] statisticsComplex(String fileName, int indexPointer, boolean isClassification) {
            return statisticsCmplxClass(fileName, indexPointer);
    }//end correlation

    private double[] confusionMatrix(String fileName, int Ot) {
        int catSize = 2;//at least two
        if (Ot > catSize) {
            catSize = Ot;
        }//if
        double[] statistics = new double[4];
        int[][] target;
        int[][] pred;
        int[] exp;
        int[] obt;
        int length = 0;
        try {
            String line;
            try (FileReader fin = new FileReader(fileName)) {
                BufferedReader br = new BufferedReader(fin);
                length = 0;
                while ((line = br.readLine()) != null) {
                    length++;
                }
                //System.out.println("\n Length"+length);
                exp = new int[length];
                obt = new int[length];
                target = new int[length][catSize];
                pred = new int[length][catSize];
                br.close();
                fin.close();
            }
            try (FileReader fin1 = new FileReader(fileName)) {
                BufferedReader br1 = new BufferedReader(fin1);
                int i = 0;
                while ((line = br1.readLine()) != null) {
                    String[] tokens = line.split(",");
                    int jt = 0;
                    int jp = 0;
                    for (int indexToken = 0; indexToken < tokens.length && (2 * Ot == tokens.length); indexToken++) {
                        if (indexToken % 2 == 0) {//target at even
                            target[i][jt] = (int) Double.parseDouble(tokens[indexToken]);//target is in x
                            jt++;
                            if (catSize == 2) {//ONLY for binary classification
                                target[i][jt] = 1 - target[i][jt - 1];
                            }
                        } else {//predicted at odd
                            pred[i][jp] = (int) Double.parseDouble(tokens[indexToken]);//predicted in y
                            jp++;
                            if (catSize == 2) {//ONLY for binary classification
                                pred[i][jt] = 1 - pred[i][jt - 1];
                            }
                        }
                    }//for                        
                    i++;//increament i for next row 
                }//while
                br1.close();
                fin1.close();
                //optional 
                FileWriter fw = new FileWriter(InitiatorFrame.absoluteFilePathOut + "classificationOut.csv");
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter pr = new PrintWriter(bw);
                for (int p = 0; p < length; p++) {
                    for (int j = 0; j < catSize; j++) {
                        pr.print(target[p][j] + ",");
                    }//for j
                    for (int j = 0; j < catSize; j++) {
                        pr.print(pred[p][j] + ",");
                    }//for j
                    pr.println();
                }//for p
                pr.close();
            }//try
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Operation failed! Adivise: Close the open excel files!");
            System.out.print(e);
            return null;
        }//catch
        try {
            int k, l, tarCls, predCls;
            for (int i = 0; i < target.length; i++) {//for all samples
                k = 0;
                l = 0;
                tarCls = 0;
                predCls = 0;
                //the jth 1 is the class e.e 0 1 0 is output then j = 1 is the target and k should be 1
                for (int j = 0; j < catSize; j++) {
                    if (target[i][j] == 1) {
                        k++;
                        tarCls = j;//found target class
                    }
                    if (pred[i][j] == 1) {
                        l++;
                        predCls = j;//found predicted class
                    }
                }//go next cat
                //if there is more than one 1's then the pattern is not a goor prediction
                if (k < 2) {
                    exp[i] = tarCls;
                } else {
                    exp[i] = catSize;//NOT Belongs to the any class
                }
                if (l < 2) {
                    obt[i] = predCls;
                } else {
                    obt[i] = catSize;//NOT Belongs to the any class
                }
                //System.out.println(exp[i]+"  - "+obt[i]);
            }//go next pat

            int[][] confusionMat = new int[catSize][catSize];
            int[] tp = new int[catSize];
            int[] tn = new int[catSize];
            int[] fp = new int[catSize];
            int[] fn = new int[catSize];
            double[] accuracy = new double[catSize];
            double[] errorRate = new double[catSize];
            double[] precision = new double[catSize];
            double[] recall = new double[catSize];
            //initializing confusion matrix
            for (int i = 0; i < catSize; i++) {
                for (int j = 0; j < catSize; j++) {
                    confusionMat[i][j] = 0;
                }
            }
            //finding values for the confusion matrix  
            for (int i = 0; i < target.length; i++) {//for all samples
                if ((exp[i] < catSize) && (obt[i] < catSize)) {
                    confusionMat[exp[i]][obt[i]] = confusionMat[exp[i]][obt[i]] + 1;
                }
            }// fpr all samples

            //System.out.println("\n\nClassification Statistics :");
            int Mii = 0, Mji = 0, Mij = 0;
            int[] sumRows = new int[catSize];
            int[] sumCols = new int[catSize];
            for (int i = 0; i < catSize; i++) {
                tp[i] = 0;
                tn[i] = 0;
                sumRows[i] = 0;
                sumCols[i] = 0;
            }
            int sum = 0;

            for (int i = 0; i < catSize; i++) {
                Mii = confusionMat[i][i];
                tp[i] = Mii; //actual cats that were correctly classified as cats
                Mji = 0;
                Mij = 0;
                for (int j = 0; j < catSize; j++) {
                    sumRows[i] = sumRows[i] + confusionMat[i][j];
                    sumCols[i] = sumCols[i] + confusionMat[j][i];
                    //System.out.print(" " + confusionMat[i][j]);
                    Mji = Mji + confusionMat[j][i];//Redundent calculation of precision and recall
                    Mij = Mij + confusionMat[i][j];//Redundent calculation of precision and recall
                    if (i != j) {
                        fn[i] = fn[i] + confusionMat[i][j];//cats that were incorrectly marked as all the remaining animals
                        fp[i] = fp[i] + confusionMat[j][i];//all the remaining animals that were incorrectly labeled as cats
                    }
                    sum = sum + confusionMat[i][j];
                }
                //precision[i] = (double)Mii/(double)Mji;
                //recall[i] = (double)Mii/(double)Mij;			
            }//end for catSize
            //Printing confusion matrix
            String space = printConfusionMatrix(confusionMat, sum, sumRows, sumCols, catSize);
            //System.out.format("\n");
            matriPrint = matriPrint + String.format("\n");
            for (int i = 0; i < catSize; i++) {
                tn[i] = sum - (tp[i] + fn[i] + fp[i]);//all the remaining animals,correctly classified as non-cats
                //System.out.format("class " + i);
                matriPrint = matriPrint + String.format("class " + i);
                //System.out.format(": true positive " + space, tp[i]);
                //System.out.format(", false negative " + space, fn[i]);
                //System.out.format(", false positive " + space, fp[i]);
                //System.out.format(", true negative " + space, tn[i]);
                matriPrint = matriPrint + String.format(": true positive " + space, tp[i]);
                matriPrint = matriPrint + String.format(", false negative " + space, fn[i]);
                matriPrint = matriPrint + String.format(", false positive " + space, fp[i]);
                matriPrint = matriPrint + String.format(", true negative " + space, tn[i]);
                accuracy[i] = ((tp[i] + fn[i] + fp[i] + tn[i]) == 0.0) ? 0.0 : (double) ((tp[i] + tn[i]) / ((double) (tp[i] + fn[i] + fp[i] + tn[i])));
                errorRate[i] = ((tp[i] + fn[i] + fp[i] + tn[i]) == 0.0) ? 0.0 : (double) ((fp[i] + fn[i]) / ((double) (tp[i] + fn[i] + fp[i] + tn[i])));
                precision[i] = ((tp[i] + fp[i]) == 0.0) ? 0.0 : (double) (tp[i] / ((double) (tp[i] + fp[i])));
                recall[i] = ((tp[i] + fn[i]) == 0.0) ? 0.0 : (double) (tp[i] / ((double) (tp[i] + fn[i])));
                //System.out.printf("\n       : accuracy: %.5f  error rate: %.5f  Precision: %.5f Recall: %.5f\n", accuracy[i], errorRate[i], precision[i], recall[i]);
                matriPrint = matriPrint + String.format("\n       : accuracy: %.5f  error rate: %.5f  Precision: %.5f Recall: %.5f\n", accuracy[i], errorRate[i], precision[i], recall[i]);
            }//for
            double acc = 0.0, err = 0.0, prec = 0.0, rec = 0.0;
            for (int i = 0; i < catSize; i++) {
                acc = acc + accuracy[i];
                err = err + errorRate[i];
                prec = prec + precision[i];
                rec = rec + recall[i];
            }//for
            acc = acc / (double) catSize;
            err = err / (double) catSize;
            prec = prec / (double) catSize;
            rec = rec / (double) catSize;
            //System.out.printf("\nTotal : accuracy: %.5f  error rate: %.5f  Precision: %.5f Recall: %.5f", acc, err, prec, rec);
            matriPrint = matriPrint + String.format("\nTotal : accuracy: %.5f  error rate: %.5f  Precision: %.5f Recall: %.5f", acc, err, prec, rec);
            statistics[0] = acc;
            statistics[1] = err;
            statistics[2] = prec;
            statistics[3] = rec;
        } catch (Exception e) {
        }
        return statistics;
    }//end confusion matrix 

    private String printConfusionMatrix(int[][] confusionMat, int sum, int[] sumRows, int[] sumCols, int catSize) {
        //calculating digiot to print
        matriPrint= "";
        int n = sum, count = 0;
        while (n != 0) {
            n /= 10; /* n=n/10 */

            ++count;
        }// while
        String space = "%" + count + "d  ";
        for (int i = 0; i < catSize; i++) {
            for (int j = 0; j < catSize; j++) {
                //System.out.format(space, confusionMat[i][j]);
                matriPrint = matriPrint + String.format(space, confusionMat[i][j]);
                if (j == catSize - 1) {
                    //System.out.format("| " + space, sumRows[i]);
                    matriPrint = matriPrint + String.format("| " + space, sumRows[i]);
                }
            }
            //System.out.println();
           matriPrint = matriPrint + String.format("\n");
        }
        for (int j = 0; j < catSize + 1; j++) {
            for (int k = 0; k < count; k++) {
                //System.out.print("-");
                matriPrint = matriPrint + String.format("-");
            }
            //System.out.print("--");
            matriPrint = matriPrint + String.format("--");
        }
        //System.out.println();
        matriPrint = matriPrint + String.format("\n");
        for (int j = 0; j < catSize; j++) {
            //System.out.format(space, sumCols[j]);
            matriPrint = matriPrint + String.format(space, sumCols[j]);
            if (j == catSize - 1) {
                //System.out.format("| " + space, sum);
                matriPrint = matriPrint + String.format("| " + space, sum);
            }
        }
        //System.out.println();
        matriPrint = matriPrint + String.format("\n");      
        return space;
    }//print

    private double[] statisticsReg(String fileName, int indexPointer) {
        indexPointer = indexPointer * 2;//2 for two columns for each output
        //System.out.println("Readinmg line from the poiner index" + indexPointer);
        double[] statistics = new double[4];
        double[] x;//target
        double[] y;//predicted
        int length = 0;
        try {
            String line;
            try (FileReader fin = new FileReader(fileName)) {
                BufferedReader br = new BufferedReader(fin);
                length = 0;
                while ((line = br.readLine()) != null) {
                    length++;
                }
                //System.out.println("\n Length"+length);
                x = new double[length];
                y = new double[length];
                br.close();
                fin.close();
            }
            try (FileReader fin1 = new FileReader(fileName)) {
                BufferedReader br1 = new BufferedReader(fin1);
                int i = 0;
                while ((line = br1.readLine()) != null) {
                    String[] tokens = line.split(",");
                    x[i] = Double.parseDouble(tokens[indexPointer]);//target is in x
                    y[i] = Double.parseDouble(tokens[indexPointer + 1]);//predicted in y
                    //System.out.println(x[i]+","+y[i]);
                    i++;//increament i for next row                   
                }
                br1.close();
                fin1.close();
            }

            double SumX = 0.0, SumY = 0.0;
            for (int i = 0; i < length; i++) {
                SumX = SumX + x[i];
                SumY = SumY + y[i];
            }
            double meanX, meanY;
            meanX = SumX / length;
            meanY = SumY / length;
            //variables for RMSE and R2 
            double SSE = 0.0;
            // variabls for correlation       
            double a[] = new double[length];
            double b[] = new double[length];
            double aXb[] = new double[length];

            double SumaXb = 0.0;
            double Suma2 = 0.0, Sumb2 = 0.0;
            for (int i = 0; i < length; i++) {
                //RMSE and R2
                SSE = SSE + (x[i] - y[i]) * (x[i] - y[i]);
                //correalation
                a[i] = x[i] - meanX;
                b[i] = y[i] - meanY;
                aXb[i] = a[i] * b[i];
                SumaXb = SumaXb + aXb[i];
                Suma2 = Suma2 + (a[i] * a[i]);
                Sumb2 = Sumb2 + (b[i] * b[i]);
            }
            //double MSE = (SSE / (2.0* length)); //Enable this for regrassion comparison
            double MSE = (SSE / (double) length); 
            double RMSE = Math.sqrt(MSE);
            //R2 = 1.0 - SSE / SST;            
            double correlation = SumaXb / (Math.sqrt(Suma2 * Sumb2));
            double R2 = correlation * correlation;
            double p = 1;//'1' for one predictor
            double R2adj = 1.0 - (((1.0 - R2) * (length - 1)) / (length - p - 1));
            statistics[0] = RMSE;//MSE;
            statistics[1] = correlation;
            statistics[2] = R2;
            statistics[3] = R2adj;
            //System.out.println("Readinmg done RMSE: " + RMSE);//RMSE or MSE
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Operation failed! Adivise: Close the open excel files!");
            System.out.print(e);
            return null;
        }
        return statistics;
    }//regression staticstics
    
    
    private double[] statisticsCmplxClass(String fileName, int indexPointer) {
        matriPrint= "No Matrix to print";
        indexPointer = indexPointer * 2;//2 for two columns for each output
        System.out.println("Readinmg line from the poiner index:" + indexPointer);
        double[] statistics = new double[2];
        double[] x;//target
        double[] y;//predicted
        int length = 0;
        try {
            String line;
            try (FileReader fin = new FileReader(fileName)) {
                BufferedReader br = new BufferedReader(fin);
                length = 0;
                while ((line = br.readLine()) != null) {
                    length++;
                }
                //System.out.println("\n Length"+length);
                x = new double[length];
                y = new double[length];
                br.close();
                fin.close();
            }
            try (FileReader fin1 = new FileReader(fileName)) {
                BufferedReader br1 = new BufferedReader(fin1);
                int i = 0;
                while ((line = br1.readLine()) != null) {
                    String[] tokens = line.split(",");
                    x[i] = Double.parseDouble(tokens[indexPointer]);//target is in x
                    y[i] = Double.parseDouble(tokens[indexPointer + 1]);//predicted in y
                    //System.out.println(x[i]+","+y[i]);
                    i++;//increament i for next row                   
                }
                br1.close();
                fin1.close();
            }

            double count = 0.0;
            for (int i = 0; i < length; i++) {
                if(x[i] == y[i]){
                    count++;
                }
            }
            double errorRate, acuracy;
            
            acuracy = (count / length)*100 ;
            errorRate = (100 - acuracy);
            statistics[0] = acuracy;
            statistics[1] = errorRate;
            System.out.println("Readinmg done Accuray: " + acuracy+"%");//accuracy
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Operation failed! Adivise: Close the open excel files!");
            System.out.print(e);
            return null;
        }
        return statistics;
    }//regression staticstics
}//end statistics
