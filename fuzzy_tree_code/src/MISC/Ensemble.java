/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MISC;

import AdditionalFrames.InitiatorFrame;
import Randoms.MersenneTwisterFast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Varun Ojha
 */
public class Ensemble {

    private  int Ot;
    private  int ensembleCandidates;
    private  String ensembleMethod;
    private  String ensembleMHalgo;
    private  int ensemblMaxIterations;
    private  int ensemblPop;
    private  String ensembleDiversity;
    private MersenneTwisterFast m_random;
    private ArrayList algoParms;
    private double ensembleWTMin;
    private double ensembleWTMax;

    public  void setEnsembleFunction(MersenneTwisterFast random,int Ot,ArrayList ensembleParameters, ArrayList ensembleAlgoParms) {
        m_random =  random;
        this.Ot = Ot;
        ensembleCandidates= (int) ensembleParameters.get(0);// int number of candidates for ensemble
        ensembleDiversity = (String) ensembleParameters.get(1);//String ensemble diversity type
        ensembleMethod = (String) ensembleParameters.get(2);//String ensemble method
        ensembleMHalgo = (String) ensembleParameters.get(3);//String ensemble algorithm
        ensemblMaxIterations = (int) ensembleParameters.get(4);//int ensemble iteration
        ensemblPop = (int) ensembleParameters.get(5);//int ensemble population
        ensembleWTMin = (double)ensembleParameters.get(6);//double ensemble min wt
        ensembleWTMax = (double) ensembleParameters.get(7);//double ensemble max wt
        algoParms = ensembleAlgoParms;
        String parms = "";
            for (Object algoParms1 : algoParms) {
                parms = parms + algoParms1 + ",";
            }
            System.out.println("EN also:"+parms);
    }

    public  double[][] ensambleReg(double[][] model_FITNESS) {
        //Finding Ensamble results
        double[][] ensemble_Weights = new double[Ot][ensembleCandidates];
        //Computing weights for other mensmble combiner methods
        System.out.print("Ensamble of " + ensembleCandidates + " candidates :");
        switch (ensembleMethod) {
            case "Weighted_M":
                for (int j = 0; j < Ot; j++) {
                    ensemble_Weights[j] = weightedMeanComputation(model_FITNESS[j], ensembleCandidates);
                }
                break;
            case "Evolutionary_WM":
                for (int j = 0; j < Ot; j++) {
                    ensemble_Weights[j] = evolutionaryWeightedComputation(ensembleCandidates, j, false);
                    //System.arraycopy(evolutanaryWeights, 0, ensemble_Weights[j], 0, ensembleCandidates);
                }
                break;
            default:
                for (int j = 0; j < Ot; j++) {
                    for (int k = 0; k < ensembleCandidates; k++) {
                        ensemble_Weights[j][k] = 1.0;//wight 1.0 for mean output regressor
                    }//k models
                }//j outputs
                break;
        }//end switch 
        //System.out.println("\nComputing ensemble result:2" + ensembleMethod);
        //double trainMSE = 10e10;

        try {
            FileWriter fwEnsembl = new FileWriter(InitiatorFrame.absoluteFilePathOut + "ensembleTrain.csv");
            BufferedWriter bwEnsembl = new BufferedWriter(fwEnsembl);
            BufferedReader br;
            //int length;
            //double error;
            try (PrintWriter fileEnsembl = new PrintWriter(bwEnsembl)) {
                FileReader fin = new FileReader(InitiatorFrame.absoluteFilePathOut + "outputTrain.csv");//readt output Train to make ensemble
                br = new BufferedReader(fin);
                String line;
                //length = 0;
                //error = 0.0;
                while ((line = br.readLine()) != null) {
                    int indexPinter = 0;//index pointer for line read
                    String[] tokens = line.split(",");
                    for (int j = 0; j < Ot; j++) {
                        double target = Double.parseDouble(tokens[indexPinter]);
                        indexPinter++;//read next double in line/token
                        double ensemble = 0.0;
                        double[] models = new double[ensembleCandidates];
                        for (int k = 0; k < ensembleCandidates; k++) {
                            models[k] = Double.parseDouble(tokens[indexPinter]);
                            indexPinter++;//read next double in line/token
                            ensemble = ensemble + ensemble_Weights[j][k] * models[k];
                        }//k models
                        if (ensembleMethod.equals("Mean")) {
                            ensemble = ensemble / ensembleCandidates;
                        }
                        fileEnsembl.print(target + ",");
                        fileEnsembl.print(ensemble + ",");
                    }//j outputs
                    fileEnsembl.println();//go to read next line
                }
                fileEnsembl.close();
            } //read output Train to make ensemble
            br.close();
        } catch (IOException | NumberFormatException e) {
            System.out.print("Error file Train Test printing:" + e);
        }
        //test set
        try {
            FileWriter fwEnsembl = new FileWriter(InitiatorFrame.absoluteFilePathOut + "ensembleTest.csv");
            BufferedWriter bwEnsembl = new BufferedWriter(fwEnsembl);
            BufferedReader br;
            //int length;
            //double error;
            try (PrintWriter fileEnsembl = new PrintWriter(bwEnsembl)) {
                FileReader fin = new FileReader(InitiatorFrame.absoluteFilePathOut + "outputTest.csv");//readt output Train to make ensemble
                br = new BufferedReader(fin);
                String line;
                //length = 0;
                //error = 0.0;
                while ((line = br.readLine()) != null) {
                    int indexPinter = 0;//index pointer for line read
                    String[] tokens = line.split(",");
                    for (int j = 0; j < Ot; j++) {
                        double target = Double.parseDouble(tokens[indexPinter]);
                        indexPinter++;//read next double in line/token
                        double ensemble = 0.0;
                        double[] models = new double[ensembleCandidates];
                        for (int k = 0; k < ensembleCandidates; k++) {
                            models[k] = Double.parseDouble(tokens[indexPinter]);
                            indexPinter++;//read next double in line/token
                            ensemble = ensemble + ensemble_Weights[j][k] * models[k];
                        }//k models
                        if (ensembleMethod.equals("Mean")) {
                            ensemble = ensemble / ensembleCandidates;
                        }
                        fileEnsembl.print(target + ",");
                        fileEnsembl.print(ensemble + ",");
                    }//j outputs
                    fileEnsembl.println();//go to read next line
                    //error = error + 0.5 * (target - ensemble) * (target - ensemble);
                    //length++;
                }
                fileEnsembl.close();
            } //readt output Train to make ensemble
            br.close();
            //testedResult = (double) (error / (double) length);
            //testedResult = Math.sqrt(testedResult);

        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Operation failed! Adivise: Close the open excel files!");
            System.out.print(e);
            return null;
        }
        //disposing variable: 
        System.out.print("Ensamble Finshed ");
        return ensemble_Weights;
    }//end ensemble

    private  double[] weightedMeanComputation(double[] fintess, int num_predictors) {
        double weights[] = new double[num_predictors];
        for (int i = 0; i < num_predictors; i++) {
            weights[i] = fintess[i] + 0.0000000001;
        }

        double sum = 0.0;
        for (int k = 0; k < num_predictors; k++) {
            sum = sum + weights[k];
        }
        //System.out.printf(" = %.3f \n",sum); 
        for (int k = 0; k < num_predictors; k++) {
            weights[k] = (float) sum / weights[k];
            //System.out.printf(" %.3f ",weights[k]);
        }
        //System.out.println();
        sum = 0.0;
        for (int k = 0; k < num_predictors; k++) {
            sum = sum + weights[k];
        }
        for (int k = 0; k < num_predictors; k++) {
            weights[k] = weights[k] / (float) sum;
            //System.out.printf(" %.3f ",x[k]);
        }
        return weights;
    }

    private  double[] evolutionaryWeightedComputation(int num_predictors, int j, boolean isClassification) {
        double weights[] = new double[num_predictors];
        System.out.println("Finding the weights using MH " + ensembleMHalgo);
        TrainingModuleFIS algorithm = new TrainingModuleFIS();
        int index = num_predictors * j + j;
        ArrayList array = new ArrayList();
        array.add(0,ensembleMHalgo); //String algorithm
        array.add(1,num_predictors);//int mh dimesion 
        array.add(2,ensemblPop); //int mh population 
        array.add(3,ensemblMaxIterations); //int iteration
        double[][] mhParmsRange = new double[num_predictors][2];
        for(int i=0;i<num_predictors;i++){
            mhParmsRange[i][0] = ensembleWTMin;
            mhParmsRange[i][1] = ensembleWTMax;
        }
        array.add(4,mhParmsRange); //double[][] mh range min max
        //boolean weightsOnly = (boolean) mhGetParms.get(8); //int iteration
        weights = algorithm.MHAlgorithmsReturn(m_random,index, isClassification, array, algoParms, num_predictors, ensembleDiversity);
        System.out.print("\n Ensemble Weights:");
        //for (int i = 0; i < weights.length; i++) {
        // System.out.printf(" " + weights[i]);
        // }
        return weights;
    }//evolutionary weights

    public  double[][] ensambleClass(double[][] model_FITNESS) {
        //Finding Ensamble results
        double[][] ensemble_Weights = new double[Ot][ensembleCandidates];
        //Computing weights for other mensmble combiner methods
        System.out.print("Ensamble of " + ensembleCandidates + " candidates :");
        switch (ensembleMethod) {
            case "Weighted_MV":
                for (int j = 0; j < Ot; j++) {
                    ensemble_Weights[j] = weightedMeanComputation(model_FITNESS[j], ensembleCandidates);
                }
                break;
            case "Evolutionary_WMV":
                for (int j = 0; j < Ot; j++) {
                    ensemble_Weights[j] = evolutionaryWeightedComputation(ensembleCandidates, j, true);
                    for (int k = 0; k < ensembleCandidates; k++) {
                        System.out.print(" " + ensemble_Weights[j][k]);
                    }
                    System.out.println();
                }
                break;
            default:
                for (int j = 0; j < Ot; j++) {
                    for (int k = 0; k < ensembleCandidates; k++) {
                        ensemble_Weights[j][k] = 1.0;//wight 1.0 for mean output regressor
                    }//k models
                }//j outputs
                break;
        }//end switch 
        //System.out.println("\nComputing ensemble result:2" + ensembleMethod);
        //double trainMSE = 10e10;

        try {
            int MajorityMark = 0;
            if (ensembleCandidates % 2 == 0) {//even
                MajorityMark = ensembleCandidates / 2 + 1;
            } else {//odd
                MajorityMark = (ensembleCandidates + 1) / 2;
            }//if

            FileWriter fwEnsembl = new FileWriter(InitiatorFrame.absoluteFilePathOut + "ensembleTrain.csv");
            BufferedWriter bwEnsembl = new BufferedWriter(fwEnsembl);
            BufferedReader br;
            //int length;
            //double error;
            try (PrintWriter fileEnsembl = new PrintWriter(bwEnsembl)) {
                FileReader fin = new FileReader(InitiatorFrame.absoluteFilePathOut + "outputTrain.csv");//readt output Train to make ensemble
                br = new BufferedReader(fin);
                String line;
                //length = 0;
                //error = 0.0;
                while ((line = br.readLine()) != null) {
                    int indexPinter = 0;//index pointer for line read
                    String[] tokens = line.split(",");
                    for (int j = 0; j < Ot; j++) {
                        double target = Double.parseDouble(tokens[indexPinter]);
                        indexPinter++;//read next double in line/token
                        double[] models = new double[ensembleCandidates];
                        double countONE = 0.0;
                        double countZERO = 0.0;
                        for (int k = 0; k < ensembleCandidates; k++) {
                            models[k] = Double.parseDouble(tokens[indexPinter]);
                            indexPinter++;//read next double in line/token
                            if (models[k] == 1.0) {//ONE
                                countONE = countONE + ensemble_Weights[j][k] * 1.0;
                            } else {//ZERO
                                countZERO = countZERO + ensemble_Weights[j][k] * 1.0;
                            }//if
                        }//k models
                        double ensemble = (countONE < countZERO) ? 0.0 : 1.0;//take the max
                        fileEnsembl.print(target + ",");
                        fileEnsembl.print(ensemble + ",");
                    }//j outputs
                    fileEnsembl.println();//go to read next line
                }
                fileEnsembl.close();
            } //read output Train to make ensemble
            br.close();
        } catch (IOException | NumberFormatException e) {
            System.out.print("Error file Train Test printing:" + e);
        }
        //test set
        try {
            FileWriter fwEnsembl = new FileWriter(InitiatorFrame.absoluteFilePathOut + "ensembleTest.csv");
            BufferedWriter bwEnsembl = new BufferedWriter(fwEnsembl);
            BufferedReader br;
            //int length;
            //double error;
            try (PrintWriter fileEnsembl = new PrintWriter(bwEnsembl)) {
                FileReader fin = new FileReader(InitiatorFrame.absoluteFilePathOut + "outputTest.csv");//readt output Train to make ensemble
                br = new BufferedReader(fin);
                String line;
                //length = 0;
                //error = 0.0;
                while ((line = br.readLine()) != null) {
                    int indexPinter = 0;//index pointer for line read
                    String[] tokens = line.split(",");
                    for (int j = 0; j < Ot; j++) {
                        double target = Double.parseDouble(tokens[indexPinter]);
                        indexPinter++;//read next double in line/token
                        double[] models = new double[ensembleCandidates];
                        double countONE = 0.0;
                        double countZERO = 0.0;
                        for (int k = 0; k < ensembleCandidates; k++) {
                            models[k] = Double.parseDouble(tokens[indexPinter]);
                            indexPinter++;//read next double in line/token
                            if (models[k] == 1.0) {//ONE
                                countONE = countONE + ensemble_Weights[j][k] * 1.0;
                            } else {//ZERO
                                countZERO = countZERO + ensemble_Weights[j][k] * 1.0;
                            }//if
                        }//k models
                        double ensemble = (countONE < countZERO) ? 0.0 : 1.0;
                        fileEnsembl.print(target + ",");
                        fileEnsembl.print(ensemble + ",");
                    }//j outputs
                    fileEnsembl.println();//go to read next line
                    //error = error + 0.5 * (target - ensemble) * (target - ensemble);
                    //length++;
                }
                fileEnsembl.close();
            } //readt output Train to make ensemble
            br.close();
            //testedResult = (double) (error / (double) length);
            //testedResult = Math.sqrt(testedResult);

        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Operation failed! Adivise: Close the open excel files!");
            System.out.print(e);
            return null;
        }
        //disposing variable: 
        //System.out.print("Ensamble done with ");
        return ensemble_Weights;
    }

    public  void ensambleRegTest(String filenPath, double[][] ensemble_Weights, int ensembleNum,String ensembleMethodOld,int outColumn) {
        try {
            FileWriter fwEnsembl = new FileWriter(filenPath + "ensembleTestOld.csv");
            BufferedWriter bwEnsembl = new BufferedWriter(fwEnsembl);
            BufferedReader br;
            //int length;
            //double error;
            try (PrintWriter fileEnsembl = new PrintWriter(bwEnsembl)) {
                FileReader fin = new FileReader(filenPath + "outputTestOld.csv");//readt output Train to make ensemble
                br = new BufferedReader(fin);
                String line;
                //length = 0;
                //error = 0.0;
                while ((line = br.readLine()) != null) {
                    int indexPinter = 0;//index pointer for line read
                    String[] tokens = line.split(",");
                    for (int j = 0; j < outColumn; j++) {
                        double target = Double.parseDouble(tokens[indexPinter]);
                        indexPinter++;//read next double in line/token
                        double ensemble = 0.0;
                        double[] models = new double[ensembleNum];
                        for (int k = 0; k < ensembleNum; k++) {
                            models[k] = Double.parseDouble(tokens[indexPinter]);
                            indexPinter++;//read next double in line/token
                            ensemble = ensemble + ensemble_Weights[j][k] * models[k];
                        }//k models
                        if (ensembleMethodOld.equals("Mean")) {
                            ensemble = ensemble / ensembleNum;
                        }
                        fileEnsembl.print(target + ",");
                        fileEnsembl.print(ensemble + ",");
                    }//j outputs
                    fileEnsembl.println();//go to read next line
                }
                fileEnsembl.close();
            } //read output Train to make ensemble
            br.close();
        } catch (IOException | NumberFormatException e) {
            System.out.print("Error file Train Test printing:" + e);
        }
    }//ensembl Regression Old Test
   
    public  void ensambleClassTest(String filenPath, double[][] ensemble_Weights, int ensembleNum,String ensembleMethodOld,int outColumn) {
        //test set
        try {
            FileWriter fwEnsembl = new FileWriter(filenPath + "ensembleTestOld.csv");
            BufferedWriter bwEnsembl = new BufferedWriter(fwEnsembl);
            BufferedReader br;
            //int length;
            //double error;
            try (PrintWriter fileEnsembl = new PrintWriter(bwEnsembl)) {
                FileReader fin = new FileReader(filenPath + "outputTestOld.csv");//readt output Train to make ensemble
                br = new BufferedReader(fin);
                String line;
                //length = 0;
                //error = 0.0;
                while ((line = br.readLine()) != null) {
                    int indexPinter = 0;//index pointer for line read
                    String[] tokens = line.split(",");
                    for (int j = 0; j < outColumn; j++) {
                        double target = Double.parseDouble(tokens[indexPinter]);
                        indexPinter++;//read next double in line/token
                        double[] models = new double[ensembleNum];
                        double countONE = 0.0;
                        double countZERO = 0.0;
                        for (int k = 0; k < ensembleNum; k++) {
                            models[k] = Double.parseDouble(tokens[indexPinter]);
                            indexPinter++;//read next double in line/token
                            if (models[k] == 1.0) {//ONE
                                countONE = ensemble_Weights[j][k] * 1.0;
                            } else {//ZERO
                                countZERO = ensemble_Weights[j][k] * 1.0;
                            }//if
                        }//k models
                        double ensemble = (countONE < countZERO) ? 0.0 : 1.0;
                        fileEnsembl.print(target + ",");
                        fileEnsembl.print(ensemble + ",");
                    }//j outputs
                    fileEnsembl.println();//go to read next line
                    //error = error + 0.5 * (target - ensemble) * (target - ensemble);
                    //length++;
                }
                fileEnsembl.close();
            } //readt output Train to make ensemble
            br.close();
            //testedResult = (double) (error / (double) length);
            //testedResult = Math.sqrt(testedResult);

        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Operation failed! Adivise: Close the open excel files!");
            System.out.print(e);
        }
    }//ensembl classification Old Test

    public double[][] ensambleComplexClass(double[][] model_FITNESS) {
         //Finding Ensamble results
        double[][] ensemble_Weights = new double[Ot][ensembleCandidates];
        //Computing weights for other mensmble combiner methods
        System.out.print("Ensamble of " + ensembleCandidates + " candidates :");
        switch (ensembleMethod) {
            case "Weighted_M":
                for (int j = 0; j < Ot; j++) {
                    ensemble_Weights[j] = weightedMeanComputation(model_FITNESS[j], ensembleCandidates);
                }
                break;
            case "Evolutionary_WM":
                for (int j = 0; j < Ot; j++) {
                    ensemble_Weights[j] = evolutionaryWeightedComputation(ensembleCandidates, j, false);
                    //System.arraycopy(evolutanaryWeights, 0, ensemble_Weights[j], 0, ensembleCandidates);
                }
                break;
            default:
                for (int j = 0; j < Ot; j++) {
                    for (int k = 0; k < ensembleCandidates; k++) {
                        ensemble_Weights[j][k] = 1.0;//wight 1.0 for mean output regressor
                    }//k models
                }//j outputs
                break;
        }//end switch 
        //System.out.println("\nComputing ensemble result:2" + ensembleMethod);
        //double trainMSE = 10e10;

        try {
            FileWriter fwEnsembl = new FileWriter(InitiatorFrame.absoluteFilePathOut + "ensembleTrain.csv");
            BufferedWriter bwEnsembl = new BufferedWriter(fwEnsembl);
            BufferedReader br;
            //int length;
            //double error;
            try (PrintWriter fileEnsembl = new PrintWriter(bwEnsembl)) {
                FileReader fin = new FileReader(InitiatorFrame.absoluteFilePathOut + "outputTrain.csv");//readt output Train to make ensemble
                br = new BufferedReader(fin);
                String line;
                //length = 0;
                //error = 0.0;
                while ((line = br.readLine()) != null) {
                    int indexPinter = 0;//index pointer for line read
                    String[] tokens = line.split(",");
                    for (int j = 0; j < Ot; j++) {
                        double target = Double.parseDouble(tokens[indexPinter]);
                        indexPinter++;//read next double in line/token
                        double ensemble = 0.0;
                        double[] models = new double[ensembleCandidates];
                        for (int k = 0; k < ensembleCandidates; k++) {
                            models[k] = Double.parseDouble(tokens[indexPinter]);
                            indexPinter++;//read next double in line/token
                            ensemble = ensemble + ensemble_Weights[j][k] * models[k];
                        }//k models
                        if (ensembleMethod.equals("Mean")) {
                            ensemble = ensemble / ensembleCandidates;
                        }
                        fileEnsembl.print(target + ",");
                        fileEnsembl.print(ensemble + ",");
                    }//j outputs
                    fileEnsembl.println();//go to read next line
                }
                fileEnsembl.close();
            } //read output Train to make ensemble
            br.close();
        } catch (IOException | NumberFormatException e) {
            System.out.print("Error file Train Test printing:" + e);
        }
        //test set
        try {
            FileWriter fwEnsembl = new FileWriter(InitiatorFrame.absoluteFilePathOut + "ensembleTest.csv");
            BufferedWriter bwEnsembl = new BufferedWriter(fwEnsembl);
            BufferedReader br;
            //int length;
            //double error;
            try (PrintWriter fileEnsembl = new PrintWriter(bwEnsembl)) {
                FileReader fin = new FileReader(InitiatorFrame.absoluteFilePathOut + "outputTest.csv");//readt output Train to make ensemble
                br = new BufferedReader(fin);
                String line;
                //length = 0;
                //error = 0.0;
                while ((line = br.readLine()) != null) {
                    int indexPinter = 0;//index pointer for line read
                    String[] tokens = line.split(",");
                    for (int j = 0; j < Ot; j++) {
                        double target = Double.parseDouble(tokens[indexPinter]);
                        indexPinter++;//read next double in line/token
                        double ensemble = 0.0;
                        double[] models = new double[ensembleCandidates];
                        for (int k = 0; k < ensembleCandidates; k++) {
                            models[k] = Double.parseDouble(tokens[indexPinter]);
                            indexPinter++;//read next double in line/token
                            ensemble = ensemble + ensemble_Weights[j][k] * models[k];
                        }//k models
                        if (ensembleMethod.equals("Mean")) {
                            ensemble = ensemble / ensembleCandidates;
                        }
                        fileEnsembl.print(target + ",");
                        fileEnsembl.print(ensemble + ",");
                    }//j outputs
                    fileEnsembl.println();//go to read next line
                    //error = error + 0.5 * (target - ensemble) * (target - ensemble);
                    //length++;
                }
                fileEnsembl.close();
            } //readt output Train to make ensemble
            br.close();
            //testedResult = (double) (error / (double) length);
            //testedResult = Math.sqrt(testedResult);

        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Operation failed! Adivise: Close the open excel files!");
            System.out.print(e);
            return null;
        }
        //disposing variable: 
        System.out.print("Ensamble Finshed ");
        return ensemble_Weights;
    }

}//end ensemble
