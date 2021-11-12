/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MISC;

import FIS.*;
import Randoms.MersenneTwisterFast;
import DataReader.Pattern;
import DataReader.ReadCVFiles;
import DataReader.ReadDataFromFile;
import AdditionalFrames.InitiatorFrame;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Varun Ojha
 */
public class CrossValidationFIS {

    MersenneTwisterFast m_RNG;
    private FuzzyFNT[][] bestGlobalTree;

    private String FoldType;
    private int manualPat;
    public double[][] meanTrain;
    private String saveDataPattern;
    private int CV_File_num;
    private String CV_File_path;
    
    private ArrayList mhParameters;
    private ArrayList aldoParameters;
    
    public String modelStatisticCV = "";
    private ArrayList treeParameters;
    private int CV_k_Number;

    public void setCorssValidation(FuzzyFNT[][] bestGlobalTree, MersenneTwisterFast random, ArrayList treeParams, ArrayList cvParameter, ArrayList mhParams, ArrayList algoParams) {
        this.bestGlobalTree = bestGlobalTree;
        this.m_RNG = random;
        treeParameters = treeParams;
        
        FoldType = (String) cvParameter.get(0);//String fold type
        manualPat = (int) cvParameter.get(1);//int manual Partition Size
        saveDataPattern = (String) cvParameter.get(2);//String fold type
        CV_File_num = (int) cvParameter.get(3);//int external file name
        CV_File_path = (String) cvParameter.get(4);//String external file path
        CV_k_Number = (int) cvParameter.get(5);//int k of k_Fold

        //m_MHAlgoParamSetting
        mhParameters = mhParams;
        aldoParameters = algoParams;
    }

    public FuzzyFNT[][] crossValidation(ArrayList dataFileInfo, int ensemble_Candidates, String m_Ensemble_Diversity) {
        //String problemDataFile = (String) dataFileInfo.get(0);//file location and name
        //int In = (int) dataFileInfo.get(1);//total input columns
        int Ot = (int) dataFileInfo.get(2);//total output coulumn
        int numPatterns = (int) dataFileInfo.get(3);//total length of original dataset
        //boolean isClassification = (boolean) dataFileInfo.get(4);//propble type
        double normalizedLow = (double) dataFileInfo.get(5);// normalization min
        double normalizedHigh = (double) dataFileInfo.get(6);// normalization max
        double[] inputsMin = (double[]) dataFileInfo.get(7);// input min vector
        double[] inputsMax = (double[]) dataFileInfo.get(8);// input max vector
        double[] targetMin = (double[]) dataFileInfo.get(9);// target min vector
        double[] targetMax = (double[]) dataFileInfo.get(10);// target max vector
        //boolean needSufuling = (boolean) dataFileInfo.get(11);// need suffuling
        boolean isaNormData = (boolean) dataFileInfo.get(12);// need suffuling

        String outputTrainFile = InitiatorFrame.absoluteFilePathOut + "outputTrain.csv";
        String outputTestFile = InitiatorFrame.absoluteFilePathOut + "outputTest.csv";

        String outputTrainFilePattern = InitiatorFrame.absoluteFilePathOut + "outputTrainPattern.csv";
        String outputTestFilePattern = InitiatorFrame.absoluteFilePathOut + "outputTestPattern.csv";

        int Fold = 10;//defult is 10 but it will change from method to method
        double[][][] trainError = new double[Ot][ensemble_Candidates][Fold];
        double[][][] testError = new double[Ot][ensemble_Candidates][Fold];
        double[][] sumTrain = new double[Ot][ensemble_Candidates];
        double[][] sumTest = new double[Ot][ensemble_Candidates];
        for (int j = 0; j < Ot; j++) {
            for (int k = 0; k < ensemble_Candidates; k++) {
                sumTrain[j][k] = 0.0000000000000000001;
                sumTest[j][k] = 0.00000000000000000001;
            }//k models
        }//j outputs
        int totalPat = numPatterns;//total patetrns

        //Printing training file  and  //De-normalization of regression output
        FileWriter fwTrn;
        FileWriter fwTst;
        BufferedWriter bwTrn;
        BufferedWriter bwTst;
        PrintWriter fileTrain;
        PrintWriter fileTest;

        FileWriter fwTrnPattern;
        FileWriter fwTstPattern;
        BufferedWriter bwTrnPattern;
        BufferedWriter bwTstPattern;
        PrintWriter fileTrainPattern;
        PrintWriter fileTestPattern;

        switch (FoldType) {
            case "k_Fold": {
                System.out.println(CV_k_Number+"-Fold Cross-Validation of Model(s))");
                //Randomize pattern once for 10 Fold CV
                //Pattern[] patRandom = ReadDataFromFile.readDataFile(problemDataFile, In, Ot, randomizeData, m_RNG, 100, numPatterns, isClassification);
                Pattern[] patRandom = ReadDataFromFile.readDataFile(dataFileInfo, 100, m_RNG);
                try {
                    fwTrnPattern = new FileWriter(outputTrainFilePattern);
                    fwTstPattern = new FileWriter(outputTestFilePattern);
                    bwTrnPattern = new BufferedWriter(fwTrnPattern);
                    bwTstPattern = new BufferedWriter(fwTstPattern);
                    fileTrainPattern = new PrintWriter(bwTrnPattern);
                    fileTestPattern = new PrintWriter(bwTstPattern);

                    fwTrn = new FileWriter(outputTrainFile);
                    fwTst = new FileWriter(outputTestFile);
                    bwTrn = new BufferedWriter(fwTrn);
                    bwTst = new BufferedWriter(fwTst);
                    fileTrain = new PrintWriter(bwTrn);
                    fileTest = new PrintWriter(bwTst);

                    Fold = CV_k_Number;
                    double k_Fold =  (double)Fold; 
                    
                    int testPat = (int) (totalPat * (100.0/k_Fold)/100.0f);
                    int trainPat = (totalPat - testPat);
                    int testIndexPointerUp = totalPat;
                    int testIndexPointerLow = testIndexPointerUp - testPat;
                    //partitioning data
                    Pattern[] patTrain = new Pattern[trainPat];
                    Pattern[] patTest = new Pattern[testPat];
                    for (int cv = 0; cv < Fold; cv++) {
                        System.out.printf(" Fold %d :",cv);
                        System.out.println(" TestPat Index  " + testIndexPointerLow + "  - " + testIndexPointerUp);
                        int test = 0;
                        int train = 0;
                        for (int i = 0; i < totalPat; i++) {
                            if (i >= testIndexPointerLow && i < testIndexPointerUp) {
                                patTest[test] = patRandom[i];//pat[i];
                                if (saveDataPattern.equalsIgnoreCase("Yes")) {
                                    for (int j = 0; j < patTest[test].input.length; j++) {
                                        double data = denormalize(patTest[test].input[j], inputsMin[j], inputsMax[j], normalizedLow, normalizedHigh, isaNormData);
                                        fileTestPattern.print(data + ",");
                                    }
                                    for (int j = 0; j < patTest[test].target.length; j++) {
                                        double data = denormalize(patTest[test].target[j], targetMin[j], targetMax[j], normalizedLow, normalizedHigh, isaNormData);
                                        fileTestPattern.print(data + ",");
                                    }
                                    fileTestPattern.println();
                                }//if print pattern is yes
                                test++;//Increment test index
                            } else {
                                patTrain[train] = patRandom[i];//pat[i];
                                if (saveDataPattern.equalsIgnoreCase("Yes")) {
                                    for (int j = 0; j < patTrain[train].input.length; j++) {
                                        double data = denormalize(patTrain[train].input[j], inputsMin[j], inputsMax[j], normalizedLow, normalizedHigh, isaNormData);
                                        fileTrainPattern.print(data + ",");
                                    }
                                    for (int j = 0; j < patTrain[train].target.length; j++) {
                                        double data = denormalize(patTrain[train].target[j], targetMin[j], targetMax[j], normalizedLow, normalizedHigh, isaNormData);
                                        fileTrainPattern.print(data + ",");
                                    }
                                    fileTrainPattern.println();//new line
                                }//if print pattern is yes
                                train++;//increament train index
                            }
                        }//end totalPat
                        //Update testIndexPointer
                        testIndexPointerLow = testIndexPointerLow - testPat;
                        testIndexPointerUp = testIndexPointerUp - testPat;

                        double[][] errorTrain;
                        double[][] errorTest;
                        ArrayList errorList = getError(dataFileInfo, patTrain, patTest, fileTrain, fileTest, ensemble_Candidates, m_Ensemble_Diversity);
                        errorTrain = (double[][]) errorList.get(0);//get training Error
                        errorTest = (double[][]) errorList.get(1);//get test Error
                        for (int j = 0; j < Ot; j++) {//j-th ouput
                            for (int k = 0; k < ensemble_Candidates; k++) {//k-th candidate
                                trainError[j][k][cv] = errorTrain[j][k];
                                sumTrain[j][k] = sumTrain[j][k] + trainError[j][k][cv];
                            }//k models
                        }//j outputs
                        for (int j = 0; j < Ot; j++) {//j-th ouput
                            for (int k = 0; k < ensemble_Candidates; k++) {//k-th candidate
                                testError[j][k][cv] = errorTest[j][k];
                                sumTest[j][k] = sumTest[j][k] + testError[j][k][cv];
                            }//k models
                        }//j outputs 
                        //Print Setup
                        for (int j = 0; j < Ot; j++) {
                            for (int k = 0; k < ensemble_Candidates; k++) {
                                System.out.printf(" Model %d, of %d : %.9f  = [ %.9f, %.9f ]\n", k, j,  bestGlobalTree[j][k].getFitness(), trainError[j][k][cv],testError[j][k][cv]);
                                
                            }// k models
                            System.out.println();
                        }//j outputs
                    }//Fold
                    fileTrainPattern.close();
                    fileTestPattern.close();

                    fileTrain.close();
                    fileTest.close();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Operation failed! " + e);
                    System.out.print(e);
                }
                break;
            }//10 Fold

            case "5_x_2_Fold": {
                System.out.println("5 x 2 Fold Cross-Validation of Model(s))");
                int testPat = (int) (totalPat / 2.0f);
                int trainPat = (totalPat - testPat);
                int foldindx = 0;
                try {
                    fwTrnPattern = new FileWriter(outputTrainFilePattern);
                    fwTstPattern = new FileWriter(outputTestFilePattern);
                    bwTrnPattern = new BufferedWriter(fwTrnPattern);
                    bwTstPattern = new BufferedWriter(fwTstPattern);
                    fileTrainPattern = new PrintWriter(bwTrnPattern);
                    fileTestPattern = new PrintWriter(bwTstPattern);

                    fwTrn = new FileWriter(outputTrainFile);
                    fwTst = new FileWriter(outputTestFile);
                    bwTrn = new BufferedWriter(fwTrn);
                    bwTst = new BufferedWriter(fwTst);
                    fileTrain = new PrintWriter(bwTrn);
                    fileTest = new PrintWriter(bwTst);
                    for (int cv = 0; cv < 5; cv++) {
                        System.out.printf("Fold %d \n", cv);
                        //ramomized patterns
                        //Pattern[] patRandom = ReadDataFromFile.readDataFile(problemDataFile, In, Ot, randomizeData, m_RNG, 100, numPatterns, isClassification);
                        Pattern[] patRandom = ReadDataFromFile.readDataFile(dataFileInfo, 100, m_RNG);
                        //find the patterns
                        Pattern[] patTrain = new Pattern[trainPat];
                        Pattern[] patTest = new Pattern[testPat];
                        int test = 0;
                        int train = 0;
                        for (int i = 0; i < totalPat; i++) {
                            if (i < trainPat) {
                                patTrain[train] = patRandom[i];//pat[i];
                                if (saveDataPattern.equalsIgnoreCase("Yes")) {
                                    for (int j = 0; j < patTrain[train].input.length; j++) {
                                        double data = denormalize(patTrain[train].input[j], inputsMin[j], inputsMax[j], normalizedLow, normalizedHigh, isaNormData);
                                        fileTrainPattern.print(data + ",");
                                    }
                                    for (int j = 0; j < patTrain[train].target.length; j++) {
                                        double data = denormalize(patTrain[train].target[j], targetMin[j], targetMax[j], normalizedLow, normalizedHigh, isaNormData);
                                        fileTrainPattern.print(data + ",");
                                    }
                                    fileTrainPattern.println();//new line
                                }//if print pattern is yes
                                train++;
                            } else {
                                patTest[test] = patRandom[i];//pat[i];
                                if (saveDataPattern.equalsIgnoreCase("Yes")) {
                                    for (int j = 0; j < patTest[test].input.length; j++) {
                                        double data = denormalize(patTest[test].input[j], inputsMin[j], inputsMax[j], normalizedLow, normalizedHigh, isaNormData);
                                        fileTestPattern.print(data + ",");
                                    }
                                    for (int j = 0; j < patTest[test].target.length; j++) {
                                        double data = denormalize(patTest[test].target[j], targetMin[j], targetMax[j], normalizedLow, normalizedHigh, isaNormData);
                                        fileTestPattern.print(data + ",");
                                    }
                                    fileTestPattern.println();
                                }//if print pattern is yes
                                test++;
                            }
                        }//end totalPat


                        double[][] errorTrain;
                        double[][] errorTest;
                        ArrayList errorList;
                        
                        //first fold
                        errorList= getError(dataFileInfo, patTrain, patTest, fileTrain, fileTest, ensemble_Candidates, m_Ensemble_Diversity);
                        errorTrain = (double[][]) errorList.get(0);//get training Error
                        errorTest = (double[][]) errorList.get(1);//get test Error
                        //saving training prediction                  
                        for (int j = 0; j < Ot; j++) {
                            for (int k = 0; k < ensemble_Candidates; k++) {
                                trainError[j][k][foldindx] = errorTrain[j][k];
                                sumTrain[j][k] = sumTrain[j][k] + trainError[j][k][foldindx];
                            }//k models
                        }//j outputs
                        //saveing test prediction
                        for (int j = 0; j < Ot; j++) {
                            for (int k = 0; k < ensemble_Candidates; k++) {
                                testError[j][k][foldindx] = errorTest[j][k];
                                sumTest[j][k] = sumTest[j][k] + testError[j][k][foldindx];
                            }//k models
                        }//j outputs 
                        for (int j = 0; j < Ot; j++) {
                            for (int k = 0; k < ensemble_Candidates; k++) {
                                System.out.printf(" Model %d, of %d : %.9f  = [ %.9f, %.9f ]\n", k, j,  bestGlobalTree[j][k].getFitness(), trainError[j][k][foldindx],testError[j][k][foldindx]);
                            }//k models
                            System.out.println();
                        }//j outputs
                        foldindx++;

                        //Second fold
                        errorList = getError(dataFileInfo, patTest, patTrain, fileTrain, fileTest, ensemble_Candidates, m_Ensemble_Diversity);
                        errorTrain = (double[][]) errorList.get(0);//get training Error
                        errorTest = (double[][]) errorList.get(1);//get test Error
                        //saving training prediction
                        for (int j = 0; j < Ot; j++) {
                            for (int k = 0; k < ensemble_Candidates; k++) {
                                trainError[j][k][foldindx] = errorTrain[j][k];
                                sumTrain[j][k] = sumTrain[j][k] + trainError[j][k][foldindx];
                            }//k models
                        }//j outputs
                        //saveing test prediction
                        for (int j = 0; j < Ot; j++) {
                            for (int k = 0; k < ensemble_Candidates; k++) {
                                testError[j][k][foldindx] = errorTest[j][k];
                                sumTest[j][k] = sumTest[j][k] + testError[j][k][foldindx];
                            }//k models
                        }//j outputs 
                        for (int j = 0; j < Ot; j++) {
                            for (int k = 0; k < ensemble_Candidates; k++) {
                                System.out.printf(" Model %d, of %d : %.9f  = [ %.9f, %.9f ]\n", k, j,  bestGlobalTree[j][k].getFitness(), trainError[j][k][foldindx],testError[j][k][foldindx]);
                            }//k models
                            System.out.println();
                        }//j outputs
                        foldindx++;
                    }//end of 5 x 2 folds
                    fileTrainPattern.close();
                    fileTestPattern.close();

                    fileTrain.close();
                    fileTest.close();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Operation failed! " + e);
                    System.out.print(e);
                }
                break;
            }//5 x 2 Fold
            case "Manual_Partition": {
                Fold = 1;
                try {
                    fwTrnPattern = new FileWriter(outputTrainFilePattern);
                    fwTstPattern = new FileWriter(outputTestFilePattern);
                    bwTrnPattern = new BufferedWriter(fwTrnPattern);
                    bwTstPattern = new BufferedWriter(fwTstPattern);
                    fileTrainPattern = new PrintWriter(bwTrnPattern);
                    fileTestPattern = new PrintWriter(bwTstPattern);

                    fwTrn = new FileWriter(outputTrainFile);
                    fwTst = new FileWriter(outputTestFile);
                    bwTrn = new BufferedWriter(fwTrn);
                    bwTst = new BufferedWriter(fwTst);
                    fileTrain = new PrintWriter(bwTrn);
                    fileTest = new PrintWriter(bwTst);
                    int trainPat = 10 + manualPat * 10;
                    trainPat = (int) (totalPat * (trainPat / 100.0f));
                    int testPat = (totalPat - trainPat);
                    System.out.println("Manual Partition of total patterns"+totalPat+" Training: "+trainPat+ " Test: "+testPat+"");
                    //Pattern[] patRandom = ReadDataFromFile.readDataFile(problemDataFile, In, Ot, randomizeData, m_RNG, 100, numPatterns, isClassification);
                    Pattern[] patRandom = ReadDataFromFile.readDataFile(dataFileInfo, 100, m_RNG);
                    Pattern[] patTrain = new Pattern[trainPat];
                    Pattern[] patTest = new Pattern[testPat];
                    int test = 0;
                    int train = 0;
                    for (int i = 0; i < totalPat; i++) {
                        if (i < trainPat) {
                            patTrain[train] = patRandom[i];//pat[i];
                            if (saveDataPattern.equalsIgnoreCase("Yes")) {
                                for (int j = 0; j < patTrain[train].input.length; j++) {
                                    double data = denormalize(patTrain[train].input[j], inputsMin[j], inputsMax[j], normalizedLow, normalizedHigh, isaNormData);
                                    fileTrainPattern.print(data + ",");
                                }
                                for (int j = 0; j < patTrain[train].target.length; j++) {
                                    double data = denormalize(patTrain[train].target[j], targetMin[j], targetMax[j], normalizedLow, normalizedHigh, isaNormData);
                                    fileTrainPattern.print(data + ",");
                                }
                                fileTrainPattern.println();//new line
                            }//if print pattern is yes
                            train++;
                        } else {
                            patTest[test] = patRandom[i];//pat[i];
                            if (saveDataPattern.equalsIgnoreCase("Yes")) {
                                for (int j = 0; j < patTest[test].input.length; j++) {
                                    double data = denormalize(patTest[test].input[j], inputsMin[j], inputsMax[j], normalizedLow, normalizedHigh, isaNormData);
                                    fileTestPattern.print(data + ",");
                                }
                                for (int j = 0; j < patTest[test].target.length; j++) {
                                    double data = denormalize(patTest[test].target[j], targetMin[j], targetMax[j], normalizedLow, normalizedHigh, isaNormData);
                                    fileTestPattern.print(data + ",");
                                }
                                fileTestPattern.println();
                            }//if print pattern is yes
                            test++;
                        }
                    }//end totalPat

                    double[][] errorTrain;
                    double[][] errorTest;
                    ArrayList errorList = getError(dataFileInfo, patTrain, patTest, fileTrain, fileTest, ensemble_Candidates, m_Ensemble_Diversity);
                    errorTrain = (double[][]) errorList.get(0);//get training Error
                    errorTest = (double[][]) errorList.get(1);//get test Error
                    //saving training error
                    for (int j = 0; j < Ot; j++) {
                        for (int k = 0; k < ensemble_Candidates; k++) {
                            trainError[j][k][0] = errorTrain[j][k];
                            sumTrain[j][k] = sumTrain[j][k] + trainError[j][k][0];
                        }//k models
                    }//j outputs
                    //saving test error
                    for (int j = 0; j < Ot; j++) {
                        for (int k = 0; k < ensemble_Candidates; k++) {
                            testError[j][k][0] = errorTest[j][k];
                            sumTest[j][k] = sumTest[j][k] + testError[j][k][0];
                        }//k models
                    }//j outputs 
                    for (int j = 0; j < Ot; j++) {
                        for (int k = 0; k < ensemble_Candidates; k++) {
                            System.out.printf(" Model %d, of %d : %.9f  = [ %.9f, %.9f ]\n", k, j,  bestGlobalTree[j][k].getFitness(), trainError[j][k][0],testError[j][k][0]);
                        }//k models
                        System.out.println();
                    }//j output s
                    fileTrainPattern.close();
                    fileTestPattern.close();

                    fileTrain.close();
                    fileTest.close();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Operation failed! " + e);
                    System.out.print(e);
                    return null;
                }
                break;
            } // case manual partition
            case "Partitioned_File": {
                Fold = CV_File_num; //take input from files/number of trainiig files 
                System.out.println(CV_File_num + " Fold Cross-Validation of Model(s))");
                try {
                    fwTrn = new FileWriter(outputTrainFile);
                    fwTst = new FileWriter(outputTestFile);
                    bwTrn = new BufferedWriter(fwTrn);
                    bwTst = new BufferedWriter(fwTst);
                    fileTrain = new PrintWriter(bwTrn);
                    fileTest = new PrintWriter(bwTst);

                    ReadCVFiles readFiles = new ReadCVFiles();
                    //Cross validation from input files
                    for (int cv = 0; cv < Fold; cv++) {
                        System.out.print(" Fold " + cv + " :");
                        //Reading Training and Test files
                        String trainingFileToRead = CV_File_path + "Training_CV_" + cv + ".txt";
                        dataFileInfo.set(0, trainingFileToRead);
                        //Pattern[] patTrain = readFiles.readDataFile(trainingFileToRead, In, Ot, randomizeData, m_RNG, 100, numPatterns, isClassification);
                        Pattern[] patTrain = readFiles.readDataFile(dataFileInfo, 100, m_RNG);
                        String testFileToRead = CV_File_path + "Test_CV_" + cv + ".txt";
                        //Pattern[] patTest = readFiles.readDataFile(testFileToRead, In, Ot, randomizeData, m_RNG, 100, numPatterns, isClassification);
                        dataFileInfo.set(0, testFileToRead);
                        Pattern[] patTest = readFiles.readDataFile(dataFileInfo, 100, m_RNG);

                        double[][] errorTrain;
                        double[][] errorTest;
                        ArrayList errorList = getError(dataFileInfo, patTrain, patTest, fileTrain, fileTest, ensemble_Candidates, m_Ensemble_Diversity);
                        errorTrain = (double[][]) errorList.get(0);//get training Error
                        errorTest = (double[][]) errorList.get(1);//get test Error
                        //saving training prediction
                        for (int j = 0; j < Ot; j++) {
                            for (int k = 0; k < ensemble_Candidates; k++) {
                                trainError[j][k][cv] = errorTrain[j][k];
                                sumTrain[j][k] = sumTrain[j][k] + trainError[j][k][cv];
                            }//k models
                        }//j outputs
                        //saveing test prediction
                        for (int j = 0; j < Ot; j++) {
                            for (int k = 0; k < ensemble_Candidates; k++) {
                                testError[j][k][cv] = errorTest[j][k];
                                sumTest[j][k] = sumTest[j][k] + testError[j][k][cv];
                            }//k models
                        }//j outputs 
                        for (int j = 0; j < Ot; j++) {
                            for (int k = 0; k < ensemble_Candidates; k++) {
                                System.out.printf(" Model %d, of %d : %.9f  = [ %.9f, %.9f ]\n", k, j,  bestGlobalTree[j][k].getFitness(), trainError[j][k][cv],testError[j][k][cv]);
                            }// k models
                            System.out.println();
                        }//j outputs
                    }//for fold from input files
                    fileTrain.close();
                    fileTest.close();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Operation failed! " + e);
                    System.out.print(e);
                }
                break;
            }//case file partition
        }//switch

        meanTrain = new double[Ot][ensemble_Candidates];
        double[][] meanTest = new double[Ot][ensemble_Candidates];
        double[][] varTrain = new double[Ot][ensemble_Candidates];
        double[][] varTest = new double[Ot][ensemble_Candidates];
        for (int j = 0; j < Ot; j++) {
            for (int k = 0; k < ensemble_Candidates; k++) {
                meanTrain[j][k] = sumTrain[j][k] / Fold;
                meanTest[j][k] = sumTest[j][k] / Fold;
                varTrain[j][k] = 0.0000000000000001;
                varTest[j][k] = 0.0000000000000001;
            }//k models
        }//j outputs
        System.out.printf("   Cross-Validation Result [ Training, Test ] \n");
        modelStatisticCV = modelStatisticCV + " Cross-Validation Result [ Training, Test ] \n";
        for (int j = 0; j < Ot; j++) {
            for (int k = 0; k < ensemble_Candidates; k++) {
                for (int cv = 0; cv < Fold; cv++) {
                    varTrain[j][k] = varTrain[j][k] + Math.pow((trainError[j][k][cv] - meanTrain[j][k]), 2);
                    varTest[j][k] = varTest[j][k] + Math.pow((testError[j][k][cv] - meanTest[j][k]), 2);
                }
                varTrain[j][k] = Math.sqrt(varTrain[j][k] / Fold);//it is standard diviation becuase we take sqrt
                varTest[j][k] = Math.sqrt(varTest[j][k] / Fold);//it is standard diviation becuase we take sqrt
                System.out.printf(" Output %d Model (%d) Traininig {%.9f,%.9f]} Test {%.9f,%.9f]} \n", j, k , meanTrain[j][k] , varTrain[j][k] , meanTest[j][k], varTest[j][k]);
                modelStatisticCV = modelStatisticCV + " Output " + j + "  Model (" + k + ") {Fitness,SD]}  [{" + meanTrain[j][k] + ", " + varTrain[j][k] + "}," + " {" + meanTest[j][k] + ", " + varTest[j][k] + "}]\n";
            }//k models
        }//j outputs 
        return bestGlobalTree;
    }//end: crossvalidation

    public double denormalize(double x, double dataLow, double dataHigh, double normalizedLow, double normalizedHigh, boolean isTrue) {
        if (isTrue) {
            return ((dataLow - dataHigh) * x - normalizedHigh * dataLow + dataHigh * normalizedLow) / (normalizedLow - normalizedHigh);
        } else {
            return x;
        }
    }//denormalization

    public ArrayList getError(ArrayList dataFileInfo, Pattern[] patTrain, Pattern[] patTest, PrintWriter fileTrain, PrintWriter fileTest, int ensemble_Candidates, String m_Ensemble_Diversity) {
        ArrayList errorList = new ArrayList();
        int In = (int) dataFileInfo.get(1);//total input columns
        int Ot = (int) dataFileInfo.get(2);//total output coulumn
        EvaluationFunction ev = new EvaluationFunction(dataFileInfo);
        ev.loadDataTrain(patTrain, In, Ot);
        //call MH to optimized parameter based on training data
        TrainingModuleFIS algorithm = new TrainingModuleFIS();
        for (int i = 0; i < Ot; i++) {//ith output
            FuzzyFNT[] bestGlobalTreeLocal = algorithm.MHAlgorithmsReturn(m_RNG,bestGlobalTree[i], ev, i, treeParameters, mhParameters, aldoParameters, ensemble_Candidates, m_Ensemble_Diversity,FoldType);
            for (int j = 0; j < ensemble_Candidates; j++) {//j-th tree/candidate
                bestGlobalTree[i][j] = bestGlobalTreeLocal[j].copyTree();
            }// all models                       
        }//all models for all output column trained on fold cv
        //test and save training prediction
        double[][] errorTrain = ev.testTrain(bestGlobalTree, ensemble_Candidates, fileTrain);
        //test and save test prediction
        ev.loadDataTest(patTest, In, Ot);
        double[][] errorTest = ev.test(bestGlobalTree, ensemble_Candidates, fileTest);

        errorList.add(errorTrain);//Add training error
        errorList.add(errorTest);//Add test error

        return errorList;
    }

}
