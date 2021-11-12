/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataReader;

import Randoms.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Varun Kumar Ojha
 */
public class ReadCVFiles {

    public Pattern[] readDataFile(ArrayList dataFileInfo, int dataRerivalSize, MersenneTwisterFast RNG) {
        //public  Pattern[] readDataFile(String fileName, int In, int Ot, boolean needSufuling, MersenneTwisterFast getRandom, int dataRerivalSize,int numPatterns,boolean isClassification) {
        String fileName = (String) dataFileInfo.get(0);//file location and name
        int In = (int) dataFileInfo.get(1);//total input columns
        int Ot = (int) dataFileInfo.get(2);//total output coulumn
        int totalPat = (int) dataFileInfo.get(3);//total length of original dataset
        boolean isClassification = (boolean) dataFileInfo.get(4);//propble type
        double normalizedLow = (double) dataFileInfo.get(5);// normalization min
        double normalizedHigh = (double) dataFileInfo.get(6);// normalization max
        double[] inputsMin = (double[]) dataFileInfo.get(7);// input min vector
        double[] inputsMax = (double[]) dataFileInfo.get(8);// input max vector
        double[] targetMin = (double[]) dataFileInfo.get(9);// target min vector
        double[] targetMax = (double[]) dataFileInfo.get(10);// target max vector
        boolean needSufuling = (boolean) dataFileInfo.get(11);// need suffuling

        MersenneTwisterFast random = RNG;
        //System.out.println("Pattern Length:" + totalPat+"  "+ fileName);
        Pattern[] patRandom = null;
        try {
            int length;
            FileReader fin = new FileReader(fileName);//readt output Train to make ensemble
            BufferedReader br = new BufferedReader(fin);
            String line;
            length = 0;
            while ((line = br.readLine()) != null) {
                length++;
            }
            br.close();
            fin.close();
            totalPat = length;

            FileReader fin1 = new FileReader(fileName);//readt output Train to make ensemble
            BufferedReader br1 = new BufferedReader(fin1);
            Pattern[] pat = new Pattern[totalPat];

            int i, j;//loop variables
            double InP[];//input pattern
            double TrP[];//target pattern
            if (isClassification) {//run problem for classification
                for (i = 0; i < totalPat; i++) {
                    line = br1.readLine();
                    String[] tokens = line.split(",");
                    InP = new double[In];
                    TrP = new double[Ot];
                    //System.out.printf("%d  :  ",i);
                    for (j = 0; j < In; j++) {
                        double valX = Double.parseDouble(tokens[j]);
                        InP[j] = normalize(valX, inputsMin[j], inputsMax[j], normalizedLow, normalizedHigh);
                        //System.out.printf(" %1.2f ", InP[j]);
                    }
                    for (j = 0; j < Ot; j++) {
                        int cat = (int) Double.parseDouble(tokens[In]);//this is ok becuase of single class column
                        if (j == cat) {
                            TrP[j] = 1.0;
                        } else {
                            TrP[j] = 0.0;
                        }
                        //System.out.printf("%d ", (int) TrP[j]);
                    }
                    //System.out.println("");
                    pat[i] = new Pattern(InP, TrP);
                    //if(i < trainPat){patTrain[i] = new Pattern(InP,TrP);}   
                    //else{patTest[i - trainPat] = new Pattern(InP,TrP);}   
                }//end totalPat 
            }//end classification
            else {//run problem for regression
                for (i = 0; i < totalPat; i++) {
                    line = br1.readLine();
                    String[] tokens = line.split(",");
                    InP = new double[In];
                    TrP = new double[Ot];
                    //System.out.printf("%d  :  ",i);
                    for (j = 0; j < In; j++) {
                        double valX = Double.parseDouble(tokens[j]);
                        InP[j] = normalize(valX, inputsMin[j], inputsMax[j], normalizedLow, normalizedHigh);
                        //System.out.println("Test Range Inputs: "+inputsMin[j]+" -"+inputsMax[j]);
                        //System.out.printf(" %1.4f ",InP[j]);
                    }
                    for (j = 0; j < Ot; j++) {
                        double valY = Double.parseDouble(tokens[In + j]);//this is ok to increment
                        TrP[j] = normalize(valY, targetMin[j], targetMax[j], normalizedLow, normalizedHigh);
                        //System.out.println("Test Range Target: "+targetMin[j]+" -"+targetMax[j]);
                        //System.out.printf(" : %1.4f ", TrP[j]);
                    }
                    pat[i] = new Pattern(InP, TrP);
                    //System.out.println("");
                }//end totalPat 
            }//end regression
            //clean-up
            br1.close();
            fin1.close();
            //System.out.println("Printed Pattern Length" + totalPat);
            totalPat = (int) (totalPat * (dataRerivalSize / 100.0f));
            patRandom = new Pattern[totalPat];
            if (needSufuling) {
                int[] ramdomVector = random.randomIntVector(0, totalPat);
                for (i = 0; i < totalPat; i++) {
                    patRandom[i] = pat[ramdomVector[i]];
                    //System.out.println(i + " - " + ramdomVector[i]);
                }
                //System.out.println(" Data patterns were randomized for training and testing:" + pat.length);
                //ComplitionReport = ComplitionReport + ("\n Data patterns are randomized for training and testing");
            } else {
                System.arraycopy(pat, 0, patRandom, 0, totalPat); //System.out.println(i + " - " + ramdomVector[i]);
            }
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Opration faild! Advise: Close all opened data files!");
            System.out.print("Error file Train Test printing:" + e);
            return null;
        }
        return patRandom;
    }//retirve patterns

    public double normalize(double x, double dataLow, double dataHigh, double normalizedLow, double normalizedHigh) {
        return ((x - dataLow) / (dataHigh - dataLow)) * (normalizedHigh - normalizedLow) + normalizedLow;
    }
}
