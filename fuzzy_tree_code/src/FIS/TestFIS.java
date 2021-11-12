/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FIS;

import DataReader.AttrClass;
import DataReader.LoadDataRegClass;
import DataReader.Pattern;
import DataReader.ReadDataFromFile;
import MISC.Ensemble;
import MISC.Statistics;
import Randoms.MersenneTwisterFast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;


/**
 *
 * @author Varun
 */
public class TestFIS {

    private  String[] nameAtr;
    private  Vector Attributes;
    private  double[] inputsMin;
    private  double[] inputsMax;
    private  double[] targetMin;
    private  double[] targetMax;
    private  MersenneTwisterFast fntRNG;
    private  int numPatterns;
    private  int In;
    private  int Ot;
    private  boolean isClassification;
    private  String m_Old_Model_Diversity;
    private  String m_En_method;
    private  int m_Old_Ensemble_NumInt;
    private  double normalizedLow;
    private  double normalizedHigh;
    private  String problemDataFile;
    private  String datasetName;

    public static void main(String args[]) {

        System.out.println("Test FIS Models ");
        TestFIS testFir = new TestFIS();
        testFir.readData();
        testFir.testModel();
    }

    private  void readData() {
        String absoluteFilePathOut = "C:\\Users\\Varun\\Dropbox\\ONSI\\";
        String fileName = "C:\\Users\\Varun\\Dropbox\\ONSI\\onis_test.csv";
        normalizedLow = 0.0;
        normalizedHigh = 1.0;
        LoadDataRegClass loadData = new LoadDataRegClass();
        loadData.setNormalizationRange(normalizedLow, normalizedHigh);
        loadData.loadDataRegressionClassification(absoluteFilePathOut, fileName, null);
        problemDataFile = absoluteFilePathOut + "normalizedData.csv";

        numPatterns = loadData.dataSetLength;
        In = loadData.getInputName().size();
        Ot = loadData.getOutputName().size();
        int catSize = loadData.getcatName().size();
        isClassification = loadData.getProblemType().equalsIgnoreCase("Classification");

        Attributes = loadData.getAttributes();
        datasetName = loadData.DataSet;
        if (!isClassification) {
        } else {
            // do nothing 
            if (isClassification) {//Number of Otputs in model
                Ot = catSize;//output size is equal to category size
                if (catSize == 2) {
                    Ot = 1;//binary classification 
                }
            }//if
        }

        AttrClass AtrCls;
        nameAtr = new String[Attributes.size()];
        for (int i = 0; i < Attributes.size(); i++) {
            AtrCls = (AttrClass) Attributes.get(i);
            nameAtr[i] = AtrCls.getAttrName();
        }

        inputsMin = new double[In];
        inputsMax = new double[In];
        targetMin = new double[Ot];
        targetMax = new double[Ot];
        for (int j = 0; j < In; j++) {
            AtrCls = (AttrClass) Attributes.get(j);
            inputsMin[j] = AtrCls.getAttrRange()[0];
            inputsMax[j] = AtrCls.getAttrRange()[1];
            System.out.println("Range Inputs :" + inputsMin[j] + " - " + inputsMax[j]);
        }//copy inputs min and max
        if (!isClassification) {//if regression
            for (int j = 0; j < Ot; j++) {
                AtrCls = (AttrClass) Attributes.get(In + j);
                targetMin[j] = AtrCls.getAttrRange()[0];
                targetMax[j] = AtrCls.getAttrRange()[1];
                System.out.println("Range Tragets :" + targetMin[j] + " - " + targetMax[j]);
            }//copy taget min and max
        } else {//if classification
            targetMin = new double[catSize];
            targetMax = new double[catSize];
            for (int j = 0; j < catSize; j++) {
                targetMin[j] = 0.0;
                targetMax[j] = 1.0;
            }//copy taget min and max 
        }//if
    }

    private  void testModel() {
        try{
        File outputFolder = new File("C:\\Users\\Varun\\Dropbox\\ONSI\\onsi");
        FileWriter fw = new FileWriter(outputFolder + "ModelRes.txt");
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter pr = new PrintWriter(bw);

        final File[] files = outputFolder.listFiles();
        for (File f1 : files) {
            //System.out.print(f1.getName() + " >");
            File ftemp = f1.getAbsoluteFile();
            File[] files1 = ftemp.listFiles();
            String currentFileDir = f1.getAbsolutePath() + "\\";
            String currentFile = "";
            String currentFilePath = "";
            for (File f2 : files1) {
                if (f2.getName().contains(".txt")) {
                    currentFile = f2.getName();
                    currentFilePath = f2.getAbsolutePath();
                    break;
                }
            }
            pr.print(f1.getName()+"\t");
            System.out.print(currentFile + " is in directory: " + currentFileDir);
            System.out.println(currentFilePath);
            readOldModel(currentFilePath, currentFileDir, pr);
        }//all files
        pr.close();
        }catch(Exception e){
            System.out.println(e);
        }
    }

    private  void readOldModel(String fileNameLocal, String fileDir, PrintWriter pr) {
        long SEED;
        SEED = System.currentTimeMillis();
        fntRNG = new MersenneTwisterFast(SEED);
        fntRNG.setIsAuto(true);

        double[][] oldModel_ensemble_Weights = preProceessFile2bTested(fileNameLocal, fileDir);

        FuzzyFNT[][] oldFNTmodel = new FuzzyFNT[Ot][m_Old_Ensemble_NumInt];
        //read FuzzyFNT structures/models
        for (int j = 0; j < Ot; j++) {
            for (int k = 0; k < m_Old_Ensemble_NumInt; k++) {
                oldFNTmodel[j][k] = new FuzzyFNT(fntRNG);
                oldFNTmodel[j][k].readSavedFNTmodel(fileDir + "fntStructureTest" + j + "" + k + ".txt");
                oldFNTmodel[j][k].setWeightsOnly("Nodes_Only");
                pr.print(oldFNTmodel[j][k].getParametersCount()+"\t");
            }//k models
        }//j outputs
        System.out.println("\n FNT Model succefuly read");
        testOldModel(oldFNTmodel, fileDir, oldModel_ensemble_Weights, pr);
    }

    private  double[][] preProceessFile2bTested(String fileName, String fileDir) {
        int totalModel = 1;
        int modelsNum = 1;
        boolean isOldisClassification = false;
        int inputOld = 1;
        int outputOld = 1;
        Vector ENweights = new Vector();

        try {
            FileReader fin = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fin);

            FileWriter fw = new FileWriter(fileDir + "ModelOldRes.txt");
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pr = new PrintWriter(bw);
            String line;
            int j = 0;
            while ((line = br.readLine()) != null) {
                if (line.contains("%,Problem:")) {
                    String[] tokens = line.split(",");
                    isOldisClassification = tokens[2].equals("Classification");
                    //System.out.println(isOldisClassification);
                } else if (line.contains("%,Input_Features:")) {
                    String[] tokens = line.split(",");
                    inputOld = Integer.parseInt(tokens[2]);
                    //System.out.println(inputOld);
                } else if (line.contains("%,Output_Featuers:")) {
                    String[] tokens = line.split(",");
                    outputOld = Integer.parseInt(tokens[2]);
                    //System.out.println(outputOld);
                } else if (line.contains("#,Ensemble_Diversity:")) {
                    String[] tokens = line.split(",");
                    m_Old_Model_Diversity = tokens[2];
                    //System.out.println(m_Old_Model_Diversity);
                    pr.println(line);
                } else if (line.contains("#,Ensemble_Method_Used:")) {
                    String[] tokens = line.split(",");
                    m_En_method = tokens[2];
                    //System.out.println(m_En_method);
                    pr.println(line);
                } else if (line.contains("#,Ensemble_Weights_Output_:")) {
                    ENweights.add(line);
                    //System.out.println(line);
                    pr.println(line);
                } else if (line.contains("#,Ensemble_Candidates:")) {
                    String[] tokens = line.split(",");
                    modelsNum = Integer.parseInt(tokens[2]);
                    //System.out.println(modelsNum);
                    pr.println(line);
                } else if (line.contains("$MODEL(S):")) {
                    String[] tokens = line.split(",");
                    totalModel = Integer.parseInt(tokens[1]);
                    //System.out.println(totalModel);
                } else if (line.contains("$")) {
                    //don't do anything
                } else {
                    pr.println(line);
                }
            }
            pr.close();
            br.close();
        } catch (IOException | NumberFormatException e) {
            System.out.print(e);
        }

        if (isOldisClassification) {//Number of Otputs in model
            if (outputOld == 2) {
                outputOld = 1;//binary classification 
            }
        }
        if (isOldisClassification == isClassification && inputOld == In && outputOld == Ot && totalModel == outputOld * modelsNum) {
            //do nothing
        } else {
            return null;
        }
        m_Old_Ensemble_NumInt = modelsNum;

        double[][] oldEnWeights = new double[outputOld][modelsNum];
        for (int j = 0; j < outputOld; j++) {
            String[] tokens = ENweights.get(j).toString().split(",");
            if (Integer.parseInt(tokens[2]) == j) {//reading the coreect weights
                for (int k = 0; k < modelsNum; k++) {
                    oldEnWeights[j][k] = Double.parseDouble(tokens[k + 3]);
                    //System.out.print(" " + oldEnWeights[j][k]);
                }
                //System.out.println();
            } else {
                return null;
            }
        }
        try {
            for (int j = 0; j < outputOld; j++) {
                for (int k = 0; k < modelsNum; k++) {
                    FileReader fin = new FileReader(fileName);
                    BufferedReader br = new BufferedReader(fin);

                    FileWriter fw = new FileWriter(fileDir + "fntStructureTest" + j + "" + k + ".txt");
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter pr = new PrintWriter(bw);
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.contains("$M" + j + "" + k + "@")) {
                            String[] modelData = line.split("@");
                            pr.println(modelData[1]);
                        }//if
                    }//while
                    pr.close();
                    br.close();
                }//read all Models
            }//all output columns
        } catch (Exception e) {
            System.out.print(e);
        }
        return oldEnWeights;
    }//preprocess

    private  void testOldModel(FuzzyFNT[][] oldFNTmodel, String fileDir,double[][] oldModel_ensemble_Weights,PrintWriter pr) {
        ArrayList fntDataFileInfo = new ArrayList();
            fntDataFileInfo.add(0, problemDataFile);//file location and name String
            fntDataFileInfo.add(1, In);//total input columns int
            fntDataFileInfo.add(2, Ot);//total output coulumns int
            fntDataFileInfo.add(3, numPatterns);//total length of original dataset int
            fntDataFileInfo.add(4, isClassification);// this is a classification boolean
            fntDataFileInfo.add(5, normalizedLow);// normalization min double
            fntDataFileInfo.add(6, normalizedHigh);// normalization max double
            fntDataFileInfo.add(7, inputsMin);// input min vector double[]
            fntDataFileInfo.add(8, inputsMax);// input max vector double[]
            fntDataFileInfo.add(9, targetMin);// target min vector double[]
            fntDataFileInfo.add(10, targetMax);// target max vector double[] 
            fntDataFileInfo.add(11, false);// is needed to shufful dataset boolean
            fntDataFileInfo.add(12, true);// is it a normalized dataset boolean
            fntDataFileInfo.add(13, datasetName);// dataset name String 

//        if (m_Old_Model_Diversity.equals("Structural")) {
//            for (int j = 0; j < Ot; j++) {
//                for (int k = 0; k < m_Old_Ensemble_NumInt; k++) {
//                    testGraphDraw drawModel = new testGraphDraw();//Retieving Model information
//                    drawModel.drawTree(fileDir+"fntStructureTest" + j + "" + k + ".txt", j, k, true);//Saving Old image
//                    drawModel = null;
//                }//k models
//            }//j outputs
//        } else {//if diversity of paramter / single model
//            testGraphDraw drawModel = new testGraphDraw();//Retieving Model information
//            drawModel.drawTree(fileDir+"fntStructureTest" + 0 + "" + 0 + ".txt", 0, 0, true);//Drawing and Saving Image
//            drawModel = null;
//        }
//        //oldFNTmodel.printTree();
//        //oldFNTmodel.printTreeFile(InitiatorFrame.absolutePathOut+"FNT_StructureTest.txt");

        int totalTestPat = 100;
        System.out.println("Patterns to read: " + totalTestPat + "%");
        //Changes in fntDataFileInfo
        //fntDataFileInfo.add(totalTestPat);
        Pattern[] patRandom = ReadDataFromFile.readDataFile(fntDataFileInfo, totalTestPat, fntRNG);
        //evaluating the saved model
        EvaluationFunction ev = new EvaluationFunction(fntDataFileInfo);
        boolean isDataLoaded = ev.loadDataTest(patRandom, In, Ot);
        if (isDataLoaded) {
            System.out.println("\nData Loaded for FNT Model testing");
        } else {
            return;//necessary return
        }

        try {
            FileWriter fw = new FileWriter(fileDir + "outputTestOld.csv");
            BufferedWriter bw = new BufferedWriter(fw);
            try (PrintWriter fileTestOld = new PrintWriter(bw)) {
                ev.test(oldFNTmodel, m_Old_Ensemble_NumInt, fileTestOld);//creating ensemble test oputput file
                fileTestOld.close();
            }
            bw.close();
            fw.close();
        } catch (IOException e) {
            System.out.print(e);
            return;
        }
        String testEnsembleFilePath = fileDir;
        if (!isClassification) {
            Ensemble ensemble = new Ensemble();
            ensemble.ensambleRegTest(testEnsembleFilePath, oldModel_ensemble_Weights, m_Old_Ensemble_NumInt, m_En_method, Ot);
            String rmse = "";
            String r = "";
            String r2 = "";
            for (int j = 0; j < Ot; j++) {
                Statistics statistics = new Statistics();
                double[] statTest = statistics.statistics(fileDir + "ensembleTestOld.csv", j, isClassification);
                rmse = rmse + statTest[0] + "\t";
                r = r + statTest[1] + "\t";
                r2 = r2 + statTest[2] + "";
            }//j ouputs
            System.out.println(rmse+""+r+""+r2);
            pr.println(rmse+""+r+""+r2);
        } else {
            Ensemble ensemble = new Ensemble();
            ensemble.ensambleClassTest(testEnsembleFilePath, oldModel_ensemble_Weights, m_Old_Ensemble_NumInt, m_En_method, Ot);
            Statistics statistics = new Statistics();
            double[] statTest = statistics.statistics(fileDir + "ensembleTestOld.csv", Ot, isClassification);
            //print results
        }

        //Display Information
        String information = "";
        try {
            FileReader fin = new FileReader(fileDir + "ModelOldRes.txt");
            BufferedReader brProb = new BufferedReader(fin);
            String rootData;
            while ((rootData = brProb.readLine()) != null) {
                information = information + rootData + "\n";
            }
            brProb.close();
            fin.close();
        } catch (Exception e) {
            System.out.print(e);
            return;
        }       
    }//test Regression Old

}//Model read
