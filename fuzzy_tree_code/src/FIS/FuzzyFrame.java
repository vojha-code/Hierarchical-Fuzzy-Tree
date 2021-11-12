package FIS;

import DataReader.AttrClass;
import DataReader.Pattern;
import DataReader.LoadDataRegClass;
import DataReader.ReadDataFromFile;
import MISC.*;
import GraphGUI.*;
import Randoms.*;
import AdditionalFrames.InitiatorFrame;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.SwingWorker;
import javax.swing.JTextField;

/**
 *
 * @author Varun Kumar Ojha
 */
public class FuzzyFrame extends javax.swing.JFrame {

    MersenneTwisterFast fntRNG;
    //JavaRand fntRNG;
    private String fntRNGSeedType = "auto";

    private File dataFile;
    private String selectedFile;
    private String dataFileName = "";
    private boolean isFileChosen = false;
    private boolean isDataLoaded = false;
    private boolean isTrained = false;
    private boolean isTested = false;
    ArrayList fntDataFileInfo;
    // 0 file location and name (String)
    // 1 total input columns (int)
    // 2 total output coulumns (int)
    // 3 total length of original dataset (int)
    // 4 problem type (String) 
    // 5 normalization min (double)
    // 6 normalization max (double)
    // 7 input min vector (double[])
    // 8 input max vector (double[])
    // 9 target min vector (double[])
    //10 target max vector (double[])
    //11 is needed to shufful dataset boolean      
    //12 is it a normalized dataset         
    //13 dataset name    

    private String problemType;
    private boolean needSufuling = true;

    //FNT variables
    FuzzyFNT[][] bestGlobalTree;
    String fntStructureTrain = InitiatorFrame.absoluteFilePathOut + "FNT_StructureTrain";
    String fntStructureTest = InitiatorFrame.absoluteFilePathOut + "FNT_StructureTest";

    //Training variables
    private AnswerWorkerFNTtraining answerWorker;
    private String selectTrainingMode = "Single Objective";
    private String selected_training_model = "new";
    private Vector holdFeatures;//track the selected features
    private Vector Attributes;
    private String nameAtr[];
    private String dataInfo;
    private String MHAlgo = "ABC";

    //Ensemble set-up
    private int ensembleCandidates = 1;
    private String ensembleDiversityType;
    private String m_Old_Model_Diversity;
    private int m_Old_Ensemble_NumInt;
    private String m_En_method;
    private String m_ensembleMHalgo = "ABC";

    private String trnMatrix;
    private String tstMatrix;
    private String tstMatrixOld;

    private FuzzyFNT[][] oldFNTmodel;
    private double[][] oldModel_ensemble_Weights;

    private String cvSavePattern = "No";
    private int cvExtFileNumbers = 10;
    private String cvExtFilePath = "";

    public String executionHistory = "";
    private String modelStatistic = "";
    private String fntTrainParam = "";
    private boolean isTrainingStarted = false;
    private String FSMFType = "Type-I";
    private boolean oldModelRead = true;

    public FuzzyFrame() {
        initComponents();
    }

    class DataWorker extends SwingWorker<Integer, Integer> {

        FuzzyFrame frameFNT;

        public DataWorker(FuzzyFrame frameFNT) {
            this.frameFNT = frameFNT;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            // Do a time-consuming task.                    
            jProgressBarIndefinite.setIndeterminate(true);
            jTextArea_DataDisplay.setText("\n\nData is loading... ");
            double normalizedLow = 0.0;
            double normalizedHigh = 1.0;
            boolean needDenormalization = true;

            try {
                normalizedLow = Double.parseDouble(jTextBox_normLow.getText()); //-1.0;
                normalizedHigh = Double.parseDouble(jTextBox_normHigh.getText());//1.0;
                needDenormalization = true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frameFNT, "Input must be a number");
                return 0;
            }
            LoadDataRegClass loadData = new LoadDataRegClass();
            loadData.setNormalizationRange(normalizedLow, normalizedHigh);
            loadData.loadDataRegressionClassification(InitiatorFrame.absoluteFilePathOut, dataFileName, frameFNT);
            String problemDataFile = InitiatorFrame.absoluteFilePathOut + "normalizedData.csv";
            if (!needDenormalization) {//if false or if not a normalized data
                problemDataFile = InitiatorFrame.absoluteFilePathOut + "filteredData.csv";
            }
            //local data loading it is not use latter
            //Pattern[] pat = loadData.runProblem();//not used later
            int numPatterns = loadData.dataSetLength;
            int In = loadData.getInputName().size();
            int Ot = loadData.getOutputName().size();
            int catSize = loadData.getcatName().size();
            boolean isClassification = loadData.getProblemType().equalsIgnoreCase("Classification");

            Attributes = loadData.getAttributes();
            String datasetName = loadData.DataSet;
            problemType = loadData.getProblemType();
            if (!isClassification) {
                jComboBox_Ensemble_Method.removeAllItems();
                jComboBox_Ensemble_Method.addItem("Mean");
                jComboBox_Ensemble_Method.addItem("Weighted_M");
                jComboBox_Ensemble_Method.addItem("Evolutionary_WM");
                dataInfo = "%,Dataset_Name:," + datasetName + "\n%,Problem:," + problemType + "\n%,Input_Features:," + In + "\n%,Output_Featuers:," + Ot + "\n%,Total_Example:," + numPatterns;
            } else {
                jComboBox_Ensemble_Method.removeAllItems();
                jComboBox_Ensemble_Method.addItem("Majority_Voting");
                jComboBox_Ensemble_Method.addItem("Weighted_MV");
                jComboBox_Ensemble_Method.addItem("Evolutionary_WMV");
                dataInfo = "%,Dataset_Name:," + datasetName + "\n%,Problem:," + problemType + "\n%,Input_Features:," + In + "\n%,Output_Featuers:," + catSize + "\n%,Total_Example:," + numPatterns;
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

            double[] inputsMin;
            double[] inputsMax;
            double[] targetMin;
            double[] targetMax;
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

            //Settting data Infor to a an array list
            fntDataFileInfo = new ArrayList();//re-frashing fntArrayList. 
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
            if (problemType.equals("TimeSerise")) {//whether to randomize dataset
                needSufuling = false;
            }//if problem type
            fntDataFileInfo.add(11, needSufuling);// is needed to shufful dataset boolean
            fntDataFileInfo.add(12, needDenormalization);// is it a normalized dataset boolean
            fntDataFileInfo.add(13, datasetName);// dataset name String 
            fntDataFileInfo.add(14, false);// dataset boolean is complex data

            //dispalyaing massage
            jTextArea_DataDisplay.setText(dataInfo);
            jTextField_Display_Massage.setForeground(Color.DARK_GRAY);
            jTextField_Display_Massage.setText("Dataset Loaded: GO TO TAB TS-FIS Modelling");

            //Confirming that data are loaded
            isDataLoaded = true;
            //return the value
            return 0;
        }//do Background

        @Override
        protected void done() {
            try {
                jProgressBarIndefinite.setIndeterminate(false);
            } catch (Exception e) {
                System.out.print(e);
            }
        }
    }//class data loading swing warker

    class AnswerWorkerFNTtraining extends SwingWorker<Integer, Integer> {

        //variable for FuzzyFNT training modeuls
        FuzzyFrame frameFNT;
        private ArrayList treeParameters;
        private ArrayList gpParameters;
        private ArrayList mhParameters;
        private ArrayList ensembleParameters;
        private ArrayList ensembleAlgoParameters;
        private ArrayList algoParameters;
        private ArrayList cvParameter;

        private int In = 0, Ot = 0;
        private boolean isClassification;
        private long SEED;

        public AnswerWorkerFNTtraining(FuzzyFrame frameFNT) {
            this.frameFNT = frameFNT;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            jProgressBarIndefinite.setIndeterminate(true);// Do a time-consuming task.
            jProgressBarIndefinite.setString("Setting Parameters...");
            jProgressBarDefinite.setValue(0);

            In = (int) fntDataFileInfo.get(1);//total input columns
            Ot = (int) fntDataFileInfo.get(2);//total output coulumn
            isClassification = (boolean) fntDataFileInfo.get(4);//propble type

            /*SETTING PARAMETERS::  regression/classification>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> */
            if (!setPatameters()) {//Method call is in the If ()
                return null;
            } else {
                System.out.println("Setting set : true");
            }//if Set Parameters

            long startTime = System.currentTimeMillis();//Training time starts
            //JTextAreaPrintStream.initGUI(); // old style
            //JTextAreaPrintStream.show(); // old style
            jTextAreaRunTime.setText("");//new approach
            System.setOut(System.out);//new approach
            System.setOut(new JTextAreaPrintStream(jTextAreaRunTime)); //new approach

            jProgressBarIndefinite.setString("Structural Training...");
            jProgressBarDefinite.setValue(10);
            //FNT training - Traing both node and weights
            if (!isClassification) {
                System.out.println("NUMERICAL VALUES ARE ROOT MEAN SQUARE ERROR (RMSE) ");
            } else {
                System.out.println("NUMERICAL VALUES ARE CLASSIFICATION ERROR RATE ");
            }
            System.out.println("PROCESSING DATA ");
            /*TRAINING::: regression/classification>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> */
            if (selected_training_model.equalsIgnoreCase("new")) {
                System.out.println("Trainnig models :" + trainFNTmodels());//Method call is in the Print Stmt
            } else {//if old models
                bestGlobalTree = null;
                bestGlobalTree = readOldModel();
                if (bestGlobalTree == null) {
                    jProgressBarIndefinite.setIndeterminate(false);// Do a time-consuming task.
                    jProgressBarIndefinite.setString("Start Training");
                    jProgressBarDefinite.setValue(0);
                    jTextAreaRunTime.setText("");
                    jTextField_trainRes.setText("");
                    return 0; //do not go ahead
                }
            }//if-else
            /*CROSS VALIDATION //test Best Tree(s) using 10 fold cross validation >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> */
            double[][] modelsFitness = crossValidation();
            jProgressBarIndefinite.setString("Constructing Ensemble...");
            jProgressBarDefinite.setValue(70);
            /*ENSEMBLE/RESULT COLLECTION  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> **/
            Ensemble ensemble = new Ensemble();
            ensemble.setEnsembleFunction(fntRNG, Ot, ensembleParameters, ensembleAlgoParameters);
            if (!isClassification) {
                double[][] ensemble_Weights = ensemble.ensambleReg(modelsFitness);//ensemble finished            
                printRegressionStat(ensemble_Weights);//stattisics printed
            } else {
                double[][] ensemble_Weights = ensemble.ensambleClass(modelsFitness);//ensemble finished            
                printClassificationStat(ensemble_Weights);//stattisics printed
            }
            jProgressBarIndefinite.setString("Prining Tree...");
            jProgressBarDefinite.setValue(80);
            isTrained = true;

            //Calculate Time
            long endTime = System.currentTimeMillis();
            long millis = (endTime - startTime);  // obtained from StopWatch
            long minutes = (millis / 1000) / 60;
            long seconds = (millis / 1000) % 60;
            System.out.println(" Total Process Time: " + minutes + "." + seconds + "  minutes ");
            modelStatistic = modelStatistic + " Total Process Time:" + minutes + "." + seconds + " minutes \n";

            jProgressBarDefinite.setValue(100);
            jProgressBarIndefinite.setString("Training Finished!");
            /*PRINTINT TREE tree and retrieving features value */
            printTreeModels(); //this returns null hence this is the last command           
            //return the value   
            executionHistory = "";
            executionHistory = jTextAreaRunTime.getText();
            stopStatus.setText("  Finished");
            return 0;
        }//doBackground

        @Override
        protected void done() {
            try {
                jProgressBarIndefinite.setIndeterminate(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean trainFNTmodels() {
            boolean done = false;
            int numPatterns = (int) fntDataFileInfo.get(3);//total patterns
            int trn_data_percent = 0;
            String cvFoldType = (String) cvParameter.get(0);//String fold type
            
            int cvManualPartition = (int) cvParameter.get(1);//int manual Partition Size
            switch (cvFoldType) {
                case "k_Fold":
                    trn_data_percent = 90;
                    break;
                case "5_x_2_Fold":
                    trn_data_percent = 50;
                    break;
                case "Manual_Partition":
                    trn_data_percent = 10 + cvManualPartition * 10;
                    //do a very small MH training becuase it will train later as well
                    mhParameters.set(3, 100);//set int iteration
                    break;
                case "Partitioned_File":
                    trn_data_percent = (int) (numPatterns * (cvExtFileNumbers / 100.0));
                    break;
            }// Training condition 

            EvaluationFunction ev = new EvaluationFunction(fntDataFileInfo);
            Pattern[] patRandom = ReadDataFromFile.readDataFile(fntDataFileInfo, trn_data_percent, fntRNG);
            isDataLoaded = ev.loadDataTrain(patRandom, In, Ot);//60 % data is used for training and model creation
            int inputsCount = In;
            if (isDataLoaded) {
                System.out.println(" Data Loaded: Training models...");
            } else {
                System.out.println("Problem in Loading data to the evaluator");
                JOptionPane.showMessageDialog(frameFNT, "Problem in Loading data to evaluator!");
                return false;
            }
            jProgressBarDefinite.setValue(20);

            // Structural training;
            //Training main loop loop
            for (int j = 0; j < Ot; j++) {//FNT training parameters
                System.out.println("Training started Out Col: " + j);//Genetic programming
                GPFIS gp = new GPFIS(fntRNG, ev, treeParameters, gpParameters, mhParameters, algoParameters, ensembleParameters);
                //Recieved trainined model(s)
                FuzzyFNT[] bestGlobalTreeLocal = gp.doEvolution(inputsCount, j);//tainng for jth target
                System.out.println("Training Finsihed Out Col: " + j);
                //System.out.print("\n"+ensembleCandidates);
                modelStatistic = "";
                for (int k = 0; k < ensembleCandidates; k++) {
                    bestGlobalTree[j][k] = bestGlobalTreeLocal[k].copyTree();
                    System.out.printf(" OUTPUT %d MODEL %d : %.9f (%3d) \n", j, k, bestGlobalTree[j][k].getFitness(), bestGlobalTree[j][k].size());
                    modelStatistic = modelStatistic + "OUTPUT " + j + " MODEL: Fitness, Size: " + k + ": " + bestGlobalTree[j][k].getFitness() + ", " + bestGlobalTree[j][k].size() + "\n";
                }//for k models
            }//for j outputs: Finsih Model scope local Tress
            jProgressBarIndefinite.setString("Cross Validation...");
            jProgressBarDefinite.setValue(50);
            done = true;
            return done;
        }//training

        private double[][] crossValidation() {
            //re-set MH iteration
            int mhITR = (int) Double.parseDouble(jTextBox_MH_ITR.getText());
            mhParameters.set(3, mhITR);//int iteration
            CrossValidationFIS crossValidation = new CrossValidationFIS();
            crossValidation.setCorssValidation(bestGlobalTree, fntRNG, treeParameters, cvParameter, mhParameters, algoParameters);
            bestGlobalTree = crossValidation.crossValidation(fntDataFileInfo, ensembleCandidates, ensembleDiversityType);
            double[][] retModelFit = crossValidation.meanTrain;//Cross-Validation Finised
            modelStatistic = modelStatistic + crossValidation.modelStatisticCV;
            //varify cross-validation result
            /*for (int j = 0; j < Ot; j++) {
             for (int k = 0; k < ensembleCandidates; k++) {
             System.out.println(" OUTPUT " + j + " MODEL " + k + " : " + bestGlobalTree[j][k].getFitness() + "  ");
             executionHistory = executionHistory + (" FITNESS of TREE " + j + " of OUTPUT " + k + " : " + bestGlobalTree[j][k].getFitness() + " \n");
             }//for k models
             }//for j outputs: Finsih Model scope local Tress*/
            return retModelFit;
        }//cross validation

        private boolean printRegressionStat(double[][] ensemble_Weights) {
            boolean done = false;
            String rmse = "";
            String r = "";
            for (int j = 0; j < Ot; j++) {
                String weigtsStr = "";
                System.out.print("Ensemble Weights:,");
                for (int k = 0; k < ensembleCandidates; k++) {
                    double d = ensemble_Weights[j][k];
                    //d = Math.round(d * 100) / 100.0d;
                    System.out.printf("%f,", d);
                    weigtsStr = weigtsStr + "," + d;
                }//k models
                modelStatistic = modelStatistic + "#,Ensemble_Weights_Output_:," + j + weigtsStr + "\n";
                System.out.println();
                //Calculate RMSE Correlation  and R2 of test result
                Statistics statistics;
                statistics = new Statistics();
                double[] statTrain = statistics.statistics(InitiatorFrame.absoluteFilePathOut + "ensembleTrain.csv", j, isClassification);
                statistics = new Statistics();
                double[] statTest = statistics.statistics(InitiatorFrame.absoluteFilePathOut + "ensembleTest.csv", j, isClassification);
                System.out.printf("  Training: RMSE, Correlation, R Squared: %.9f, %.5f, %.5f \n", statTrain[0], statTrain[1], statTrain[2]);
                System.out.printf("  Test    : RMSE, Correlation, R Squared: %.9f, %.5f, %.5f \n", statTest[0], statTest[1], statTest[2]);

                //Storing into model stat
                modelStatistic = modelStatistic + "\n";
                modelStatistic = modelStatistic + "  Training: RMSE, Correlation, R Squared: "
                        + Math.round(statTrain[0] * 1000000) / 1000000.0d + ", "
                        + Math.round(statTrain[1] * 1000) / 1000.0d + ", "
                        + Math.round(statTrain[2] * 1000) / 1000.0d + "\n";
                modelStatistic = modelStatistic + "  Test    : RMSE, Correlation, R Squared: "
                        + Math.round(statTest[0] * 1000000) / 1000000.0d + ", "
                        + Math.round(statTest[1] * 1000) / 1000.0d + ", "
                        + Math.round(statTest[2] * 1000) / 1000.0d + "\n";

                rmse = rmse + Math.round(statTrain[0] * 10000) / 10000.0d + ",";
                r = r + Math.round(statTrain[1] * 100) / 100.0d + ",";
            }//j outputs
            jTextField_trainRes.setForeground(Color.BLUE);
            jTextField_trainRes.setText("RMSE: " + rmse + "  Correlation :" + r);
            done = true;
            return done;
        }//print regression

        private boolean printClassificationStat(double[][] ensemble_Weights) {
            boolean done = false;
            String acc = "";
            for (int j = 0; j < Ot; j++) {
                String weigtsStr = "";
                System.out.print("Ensemble Weights:,");
                for (int k = 0; k < ensembleCandidates; k++) {
                    double d = ensemble_Weights[j][k];
                    //d = Math.round(d * 100) / 100.0d;
                    System.out.printf("%f,", d);
                    weigtsStr = weigtsStr + "," + d;
                }//k models
                System.out.println();
                modelStatistic = modelStatistic + "#,Ensemble_Weights_Output_:," + j + weigtsStr + "\n";
            }//j outputs

            //Calculate confusionMatrix of  result
            Statistics statistics;
            statistics = new Statistics();
            double[] statTrain = statistics.statistics(InitiatorFrame.absoluteFilePathOut + "ensembleTrain.csv", Ot, isClassification);
            trnMatrix = statistics.matriPrint;
            statistics = new Statistics();
            double[] statTest = statistics.statistics(InitiatorFrame.absoluteFilePathOut + "ensembleTest.csv", Ot, isClassification);
            tstMatrix = statistics.matriPrint;

            System.out.printf("  Training: Accuracy, Error Rate, Precision, Recall: %.5f, %.5f, %.5f, %.5f \n", statTrain[0], statTrain[1], statTrain[2], statTrain[3]);
            System.out.printf("  Test    :: Accuracy, Error Rate, Precision, Recall: %.5f, %.5f, %.5f, %.5f \n", statTest[0], statTest[1], statTest[2], statTest[3]);
            System.out.println("\n Training Confusion Matrix: \n" + trnMatrix);
            System.out.println("\n Test Confusion Matrix: \n" + tstMatrix);

            //Storing into model stat
            modelStatistic = modelStatistic + "\n";
            modelStatistic = modelStatistic + "  Training: Accuracy, Error Rate, Precision, Recall: "
                    + Math.round(statTrain[0] * 100000) / 100000.0d + ", "
                    + Math.round(statTrain[1] * 100000) / 100000.0d + ", "
                    + Math.round(statTrain[2] * 100000) / 100000.0d + ","
                    + Math.round(statTrain[3] * 100000) / 100000.0d + "\n";
            modelStatistic = modelStatistic + "  Test    : RMSE, Correlation, R Squared: "
                    + Math.round(statTest[0] * 100000) / 100000.0d + ", "
                    + Math.round(statTest[1] * 100000) / 100000.0d + ", "
                    + Math.round(statTest[2] * 100000) / 100000.0d + ", "
                    + Math.round(statTest[3] * 100000) / 100000.0d + "\n";
            modelStatistic = modelStatistic + "\n Training Confusion Matrix: \n" + trnMatrix;
            modelStatistic = modelStatistic + "\n Test Confusion Matrix: \n" + tstMatrix;

            acc = acc + Math.round(statTrain[0] * 10000) / 10000.0d + ",";
            jTextField_trainRes.setForeground(Color.BLUE);
            jTextField_trainRes.setText("Classfication Acuracy: " + acc);
            done = true;
            return done;
        }

        private boolean setPatameters() {
            boolean done = false;
            if (fntRNGSeedType.equalsIgnoreCase("input")) {
                SEED = Long.parseLong(jTextBox_SEED_INPUT.getText());
                fntRNG = new MersenneTwisterFast(SEED);
                fntRNG.setIsAuto(false);
            } else {
                SEED = System.currentTimeMillis();
                fntRNG = new MersenneTwisterFast(SEED);
                fntRNG.setIsAuto(true);
            }//if

            jTextField_trainRes.setForeground(Color.RED);
            jTextField_trainRes.setText(selectTrainingMode + " Training, Please wait...");
            try {
                //setting data suffling status
                fntDataFileInfo.set(11, needSufuling);// need suffuling

                int treeGenITR = (int) Double.parseDouble(jTextBox_Max_Gen.getText());
                int treeDepth = (int) Double.parseDouble(jTextBox_Tree_Depth.getText());
                int treeNodeChilds = (int) Double.parseDouble(jTextBox_Tree_Arity.getText());
                int treeFunType = jComboBox_FunType.getSelectedIndex();

                int gpPOP = (int) Double.parseDouble(jTextBox_GP_POP.getText());
                int gpITR = (int) Double.parseDouble(jTextBox_Max_GP_ITR.getText());
                int gpELITISM = (int) Double.parseDouble(jTextField_Elitism.getText());
                double gpMUTATION_PROB = Double.parseDouble(jTextField_GP_MR.getText());
                double gpCROSSOVER_PROB = Double.parseDouble(jTextField_GP_CR.getText());
                int gpTOURNAMENT_SIZE = (int) Double.parseDouble(jTextBox_GP_TS.getText());
                if ((gpPOP - 2) < gpTOURNAMENT_SIZE) {
                    JOptionPane.showMessageDialog(frameFNT, "GP population must be greter than Tournament Size");
                    return false;
                }
                double center_lo = Double.parseDouble(jTextBox_MF_center_lo.getText());
                double center_hi = Double.parseDouble(jTextBox__MF_center_hi.getText());
                double width_lo = Double.parseDouble(jTextBox_MF_center_lo.getText());
                double width_hi = Double.parseDouble(jTextBox__MF_center_hi.getText());
                double center_dev_lo = Double.parseDouble(jTextBox_MF_center_lo.getText());
                double center_dev_hi = Double.parseDouble(jTextBox__MF_center_hi.getText());
                double then_wt_lo = Double.parseDouble(jTextBox_THEN_weight_lo.getText());
                double then_wt_hi = Double.parseDouble(jTextBox_THEN_weight_hi.getText());
                double then_wt_dev_lo = Double.parseDouble(jTextBox_THEN_weight_lo.getText());
                double then_wt_dev_hi = Double.parseDouble(jTextBox_THEN_weight_hi.getText());
                double tree_branch_wt_lo = Double.parseDouble(jTextBox_tree_branch_weight_lo.getText());
                double tree_branch_wt_hi = Double.parseDouble(jTextBox_tree_branch_weight_hi.getText());
                String weightsOnly = jComboBox_WeightsOnly.getSelectedItem().toString();
                int mhPOP = (int) Double.parseDouble(jTextBox_MH_POP.getText());
                int mhITR = (int) Double.parseDouble(jTextBox_MH_ITR.getText());

                int ensembPOP = (int) Double.parseDouble(jTextBox_EN_MH_POP.getText());
                int ensembITR = (int) Double.parseDouble(jTextBox_EN_MH_ITR.getText());
                double ensemblWTMin = Double.parseDouble(jTextBox_enWTmin.getText());
                double ensemblWTMax = Double.parseDouble(jTextBox_enWTmax.getText());
                String m_Ensemble_Candidates = jComboBox_Ensemble_Candidates.getSelectedItem().toString();
                String ensembleMethod = "";
                if (!m_Ensemble_Candidates.equals("1")) {
                    ensembleCandidates = Integer.parseInt(m_Ensemble_Candidates);
                    ensembleDiversityType = jComboBox_Ensemble_Diversity.getSelectedItem().toString();
                    ensembleMethod = jComboBox_Ensemble_Method.getSelectedItem().toString();
                    bestGlobalTree = new FuzzyFNT[Ot][ensembleCandidates];
                } else {
                    ensembleCandidates = 1;
                    ensembleDiversityType = "Nill";
                    ensembleMethod = "NillMethod";
                    bestGlobalTree = new FuzzyFNT[Ot][ensembleCandidates];
                }
                String cvFoldType = jComboBox_FoldType.getSelectedItem().toString();
                int k_Fold_Value = (int) Double.parseDouble(jTextField_k_Value.getText());
                int cvManualPartition = jComboBox_Manual_TRN_Size.getSelectedIndex();

                //Setting the array lists
                double[] range_a = {center_lo, center_hi};
                double[] range_b = {width_lo, width_hi};
                double[] range_c = {then_wt_lo, then_wt_hi};
                double[] range_d = {center_dev_lo, center_dev_hi};
                double[] range_e = {then_wt_dev_lo, then_wt_dev_hi};
                double[] range_weight = {tree_branch_wt_lo, tree_branch_wt_hi};
                int[] input_part = {1, 2};//{fixed rules = 1 else 0, #rules = 1}
                treeParameters = new ArrayList();
                treeParameters.add(0, treeGenITR);//  int general iteration
                treeParameters.add(1, treeDepth);// int depth of tree
                treeParameters.add(2, treeNodeChilds);//int arity of a tree
                treeParameters.add(3, treeFunType);// int  activation function type                
                treeParameters.add(4, range_a);//double mh min node 
                treeParameters.add(5, range_b);//double mh max naode
                treeParameters.add(6, range_c);//double mh min edge
                treeParameters.add(7, range_weight);//double mh max edge
                treeParameters.add(8, input_part);//double mh max edge
                treeParameters.add(9, weightsOnly);//boolean parameter optimization type
                treeParameters.add(10, range_d);//double mh min edge
                treeParameters.add(11, range_e);//double mh min edge
                treeParameters.add(12, FSMFType);//double mh min edge

                ensembleParameters = new ArrayList();
                ensembleParameters.add(0, ensembleCandidates);// int number of candidates for ensemble
                ensembleParameters.add(1, ensembleDiversityType);//String ensemble diversity type
                ensembleParameters.add(2, ensembleMethod);//String ensemble method
                ensembleParameters.add(3, m_ensembleMHalgo);//String ensemble algorithm
                ensembleParameters.add(4, ensembITR);//int ensemble iteration
                ensembleParameters.add(5, ensembPOP);//int ensemble population
                ensembleParameters.add(6, ensemblWTMin);//int ensemble population
                ensembleParameters.add(7, ensemblWTMax);//int ensemble population
                ensembleAlgoParameters = getENalgoParms();//set ensemble algorithm parameters

                gpParameters = new ArrayList();
                gpParameters.add(0, gpPOP);//int GPFIS population
                gpParameters.add(1, gpELITISM);//double GPFIS Elitism
                gpParameters.add(2, gpMUTATION_PROB);//double GPFIS Mutation
                gpParameters.add(3, gpCROSSOVER_PROB);//double GPFIS Crossover
                gpParameters.add(4, gpTOURNAMENT_SIZE);//double GPFIS Tournament
                gpParameters.add(5, gpITR);//int GPFIS iteration
                gpParameters.add(6, selectTrainingMode);//int GPFIS Training Mode

                mhParameters = new ArrayList();
                mhParameters.add(0, MHAlgo);//String algorithm
                mhParameters.add(1, 0);//int mh dimesion (to be set later)
                mhParameters.add(2, mhPOP);//int mh population 
                mhParameters.add(3, mhITR);//int iteration
                algoParameters = getFNTalgoParms();//get parameter settings of mh algorithm

                cvParameter = new ArrayList();
                cvParameter.add(0, cvFoldType);//String fold type
                cvParameter.add(1, cvManualPartition);//int manual Partition Size
                cvParameter.add(2, cvSavePattern);//String fold type
                cvParameter.add(3, cvExtFileNumbers);//int external file name
                cvParameter.add(4, cvExtFilePath);//String external file path
                cvParameter.add(5, k_Fold_Value);//int k_Fold value

                //Storing and printing parameter valuse
                storeParameters(treeParameters, ensembleParameters, gpParameters, mhParameters, algoParameters, ensembleAlgoParameters, cvParameter);
            } catch (NumberFormatException | HeadlessException e) {
                JOptionPane.showMessageDialog(frameFNT, "Input must be a number");
                return false;
            }
            done = true;
            return done;
        }//set parameters

        private ArrayList getFNTalgoParms() {
            ArrayList algoParams = new ArrayList();
            switch (MHAlgo) {
                case "ABC": {
                    System.out.println("Atrificial Bee Colony Training");
                    algoParams.add(0, (int) Double.parseDouble(jTextField_abc_trail.getText()));//int trail limit
                    algoParams.add(1, jComboBox_abc_Food.getSelectedIndex());//int Food Size
                    break;
                }
                case "BFO": {
                    System.out.println("Bacteria Foregging Optimization Training");
                    algoParams.add(0, (int) Double.parseDouble(jTextField_bfo_NC.getText()));//int cheimotactic
                    algoParams.add(1, (int) Double.parseDouble(jTextField_bfo_SL.getText()));//int swim length
                    algoParams.add(2, (int) Double.parseDouble(jTextField_bfo_RS.getText()));//int reporoduction
                    algoParams.add(3, (int) Double.parseDouble(jTextField_bfo_EL.getText()));//int elimination
                    algoParams.add(4, Double.parseDouble(jTextField_bfo_PR.getText()));//double elimination prob
                    algoParams.add(5, Double.parseDouble(jTextField_bfo_RL.getText()));//double run length
                    break;
                }
                case "DE": {
                    System.out.println("Differential Evolution Training");
                    algoParams.add(0, Double.parseDouble(jTextField_de_CR.getText()));//double crossover Rate
                    algoParams.add(1, Double.parseDouble(jTextField_de_F.getText()));//double Weight Factor
                    algoParams.add(2, jComboBox_de_stat.getSelectedItem().toString());//String starategy 
                    break;
                }
                case "GWO": {
                    System.out.println("Gray Wolf Optimization Training");
                    break;
                }
                case "PSO": {
                    System.out.println("Particle Swarm Optimization Training");
                    algoParams.add(0, Double.parseDouble(jTextField_pso_c0.getText()));//double crossover Rate
                    algoParams.add(1, Double.parseDouble(jTextField_pso_c1.getText()));//double Weight Factor
                    algoParams.add(2, Double.parseDouble(jTextField_pso_c2.getText()));//double Weight Factor
                    break;
                }
            }//swtich
            return algoParams;
        }

        private ArrayList getENalgoParms() {
            ArrayList algoENParames = new ArrayList();
            switch (MHAlgo) {
                case "ABC": {
                    System.out.println("Atrificial Bee Colony Training");
                    algoENParames.add(0, (int) Double.parseDouble(jTextField_abc_trail1.getText()));//int trail limit
                    algoENParames.add(1, jComboBox_abc_Food1.getSelectedIndex());//int Food Size
                    break;
                }
                case "BFO": {
                    System.out.println("Bacteria Foregging Optimization Training");
                    algoENParames.add(0, (int) Double.parseDouble(jTextField_bfo_NC1.getText()));//int cheimotactic
                    algoENParames.add(1, (int) Double.parseDouble(jTextField_bfo_SL1.getText()));//int swim length
                    algoENParames.add(2, (int) Double.parseDouble(jTextField_bfo_RS1.getText()));//int reporoduction
                    algoENParames.add(3, (int) Double.parseDouble(jTextField_bfo_EL1.getText()));//int elimination
                    algoENParames.add(4, Double.parseDouble(jTextField_bfo_PR1.getText()));//double elimination prob
                    algoENParames.add(5, Double.parseDouble(jTextField_bfo_RL1.getText()));//double run length
                    break;
                }
                case "DE": {
                    System.out.println("Differential Evolution Training");
                    algoENParames.add(0, Double.parseDouble(jTextField_de_CR1.getText()));//double crossover Rate
                    algoENParames.add(1, Double.parseDouble(jTextField_de_F1.getText()));//double Weight Factor
                    algoENParames.add(2, jComboBox_de_stat1.getSelectedItem().toString());//String starategy 
                    break;
                }
                case "GWO": {
                    System.out.println("Gray Wolf Optimization Training");
                    break;
                }
                case "PSO": {
                    System.out.println("Particle Swarm Optimization Training");
                    algoENParames.add(0, Double.parseDouble(jTextField_pso_c4.getText()));//double crossover Rate
                    algoENParames.add(1, Double.parseDouble(jTextField_pso_c5.getText()));//double Weight Factor
                    algoENParames.add(2, Double.parseDouble(jTextField_pso_c6.getText()));//double Weight Factor
                    break;
                }
            }//swtich
            return algoENParames;
        }

        private void storeParameters(ArrayList treeParameters, ArrayList ensembleParameters, ArrayList gpParameters, ArrayList mhParameters, ArrayList algoParameters, ArrayList ensembleAlgoParameters, ArrayList cvParameter) {

            fntTrainParam = ""
                    + "#,Random_Seed:," + SEED + "\n"
                    + "#,Scale_Low:," + fntDataFileInfo.get(5) + "\n"
                    + "#,Scale_High:," + fntDataFileInfo.get(6) + "\n"
                    + "#,Scaling:," + fntDataFileInfo.get(12) + "\n"
                    + "#,Suffling:," + fntDataFileInfo.get(11) + "\n"
                    + "#,Maximum_Genral_Iteration:," + treeParameters.get(0) + "\n"//  int general iteration
                    + "#,Tree_Height:," + treeParameters.get(1) + "\n"// int depth of tree
                    + "#,Tree_Arity:," + treeParameters.get(2) + "\n"//int arity of a tree
                    + "#,Tree_FIS_Type:," + treeParameters.get(12) + "\n"
                    //+ "#,Tree_MF_Input_Partition:," +((double[]) treeParameters.get(8))[1]+ "\n"
                    + "#,Tree_MF_Type:," + treeParameters.get(3) + "\n"// int  activation function type
                    + "#,Tree_MF_Ceneter_Range:," + ((double[]) treeParameters.get(4))[0] + "," + ((double[]) treeParameters.get(4))[1] + "\n"
                    + "#,Tree_MF_Width_Range:," + ((double[]) treeParameters.get(5))[0] + "," + ((double[]) treeParameters.get(5))[1] + "\n"
                    + "#,Tree_MF_Then_Weight_Range:," + ((double[]) treeParameters.get(6))[0] + "," + ((double[]) treeParameters.get(6))[1] + "\n"
                    + "#,Tree_MF_Center_Deviation_Range:," + ((double[]) treeParameters.get(10))[0] + "," + ((double[]) treeParameters.get(10))[1] + "\n"
                    + "#,Tree_MF_Then_Weight_Deviation_Range:," + ((double[]) treeParameters.get(11))[0] + "," + ((double[]) treeParameters.get(11))[1] + "\n"
                    + "#,Tree_Branch_Weight_Range:," + ((double[]) treeParameters.get(7))[0] + "," + ((double[]) treeParameters.get(7))[1] + "\n"
                    + "#,Tree_Parameter_Opt_Type:," + treeParameters.get(9) + "\n"
                    + "#,GP_Population:," + gpParameters.get(0) + "\n"//int GPFIS population
                    + "#,gpELITISM:," + gpParameters.get(1) + "\n"//double GPFIS Elitism
                    + "#,gpMUTATION_PROB:," + gpParameters.get(2) + "\n"//double GPFIS Mutation
                    + "#,gpCROSSOVER_PROB:," + gpParameters.get(3) + "\n"//double GPFIS Crossover
                    + "#,Tournament_Size:," + gpParameters.get(4) + "\n"//double GPFIS Tournament
                    + "#,Maximum_Structure_Iteration:," + gpParameters.get(5) + "\n"//int GPFIS iteration
                    + "#,GP_Training_Model:," + gpParameters.get(6) + "\n"//int GPFIS Training Mode

                    + "#,Metaheuristic_Algorithm:," + mhParameters.get(0) + "\n"//String algorithm
                    //mhParameters.add(1, 0);//int mh dimesion (to be set later)
                    + "#,MH_Algorithm_Population:," + mhParameters.get(2) + "\n"//int mh population 
                    + "#,Maximum_Parameter_Iteration:," + mhParameters.get(3) + "\n"//int iteration

                    + "#,Ensemble_Candidates:," + ensembleParameters.get(0) + "\n"// int number of candidates for ensemble
                    + "#,Ensemble_Diversity:," + ensembleParameters.get(1) + "\n"//String ensemble diversity type
                    + "#,Ensemble_Method_Used:," + ensembleParameters.get(2) + "\n"//String ensemble method
                    + "#,Ensemble_Algorithm_Used:," + ensembleParameters.get(3) + "\n"//String ensemble algorithm
                    + "#,Ensemble_Algorithm_Iteration:," + ensembleParameters.get(4) + "\n"//int ensemble iteration
                    + "#,Ensemble_Algorithm_Population:," + ensembleParameters.get(5) + "\n"//int ensemble population
                    + "#,Ensemble_Algorithm_Search_Min:," + ensembleParameters.get(6) + "\n"//int ensemble population
                    + "#,Ensemble_Algorithm_Search_Max:," + ensembleParameters.get(7) + "\n"//int ensemble population

                    + "#,Cross_Validation:," + cvParameter.get(0) + "\n"//String fold type
                    + "#,Cross_Validation_Manual_Partition:," + cvParameter.get(1) + "\n"//int manual Partition Size
                    + "#,Cross_Validation_Save_Pattern:," + cvParameter.get(2) + "\n"//String fold type
                    + "#,Cross_Validation_External_Fold_Num:," + cvParameter.get(3) + "\n";//int external file name 

            String parms = "";
            for (Object algoParameter : algoParameters) {
                parms = parms + algoParameter + ",";
            }
            fntTrainParam = fntTrainParam + "#,MH_Algorithm_Parms:," + parms + "\n";
            parms = "";
            for (Object ensembleAlgoParameter : ensembleAlgoParameters) {
                parms = parms + ensembleAlgoParameter + ",";
            }
            fntTrainParam = fntTrainParam + "#,MH_EN_Algorithm_Parms:," + parms + "\n";
        }//setparameters

    }//END:AnswerWorkerFNTtraining

    private void printTreeModels() {
        int Ot = (int) fntDataFileInfo.get(2);//total output coulumn
        try {
            holdFeatures = new Vector();
            for (int j = 0; j < Ot; j++) {
                Vector Fval = new Vector();
                for (int k = 0; k < ensembleCandidates; k++) {
                    bestGlobalTree[j][k].printTreeFile(fntStructureTrain + j + "" + k + ".txt");
                    testGraphDraw drawModel = new testGraphDraw();//Retieving Model information
                    drawModel.drawTree(fntStructureTrain + j + "" + k + ".txt", j, k, false);//Drawing and Saving Image
                    drawModel = null;
                    //retrieving features value
                    FileReader fin = new FileReader(fntStructureTrain + j + "" + k + ".txt");
                    int val;
                    try (BufferedReader brProb = new BufferedReader(fin)) {
                        String rootData;
                        while ((rootData = brProb.readLine()) != null) {
                            String[] tokens = rootData.split(",");
                            if (tokens[0].equals("f")) {
                                //do nothing
                            } else {
                                val = (int) Double.parseDouble(tokens[0]);
                                //System.out.println(val);
                                Fval.add(val);
                            }
                        }
                        brProb.close();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, "Operation failed! " + e);
                        System.out.print(e);
                        return;
                    }
                }//m_candidates

                //find the selected features //Select disting values
                Vector FeatureList = find_Distinct_Elements(Fval);
                //Sort Feature List
                Collections.sort(FeatureList);
                //retrive all features list              
                int featuresNumber[] = new int[FeatureList.size()];
                String[] selectedFeatures = new String[FeatureList.size()];
                for (int l = 0; l < FeatureList.size(); l++) {
                    if (problemType.equals("TimeSerise")) {
                        featuresNumber[l] = Integer.parseInt(FeatureList.get(l).toString()) + 1;
                        selectedFeatures[l] = "Index:" + featuresNumber[l] + " Name: " + nameAtr[featuresNumber[l]];
                    } else {
                        featuresNumber[l] = Integer.parseInt(FeatureList.get(l).toString());
                        selectedFeatures[l] = "Index:" + featuresNumber[l] + " Name: " + nameAtr[featuresNumber[l]];
                    }
                    //System.out.println(selectedFeatures[i]);
                }//features for jth output column
                holdFeatures.add(selectedFeatures);
            }//j outputs
            System.out.println("Displaying Image");
            //displaying tree in GUI
            displayTreeModelsImage(Ot, ensembleCandidates, ensembleDiversityType, false);
        } catch (FileNotFoundException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }
    }//print  Tree 

    private Vector find_Distinct_Elements(Vector Fval) {
        int val;
        Vector FeatureList = new Vector();
        for (int i = 0; i < Fval.size(); i++) {
            val = Integer.parseInt(Fval.get(i).toString());
            if (i == 0) {
                FeatureList.add(val);
            } else {
                boolean match = false;
                for (Object FeatureList1 : FeatureList) {
                    if (val == Integer.parseInt(FeatureList1.toString())) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    FeatureList.add(val);
                }
            }
        }
        return FeatureList;
    }//find distinct element

    private void displayTreeModelsImage(int outputCol, int candidates, String diversity, boolean isOld) {
        Images imgs;
        if (diversity.equals("Structural")) {
            imgs = new Images(outputCol, candidates, isOld);//true if to diplay old model
        } else {
            imgs = new Images(outputCol, 1, isOld);//1 for both parametric diversity and no Ensemble
        }
        imgs = null;
    }//display tree

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup_MODEL = new javax.swing.ButtonGroup();
        buttonGroup_RAND = new javax.swing.ButtonGroup();
        buttonGroup_Objective = new javax.swing.ButtonGroup();
        buttonGroup_Model_Sel = new javax.swing.ButtonGroup();
        buttonGroup_SaveData = new javax.swing.ButtonGroup();
        buttonGroup_Randomized = new javax.swing.ButtonGroup();
        buttonGroupRandomizedTest = new javax.swing.ButtonGroup();
        fuzzySetType = new javax.swing.ButtonGroup();
        jTabbedPaneDataselection = new javax.swing.JTabbedPane();
        dataSelect = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jPanel10 = new javax.swing.JPanel();
        showdata_ = new javax.swing.JButton();
        showdata_1 = new javax.swing.JButton();
        showdata_2 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        jTextArea6 = new javax.swing.JTextArea();
        fileBrowsPanel = new javax.swing.JPanel();
        jComboBox_Select_a_Data_File = new javax.swing.JComboBox();
        jComboBox_Select_a_Problem = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jTextField_fileName = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextBox_normLow = new javax.swing.JTextField();
        jTextBox_normHigh = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        loadData_ = new javax.swing.JButton();
        jTextField_Display_Massage = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        jSeparator7 = new javax.swing.JSeparator();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextArea_DataDisplay = new javax.swing.JTextArea();
        jTabbedPaneTraining = new javax.swing.JTabbedPane();
        fntParamPanale = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        trainFNTModel_TRN = new javax.swing.JButton();
        showTrainedModeStructure_ = new javax.swing.JButton();
        showTrainingResult_ = new javax.swing.JButton();
        saveTrainedModel_ = new javax.swing.JButton();
        plotTrainedModel_ = new javax.swing.JButton();
        jLabel30 = new javax.swing.JLabel();
        jRadioButton_RND_AUTO = new javax.swing.JRadioButton();
        jRadioButton_RND_INPUT = new javax.swing.JRadioButton();
        jTextBox_SEED_INPUT = new javax.swing.JTextField();
        jTextField_trainRes = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        jRadioButton_GP_SO = new javax.swing.JRadioButton();
        jRadioButton_GP_MO = new javax.swing.JRadioButton();
        jRadioButton_TRN_OLD = new javax.swing.JRadioButton();
        jRadioButton_TRN_NEW = new javax.swing.JRadioButton();
        jProgressBarDefinite = new javax.swing.JProgressBar();
        jProgressBarIndefinite = new javax.swing.JProgressBar();
        trainFNTModel_STOP = new javax.swing.JButton();
        stopStatus = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jPanel_Parameter = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextBox_Tree_Depth = new javax.swing.JTextField();
        jTextBox_Tree_Arity = new javax.swing.JTextField();
        jTextBox_GP_POP = new javax.swing.JTextField();
        jTextBox_GP_TS = new javax.swing.JTextField();
        jComboBox_WeightsOnly = new javax.swing.JComboBox();
        jComboBox_MH_Algo = new javax.swing.JComboBox();
        jTextBox_MH_POP = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jComboBox_FunType = new javax.swing.JComboBox();
        jLabel18 = new javax.swing.JLabel();
        jTextBox_MF_center_lo = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jTextBox__MF_center_hi = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jTextField_GP_MR = new javax.swing.JTextField();
        jTextField_GP_CR = new javax.swing.JTextField();
        jTextBox_THEN_weight_hi = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jTextBox_THEN_weight_lo = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jTextField_Elitism = new javax.swing.JTextField();
        jRadioButton_sufuling_of_data_yes = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextBox_Max_Gen = new javax.swing.JTextField();
        jTextBox_MH_ITR = new javax.swing.JTextField();
        setParamFromFile_ = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        jComboBox_Ensemble_Diversity = new javax.swing.JComboBox();
        jComboBox_Ensemble_Method = new javax.swing.JComboBox();
        jComboBox_Manual_TRN_Size = new javax.swing.JComboBox();
        jLabel28 = new javax.swing.JLabel();
        jRadioButton6 = new javax.swing.JRadioButton();
        jLabel_EN_2 = new javax.swing.JLabel();
        jLabel_EN_1 = new javax.swing.JLabel();
        jRadioButton5 = new javax.swing.JRadioButton();
        jTextBox_Max_GP_ITR = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jRadioButton9 = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        resetParam_ = new javax.swing.JButton();
        jComboBox_Ensemble_Candidates = new javax.swing.JComboBox();
        jComboBox_FoldType = new javax.swing.JComboBox();
        jLabel17 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        jPanel_ChooseAlgo = new javax.swing.JPanel();
        abcPanel = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        jTextField_abc_trail = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        jComboBox_abc_Food = new javax.swing.JComboBox();
        jLabel45 = new javax.swing.JLabel();
        bfoPanel = new javax.swing.JPanel();
        jLabel40 = new javax.swing.JLabel();
        jTextField_bfo_NC = new javax.swing.JTextField();
        jLabel41 = new javax.swing.JLabel();
        jTextField_bfo_SL = new javax.swing.JTextField();
        jLabel42 = new javax.swing.JLabel();
        jTextField_bfo_RS = new javax.swing.JTextField();
        jLabel43 = new javax.swing.JLabel();
        jTextField_bfo_EL = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        jTextField_bfo_PR = new javax.swing.JTextField();
        jLabel46 = new javax.swing.JLabel();
        jTextField_bfo_RL = new javax.swing.JTextField();
        dePanel = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jTextField_de_CR = new javax.swing.JTextField();
        jLabel48 = new javax.swing.JLabel();
        jTextField_de_F = new javax.swing.JTextField();
        jLabel49 = new javax.swing.JLabel();
        jComboBox_de_stat = new javax.swing.JComboBox();
        gwoPanel = new javax.swing.JPanel();
        jLabel57 = new javax.swing.JLabel();
        psoPanel = new javax.swing.JPanel();
        jLabel58 = new javax.swing.JLabel();
        jTextField_pso_c2 = new javax.swing.JTextField();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jTextField_pso_c0 = new javax.swing.JTextField();
        jTextField_pso_c1 = new javax.swing.JTextField();
        jPanel_ChooseAlgoEN = new javax.swing.JPanel();
        abcPanel1 = new javax.swing.JPanel();
        jLabel61 = new javax.swing.JLabel();
        jTextField_abc_trail1 = new javax.swing.JTextField();
        jLabel62 = new javax.swing.JLabel();
        jComboBox_abc_Food1 = new javax.swing.JComboBox();
        jLabel63 = new javax.swing.JLabel();
        bfoPanel1 = new javax.swing.JPanel();
        jLabel64 = new javax.swing.JLabel();
        jTextField_bfo_NC1 = new javax.swing.JTextField();
        jLabel65 = new javax.swing.JLabel();
        jTextField_bfo_SL1 = new javax.swing.JTextField();
        jLabel66 = new javax.swing.JLabel();
        jTextField_bfo_RS1 = new javax.swing.JTextField();
        jLabel67 = new javax.swing.JLabel();
        jTextField_bfo_EL1 = new javax.swing.JTextField();
        jLabel68 = new javax.swing.JLabel();
        jTextField_bfo_PR1 = new javax.swing.JTextField();
        jLabel69 = new javax.swing.JLabel();
        jTextField_bfo_RL1 = new javax.swing.JTextField();
        dePanel1 = new javax.swing.JPanel();
        jLabel70 = new javax.swing.JLabel();
        jTextField_de_CR1 = new javax.swing.JTextField();
        jLabel71 = new javax.swing.JLabel();
        jTextField_de_F1 = new javax.swing.JTextField();
        jLabel72 = new javax.swing.JLabel();
        jComboBox_de_stat1 = new javax.swing.JComboBox();
        gwoPanel1 = new javax.swing.JPanel();
        jLabel80 = new javax.swing.JLabel();
        psoPanel1 = new javax.swing.JPanel();
        jLabel81 = new javax.swing.JLabel();
        jTextField_pso_c6 = new javax.swing.JTextField();
        jLabel82 = new javax.swing.JLabel();
        jLabel83 = new javax.swing.JLabel();
        jTextField_pso_c4 = new javax.swing.JTextField();
        jTextField_pso_c5 = new javax.swing.JTextField();
        jLabel_EN_4 = new javax.swing.JLabel();
        jLabel_EN_7 = new javax.swing.JLabel();
        jTextBox_enWTmin = new javax.swing.JTextField();
        jLabel_EN_8 = new javax.swing.JLabel();
        jTextBox_enWTmax = new javax.swing.JTextField();
        jLabel_EN_3 = new javax.swing.JLabel();
        jComboBox_MHAlgo1 = new javax.swing.JComboBox();
        jLabel_EN_5 = new javax.swing.JLabel();
        jTextBox_EN_MH_POP = new javax.swing.JTextField();
        jLabel85 = new javax.swing.JLabel();
        jSeparator8 = new javax.swing.JSeparator();
        jLabel_EN_6 = new javax.swing.JLabel();
        jTextBox_EN_MH_ITR = new javax.swing.JTextField();
        jLabel86 = new javax.swing.JLabel();
        jSeparator9 = new javax.swing.JSeparator();
        jLabel24 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jTextBox_MF_width_lo = new javax.swing.JTextField();
        jLabel36 = new javax.swing.JLabel();
        jTextBox_MF_width_hi = new javax.swing.JTextField();
        jSeparator12 = new javax.swing.JSeparator();
        jLabel87 = new javax.swing.JLabel();
        jSeparator13 = new javax.swing.JSeparator();
        jLabel88 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        MF_Dev_1 = new javax.swing.JLabel();
        MF_Dev_2 = new javax.swing.JLabel();
        jTextBox_MF_center_dev_lo = new javax.swing.JTextField();
        MF_Dev_3 = new javax.swing.JLabel();
        jTextBox_MF_center_dev_hi = new javax.swing.JTextField();
        Rule_W_1 = new javax.swing.JLabel();
        Rule_W_2 = new javax.swing.JLabel();
        jTextBox_THEN_weight__dev_lo = new javax.swing.JTextField();
        Rule_W_3 = new javax.swing.JLabel();
        jTextBox_THEN_weight__dev_hi = new javax.swing.JTextField();
        Branch_W_1 = new javax.swing.JLabel();
        Branch_W_2 = new javax.swing.JLabel();
        jTextBox_tree_branch_weight_lo = new javax.swing.JTextField();
        Branch_W_3 = new javax.swing.JLabel();
        jTextBox_tree_branch_weight_hi = new javax.swing.JTextField();
        jLabel50 = new javax.swing.JLabel();
        jTextField_k_Value = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        jTextAreaRunTime = new javax.swing.JTextArea();
        testPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea_TestModelInfo = new javax.swing.JTextArea();
        jPanel7 = new javax.swing.JPanel();
        jRadioButton_current = new javax.swing.JRadioButton();
        jRadioButton_old = new javax.swing.JRadioButton();
        testTrainedModel_ = new javax.swing.JButton();
        showTestModeStructure_ = new javax.swing.JButton();
        plotTestModel_ = new javax.swing.JButton();
        jTextField_testRMSE = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabelTestSize = new javax.swing.JLabel();
        jComboBoxTestPercent = new javax.swing.JComboBox();
        jTextField_testCORREL = new javax.swing.JTextField();
        rmseLabel = new javax.swing.JLabel();
        correlLabel = new javax.swing.JLabel();
        jTextField_testCORREL1 = new javax.swing.JTextField();
        r2Label = new javax.swing.JLabel();
        showdata_3 = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel31 = new javax.swing.JLabel();
        jRadioButton_tst_sufuling_yes = new javax.swing.JRadioButton();
        jRadioButton_tst_sufling_no = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel_study_ref = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        save_ = new javax.swing.JMenuItem();
        exit_ = new javax.swing.JMenuItem();
        help = new javax.swing.JMenu();
        help_ = new javax.swing.JMenuItem();
        keyShortcut_ = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        about_ = new javax.swing.JMenuItem();
        developer_ = new javax.swing.JMenuItem();
        version_ = new javax.swing.JMenuItem();
        referances_ = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        back2Main_ = new javax.swing.JMenuItem();
        back_ = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Flexible Fuzzy Inference System");
        setResizable(false);

        dataSelect.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jTextArea2.setColumns(20);
        jTextArea2.setLineWrap(true);
        jTextArea2.setRows(5);
        jTextArea2.setText("The System can accept ARFF and CSV file format.\nARFF Format: if data is for classification and/or has string/nominal attributes. More specifically, it can read information about the data file such as dataset name, number of attributes, minimum and maximum value of an attributes, name and numbers of input and Output attributes, etc. from the header of the ARFF format file. \n\nThe data into the file must be comma (,) separated. \n\nCSV Format: \nThe first row of CSV must contain attributes name.\nIt must contain only numeric values.\n\nScaling of dataset:\nDefault normalization low : 0.0\nDefault normalization High: 1.0\nThe algorithm automatically normalize the dataset as well as de-normalize the outputs.\n");
        jTextArea2.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(80, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Data File Format", jPanel3);

        showdata_.setForeground(new java.awt.Color(102, 102, 102));
        showdata_.setText("Optional: Check the chosen data File");
        showdata_.setEnabled(false);
        showdata_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showdata_ActionPerformed(evt);
            }
        });

        showdata_1.setForeground(new java.awt.Color(102, 102, 102));
        showdata_1.setText("Optional: Check the filtered file");
        showdata_1.setEnabled(false);
        showdata_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showdata_1ActionPerformed(evt);
            }
        });

        showdata_2.setForeground(new java.awt.Color(102, 102, 102));
        showdata_2.setText("Optional: Check the normalized file");
        showdata_2.setEnabled(false);
        showdata_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showdata_2ActionPerformed(evt);
            }
        });

        jScrollPane3.setBorder(null);
        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jTextArea3.setColumns(20);
        jTextArea3.setLineWrap(true);
        jTextArea3.setRows(5);
        jTextArea3.setText("Before training a model it is good to check the filtered and normalized file to ensure not to have any discrepancy in the loaded data file.\n\nOnly normalized data file will be loaded");
        jTextArea3.setWrapStyleWord(true);
        jScrollPane3.setViewportView(jTextArea3);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3)
                    .addComponent(showdata_2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(showdata_1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(showdata_, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE))
                .addGap(31, 31, 31))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(showdata_)
                .addGap(18, 18, 18)
                .addComponent(showdata_1)
                .addGap(18, 18, 18)
                .addComponent(showdata_2)
                .addContainerGap(268, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Optional Checks", jPanel10);

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Treaining/Test Steps"));

        jScrollPane10.setBorder(null);
        jScrollPane10.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane10.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jTextArea6.setColumns(20);
        jTextArea6.setLineWrap(true);
        jTextArea6.setRows(5);
        jTextArea6.setText("Treaing a Model Steps:\n   Set/Reset Parameters\n   Train a model\n   View the trained models \n   Plot results\n   View Statistics\n   Save the model\n\nTesting the current or old Model: Test, View and Plot\n\nThe reult will be RMSE and Correlation computeed between target and predicted set.");
        jTextArea6.setWrapStyleWord(true);
        jScrollPane10.setViewportView(jTextArea6);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Training Steps", jPanel9);

        fileBrowsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Select a Dataset"));

        jComboBox_Select_a_Data_File.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select a file...." }));
        jComboBox_Select_a_Data_File.setEnabled(false);
        jComboBox_Select_a_Data_File.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_Select_a_Data_FileActionPerformed(evt);
            }
        });

        jComboBox_Select_a_Problem.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select Problem Type...", "Regression", "Classification" }));
        jComboBox_Select_a_Problem.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_Select_a_ProblemItemStateChanged(evt);
            }
        });
        jComboBox_Select_a_Problem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_Select_a_ProblemActionPerformed(evt);
            }
        });

        jButton1.setText("Load File");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextField_fileName.setEditable(false);
        jTextField_fileName.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel10.setText("Data Normalization Low");

        jTextBox_normLow.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_normLow.setText("0.0");
        jTextBox_normLow.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_normLowFocusLost(evt);
            }
        });

        jTextBox_normHigh.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_normHigh.setText("1.0");
        jTextBox_normHigh.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_normHighFocusLost(evt);
            }
        });

        jLabel11.setText("Data Normalization High");

        loadData_.setText("Load Data");
        loadData_.setEnabled(false);
        loadData_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadData_ActionPerformed(evt);
            }
        });

        jTextField_Display_Massage.setEditable(false);
        jTextField_Display_Massage.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        jLabel34.setText("Scaling of the Selected Dataset");

        jTextArea_DataDisplay.setColumns(20);
        jTextArea_DataDisplay.setLineWrap(true);
        jTextArea_DataDisplay.setRows(5);
        jScrollPane5.setViewportView(jTextArea_DataDisplay);

        javax.swing.GroupLayout fileBrowsPanelLayout = new javax.swing.GroupLayout(fileBrowsPanel);
        fileBrowsPanel.setLayout(fileBrowsPanelLayout);
        fileBrowsPanelLayout.setHorizontalGroup(
            fileBrowsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileBrowsPanelLayout.createSequentialGroup()
                .addGroup(fileBrowsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fileBrowsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(fileBrowsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox_Select_a_Data_File, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox_Select_a_Problem, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextField_fileName)))
                    .addGroup(fileBrowsPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(fileBrowsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 175, Short.MAX_VALUE)
                        .addGroup(fileBrowsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextBox_normHigh, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextBox_normLow, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(fileBrowsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(loadData_, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(fileBrowsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator7))
                    .addGroup(fileBrowsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel34)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(fileBrowsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane5))
                    .addComponent(jTextField_Display_Massage, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        fileBrowsPanelLayout.setVerticalGroup(
            fileBrowsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileBrowsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox_Select_a_Problem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBox_Select_a_Data_File, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(jTextField_fileName, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel34)
                .addGap(5, 5, 5)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(fileBrowsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jTextBox_normLow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(fileBrowsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jTextBox_normHigh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(loadData_)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jTextField_Display_Massage, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout dataSelectLayout = new javax.swing.GroupLayout(dataSelect);
        dataSelect.setLayout(dataSelectLayout);
        dataSelectLayout.setHorizontalGroup(
            dataSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataSelectLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fileBrowsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        dataSelectLayout.setVerticalGroup(
            dataSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataSelectLayout.createSequentialGroup()
                .addGroup(dataSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileBrowsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jTabbedPaneDataselection.addTab("Data Selection", dataSelect);

        fntParamPanale.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        trainFNTModel_TRN.setBackground(new java.awt.Color(51, 204, 0));
        trainFNTModel_TRN.setText("Train");
        trainFNTModel_TRN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trainFNTModel_TRNActionPerformed(evt);
            }
        });

        showTrainedModeStructure_.setText("Show Model ");
        showTrainedModeStructure_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTrainedModeStructure_ActionPerformed(evt);
            }
        });

        showTrainingResult_.setText("Show Result");
        showTrainingResult_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTrainingResult_ActionPerformed(evt);
            }
        });

        saveTrainedModel_.setText("Save");
        saveTrainedModel_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveTrainedModel_ActionPerformed(evt);
            }
        });

        plotTrainedModel_.setText("Plot Result");
        plotTrainedModel_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotTrainedModel_ActionPerformed(evt);
            }
        });

        jLabel30.setText("Random Seed");

        buttonGroup_RAND.add(jRadioButton_RND_AUTO);
        jRadioButton_RND_AUTO.setSelected(true);
        jRadioButton_RND_AUTO.setText("Auto");
        jRadioButton_RND_AUTO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_RND_AUTOActionPerformed(evt);
            }
        });

        buttonGroup_RAND.add(jRadioButton_RND_INPUT);
        jRadioButton_RND_INPUT.setText("Input");
        jRadioButton_RND_INPUT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_RND_INPUTActionPerformed(evt);
            }
        });

        jTextBox_SEED_INPUT.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_SEED_INPUT.setText("1429807889589");
        jTextBox_SEED_INPUT.setEnabled(false);
        jTextBox_SEED_INPUT.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_SEED_INPUTFocusLost(evt);
            }
        });

        jTextField_trainRes.setEditable(false);
        jTextField_trainRes.setForeground(new java.awt.Color(255, 102, 102));
        jTextField_trainRes.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel32.setText("Mode of Training");

        buttonGroup_Objective.add(jRadioButton_GP_SO);
        jRadioButton_GP_SO.setSelected(true);
        jRadioButton_GP_SO.setText("Single Objective");
        jRadioButton_GP_SO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_GP_SOActionPerformed(evt);
            }
        });

        buttonGroup_Objective.add(jRadioButton_GP_MO);
        jRadioButton_GP_MO.setText("Multi-Objective");
        jRadioButton_GP_MO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_GP_MOActionPerformed(evt);
            }
        });

        buttonGroup_Model_Sel.add(jRadioButton_TRN_OLD);
        jRadioButton_TRN_OLD.setText("Old");
        jRadioButton_TRN_OLD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_TRN_OLDActionPerformed(evt);
            }
        });

        buttonGroup_Model_Sel.add(jRadioButton_TRN_NEW);
        jRadioButton_TRN_NEW.setSelected(true);
        jRadioButton_TRN_NEW.setText("New");
        jRadioButton_TRN_NEW.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_TRN_NEWActionPerformed(evt);
            }
        });

        jProgressBarDefinite.setStringPainted(true);

        jProgressBarIndefinite.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jProgressBarIndefinite.setString("Start Training");
        jProgressBarIndefinite.setStringPainted(true);

        trainFNTModel_STOP.setBackground(new java.awt.Color(255, 0, 0));
        trainFNTModel_STOP.setText("Stop/Clean");
        trainFNTModel_STOP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trainFNTModel_STOPActionPerformed(evt);
            }
        });

        stopStatus.setText("  Select");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jTextField_trainRes)
                    .addComponent(jProgressBarDefinite, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBarIndefinite, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(trainFNTModel_STOP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(trainFNTModel_TRN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(13, 13, 13)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(stopStatus)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(showTrainingResult_, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jRadioButton_TRN_NEW, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton_TRN_OLD)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(showTrainedModeStructure_, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(saveTrainedModel_, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(plotTrainedModel_, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel32)
                .addGap(18, 18, 18)
                .addComponent(jRadioButton_GP_SO)
                .addGap(18, 18, 18)
                .addComponent(jRadioButton_GP_MO, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(102, 102, 102))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel30)
                .addGap(32, 32, 32)
                .addComponent(jRadioButton_RND_AUTO)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton_RND_INPUT)
                .addGap(18, 18, 18)
                .addComponent(jTextBox_SEED_INPUT)
                .addGap(10, 10, 10))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButton_GP_SO)
                    .addComponent(jRadioButton_GP_MO))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButton_RND_AUTO)
                    .addComponent(jRadioButton_RND_INPUT)
                    .addComponent(jTextBox_SEED_INPUT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(trainFNTModel_TRN)
                    .addComponent(jRadioButton_TRN_NEW)
                    .addComponent(jRadioButton_TRN_OLD)
                    .addComponent(showTrainedModeStructure_)
                    .addComponent(plotTrainedModel_))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(trainFNTModel_STOP)
                    .addComponent(stopStatus)
                    .addComponent(showTrainingResult_)
                    .addComponent(saveTrainedModel_))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField_trainRes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jProgressBarDefinite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBarIndefinite, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel_Parameter.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameter Setup"));

        jLabel3.setText("Max Tree Depth");

        jLabel2.setText("Max Tree Arity");

        jTextBox_Tree_Depth.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_Tree_Depth.setText("5");
        jTextBox_Tree_Depth.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_Tree_DepthFocusLost(evt);
            }
        });

        jTextBox_Tree_Arity.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_Tree_Arity.setText("4");
        jTextBox_Tree_Arity.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_Tree_ArityFocusLost(evt);
            }
        });

        jTextBox_GP_POP.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_GP_POP.setText("20");
        jTextBox_GP_POP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextBox_GP_POPActionPerformed(evt);
            }
        });
        jTextBox_GP_POP.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_GP_POPFocusLost(evt);
            }
        });

        jTextBox_GP_TS.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_GP_TS.setText("2");
        jTextBox_GP_TS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextBox_GP_TSActionPerformed(evt);
            }
        });
        jTextBox_GP_TS.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_GP_TSFocusLost(evt);
            }
        });

        jComboBox_WeightsOnly.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Rules_Parmeters", "Rules_Parmeters_And_Input_Weights", "Inputs_Weights" }));
        jComboBox_WeightsOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_WeightsOnlyActionPerformed(evt);
            }
        });

        jComboBox_MH_Algo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Artificial Bee Colony", "Bacteria Foraging Optimization", "Differential Evolution", "Gray Wolf Optimization", "Particle Swarm Optimization" }));
        jComboBox_MH_Algo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_MH_AlgoActionPerformed(evt);
            }
        });

        jTextBox_MH_POP.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_MH_POP.setText("50");
        jTextBox_MH_POP.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_MH_POPFocusLost(evt);
            }
        });

        jLabel8.setText("Metaheuristic Population     ");

        jLabel12.setText("Metaheuristic Algorithm  ");

        jLabel13.setText("Optimize Tree Parameters         ");

        jLabel9.setText("Tournament Size");

        jLabel7.setText("Genetic Programming Population ");

        jLabel16.setText("Membership Function");

        jComboBox_FunType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Random_Selection", "Gaussian", "Tangent_hyperbolic", "Fermi", "1/(1+((x-c)/w)^2)", "Linear_Tangent_hyperbolic", "Unipolar_Sigmoid", "Bipolar_Sigmoid" }));
        jComboBox_FunType.setSelectedIndex(4);

        jLabel18.setText("MF Param (center of a MF)");

        jTextBox_MF_center_lo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_MF_center_lo.setText("0.0");
        jTextBox_MF_center_lo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_MF_center_loFocusLost(evt);
            }
        });

        jLabel19.setText("Min");

        jLabel20.setText("Max");

        jTextBox__MF_center_hi.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox__MF_center_hi.setText("1.0");
        jTextBox__MF_center_hi.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox__MF_center_hiFocusLost(evt);
            }
        });

        jLabel22.setText("Mutation Rate [0,1]");

        jLabel23.setText("Crossover Rate");

        jTextField_GP_MR.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_GP_MR.setText("0.2");
        jTextField_GP_MR.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField_GP_MRFocusLost(evt);
            }
        });

        jTextField_GP_CR.setEditable(false);
        jTextField_GP_CR.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_GP_CR.setText("0.8");
        jTextField_GP_CR.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField_GP_CRFocusLost(evt);
            }
        });

        jTextBox_THEN_weight_hi.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_THEN_weight_hi.setText("1.0");
        jTextBox_THEN_weight_hi.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_THEN_weight_hiFocusLost(evt);
            }
        });

        jLabel25.setText("Max");

        jTextBox_THEN_weight_lo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_THEN_weight_lo.setText("0.0");
        jTextBox_THEN_weight_lo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_THEN_weight_loFocusLost(evt);
            }
        });

        jLabel26.setText("Min");

        jLabel27.setText("Concequent Params :");

        jLabel29.setText("Elitism [Preserve Best]");

        jTextField_Elitism.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_Elitism.setText("2");
        jTextField_Elitism.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_ElitismActionPerformed(evt);
            }
        });
        jTextField_Elitism.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField_ElitismFocusLost(evt);
            }
        });

        buttonGroup_Randomized.add(jRadioButton_sufuling_of_data_yes);
        jRadioButton_sufuling_of_data_yes.setSelected(true);
        jRadioButton_sufuling_of_data_yes.setText("Yes");
        jRadioButton_sufuling_of_data_yes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_sufuling_of_data_yesActionPerformed(evt);
            }
        });

        jLabel5.setText("Max GP Iteration");

        jLabel6.setText("Metaheuristic Iteration");

        jTextBox_Max_Gen.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_Max_Gen.setText("1");
        jTextBox_Max_Gen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextBox_Max_GenMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jTextBox_Max_GenMouseExited(evt);
            }
        });
        jTextBox_Max_Gen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextBox_Max_GenActionPerformed(evt);
            }
        });
        jTextBox_Max_Gen.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_Max_GenFocusLost(evt);
            }
        });

        jTextBox_MH_ITR.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_MH_ITR.setText("100");
        jTextBox_MH_ITR.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jTextBox_MH_ITRMouseExited(evt);
            }
        });
        jTextBox_MH_ITR.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_MH_ITRFocusLost(evt);
            }
        });

        setParamFromFile_.setText("Browse Setup");
        setParamFromFile_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setParamFromFile_ActionPerformed(evt);
            }
        });

        jLabel21.setText("Cross-Validation");

        jComboBox_Ensemble_Diversity.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Structural", "Parametric" }));
        jComboBox_Ensemble_Diversity.setEnabled(false);

        jComboBox_Ensemble_Method.setEnabled(false);
        jComboBox_Ensemble_Method.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_Ensemble_MethodActionPerformed(evt);
            }
        });

        jComboBox_Manual_TRN_Size.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10", "20", "30", "40", "50", "60", "70", "80", "90", "100" }));
        jComboBox_Manual_TRN_Size.setSelectedIndex(4);
        jComboBox_Manual_TRN_Size.setEnabled(false);
        jComboBox_Manual_TRN_Size.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_Manual_TRN_SizeActionPerformed(evt);
            }
        });

        jLabel28.setText("Training-Set (in %)");
        jLabel28.setEnabled(false);

        buttonGroup_SaveData.add(jRadioButton6);
        jRadioButton6.setSelected(true);
        jRadioButton6.setText("No");
        jRadioButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton6ActionPerformed(evt);
            }
        });

        jLabel_EN_2.setText("Mathod of Ensemble");
        jLabel_EN_2.setEnabled(false);

        jLabel_EN_1.setText("Ensemble Diversity");
        jLabel_EN_1.setEnabled(false);

        buttonGroup_SaveData.add(jRadioButton5);
        jRadioButton5.setText("Yes");
        jRadioButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton5ActionPerformed(evt);
            }
        });

        jTextBox_Max_GP_ITR.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_Max_GP_ITR.setText("50");
        jTextBox_Max_GP_ITR.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_Max_GP_ITRFocusLost(evt);
            }
        });
        jTextBox_Max_GP_ITR.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jTextBox_Max_GP_ITRMouseExited(evt);
            }
        });

        jLabel14.setText("Number of Models");

        jLabel15.setText("Save CV Data Patterns");

        buttonGroup_Randomized.add(jRadioButton9);
        jRadioButton9.setText("No");
        jRadioButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton9ActionPerformed(evt);
            }
        });

        jLabel4.setText("Max General Iteration");

        resetParam_.setText("Reset");
        resetParam_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetParam_ActionPerformed(evt);
            }
        });

        jComboBox_Ensemble_Candidates.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        jComboBox_Ensemble_Candidates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_Ensemble_CandidatesActionPerformed(evt);
            }
        });

        jComboBox_FoldType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "k_Fold", "5_x_2_Fold", "Manual_Partition", "Partitioned_File" }));
        jComboBox_FoldType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_FoldTypeActionPerformed(evt);
            }
        });

        jLabel17.setText("Randomized Data Pattern");

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel33.setText("Model Training Criterion");

        jPanel_ChooseAlgo.setLayout(new java.awt.CardLayout());

        abcPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Artificial Bee Colony"));

        jLabel38.setText("Food Size");

        jTextField_abc_trail.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_abc_trail.setText("100");
        jTextField_abc_trail.setToolTipText("");

        jLabel39.setText("Trail Limit");

        jComboBox_abc_Food.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", " " }));
        jComboBox_abc_Food.setSelectedIndex(4);

        jLabel45.setText("% of Population");

        javax.swing.GroupLayout abcPanelLayout = new javax.swing.GroupLayout(abcPanel);
        abcPanel.setLayout(abcPanelLayout);
        abcPanelLayout.setHorizontalGroup(
            abcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(abcPanelLayout.createSequentialGroup()
                .addGap(124, 124, 124)
                .addGroup(abcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(abcPanelLayout.createSequentialGroup()
                        .addComponent(jLabel39)
                        .addGap(66, 66, 66)
                        .addComponent(jTextField_abc_trail, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(abcPanelLayout.createSequentialGroup()
                        .addComponent(jLabel38)
                        .addGap(64, 64, 64)
                        .addComponent(jComboBox_abc_Food, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(jLabel45))))
        );
        abcPanelLayout.setVerticalGroup(
            abcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(abcPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(abcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jTextField_abc_trail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel39))
                .addGap(20, 20, 20)
                .addGroup(abcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel45)
                    .addComponent(jComboBox_abc_Food, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel38)))
        );

        jPanel_ChooseAlgo.add(abcPanel, "card4");

        bfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Bacteria Foreging Optimization"));

        jLabel40.setText("Chemotactic Steps");

        jTextField_bfo_NC.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_bfo_NC.setText("10");

        jLabel41.setText("Swim Length");

        jTextField_bfo_SL.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_bfo_SL.setText("5");

        jLabel42.setText("Reproduction Steps");

        jTextField_bfo_RS.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_bfo_RS.setText("10");

        jLabel43.setText("Elimination-Dispersal Steps");

        jTextField_bfo_EL.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_bfo_EL.setText("10");

        jLabel44.setText("Elimination Probability");

        jTextField_bfo_PR.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_bfo_PR.setText("0.25");

        jLabel46.setText("Run Length");

        jTextField_bfo_RL.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_bfo_RL.setText("0.05");

        javax.swing.GroupLayout bfoPanelLayout = new javax.swing.GroupLayout(bfoPanel);
        bfoPanel.setLayout(bfoPanelLayout);
        bfoPanelLayout.setHorizontalGroup(
            bfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bfoPanelLayout.createSequentialGroup()
                .addGap(120, 120, 120)
                .addGroup(bfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(bfoPanelLayout.createSequentialGroup()
                        .addComponent(jLabel40)
                        .addGap(41, 41, 41)
                        .addComponent(jTextField_bfo_NC, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(bfoPanelLayout.createSequentialGroup()
                        .addComponent(jLabel41)
                        .addGap(70, 70, 70)
                        .addComponent(jTextField_bfo_SL, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(bfoPanelLayout.createSequentialGroup()
                        .addComponent(jLabel42)
                        .addGap(36, 36, 36)
                        .addComponent(jTextField_bfo_RS, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(bfoPanelLayout.createSequentialGroup()
                        .addComponent(jLabel43)
                        .addGap(3, 3, 3)
                        .addComponent(jTextField_bfo_EL, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(bfoPanelLayout.createSequentialGroup()
                        .addComponent(jLabel44)
                        .addGap(27, 27, 27)
                        .addComponent(jTextField_bfo_PR, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(bfoPanelLayout.createSequentialGroup()
                        .addComponent(jLabel46)
                        .addGap(75, 75, 75)
                        .addComponent(jTextField_bfo_RL, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        bfoPanelLayout.setVerticalGroup(
            bfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel40)
                    .addComponent(jTextField_bfo_NC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel41)
                    .addComponent(jTextField_bfo_SL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel42)
                    .addComponent(jTextField_bfo_RS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel43)
                    .addComponent(jTextField_bfo_EL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel44)
                    .addComponent(jTextField_bfo_PR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel46)
                    .addComponent(jTextField_bfo_RL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel_ChooseAlgo.add(bfoPanel, "card5");

        dePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Differential Evolution"));

        jLabel47.setText("Crossover Factor");

        jTextField_de_CR.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_de_CR.setText("0.9");

        jLabel48.setText("Stratagy");

        jTextField_de_F.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_de_F.setText("0.7");

        jLabel49.setText("Weight Factor");

        jComboBox_de_stat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DE/rand-to-best/1/bin", "DE/rand-to-best/2/bin", "DE/best/2/bin" }));

        javax.swing.GroupLayout dePanelLayout = new javax.swing.GroupLayout(dePanel);
        dePanel.setLayout(dePanelLayout);
        dePanelLayout.setHorizontalGroup(
            dePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dePanelLayout.createSequentialGroup()
                .addGap(111, 111, 111)
                .addGroup(dePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dePanelLayout.createSequentialGroup()
                        .addComponent(jLabel47)
                        .addGap(67, 67, 67)
                        .addComponent(jTextField_de_CR, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dePanelLayout.createSequentialGroup()
                        .addComponent(jLabel49)
                        .addGap(82, 82, 82)
                        .addComponent(jTextField_de_F, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dePanelLayout.createSequentialGroup()
                        .addComponent(jLabel48)
                        .addGap(48, 48, 48)
                        .addComponent(jComboBox_de_stat, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        dePanelLayout.setVerticalGroup(
            dePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel47)
                    .addComponent(jTextField_de_CR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(dePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel49)
                    .addComponent(jTextField_de_F, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(dePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel48)
                    .addComponent(jComboBox_de_stat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel_ChooseAlgo.add(dePanel, "card6");

        gwoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Gray Wolf Optimization"));

        jLabel57.setText("No parameters to set");

        javax.swing.GroupLayout gwoPanelLayout = new javax.swing.GroupLayout(gwoPanel);
        gwoPanel.setLayout(gwoPanelLayout);
        gwoPanelLayout.setHorizontalGroup(
            gwoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gwoPanelLayout.createSequentialGroup()
                .addGap(154, 154, 154)
                .addComponent(jLabel57))
        );
        gwoPanelLayout.setVerticalGroup(
            gwoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gwoPanelLayout.createSequentialGroup()
                .addGap(84, 84, 84)
                .addComponent(jLabel57))
        );

        jPanel_ChooseAlgo.add(gwoPanel, "card8");

        psoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Particle Swarm Optimization"));

        jLabel58.setText("Social/Global Weight");

        jTextField_pso_c2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_pso_c2.setText("1.49445");

        jLabel59.setText("Cognitive/Local Weight");

        jLabel60.setText("Inertia Weight");

        jTextField_pso_c0.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_pso_c0.setText("0.729");

        jTextField_pso_c1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_pso_c1.setText("1.49445");

        javax.swing.GroupLayout psoPanelLayout = new javax.swing.GroupLayout(psoPanel);
        psoPanel.setLayout(psoPanelLayout);
        psoPanelLayout.setHorizontalGroup(
            psoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(psoPanelLayout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addGroup(psoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(psoPanelLayout.createSequentialGroup()
                        .addComponent(jLabel58, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(jTextField_pso_c2, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE))
                    .addGroup(psoPanelLayout.createSequentialGroup()
                        .addGroup(psoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel59, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(psoPanelLayout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(jLabel60, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(17, 17, 17)
                        .addGroup(psoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField_pso_c1, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                            .addComponent(jTextField_pso_c0))))
                .addContainerGap(125, Short.MAX_VALUE))
        );
        psoPanelLayout.setVerticalGroup(
            psoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(psoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(psoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel60)
                    .addComponent(jTextField_pso_c0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(psoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel59)
                    .addComponent(jTextField_pso_c1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(psoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel58)
                    .addComponent(jTextField_pso_c2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel_ChooseAlgo.add(psoPanel, "card9");

        jPanel_ChooseAlgoEN.setLayout(new java.awt.CardLayout());

        abcPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Artificial Bee Colony"));

        jLabel61.setText("Food Size");

        jTextField_abc_trail1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_abc_trail1.setText("100");
        jTextField_abc_trail1.setToolTipText("");

        jLabel62.setText("Trail Limit");

        jComboBox_abc_Food1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", " " }));
        jComboBox_abc_Food1.setSelectedIndex(4);

        jLabel63.setText("% of Population");

        javax.swing.GroupLayout abcPanel1Layout = new javax.swing.GroupLayout(abcPanel1);
        abcPanel1.setLayout(abcPanel1Layout);
        abcPanel1Layout.setHorizontalGroup(
            abcPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(abcPanel1Layout.createSequentialGroup()
                .addGap(124, 124, 124)
                .addGroup(abcPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(abcPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel62)
                        .addGap(66, 66, 66)
                        .addComponent(jTextField_abc_trail1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(abcPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel61)
                        .addGap(64, 64, 64)
                        .addComponent(jComboBox_abc_Food1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(jLabel63))))
        );
        abcPanel1Layout.setVerticalGroup(
            abcPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(abcPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(abcPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jTextField_abc_trail1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel62))
                .addGap(20, 20, 20)
                .addGroup(abcPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel63)
                    .addComponent(jComboBox_abc_Food1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel61)))
        );

        jPanel_ChooseAlgoEN.add(abcPanel1, "card4");

        bfoPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Bacteria Foreging Optimization"));

        jLabel64.setText("Chemotactic Steps");

        jTextField_bfo_NC1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_bfo_NC1.setText("10");

        jLabel65.setText("Swim Length");

        jTextField_bfo_SL1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_bfo_SL1.setText("5");

        jLabel66.setText("Reproduction Steps");

        jTextField_bfo_RS1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_bfo_RS1.setText("10");

        jLabel67.setText("Elimination-Dispersal Steps");

        jTextField_bfo_EL1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_bfo_EL1.setText("10");

        jLabel68.setText("Elimination Probability");

        jTextField_bfo_PR1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_bfo_PR1.setText("0.25");

        jLabel69.setText("Run Length");

        jTextField_bfo_RL1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_bfo_RL1.setText("0.05");

        javax.swing.GroupLayout bfoPanel1Layout = new javax.swing.GroupLayout(bfoPanel1);
        bfoPanel1.setLayout(bfoPanel1Layout);
        bfoPanel1Layout.setHorizontalGroup(
            bfoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bfoPanel1Layout.createSequentialGroup()
                .addGap(120, 120, 120)
                .addGroup(bfoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(bfoPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel64)
                        .addGap(41, 41, 41)
                        .addComponent(jTextField_bfo_NC1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(bfoPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel65)
                        .addGap(70, 70, 70)
                        .addComponent(jTextField_bfo_SL1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(bfoPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel66)
                        .addGap(36, 36, 36)
                        .addComponent(jTextField_bfo_RS1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(bfoPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel67)
                        .addGap(3, 3, 3)
                        .addComponent(jTextField_bfo_EL1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(bfoPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel68)
                        .addGap(27, 27, 27)
                        .addComponent(jTextField_bfo_PR1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(bfoPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel69)
                        .addGap(75, 75, 75)
                        .addComponent(jTextField_bfo_RL1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        bfoPanel1Layout.setVerticalGroup(
            bfoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bfoPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bfoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel64)
                    .addComponent(jTextField_bfo_NC1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bfoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel65)
                    .addComponent(jTextField_bfo_SL1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bfoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel66)
                    .addComponent(jTextField_bfo_RS1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bfoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel67)
                    .addComponent(jTextField_bfo_EL1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bfoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel68)
                    .addComponent(jTextField_bfo_PR1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bfoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel69)
                    .addComponent(jTextField_bfo_RL1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel_ChooseAlgoEN.add(bfoPanel1, "card5");

        dePanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Differential Evolution"));

        jLabel70.setText("Crossover Factor");

        jTextField_de_CR1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_de_CR1.setText("0.9");

        jLabel71.setText("Stratagy");

        jTextField_de_F1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_de_F1.setText("0.7");

        jLabel72.setText("Weight Factor");

        jComboBox_de_stat1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DE/rand-to-best/1/bin", "DE/rand-to-best/2/bin", "DE/best/2/bin" }));

        javax.swing.GroupLayout dePanel1Layout = new javax.swing.GroupLayout(dePanel1);
        dePanel1.setLayout(dePanel1Layout);
        dePanel1Layout.setHorizontalGroup(
            dePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dePanel1Layout.createSequentialGroup()
                .addGap(111, 111, 111)
                .addGroup(dePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dePanel1Layout.createSequentialGroup()
                        .addComponent(jLabel70)
                        .addGap(67, 67, 67)
                        .addComponent(jTextField_de_CR1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dePanel1Layout.createSequentialGroup()
                        .addComponent(jLabel72)
                        .addGap(82, 82, 82)
                        .addComponent(jTextField_de_F1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dePanel1Layout.createSequentialGroup()
                        .addComponent(jLabel71)
                        .addGap(48, 48, 48)
                        .addComponent(jComboBox_de_stat1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        dePanel1Layout.setVerticalGroup(
            dePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dePanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel70)
                    .addComponent(jTextField_de_CR1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(dePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel72)
                    .addComponent(jTextField_de_F1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(dePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel71)
                    .addComponent(jComboBox_de_stat1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel_ChooseAlgoEN.add(dePanel1, "card6");

        gwoPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Gray Wolf Optimization"));

        jLabel80.setText("No parameters to set");

        javax.swing.GroupLayout gwoPanel1Layout = new javax.swing.GroupLayout(gwoPanel1);
        gwoPanel1.setLayout(gwoPanel1Layout);
        gwoPanel1Layout.setHorizontalGroup(
            gwoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gwoPanel1Layout.createSequentialGroup()
                .addGap(154, 154, 154)
                .addComponent(jLabel80))
        );
        gwoPanel1Layout.setVerticalGroup(
            gwoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gwoPanel1Layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addComponent(jLabel80))
        );

        jPanel_ChooseAlgoEN.add(gwoPanel1, "card8");

        psoPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Particle Swarm Optimization"));

        jLabel81.setText("Social/Global Weight");

        jTextField_pso_c6.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_pso_c6.setText("1.49445");

        jLabel82.setText("Cognitive/Local Weight");

        jLabel83.setText("Inertia Weight");

        jTextField_pso_c4.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_pso_c4.setText("0.729");

        jTextField_pso_c5.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_pso_c5.setText("1.49445");

        javax.swing.GroupLayout psoPanel1Layout = new javax.swing.GroupLayout(psoPanel1);
        psoPanel1.setLayout(psoPanel1Layout);
        psoPanel1Layout.setHorizontalGroup(
            psoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(psoPanel1Layout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addGroup(psoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(psoPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel81, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(jTextField_pso_c6, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE))
                    .addGroup(psoPanel1Layout.createSequentialGroup()
                        .addGroup(psoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel82, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(psoPanel1Layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(jLabel83, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(17, 17, 17)
                        .addGroup(psoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField_pso_c5, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                            .addComponent(jTextField_pso_c4))))
                .addContainerGap(125, Short.MAX_VALUE))
        );
        psoPanel1Layout.setVerticalGroup(
            psoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(psoPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(psoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel83)
                    .addComponent(jTextField_pso_c4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(psoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel82)
                    .addComponent(jTextField_pso_c5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(psoPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel81)
                    .addComponent(jTextField_pso_c6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel_ChooseAlgoEN.add(psoPanel1, "card9");

        jLabel_EN_4.setText("Weight Range");
        jLabel_EN_4.setEnabled(false);

        jLabel_EN_7.setText("Min");
        jLabel_EN_7.setEnabled(false);

        jTextBox_enWTmin.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_enWTmin.setText("0.0");
        jTextBox_enWTmin.setEnabled(false);
        jTextBox_enWTmin.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_enWTminFocusLost(evt);
            }
        });

        jLabel_EN_8.setText("Max");
        jLabel_EN_8.setEnabled(false);

        jTextBox_enWTmax.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_enWTmax.setText("1.0");
        jTextBox_enWTmax.setEnabled(false);
        jTextBox_enWTmax.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_enWTmaxFocusLost(evt);
            }
        });

        jLabel_EN_3.setText("Metaheuristic Algorithm  ");
        jLabel_EN_3.setEnabled(false);

        jComboBox_MHAlgo1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Artificial Bee Colony", "Bacteria Foraging Optimization", "Differential Evolution", "Gray Wolf Optimization", "Particle Swarm Optimization" }));
        jComboBox_MHAlgo1.setEnabled(false);
        jComboBox_MHAlgo1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_MHAlgo1ActionPerformed(evt);
            }
        });

        jLabel_EN_5.setText("Metaheuristic Population     ");
        jLabel_EN_5.setEnabled(false);

        jTextBox_EN_MH_POP.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_EN_MH_POP.setText("50");
        jTextBox_EN_MH_POP.setEnabled(false);
        jTextBox_EN_MH_POP.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_EN_MH_POPFocusLost(evt);
            }
        });

        jLabel85.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel85.setText("Ensemble Setup ");

        jLabel_EN_6.setText("Metaheuristic Iterations");
        jLabel_EN_6.setEnabled(false);

        jTextBox_EN_MH_ITR.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_EN_MH_ITR.setText("1000");
        jTextBox_EN_MH_ITR.setEnabled(false);
        jTextBox_EN_MH_ITR.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_EN_MH_ITRFocusLost(evt);
            }
        });

        jLabel86.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel86.setText("Tree Setup ");

        jLabel24.setText("MF Param (width of a MF)");

        jLabel35.setText("Min");

        jTextBox_MF_width_lo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_MF_width_lo.setText("0.0");
        jTextBox_MF_width_lo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_MF_width_loFocusLost(evt);
            }
        });

        jLabel36.setText("Max");

        jTextBox_MF_width_hi.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_MF_width_hi.setText("1.0");
        jTextBox_MF_width_hi.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_MF_width_hiFocusLost(evt);
            }
        });

        jLabel87.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel87.setText("Fuzzy Rule Setup ");

        jLabel88.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel88.setText("Metaheurisic Setup ");

        jLabel37.setText("Fuzzy System Type");

        fuzzySetType.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("Type-I");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        fuzzySetType.add(jRadioButton2);
        jRadioButton2.setText("Type-II");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        MF_Dev_1.setText("MF Param Deviation");
        MF_Dev_1.setEnabled(false);

        MF_Dev_2.setText("Min");
        MF_Dev_2.setEnabled(false);

        jTextBox_MF_center_dev_lo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_MF_center_dev_lo.setText("0.0");
        jTextBox_MF_center_dev_lo.setEnabled(false);
        jTextBox_MF_center_dev_lo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_MF_center_dev_loFocusLost(evt);
            }
        });

        MF_Dev_3.setText("Max");
        MF_Dev_3.setEnabled(false);

        jTextBox_MF_center_dev_hi.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_MF_center_dev_hi.setText("1.0");
        jTextBox_MF_center_dev_hi.setEnabled(false);
        jTextBox_MF_center_dev_hi.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_MF_center_dev_hiFocusLost(evt);
            }
        });

        Rule_W_1.setText("Concequent Deviation");
        Rule_W_1.setEnabled(false);

        Rule_W_2.setText("Min");
        Rule_W_2.setEnabled(false);

        jTextBox_THEN_weight__dev_lo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_THEN_weight__dev_lo.setText("0.0");
        jTextBox_THEN_weight__dev_lo.setEnabled(false);
        jTextBox_THEN_weight__dev_lo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_THEN_weight__dev_loFocusLost(evt);
            }
        });

        Rule_W_3.setText("Max");
        Rule_W_3.setEnabled(false);

        jTextBox_THEN_weight__dev_hi.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_THEN_weight__dev_hi.setText("1.0");
        jTextBox_THEN_weight__dev_hi.setEnabled(false);
        jTextBox_THEN_weight__dev_hi.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_THEN_weight__dev_hiFocusLost(evt);
            }
        });

        Branch_W_1.setText("Weighted input");
        Branch_W_1.setEnabled(false);

        Branch_W_2.setText("Min");
        Branch_W_2.setEnabled(false);

        jTextBox_tree_branch_weight_lo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_tree_branch_weight_lo.setText("0.0");
        jTextBox_tree_branch_weight_lo.setEnabled(false);
        jTextBox_tree_branch_weight_lo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_tree_branch_weight_loFocusLost(evt);
            }
        });

        Branch_W_3.setText("Max");
        Branch_W_3.setEnabled(false);

        jTextBox_tree_branch_weight_hi.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBox_tree_branch_weight_hi.setText("1.0");
        jTextBox_tree_branch_weight_hi.setEnabled(false);
        jTextBox_tree_branch_weight_hi.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBox_tree_branch_weight_hiFocusLost(evt);
            }
        });

        jLabel50.setText("Number of Folds");

        jTextField_k_Value.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField_k_Value.setText("5");

        javax.swing.GroupLayout jPanel_ParameterLayout = new javax.swing.GroupLayout(jPanel_Parameter);
        jPanel_Parameter.setLayout(jPanel_ParameterLayout);
        jPanel_ParameterLayout.setHorizontalGroup(
            jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator12))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator9))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator8))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel_ChooseAlgo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator6))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextBox_GP_TS, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextBox_Tree_Depth, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextBox_Max_Gen, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel29, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                                    .addComponent(jLabel22, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField_GP_MR, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextBox_Max_GP_ITR, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(Branch_W_1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(Rule_W_1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(MF_Dev_1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel24, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel_EN_1, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel_EN_2, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel27, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                                        .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextBox_MF_width_lo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextBox_MF_width_hi, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                                        .addComponent(MF_Dev_2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextBox_MF_center_dev_lo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(MF_Dev_3, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextBox_MF_center_dev_hi, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                                        .addComponent(Rule_W_2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextBox_THEN_weight__dev_lo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(Rule_W_3, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextBox_THEN_weight__dev_hi, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                                        .addComponent(Branch_W_2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextBox_tree_branch_weight_lo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(Branch_W_3, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextBox_tree_branch_weight_hi, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jTextBox_MH_POP, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel_ChooseAlgoEN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel_EN_4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel_EN_7, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextBox_enWTmin, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel_EN_8, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextBox_enWTmax, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel_EN_3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jComboBox_MHAlgo1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel_EN_5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextBox_EN_MH_POP, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                .addComponent(jLabel85)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                .addComponent(jLabel_EN_6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextBox_EN_MH_ITR, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextBox_MH_ITR, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                .addComponent(jLabel86)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(setParamFromFile_, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                        .addComponent(jLabel50)
                                        .addGap(18, 18, 18)
                                        .addComponent(jTextField_k_Value, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(jComboBox_Ensemble_Diversity, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jTextBox_Tree_Arity, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jTextBox_GP_POP, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jTextField_Elitism, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jTextField_GP_CR, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                                                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(jTextBox_MF_center_lo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(jTextBox__MF_center_hi, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                                                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(jTextBox_THEN_weight_lo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(jTextBox_THEN_weight_hi, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(jComboBox_MH_Algo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 195, Short.MAX_VALUE)
                                                .addComponent(jComboBox_Ensemble_Method, javax.swing.GroupLayout.Alignment.TRAILING, 0, 195, Short.MAX_VALUE)
                                                .addComponent(resetParam_, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jComboBox_WeightsOnly, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jComboBox_FoldType, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jComboBox_Manual_TRN_Size, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jComboBox_Ensemble_Candidates, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                                .addComponent(jLabel28)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                            .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                                .addComponent(jRadioButton5)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jRadioButton6))
                                            .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                                .addComponent(jRadioButton_sufuling_of_data_yes)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jRadioButton9))))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_ParameterLayout.createSequentialGroup()
                                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel33)
                                    .addComponent(jLabel88)
                                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                                    .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                        .addComponent(jRadioButton1)
                                        .addGap(37, 37, 37)
                                        .addComponent(jRadioButton2))
                                    .addComponent(jComboBox_FunType, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                                .addComponent(jLabel87)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jSeparator13, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        jPanel_ParameterLayout.setVerticalGroup(
            jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_ParameterLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel86, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextBox_Max_Gen))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextBox_Tree_Depth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jTextBox_Tree_Arity)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jTextBox_GP_POP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jTextBox_Max_GP_ITR)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jTextField_Elitism, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jTextField_GP_MR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jTextField_GP_CR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextBox_GP_TS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(jLabel87, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator12, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton1)
                    .addComponent(jLabel37))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox_FunType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(jTextBox_MF_center_lo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(jTextBox__MF_center_hi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel35)
                    .addComponent(jTextBox_MF_width_lo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36)
                    .addComponent(jTextBox_MF_width_hi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(MF_Dev_2)
                    .addComponent(jTextBox_MF_center_dev_lo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MF_Dev_3)
                    .addComponent(jTextBox_MF_center_dev_hi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MF_Dev_1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26)
                    .addComponent(jTextBox_THEN_weight_lo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25)
                    .addComponent(jTextBox_THEN_weight_hi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(Rule_W_1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Rule_W_2)
                    .addComponent(jTextBox_THEN_weight__dev_lo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Rule_W_3)
                    .addComponent(jTextBox_THEN_weight__dev_hi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox_WeightsOnly, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(Branch_W_1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Branch_W_2)
                    .addComponent(jTextBox_tree_branch_weight_lo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Branch_W_3)
                    .addComponent(jTextBox_tree_branch_weight_hi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel88, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator13, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox_MH_Algo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextBox_MH_POP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextBox_MH_ITR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel_ChooseAlgo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel85, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator8, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel14)
                    .addComponent(jComboBox_Ensemble_Candidates, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel_EN_1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox_Ensemble_Diversity))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel_EN_2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox_Ensemble_Method, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox_MHAlgo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_EN_3, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel_EN_4)
                    .addComponent(jLabel_EN_7)
                    .addComponent(jTextBox_enWTmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_EN_8)
                    .addComponent(jTextBox_enWTmax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextBox_EN_MH_POP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_EN_5, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextBox_EN_MH_ITR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_EN_6, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel_ChooseAlgoEN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox_FoldType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBox_Manual_TRN_Size, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel28))
                    .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jTextField_k_Value, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel50)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel17)
                    .addComponent(jRadioButton_sufuling_of_data_yes)
                    .addComponent(jRadioButton9))
                .addGap(8, 8, 8)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel15)
                    .addComponent(jRadioButton5)
                    .addComponent(jRadioButton6))
                .addGap(18, 18, 18)
                .addGroup(jPanel_ParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(setParamFromFile_)
                    .addComponent(resetParam_)))
        );

        jScrollPane8.setViewportView(jPanel_Parameter);
        jPanel_Parameter.getAccessibleContext().setAccessibleParent(jScrollPane8);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Training Observation..."));

        jTextAreaRunTime.setEditable(false);
        jTextAreaRunTime.setColumns(20);
        jTextAreaRunTime.setLineWrap(false);
        jTextAreaRunTime.setRows(5);
        jTextAreaRunTime.setWrapStyleWord(false);
        jTextAreaRunTime.setBorder(null);
        jScrollPane9.setViewportView(jTextAreaRunTime);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane9)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout fntParamPanaleLayout = new javax.swing.GroupLayout(fntParamPanale);
        fntParamPanale.setLayout(fntParamPanaleLayout);
        fntParamPanaleLayout.setHorizontalGroup(
            fntParamPanaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fntParamPanaleLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 522, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fntParamPanaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(15, 15, 15))
        );
        fntParamPanaleLayout.setVerticalGroup(
            fntParamPanaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fntParamPanaleLayout.createSequentialGroup()
                .addGroup(fntParamPanaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fntParamPanaleLayout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(fntParamPanaleLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPaneTraining.addTab("Train a Model", fntParamPanale);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Model Information"));

        jTextArea_TestModelInfo.setEditable(false);
        jTextArea_TestModelInfo.setBackground(new java.awt.Color(240, 240, 240));
        jTextArea_TestModelInfo.setColumns(20);
        jTextArea_TestModelInfo.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        jTextArea_TestModelInfo.setLineWrap(true);
        jTextArea_TestModelInfo.setRows(5);
        jScrollPane2.setViewportView(jTextArea_TestModelInfo);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Test Criteria"));

        buttonGroup_MODEL.add(jRadioButton_current);
        jRadioButton_current.setSelected(true);
        jRadioButton_current.setText("Current Model");
        jRadioButton_current.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_currentActionPerformed(evt);
            }
        });

        buttonGroup_MODEL.add(jRadioButton_old);
        jRadioButton_old.setText("Old Model");
        jRadioButton_old.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_oldActionPerformed(evt);
            }
        });

        testTrainedModel_.setText("Test Model");
        testTrainedModel_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testTrainedModel_ActionPerformed(evt);
            }
        });

        showTestModeStructure_.setText("Show Model Structure");
        showTestModeStructure_.setEnabled(false);
        showTestModeStructure_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTestModeStructure_ActionPerformed(evt);
            }
        });

        plotTestModel_.setText("Plot Test");
        plotTestModel_.setEnabled(false);
        plotTestModel_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotTestModel_ActionPerformed(evt);
            }
        });

        jTextField_testRMSE.setEditable(false);

        jLabel1.setText("Test");

        jLabelTestSize.setText("Input Test Sample Size (in %)");
        jLabelTestSize.setEnabled(false);

        jComboBoxTestPercent.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10", "20", "30", "40", "50", "60", "70", "80", "90", "100" }));
        jComboBoxTestPercent.setSelectedIndex(3);
        jComboBoxTestPercent.setEnabled(false);

        jTextField_testCORREL.setEditable(false);

        rmseLabel.setText("RMSE");

        correlLabel.setText("Correlation:");

        jTextField_testCORREL1.setEditable(false);

        r2Label.setText("R2");

        showdata_3.setForeground(new java.awt.Color(102, 102, 102));
        showdata_3.setText("Optional: Open Test File");
        showdata_3.setEnabled(false);
        showdata_3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showdata_3ActionPerformed(evt);
            }
        });

        jLabel31.setText("Randomized Data Pattern");

        buttonGroupRandomizedTest.add(jRadioButton_tst_sufuling_yes);
        jRadioButton_tst_sufuling_yes.setSelected(true);
        jRadioButton_tst_sufuling_yes.setText("Yes");
        jRadioButton_tst_sufuling_yes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_tst_sufuling_yesActionPerformed(evt);
            }
        });

        buttonGroupRandomizedTest.add(jRadioButton_tst_sufling_no);
        jRadioButton_tst_sufling_no.setText("No");
        jRadioButton_tst_sufling_no.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_tst_sufling_noActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator4)
                    .addComponent(showTestModeStructure_, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(testTrainedModel_, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(plotTestModel_, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addComponent(rmseLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField_testRMSE, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(correlLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField_testCORREL, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(r2Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField_testCORREL1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(showdata_3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(17, 17, 17)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton_old)
                            .addComponent(jRadioButton_current))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel31)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                        .addComponent(jRadioButton_tst_sufuling_yes)
                        .addGap(18, 18, 18)
                        .addComponent(jRadioButton_tst_sufling_no))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabelTestSize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jComboBoxTestPercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton_current)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton_old)
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(jRadioButton_tst_sufuling_yes)
                    .addComponent(jRadioButton_tst_sufling_no))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelTestSize)
                    .addComponent(jComboBoxTestPercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(testTrainedModel_)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(showTestModeStructure_)
                .addGap(18, 18, 18)
                .addComponent(plotTestModel_)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField_testRMSE, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rmseLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_testCORREL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(correlLabel))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_testCORREL1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(r2Label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(showdata_3)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout testPanelLayout = new javax.swing.GroupLayout(testPanel);
        testPanel.setLayout(testPanelLayout);
        testPanelLayout.setHorizontalGroup(
            testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(testPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        testPanelLayout.setVerticalGroup(
            testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(testPanelLayout.createSequentialGroup()
                .addGroup(testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        jTabbedPaneTraining.addTab("Test a Model", testPanel);

        jTabbedPaneDataselection.addTab("FIS modelling", jTabbedPaneTraining);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1017, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 558, Short.MAX_VALUE)
        );

        jTabbedPaneDataselection.addTab("Parameter Setting Instruction", jPanel1);

        javax.swing.GroupLayout jPanel_study_refLayout = new javax.swing.GroupLayout(jPanel_study_ref);
        jPanel_study_ref.setLayout(jPanel_study_refLayout);
        jPanel_study_refLayout.setHorizontalGroup(
            jPanel_study_refLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1017, Short.MAX_VALUE)
        );
        jPanel_study_refLayout.setVerticalGroup(
            jPanel_study_refLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 558, Short.MAX_VALUE)
        );

        jTabbedPaneDataselection.addTab("Study Refernce", jPanel_study_ref);

        jMenu1.setText("File");

        save_.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        save_.setText("Save");
        save_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_ActionPerformed(evt);
            }
        });
        jMenu1.add(save_);

        exit_.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        exit_.setText("Exit");
        exit_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exit_ActionPerformed(evt);
            }
        });
        jMenu1.add(exit_);

        jMenuBar1.add(jMenu1);

        help.setText("Help");

        help_.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        help_.setText("Help Contents");
        help_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                help_ActionPerformed(evt);
            }
        });
        help.add(help_);

        keyShortcut_.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_MASK));
        keyShortcut_.setText("Keyboard Shortcuts");
        keyShortcut_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyShortcut_ActionPerformed(evt);
            }
        });
        help.add(keyShortcut_);
        help.add(jSeparator1);

        about_.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.ALT_MASK));
        about_.setText("About");
        about_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                about_ActionPerformed(evt);
            }
        });
        help.add(about_);

        developer_.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.ALT_MASK));
        developer_.setText("Developers");
        developer_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                developer_ActionPerformed(evt);
            }
        });
        help.add(developer_);

        version_.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.ALT_MASK));
        version_.setText("Version Info");
        version_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                version_ActionPerformed(evt);
            }
        });
        help.add(version_);

        referances_.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.ALT_MASK));
        referances_.setText("References");
        referances_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                referances_ActionPerformed(evt);
            }
        });
        help.add(referances_);
        help.add(jSeparator2);

        back2Main_.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK));
        back2Main_.setText("Start Page");
        back2Main_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                back2Main_ActionPerformed(evt);
            }
        });
        help.add(back2Main_);

        jMenuBar1.add(help);

        back_.setText("Main");
        back_.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                back_MouseClicked(evt);
            }
        });
        jMenuBar1.add(back_);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneDataselection)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneDataselection, javax.swing.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public double denormalize(double x, double dataLow, double dataHigh, double normalizedLow, double normalizedHigh) {
        return ((dataLow - dataHigh) * x - normalizedHigh * dataLow + dataHigh * normalizedLow) / (normalizedLow - normalizedHigh);
    }

    private void trainFNTModel_TRNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trainFNTModel_TRNActionPerformed
        if (!isDataLoaded) {
            JOptionPane.showMessageDialog(this, "Data is not loaded!");
            return;//necessary return
        } else {
            executionHistory = "";
            jTextField_trainRes.setText("");
            jTextField_testRMSE.setText("");
            //answerWorker = null;//make things clear al the timer
            answerWorker = new AnswerWorkerFNTtraining(this);
            answerWorker.execute();
            isTrainingStarted = true;
            stopStatus.setText("  Started");
        }//end training 
    }//GEN-LAST:event_trainFNTModel_TRNActionPerformed

    private void testCurrentModel() {
        int Ot = (int) fntDataFileInfo.get(2);//total output coulumn
        boolean isClassification = (boolean) fntDataFileInfo.get(4);//propble type
        String rmse = "";
        String r = "";
        String r2 = "";
        if (!isClassification) {
            for (int j = 0; j < Ot; j++) {
                Statistics statistics = new Statistics();
                double[] statTest = statistics.statistics(InitiatorFrame.absoluteFilePathOut + "ensembleTest.csv", j, isClassification);
                rmse = rmse + statTest[0] + ", ";
                r = r + statTest[1] + ", ";
                r2 = r2 + statTest[2] + ", ";
            }//j ouputs
            jTextField_testRMSE.setForeground(Color.blue);
            jTextField_testRMSE.setText(rmse);
            jTextField_testCORREL.setText(r);
            jTextField_testCORREL1.setText(r2);
        } else {
            Statistics statistics = new Statistics();
            double[] statTest = statistics.statistics(InitiatorFrame.absoluteFilePathOut + "ensembleTest.csv", Ot, isClassification);
            rmseLabel.setText("Accuracy");
            correlLabel.setText("Precision");
            r2Label.setText("Recal");
            jTextField_testRMSE.setForeground(Color.blue);
            jTextField_testRMSE.setText(statTest[0] + "");
            jTextField_testCORREL.setText(statTest[2] + "");
            jTextField_testCORREL1.setText(statTest[3] + "");
        }
        String modelInfo;
        modelInfo = "The problem used for experment:" + "\n";
        modelInfo = modelInfo + dataInfo + "\n";
        modelInfo = modelInfo + "The parameter used for experment:" + "\n";
        modelInfo = modelInfo + fntTrainParam + "\n";
        modelInfo = modelInfo + "The results' Statistics: " + "\n";
        modelInfo = modelInfo + modelStatistic + "\n";
        modelInfo = modelInfo + "The selected features:" + "\n";
        String SList = "";
        for (int j = 0; j < Ot; j++) {
            String[] selectedFeatures = (String[]) holdFeatures.get(j);
            SList = SList + "\nFor output " + j + " selected features are " + selectedFeatures.length;
            for (int k = 0; k < selectedFeatures.length; k++) {
                SList = SList + "\n  " + k + " " + selectedFeatures[k];
            }//k models
        }//j outputs
        modelInfo = modelInfo + SList + "\n";
        jTextArea_TestModelInfo.setText(modelInfo);
        isTested = true;
        showTestModeStructure_.setEnabled(true);
        plotTestModel_.setEnabled(true);
    }//test regression

    private FuzzyFNT[][] readOldModel() {
        long SEED;
        if (fntRNGSeedType.equalsIgnoreCase("input")) {
            SEED = Long.parseLong(jTextBox_SEED_INPUT.getText());
            fntRNG = new MersenneTwisterFast(SEED);
            fntRNG.setIsAuto(false);
        } else {
            SEED = System.currentTimeMillis();
            fntRNG = new MersenneTwisterFast(SEED);
            fntRNG.setIsAuto(true);
        }//if

        int Ot = (int) fntDataFileInfo.get(2);//total output coulumn
        int numPatterns = (int) fntDataFileInfo.get(3);
        FileFilter filter = new FileNameExtensionFilter("FNT Model Files", "txt", "fnt");
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File myFileLocal = chooser.getSelectedFile();

            String fileNameLocal = myFileLocal + "";

            oldModel_ensemble_Weights = preProceessFile2bTested(fileNameLocal);

            oldFNTmodel = new FuzzyFNT[Ot][m_Old_Ensemble_NumInt];
            //read FuzzyFNT structures/models
            for (int j = 0; j < Ot; j++) {
                for (int k = 0; k < m_Old_Ensemble_NumInt; k++) {
                    oldFNTmodel[j][k] = new FuzzyFNT(fntRNG);
                    oldFNTmodel[j][k].readSavedFNTmodel(fntStructureTest + j + "" + k + ".txt");
                }//k models
            }//j outputs
            System.out.println("\n FNT Model succefuly read");
            oldModelRead = true;
        } else {
            oldModelRead = false;
            System.out.println("\n Reading was canceled");
        }
        return oldFNTmodel;
    }//Model read

    private void testOldModel() {
        int In = (int) fntDataFileInfo.get(1);//total input coulumn
        int Ot = (int) fntDataFileInfo.get(2);//total output coulumn
        boolean isClassification = (boolean) fntDataFileInfo.get(4);//propble type
        fntDataFileInfo.set(11, needSufuling);// need suffuling

        if (m_Old_Model_Diversity.equals("Structural")) {
            for (int j = 0; j < Ot; j++) {
                for (int k = 0; k < m_Old_Ensemble_NumInt; k++) {
                    testGraphDraw drawModel = new testGraphDraw();//Retieving Model information
                    drawModel.drawTree(fntStructureTest + j + "" + k + ".txt", j, k, true);//Saving Old image
                    drawModel = null;
                }//k models
            }//j outputs
        } else {//if diversity of paramter / single model
            testGraphDraw drawModel = new testGraphDraw();//Retieving Model information
            drawModel.drawTree(fntStructureTest + 0 + "" + 0 + ".txt", 0, 0, true);//Drawing and Saving Image
            drawModel = null;
        }
        //oldFNTmodel.printTree();
        //oldFNTmodel.printTreeFile(InitiatorFrame.absolutePathOut+"FNT_StructureTest.txt");

        int totalTestPat = Integer.parseInt(jComboBoxTestPercent.getSelectedItem().toString());
        System.out.println("Patterns to read: " + totalTestPat + "%");
        //Changes in fntDataFileInfo
        //fntDataFileInfo.add(totalTestPat);
        Pattern[] patRandom = ReadDataFromFile.readDataFile(fntDataFileInfo, totalTestPat, fntRNG);
        //evaluating the saved model
        EvaluationFunction ev = new EvaluationFunction(fntDataFileInfo);
        isDataLoaded = ev.loadDataTest(patRandom, In, Ot);
        if (isDataLoaded) {
            System.out.println("\nData Loaded for FNT Model testing");
        } else {
            return;//necessary return
        }

        try {
            FileWriter fw = new FileWriter(InitiatorFrame.absoluteFilePathOut + "outputTestOld.csv");
            BufferedWriter bw = new BufferedWriter(fw);
            try (PrintWriter fileTestOld = new PrintWriter(bw)) {
                ev.test(oldFNTmodel, m_Old_Ensemble_NumInt, fileTestOld);//creating ensemble test oputput file
                fileTestOld.close();
            }
            bw.close();
            fw.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
            return;
        }
        String testEnsembleFilePath = InitiatorFrame.absoluteFilePathOut;
        if (!isClassification) {
            Ensemble ensemble = new Ensemble();
            
            ensemble.ensambleRegTest(testEnsembleFilePath,oldModel_ensemble_Weights, m_Old_Ensemble_NumInt, m_En_method, Ot);
            String rmse = "";
            String r = "";
            String r2 = "";
            for (int j = 0; j < Ot; j++) {
                Statistics statistics = new Statistics();
                double[] statTest = statistics.statistics(InitiatorFrame.absoluteFilePathOut + "ensembleTestOld.csv", j, isClassification);
                rmse = rmse + statTest[0] + ", ";
                r = r + statTest[1] + ", ";
                r2 = r2 + statTest[2] + ", ";
            }//j ouputs
            jTextField_testRMSE.setForeground(Color.blue);
            jTextField_testRMSE.setText(rmse);
            jTextField_testCORREL.setText(r);
            jTextField_testCORREL1.setText(r2);
        } else {
            Ensemble ensemble = new Ensemble();
            ensemble.ensambleClassTest(testEnsembleFilePath, oldModel_ensemble_Weights, m_Old_Ensemble_NumInt, m_En_method, Ot);
            Statistics statistics = new Statistics();
            double[] statTest = statistics.statistics(InitiatorFrame.absoluteFilePathOut + "ensembleTestOld.csv", Ot, isClassification);
            tstMatrixOld = "\n" + statistics.matriPrint;
            rmseLabel.setText("Accuracy");
            correlLabel.setText("Precision");
            r2Label.setText("Recal");
            jTextField_testRMSE.setForeground(Color.blue);
            jTextField_testRMSE.setText(statTest[0] + "");
            jTextField_testCORREL.setText(statTest[2] + "");
            jTextField_testCORREL1.setText(statTest[3] + "");
        }

        //Display Information
        String information = "";
        try {
            FileReader fin = new FileReader(InitiatorFrame.absoluteFilePathOut + "ModelOldRes.txt");
            BufferedReader brProb = new BufferedReader(fin);
            String rootData;
            while ((rootData = brProb.readLine()) != null) {
                information = information + rootData + "\n";
            }
            brProb.close();
            fin.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
            return;
        }

        jTextArea_TestModelInfo.setText(information);
        isTested = true;
        showdata_3.setEnabled(isTested);
        showTestModeStructure_.setEnabled(isTested);
        plotTestModel_.setEnabled(isTested);
        if (!isClassification) {
            showdata_3.setEnabled(isTested);
        }//if        
    }//test Regression Old

    private void testTrainedModel_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testTrainedModel_ActionPerformed
        if (jRadioButton_current.isSelected() && isTrained) {
            testCurrentModel();
        } else if (jRadioButton_old.isSelected() && isDataLoaded) {
            //Cleaning output folder (deleting files in folder Except the data and triaing files)
            File outputFolder = new File(InitiatorFrame.absoluteFilePathOut);
            final File[] files = outputFolder.listFiles();
            for (File f1 : files) {
                System.out.println(f1.getName());
                if (f1.getName().equalsIgnoreCase("outputTestOld.csv") || f1.getName().equalsIgnoreCase("ensembleTestOld.csv")) {
                    f1.delete();
                } else if (f1.getName().equalsIgnoreCase("ModelOldRes.txt")) {
                    f1.delete();
                } else if (f1.getName().contains("Old")) {
                    f1.delete();
                } else {
                    //Do Not delete data and training files
                }
            }
            readOldModel();
            if (oldModelRead) {
                testOldModel();
            }
        } else {
            if (jRadioButton_current.isSelected()) {
                JOptionPane.showMessageDialog(this, "No model trained currently!");
            } else {
                JOptionPane.showMessageDialog(this, "Load a dataset");
            }
        }//if
    }//GEN-LAST:event_testTrainedModel_ActionPerformed

    private double[][] preProceessFile2bTested(String fileName) {
        int In = (int) fntDataFileInfo.get(1);//total input coulumn
        int Ot = (int) fntDataFileInfo.get(2);//total output coulumn 
        boolean isClassification = (boolean) fntDataFileInfo.get(4);//propble type

        int totalModel = 1;
        int modelsNum = 1;
        boolean isOldisClassification = false;
        int inputOld = 1;
        int outputOld = 1;
        Vector ENweights = new Vector();

        try {
            FileReader fin = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fin);

            FileWriter fw = new FileWriter(InitiatorFrame.absoluteFilePathOut + "ModelOldRes.txt");
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pr = new PrintWriter(bw);
            String line;
            int j = 0;
            while ((line = br.readLine()) != null) {
                if (line.contains("%,Problem:")) {
                    String[] tokens = line.split(",");
                    isOldisClassification = tokens[2].equals("Classification");
                    System.out.println(isOldisClassification);
                } else if (line.contains("%,Input_Features:")) {
                    String[] tokens = line.split(",");
                    inputOld = Integer.parseInt(tokens[2]);
                    System.out.println(inputOld);
                } else if (line.contains("%,Output_Featuers:")) {
                    String[] tokens = line.split(",");
                    outputOld = Integer.parseInt(tokens[2]);
                    System.out.println(outputOld);
                } else if (line.contains("#,Ensemble_Diversity:")) {
                    String[] tokens = line.split(",");
                    m_Old_Model_Diversity = tokens[2];
                    System.out.println(m_Old_Model_Diversity);
                    pr.println(line);
                } else if (line.contains("#,Ensemble_Method_Used:")) {
                    String[] tokens = line.split(",");
                    m_En_method = tokens[2];
                    System.out.println(m_En_method);
                    pr.println(line);
                } else if (line.contains("#,Ensemble_Weights_Output_:")) {
                    ENweights.add(line);
                    System.out.println(line);
                    pr.println(line);
                } else if (line.contains("#,Ensemble_Candidates:")) {
                    String[] tokens = line.split(",");
                    modelsNum = Integer.parseInt(tokens[2]);
                    System.out.println(modelsNum);
                    pr.println(line);
                } else if (line.contains("$MODEL(S):")) {
                    String[] tokens = line.split(",");
                    totalModel = Integer.parseInt(tokens[1]);
                    System.out.println(totalModel);
                } else if (line.contains("$")) {
                    //don't do anything
                } else {
                    pr.println(line);
                }
            }
            pr.close();
            br.close();
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
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
            JOptionPane.showMessageDialog(this, "Operation failed! Selected model and Loaded data doesnot match!");
            return null;
        }
        m_Old_Ensemble_NumInt = modelsNum;

        double[][] oldEnWeights = new double[outputOld][modelsNum];
        for (int j = 0; j < outputOld; j++) {
            String[] tokens = ENweights.get(j).toString().split(",");
            if (Integer.parseInt(tokens[2]) == j) {//reading the coreect weights
                for (int k = 0; k < modelsNum; k++) {
                    oldEnWeights[j][k] = Double.parseDouble(tokens[k + 3]);
                    System.out.print(" " + oldEnWeights[j][k]);
                }
                System.out.println();
            } else {
                JOptionPane.showMessageDialog(this, "Operation failed! File read issues!");
                return null;
            }
        }
        try {
            for (int j = 0; j < outputOld; j++) {
                for (int k = 0; k < modelsNum; k++) {
                    FileReader fin = new FileReader(fileName);
                    BufferedReader br = new BufferedReader(fin);

                    FileWriter fw = new FileWriter(fntStructureTest + j + "" + k + ".txt");
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
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }
        return oldEnWeights;
    }//preprocess

    private void plotTrainedModel_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotTrainedModel_ActionPerformed
        if (!isTrained) {
            JOptionPane.showMessageDialog(this, "Train a Model");
        } else {
            int Ot = (int) fntDataFileInfo.get(2);//total output coulumn
            int numPatterns = (int) fntDataFileInfo.get(3);
            boolean isClassification = (boolean) fntDataFileInfo.get(4);//propble type
            if (!isClassification) {
                plotOutputFiles("training", "ensembleTrain.csv", numPatterns, Ot);
            } else {
                FramesCall.showMatrix("\n" + trnMatrix, false);//false beacause it is not tested
            }
        }
    }//GEN-LAST:event_plotTrainedModel_ActionPerformed

    private void plotOutputFiles(String setName, String fileName, int points, int outputColumns) {
        //Options for the combo box dialog
        fileName = InitiatorFrame.absoluteFilePathOut + fileName;
        String[] choicesPoints = {"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
        String[] choicesPlot = {"fitting: traget and prediction", "regression plot"};
        JComboBox comboBox1 = new JComboBox(choicesPoints);
        comboBox1.setSelectedIndex(3);
        JComboBox comboBox2 = new JComboBox(choicesPlot);
        comboBox2.setSelectedIndex(0);
        Object[] Fields = {"Points to Plot", comboBox1, "Plot Type", comboBox2};
        int option = JOptionPane.showConfirmDialog(null, Fields, "Plot Setting", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int plotPoints = (10 + comboBox1.getSelectedIndex() * 10);
            //System.out.println("Points" + plotPoints);
            String plotType = "";
            String PlotName = "";
            String picked = comboBox2.getSelectedItem().toString();
            if (picked.equals("fitting: traget and prediction")) {
                plotType = "prediction";
                PlotName = setName + " Plot: target (in red) vs. predicted (in blue)";
            } else {
                plotType = "regression";
                PlotName = setName + " Plot: scatter plot";
            }
            for (int j = 0; j < outputColumns; j++) {
                new EasyPtPlot(fileName, plotType, PlotName + " Output :" + (j + 1), plotPoints, j);
            }
        } else {
            //System.out.println("Plot Canceled");
        }
    }//plot output files 


    private void plotTestModel_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotTestModel_ActionPerformed
        int Ot = (int) fntDataFileInfo.get(2);//total output coulumn
        int numPatterns = (int) fntDataFileInfo.get(3);
        boolean isClassification = (boolean) fntDataFileInfo.get(4);//propble type

        if (!isTested && !isDataLoaded) {
            JOptionPane.showMessageDialog(this, "No Model Available Or Dataset Not Loaded!");
        } else {
            if (jRadioButton_current.isSelected() && isTrained) {
                if (!isClassification) {
                    int points = (int) (numPatterns - 0.7 * numPatterns);
                    plotOutputFiles("test", "ensembleTest.csv", points, Ot);
                } else {
                    FramesCall.showMatrix("\n" + tstMatrix, true);//true beacause it is tested
                }
                isTested = true;
            } else if (jRadioButton_old.isSelected()) {
                if (!isClassification) {
                    int points = 10 + jComboBoxTestPercent.getSelectedIndex() * 10;
                    points = (int) points * numPatterns;
                    plotOutputFiles("test", "ensembleTestOld.csv", points, Ot);
                } else {
                    FramesCall.showMatrix("\n" + tstMatrixOld, true);//true beacause it is tested
                }
                isTested = true;
            } else {
                JOptionPane.showMessageDialog(this, "No Model Selected!");
            }
        }
    }//GEN-LAST:event_plotTestModel_ActionPerformed

    private void saveTrainedModel_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTrainedModel_ActionPerformed
        if (!isTrained) {
            JOptionPane.showMessageDialog(this, "Train a Model");
            return;
        } else {
            saveCurrentModel();
        }//saved
    }//GEN-LAST:event_saveTrainedModel_ActionPerformed

    private void exit_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exit_ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exit_ActionPerformed

    private void help_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_help_ActionPerformed
        FramesCall.helpCall();
    }//GEN-LAST:event_help_ActionPerformed

    private void keyShortcut_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyShortcut_ActionPerformed
        FramesCall.keyShoutcut();
    }//GEN-LAST:event_keyShortcut_ActionPerformed

    private void about_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_about_ActionPerformed
        FramesCall.aboutTool();
    }//GEN-LAST:event_about_ActionPerformed

    private void developer_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_developer_ActionPerformed
        FramesCall.developers();
    }//GEN-LAST:event_developer_ActionPerformed

    private void version_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_version_ActionPerformed
        FramesCall.versions();
    }//GEN-LAST:event_version_ActionPerformed

    private void back2Main_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_back2Main_ActionPerformed
        FramesCall.initiator();
        this.dispose();
    }//GEN-LAST:event_back2Main_ActionPerformed

    private void back_MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_back_MouseClicked
        FramesCall.initiator();
        this.dispose();
    }//GEN-LAST:event_back_MouseClicked

    private void save_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_ActionPerformed
        if (!isTrained) {
            JOptionPane.showMessageDialog(this, "Model is not trained!");
            return;
        } else {
            saveCurrentModel();
        }//saved
    }//GEN-LAST:event_save_ActionPerformed

    public void saveCurrentModel() {
        int Ot = (int) fntDataFileInfo.get(2);//total output coulumn
        try {
            JFileChooser saveFile;
            saveFile = new JFileChooser();
            //chooser.setCurrentDirectory(new java.io.File("."));
            saveFile.setDialogTitle("Create a directory and Save the Model");
            saveFile.setAcceptAllFileFilterUsed(false);
            saveFile.addChoosableFileFilter(new FileNameExtensionFilter("Model Files", "fnt", "dat", "txt"));
            saveFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
            String fileSuggetion = "FIS_" + FSMFType + (String) fntDataFileInfo.get(13);//taking dataset Name
            saveFile.setSelectedFile(new File(fileSuggetion));
            //File myFileLocal = null;
            File myFileDirectory = null;
            if (saveFile.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                myFileDirectory = saveFile.getSelectedFile();
                //myFileDirectory = saveFile.getCurrentDirectory();
                System.out.println("getCurrentDirectory(): " + saveFile.getCurrentDirectory());
                System.out.println("getSelectedFile() : " + saveFile.getSelectedFile());
            } else {
                //System.out.println("No Selection ");
            }
            //String fileNameLocal = myFileLocal + "";
            String fileDirectoryLocal = myFileDirectory + "";// + File.separator + datasetName;
            File directory = new File(fileDirectoryLocal);
            if (!directory.exists()) {
                directory.mkdir();
            } else {
                //System.out.println("Folder is already exists");
                //JOptionPane.showMessageDialog(this, "Folder already exists: Try new Name!");
                return;//return if null string found
            }
            fileDirectoryLocal = fileDirectoryLocal + File.separator + fileSuggetion;
            //bestGlobalTree[0].printTreeFile(fileNameLocal + "Model.fnt");
            //bestGlobalTree[0].printTree();
            FileWriter fw = new FileWriter(fileDirectoryLocal + "Model.txt");
            BufferedWriter bw = new BufferedWriter(fw);
            try (PrintWriter pr = new PrintWriter(bw)) {
                pr.println("Person who conduct Experiment:");
                pr.println(InitiatorFrame.personData);
                pr.println("The problem used for experment:");
                pr.println(dataInfo);
                pr.println("The parameter used for experment:");
                pr.println(fntTrainParam);
                pr.println("The results' Statistics: ");
                pr.println(modelStatistic);
                pr.println("The selected features:");
                String SList = "";
                for (int j = 0; j < Ot; j++) {
                    String[] selectedFeatures = (String[]) holdFeatures.get(j);
                    SList = SList + "\nFor output " + j + " selected features are " + selectedFeatures.length;
                    for (int k = 0; k < selectedFeatures.length; k++) {
                        SList = SList + "\n  " + k + " " + selectedFeatures[k];
                    }//k models
                }//j outputs
                pr.println(SList);
                //pr.println("The program execution report");
                //pr.println(executionHistory);
                pr.println("$MODEL(S):," + ensembleCandidates * Ot);
                for (int j = 0; j < Ot; j++) {
                    for (int k = 0; k < ensembleCandidates; k++) {
                        try (FileReader fin = new FileReader(fntStructureTrain + j + "" + k + ".txt")) {
                            BufferedReader brProb = new BufferedReader(fin);
                            String rootData;
                            pr.println("$Model " + j + "" + k + ":");
                            while ((rootData = brProb.readLine()) != null) {
                                pr.println("$M" + j + "" + k + "@" + rootData);
                            }
                            brProb.close();
                        }
                        pr.println();
                    }//all models
                }//all outputs
                pr.close();
            }

            //saving the training and test output files
            FileReader fin;
            BufferedReader brProb;
            FileWriter fwTrn;
            BufferedWriter bwTrn;
            PrintWriter prTrn;
            String data;

            fin = new FileReader(InitiatorFrame.absoluteFilePathOut + "outputTrain.csv");
            brProb = new BufferedReader(fin);
            fwTrn = new FileWriter(fileDirectoryLocal + "TrainOut.csv");
            bwTrn = new BufferedWriter(fwTrn);
            prTrn = new PrintWriter(bwTrn);
            while ((data = brProb.readLine()) != null) {
                prTrn.println(data);
            }//end printing
            brProb.close();
            fin.close();
            prTrn.close();
            fwTrn.close();

            fin = new FileReader(InitiatorFrame.absoluteFilePathOut + "outputTest.csv");
            brProb = new BufferedReader(fin);
            fwTrn = new FileWriter(fileDirectoryLocal + "TestOut.csv");
            bwTrn = new BufferedWriter(fwTrn);
            prTrn = new PrintWriter(bwTrn);
            while ((data = brProb.readLine()) != null) {
                prTrn.println(data);
            }//end printing
            brProb.close();
            fin.close();
            prTrn.close();
            fwTrn.close();
            //end file writting
            //saving image
            if (ensembleDiversityType.equals("Structural")) {
                for (int j = 0; j < Ot; j++) {
                    for (int k = 0; k < ensembleCandidates; k++) {
                        Image img = null;
                        img = ImageIO.read(new File(InitiatorFrame.absoluteFilePathOut + "SaveImage" + j + "" + k + ".png"));
                        String ImageName = fileDirectoryLocal + "Image" + j + "" + k + ".png";
                        ImageIO.write((RenderedImage) img, "png", new File(ImageName));
                        img.flush();
                    }//all models
                }//all outputs   
            } else {
                for (int j = 0; j < Ot; j++) {
                    int k = 0;
                    Image img = null;
                    img = ImageIO.read(new File(InitiatorFrame.absoluteFilePathOut + "SaveImage" + j + "" + k + ".png"));
                    String ImageName = fileDirectoryLocal + "Image.png";
                    ImageIO.write((RenderedImage) img, "png", new File(ImageName));
                    img.flush();
                }//all outputs 
            }//else modle image saved
        } catch (HeadlessException | IOException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }
    }//saveCurrent model

    private void showTrainedModeStructure_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTrainedModeStructure_ActionPerformed
        if (!isTrained) {
            JOptionPane.showMessageDialog(this, "Train a Model");
            return;
        } else {
            int Ot = (int) fntDataFileInfo.get(2);//total output coulumn
            displayTreeModelsImage(Ot, ensembleCandidates, ensembleDiversityType, false);
        }
    }//GEN-LAST:event_showTrainedModeStructure_ActionPerformed

    private void showTrainingResult_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTrainingResult_ActionPerformed
        if (!isTrained) {
            JOptionPane.showMessageDialog(this, "Train a Model");
            return;
        } else {
            boolean isClassification = (boolean) fntDataFileInfo.get(4);//propble type
            FramesCall.showResult(fntTrainParam, executionHistory, modelStatistic, holdFeatures, nameAtr, dataInfo, isClassification);
        }
    }//GEN-LAST:event_showTrainingResult_ActionPerformed

    private void showTestModeStructure_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTestModeStructure_ActionPerformed
        int Ot = (int) fntDataFileInfo.get(2);//total output coulumn
        if (!isTested && !isDataLoaded) {
            JOptionPane.showMessageDialog(this, "No model available Or dataset not loaded!");
        } else {
            if (jRadioButton_current.isSelected() && isTrained) {
                displayTreeModelsImage(Ot, ensembleCandidates, ensembleDiversityType, false);
            } else if (jRadioButton_old.isSelected() && isDataLoaded) {
                displayTreeModelsImage(Ot, m_Old_Ensemble_NumInt, m_Old_Model_Diversity, true);
            }//Oldmodel
        }//else
    }//GEN-LAST:event_showTestModeStructure_ActionPerformed

    private void referances_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_referances_ActionPerformed
        FramesCall.referance();
    }//GEN-LAST:event_referances_ActionPerformed

    private void showdata_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showdata_ActionPerformed
        if (!isFileChosen) {
            JOptionPane.showMessageDialog(this, "Selected A Data File!");
            return;
        } else {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(dataFile);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Operation failed! " + e);
                    System.out.print(e);
                }
            } else {
                JOptionPane.showMessageDialog(this, "File Not Supported!");
                return;
            }

        }
    }//GEN-LAST:event_showdata_ActionPerformed

    private void showdata_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showdata_1ActionPerformed
        FramesCall.filterData();
    }//GEN-LAST:event_showdata_1ActionPerformed

    private void showdata_2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showdata_2ActionPerformed
        FramesCall.normalizedData();
    }//GEN-LAST:event_showdata_2ActionPerformed

    private void loadData_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadData_ActionPerformed
        if (!isFileChosen) {
            JOptionPane.showMessageDialog(this, "Selected a data file!");
            return;//necessary return
        } else {
            //Cleaning output folder (deleting every file in folder)
//            File outputFolder = new File(InitiatorFrame.absoluteFilePathOut);
//            final File[] files = outputFolder.listFiles();
//            for (File f1 : files) {
//                //System.out.println(f1.getName());
//                f1.delete();
//            }
            ////outputFolder.delete();//Be careful using this comond
            new DataWorker(this).execute();
            showdata_1.setEnabled(true);
            showdata_2.setEnabled(true);
        }
    }//GEN-LAST:event_loadData_ActionPerformed

    private void showdata_3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showdata_3ActionPerformed
        if (Desktop.isDesktopSupported()) {
            try {
                if (jRadioButton_current.isSelected() && isTrained) {
                    Desktop.getDesktop().open(new File(InitiatorFrame.absoluteFilePathOut + "ensembleTest.csv"));
                } else if (jRadioButton_old.isSelected() && isDataLoaded) {
                    Desktop.getDesktop().open(new File(InitiatorFrame.absoluteFilePathOut + "ensembleTestOld.csv"));
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Operation failed! " + e);
                System.out.print(e);
            }
        }
    }//GEN-LAST:event_showdata_3ActionPerformed

    private void jTextBox_normLowFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_normLowFocusLost
        try {
            Double.parseDouble(jTextBox_normLow.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_normLowFocusLost

    private void jTextBox_normHighFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_normHighFocusLost
        try {
            Double.parseDouble(jTextBox_normHigh.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_normHighFocusLost

    private void jRadioButton_currentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_currentActionPerformed
        isTested = false;
        jLabelTestSize.setEnabled(false);
        jComboBoxTestPercent.setEnabled(false);
        showTestModeStructure_.setEnabled(false);
        plotTestModel_.setEnabled(false);
        jTextField_testRMSE.setText("");
        jTextField_testCORREL.setText("");
        jTextField_testCORREL1.setText("");
        jTextArea_TestModelInfo.setText("");        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_currentActionPerformed

    private void jRadioButton_oldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_oldActionPerformed
        jLabelTestSize.setEnabled(true);
        jComboBoxTestPercent.setEnabled(true);
        showTestModeStructure_.setEnabled(false);
        plotTestModel_.setEnabled(false);
        jTextField_testRMSE.setText("");
        jTextField_testCORREL.setText("");
        jTextField_testCORREL1.setText("");
        jTextArea_TestModelInfo.setText("");        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton_oldActionPerformed

    private void jComboBox_Select_a_ProblemItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_Select_a_ProblemItemStateChanged

    }//GEN-LAST:event_jComboBox_Select_a_ProblemItemStateChanged

    private void jComboBox_Select_a_ProblemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_Select_a_ProblemActionPerformed
        jTextField_fileName.setText("");
        loadData_.setEnabled(false);
        isFileChosen = false;
        jComboBox_Select_a_Data_File.addItem("Select a file....");
        String selectdProblem = jComboBox_Select_a_Problem.getSelectedItem().toString();
        switch (selectdProblem) {
            case "Classification":
                if (jComboBox_Select_a_Data_File.getItemCount() <= 0) {
                } else {
                    jComboBox_Select_a_Data_File.removeAllItems();
                }
                jComboBox_Select_a_Data_File.addItem("Select a file....");
                jComboBox_Select_a_Data_File.addItem("australian");
                jComboBox_Select_a_Data_File.addItem("breast");
                jComboBox_Select_a_Data_File.addItem("german");
                jComboBox_Select_a_Data_File.addItem("glass");
                jComboBox_Select_a_Data_File.addItem("heart");
                jComboBox_Select_a_Data_File.addItem("iris");
                jComboBox_Select_a_Data_File.addItem("pima");
                jComboBox_Select_a_Data_File.addItem("wdbc");
                jComboBox_Select_a_Data_File.addItem("wine");
                jComboBox_Select_a_Data_File.addItem("Choose another file...");
                jComboBox_Select_a_Data_File.setEnabled(true);
                break;
            case "Regression":
                if (jComboBox_Select_a_Data_File.getItemCount() <= 0) {
                    //do nothing
                } else {
                    jComboBox_Select_a_Data_File.removeAllItems();
                }
                jComboBox_Select_a_Data_File.setEnabled(true);
                jComboBox_Select_a_Data_File.addItem("Select a file....");
                jComboBox_Select_a_Data_File.addItem("abalone");
                jComboBox_Select_a_Data_File.addItem("baseball");
                jComboBox_Select_a_Data_File.addItem("dee");
                jComboBox_Select_a_Data_File.addItem("diabetes");
                jComboBox_Select_a_Data_File.addItem("elevators");
                jComboBox_Select_a_Data_File.addItem("forestFires");
                jComboBox_Select_a_Data_File.addItem("friedman");
                jComboBox_Select_a_Data_File.addItem("TimeSeries_NNGC");
                jComboBox_Select_a_Data_File.addItem("TimeSeries_Mackey_Glass_4in");
                jComboBox_Select_a_Data_File.addItem("TimeSeries_Gas_Furnace");
                jComboBox_Select_a_Data_File.addItem("Choose another file...");
                break;
            case "Select Problem Type...":
                jComboBox_Select_a_Data_File.setEnabled(false);
                break;
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox_Select_a_ProblemActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //Cleaning system for New Data
        executionHistory = "";
        isTrained = false;
        jTextArea_DataDisplay.setText("");
        jTextField_fileName.setText("");
        jTextField_trainRes.setText("");
        jTextField_testRMSE.setText("");
        jTextField_Display_Massage.setText("");
        isTested = false;
        jLabelTestSize.setEnabled(false);
        jComboBoxTestPercent.setEnabled(false);
        showTestModeStructure_.setEnabled(false);
        plotTestModel_.setEnabled(false);
        jTextField_testRMSE.setText("");
        jTextField_testCORREL.setText("");
        jTextField_testCORREL1.setText("");
        jTextArea_TestModelInfo.setText("");

        //FileFilter filter = new FileNameExtensionFilter("Data Files _", "txt", "arff", "dat", "csv");
        selectedFile = jComboBox_Select_a_Data_File.getSelectedItem().toString();
        switch (selectedFile) {
            case "Select a file....":
                isFileChosen = false;
                loadData_.setEnabled(false);
                JOptionPane.showMessageDialog(this, "Select a file");
                break;
            case "Choose another file...":
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("Data Files", "dat", "arff", "csv", "txt"));
                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    dataFile = chooser.getSelectedFile();
                    dataFileName = dataFile + "";
                    //jTextField_fileName.setText("File: " + retval[retval.length - 1]);
                    jTextField_fileName.setText("File selected: " + dataFile.getName());
                    System.out.println(dataFileName);

                    isFileChosen = true;
                    loadData_.setEnabled(true);
                    showdata_.setEnabled(true);
                } else {
                    loadData_.setEnabled(false);
                    isFileChosen = false;
                    showdata_.setEnabled(false);
                }
                break;
            default:
                dataFileName = InitiatorFrame.absoluteFilePathInp + selectedFile + ".dat";
                jTextField_fileName.setText("File selected: " + selectedFile + ".dat");
                loadData_.setEnabled(true);
                isFileChosen = true;
                dataFile = new File(dataFileName);
                showdata_.setEnabled(true);

                break;
        }
        if (isFileChosen) {
            loadData_.setEnabled(true);
            showdata_.setEnabled(true);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextBox_SEED_INPUTFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_SEED_INPUTFocusLost
        try {
            Double.parseDouble(jTextBox_SEED_INPUT.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }         // TODO add your handling code here: 
    }//GEN-LAST:event_jTextBox_SEED_INPUTFocusLost

    private void jRadioButton_RND_AUTOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_RND_AUTOActionPerformed
        fntRNGSeedType = "auto";
        jTextBox_SEED_INPUT.setEnabled(false);
    }//GEN-LAST:event_jRadioButton_RND_AUTOActionPerformed

    private void jRadioButton_RND_INPUTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_RND_INPUTActionPerformed
        fntRNGSeedType = "input";
        jTextBox_SEED_INPUT.setEnabled(true);
    }//GEN-LAST:event_jRadioButton_RND_INPUTActionPerformed

    private void jRadioButton_GP_SOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_GP_SOActionPerformed
        selectTrainingMode = "Single Objective";
    }//GEN-LAST:event_jRadioButton_GP_SOActionPerformed

    private void jRadioButton_GP_MOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_GP_MOActionPerformed
        selectTrainingMode = "Multi-Objectives";
    }//GEN-LAST:event_jRadioButton_GP_MOActionPerformed

    private void jRadioButton_TRN_NEWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_TRN_NEWActionPerformed
        selected_training_model = "new";
        jTextBox_Tree_Arity.setEnabled(true);
        jTextBox_Tree_Depth.setEnabled(true);
        jComboBox_FunType.setEnabled(true);
        jTextBox_Max_Gen.setEnabled(true);
        jTextBox_GP_POP.setEnabled(true);
        jTextBox_GP_TS.setEnabled(true);
        jTextField_Elitism.setEnabled(true);
        jTextField_GP_MR.setEnabled(true);
        jTextField_GP_CR.setEnabled(true);
        jTextBox_Max_GP_ITR.setEnabled(true);
    }//GEN-LAST:event_jRadioButton_TRN_NEWActionPerformed

    private void jRadioButton_TRN_OLDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_TRN_OLDActionPerformed
        selected_training_model = "old";
        jTextBox_Tree_Arity.setEnabled(false);
        jTextBox_Tree_Depth.setEnabled(false);
        jComboBox_FunType.setEnabled(false);
        jTextBox_Max_Gen.setEnabled(false);
        jTextBox_GP_POP.setEnabled(false);
        jTextBox_GP_TS.setEnabled(false);
        jTextField_Elitism.setEnabled(false);
        jTextField_GP_MR.setEnabled(false);
        jTextField_GP_CR.setEnabled(false);
        jTextBox_Max_GP_ITR.setEnabled(false);
    }//GEN-LAST:event_jRadioButton_TRN_OLDActionPerformed

    private void jRadioButton_tst_sufuling_yesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_tst_sufuling_yesActionPerformed
        needSufuling = true;// is needed to shufful dataset boolean
    }//GEN-LAST:event_jRadioButton_tst_sufuling_yesActionPerformed

    private void jRadioButton_tst_sufling_noActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_tst_sufling_noActionPerformed
        needSufuling = false;// is needed to shufful dataset boolean
    }//GEN-LAST:event_jRadioButton_tst_sufling_noActionPerformed

    private void jTextBox_EN_MH_POPFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_EN_MH_POPFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_EN_MH_POPFocusLost

    private void jComboBox_MHAlgo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_MHAlgo1ActionPerformed
        switch (jComboBox_MHAlgo1.getSelectedIndex()) {
            case 0: {
                System.out.println("Atrificial Bee Colony Training");
                m_ensembleMHalgo = "ABC";
                //removing all panels
                jPanel_ChooseAlgoEN.removeAll();
                jPanel_ChooseAlgoEN.repaint();
                jPanel_ChooseAlgoEN.revalidate();
                //adding panels
                jPanel_ChooseAlgoEN.add(abcPanel);
                jPanel_ChooseAlgoEN.repaint();
                jPanel_ChooseAlgoEN.revalidate();
                break;
            }
            case 1: {
                System.out.println("Bacteria Foregging Optimization Training");
                m_ensembleMHalgo = "BFO";
                //removing all panels
                jPanel_ChooseAlgoEN.removeAll();
                jPanel_ChooseAlgoEN.repaint();
                jPanel_ChooseAlgoEN.revalidate();
                //adding panels
                jPanel_ChooseAlgoEN.add(bfoPanel);
                jPanel_ChooseAlgoEN.repaint();
                jPanel_ChooseAlgoEN.revalidate();
                break;
            }
            case 2: {
                System.out.println("Differential Evolution Training");
                m_ensembleMHalgo = "DE";
                //removing all panels
                jPanel_ChooseAlgoEN.removeAll();
                jPanel_ChooseAlgoEN.repaint();
                jPanel_ChooseAlgoEN.revalidate();
                //adding panels
                jPanel_ChooseAlgoEN.add(dePanel);
                jPanel_ChooseAlgoEN.repaint();
                jPanel_ChooseAlgoEN.revalidate();
                break;
            }
            case 3: {
                System.out.println("Gray Wolf Optimization Training");
                m_ensembleMHalgo = "GWO";
                jPanel_ChooseAlgoEN.removeAll();
                jPanel_ChooseAlgoEN.repaint();
                jPanel_ChooseAlgoEN.revalidate();
                //adding panels
                jPanel_ChooseAlgoEN.add(gwoPanel);
                jPanel_ChooseAlgoEN.repaint();
                jPanel_ChooseAlgoEN.revalidate();
                break;
            }
            case 4: {
                System.out.println("Particle Swarm Optimization Training");
                m_ensembleMHalgo = "PSO";
                jPanel_ChooseAlgo.removeAll();
                jPanel_ChooseAlgo.repaint();
                jPanel_ChooseAlgo.revalidate();
                //adding panels
                jPanel_ChooseAlgo.add(psoPanel);
                jPanel_ChooseAlgo.repaint();
                jPanel_ChooseAlgo.revalidate();
                break;
            }
        }//switch
        return;
    }//GEN-LAST:event_jComboBox_MHAlgo1ActionPerformed

    private void jTextBox_enWTmaxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_enWTmaxFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_enWTmaxFocusLost

    private void jTextBox_enWTminFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_enWTminFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_enWTminFocusLost

    private void jComboBox_FoldTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_FoldTypeActionPerformed
        switch (jComboBox_FoldType.getSelectedItem().toString()) {
            case "Manual_Partition":
                jLabel28.setEnabled(true);
                jComboBox_Manual_TRN_Size.setEnabled(true);
                break;
            case "Partitioned_File":
                jLabel28.setEnabled(false);
                jComboBox_Manual_TRN_Size.setEnabled(false);
                JTextField text1 = new JTextField();
                text1.setText("10");// c0 = 0.729; // inertia weight
                Object[] FoldpramFields = {"Cross Validation Numbers", text1};
                int answer = JOptionPane.showConfirmDialog(null, FoldpramFields, "Choose Directory Only", JOptionPane.OK_CANCEL_OPTION);
                if (answer == JOptionPane.OK_OPTION) {
                    cvExtFileNumbers = Integer.parseInt(text1.getText());
                    FoldpramFields = null;
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    chooser.setAcceptAllFileFilterUsed(false);
                    chooser.addChoosableFileFilter(new FileNameExtensionFilter("Data Files", "dat", "arff", "csv", "txt"));
                    int returnVal = chooser.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File dataFile1 = chooser.getSelectedFile();
                        cvExtFilePath = dataFile1 + "" + File.separator;
                    }
                } else {
                    System.out.println("You pressed cancele button. Going for automatic CV");
                    jComboBox_FoldType.setSelectedIndex(0);
                }
                break;
            default:
                jLabel28.setEnabled(false);
                jComboBox_Manual_TRN_Size.setEnabled(false);
                break;
        }
    }//GEN-LAST:event_jComboBox_FoldTypeActionPerformed

    private void jComboBox_Ensemble_CandidatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_Ensemble_CandidatesActionPerformed
        String m_Ensemble_Candidates = jComboBox_Ensemble_Candidates.getSelectedItem().toString();
        if (!m_Ensemble_Candidates.equals("1")) {//NOT 1
            jComboBox_Ensemble_Diversity.setEnabled(true);
            jComboBox_Ensemble_Method.setEnabled(true);
            jComboBox_MHAlgo1.setEnabled(true);
            jTextBox_enWTmin.setEnabled(true);
            jTextBox_enWTmax.setEnabled(true);
            jTextBox_EN_MH_POP.setEnabled(true);
            jTextBox_EN_MH_ITR.setEnabled(true);
            jLabel_EN_1.setEnabled(true);
            jLabel_EN_2.setEnabled(true);
            jLabel_EN_3.setEnabled(true);
            jLabel_EN_4.setEnabled(true);
            jLabel_EN_5.setEnabled(true);
            jLabel_EN_6.setEnabled(true);
            jLabel_EN_7.setEnabled(true);
            jLabel_EN_8.setEnabled(true);
        } else {//for 1
            jComboBox_Ensemble_Diversity.setEnabled(false);
            jComboBox_Ensemble_Method.setEnabled(false);
            jComboBox_MHAlgo1.setEnabled(false);
            jTextBox_enWTmin.setEnabled(false);
            jTextBox_enWTmax.setEnabled(false);
            jTextBox_EN_MH_POP.setEnabled(false);
            jTextBox_EN_MH_ITR.setEnabled(false);
            jLabel_EN_1.setEnabled(false);
            jLabel_EN_2.setEnabled(false);
            jLabel_EN_3.setEnabled(false);
            jLabel_EN_4.setEnabled(false);
            jLabel_EN_5.setEnabled(false);
            jLabel_EN_6.setEnabled(false);
            jLabel_EN_7.setEnabled(false);
            jLabel_EN_8.setEnabled(false);
        }
    }//GEN-LAST:event_jComboBox_Ensemble_CandidatesActionPerformed

    private void resetParam_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetParam_ActionPerformed
        jTextBox_Tree_Depth.setText("5");
        jTextBox_Tree_Arity.setText("4");
        jComboBox_FunType.setSelectedIndex(0);
        jTextBox_GP_POP.setText("30");
        jTextField_GP_MR.setText("0.2");
        jTextField_GP_CR.setText("0.8");
        jTextBox_GP_TS.setText("2");
        jTextBox_MF_center_lo.setText("0.0");
        jTextBox__MF_center_hi.setText("1.0");
        jTextBox_THEN_weight_lo.setText("-1.0");
        jTextBox_THEN_weight_hi.setText("1.0");
        jComboBox_WeightsOnly.setSelectedIndex(0);
        jComboBox_MH_Algo.setSelectedIndex(0);
        jTextBox_MH_POP.setText("50");

        jComboBox_Ensemble_Candidates.setSelectedIndex(0);
        jComboBox_Ensemble_Diversity.setSelectedIndex(0);
        jTextBox_Max_Gen.setText("1");
        jTextBox_Max_GP_ITR.setText("50");
        jTextBox_MH_ITR.setText("100");
        jComboBox_FoldType.setSelectedIndex(0);
    }//GEN-LAST:event_resetParam_ActionPerformed

    private void jRadioButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton9ActionPerformed
        needSufuling = false;// is needed to shufful dataset boolean
    }//GEN-LAST:event_jRadioButton9ActionPerformed

    private void jTextBox_Max_GP_ITRMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextBox_Max_GP_ITRMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_Max_GP_ITRMouseExited

    private void jTextBox_Max_GP_ITRFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_Max_GP_ITRFocusLost
        try {
            Double.parseDouble(jTextBox_Max_GP_ITR.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }
    }//GEN-LAST:event_jTextBox_Max_GP_ITRFocusLost

    private void jRadioButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton5ActionPerformed
        cvSavePattern = "Yes";
    }//GEN-LAST:event_jRadioButton5ActionPerformed

    private void jRadioButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton6ActionPerformed
        cvSavePattern = "No";
    }//GEN-LAST:event_jRadioButton6ActionPerformed

    private void jComboBox_Manual_TRN_SizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_Manual_TRN_SizeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox_Manual_TRN_SizeActionPerformed

    private void jComboBox_Ensemble_MethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_Ensemble_MethodActionPerformed
//        JTextField text1 = new JTextField();
//        JTextField text2 = new JTextField();
//        JTextField text3 = new JTextField();
//        String selectionEnAlgo = jComboBox_Ensemble_Method.getSelectedItem().toString();
//        if (selectionEnAlgo.equalsIgnoreCase("Evolutionary_Weighted") || selectionEnAlgo.equalsIgnoreCase("Evolutionary_Weighted_Majority_Voting")) {
//            text1.setText("100");//weight factor (default 0.7)
//            text2.setText("1000");//Crossing over factor (default 0.9)
//            // Create our array of combo box choices
//            String[] comboBoxChoices = {// for DE stragey
//                "Artificial_Bee_Colony",
//                "Deferential_Evolution",
//                "Gray_Wolf_Optimization",};
//            JComboBox comboBox = new JComboBox(comboBoxChoices);
//            comboBox.setSelectedIndex(0);
//            Object[] DEpramFields = {"Population ", text1, "Iterations", text2, "Algorithm", comboBox};
//            JOptionPane.showConfirmDialog(null, DEpramFields, "Meta-Heuristic parameters Setting", JOptionPane.OK_CANCEL_OPTION);
//            //ensembPOP = (int) Double.parseDouble(text1.getText());
//            //ensembITR = (int) Double.parseDouble(text2.getText());
//            //m_ensembleMHalgo = comboBox.getSelectedItem().toString();
//        }
    }//GEN-LAST:event_jComboBox_Ensemble_MethodActionPerformed

    private void setParamFromFile_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setParamFromFile_ActionPerformed
        try {
            FileFilter filter = new FileNameExtensionFilter("FNT Model Files", "txt", "fnt");
            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(filter);
            int returnVal = chooser.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File myFileLocal = chooser.getSelectedFile();

                String fileNameLocal = myFileLocal + "";

                FileReader fin = new FileReader(fileNameLocal);
                BufferedReader br = new BufferedReader(fin);
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("#")) {

                        String[] tokens = line.split(",");
                        System.out.println(tokens[0] + ":" + tokens[1] + ":" + tokens[2]);
                        switch (tokens[1]) {
                            case "Tree_Height:":
                                jTextBox_Tree_Depth.setText(tokens[2]);
                                break;
                            case "Tree_Arity:":
                                jTextBox_Tree_Arity.setText(tokens[2]);
                                break;
                            case "Tree_Node_Type:":
                                jComboBox_FunType.setSelectedItem(tokens[2]);
                                break;
                            case "GP_Population:":
                                jTextBox_GP_POP.setText(tokens[2]);
                                break;
                            case "gpMUTATION_PROB:":
                                jTextField_GP_MR.setText(tokens[2]);
                                break;
                            case "gpCROSSOVER_PROB:":
                                jTextField_GP_CR.setText(tokens[2]);
                                break;
                            case "Tournament_Size:":
                                jTextBox_GP_TS.setText(tokens[2]);
                                break;
                            case "Optimize_Tree_parameters:":
                                jComboBox_WeightsOnly.setSelectedItem(tokens[2]);
                                break;
                            case "Metaheuristic_Algorithm:":
                                jComboBox_MH_Algo.setSelectedItem(tokens[2]);
                                break;
                            case "MH_Algorithm_Population:":
                                jTextBox_MH_POP.setText(tokens[2]);
                                break;
                            case "MH_Algorithm_Node_Range:":
                                jTextBox_MF_center_lo.setText(tokens[2]);
                                jTextBox__MF_center_hi.setText(tokens[3]);
                                break;
                            case "MH_Algorithm_Edge_Range:":
                                jTextBox_THEN_weight_lo.setText(tokens[2]);
                                jTextBox_THEN_weight_hi.setText(tokens[3]);
                                break;
                            case "Ensemble_Candidates:":
                                jComboBox_Ensemble_Candidates.setSelectedItem(tokens[2]);
                                break;
                            case "Ensemble_Diversity:":
                                if (!tokens[2].equals("Nill")) {
                                    jComboBox_Ensemble_Diversity.setSelectedItem(tokens[2]);
                                }
                                break;
                            case "Ensemble_Method_Used:":
                                if (!tokens[2].equals("NillMethod")) {
                                    jComboBox_Ensemble_Method.setSelectedItem(tokens[2]);
                                }
                                break;
                            case "Maximum_Genral_Iteration:":
                                jTextBox_Max_Gen.setText(tokens[2]);
                                break;
                            case "Maximum_Structure_Iteration:":
                                jTextBox_Max_GP_ITR.setText(tokens[2]);
                                break;
                            case "Maximum_Parameter_Iteration:":
                                jTextBox_MH_ITR.setText(tokens[2]);
                                break;
                            case "Cross_Validation:":
                                jComboBox_FoldType.setSelectedItem(tokens[2]);
                                break;
                        }
                    }//go to next # line
                }//while
            }
        } catch (HeadlessException | IOException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }
    }//GEN-LAST:event_setParamFromFile_ActionPerformed

    private void jTextBox_MH_ITRMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextBox_MH_ITRMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_MH_ITRMouseExited

    private void jTextBox_MH_ITRFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_MH_ITRFocusLost
        try {
            Double.parseDouble(jTextBox_MH_ITR.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_MH_ITRFocusLost

    private void jTextBox_Max_GenMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextBox_Max_GenMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_Max_GenMouseExited

    private void jTextBox_Max_GenMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextBox_Max_GenMouseClicked

    }//GEN-LAST:event_jTextBox_Max_GenMouseClicked

    private void jTextBox_Max_GenFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_Max_GenFocusLost
        try {
            if (Double.parseDouble(jTextBox_Max_Gen.getText()) > 5) {
                JOptionPane.showMessageDialog(this, "Total Iterations = Max General Iterations x ( Max GP Iteration + Max MH Iterations)");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }          // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_Max_GenFocusLost

    private void jRadioButton_sufuling_of_data_yesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_sufuling_of_data_yesActionPerformed
        needSufuling = true;// is needed to shufful dataset boolean
    }//GEN-LAST:event_jRadioButton_sufuling_of_data_yesActionPerformed

    private void jTextField_ElitismFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField_ElitismFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_ElitismFocusLost

    private void jTextBox_THEN_weight_loFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_THEN_weight_loFocusLost
        try {
            Double.parseDouble(jTextBox_THEN_weight_lo.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_THEN_weight_loFocusLost

    private void jTextBox_THEN_weight_hiFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_THEN_weight_hiFocusLost
        try {
            Double.parseDouble(jTextBox_THEN_weight_hi.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_THEN_weight_hiFocusLost

    private void jTextField_GP_CRFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField_GP_CRFocusLost
        try {
            Double.parseDouble(jTextField_GP_CR.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_GP_CRFocusLost

    private void jTextField_GP_MRFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField_GP_MRFocusLost
        try {
            double mu = Double.parseDouble(jTextField_GP_MR.getText());
            if (mu > 1.0) {
                JOptionPane.showMessageDialog(this, "Mutation Rate cannot be greter than 1.0");
            } else if (mu < 0.0) {
                JOptionPane.showMessageDialog(this, "Mutation Rate cannot be less than 0.0");
            }//if
            mu = (1.0 - mu);
            jTextField_GP_CR.setText(mu + "");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_GP_MRFocusLost

    private void jTextBox__MF_center_hiFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox__MF_center_hiFocusLost
        try {
            Double.parseDouble(jTextBox__MF_center_hi.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }         // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox__MF_center_hiFocusLost

    private void jTextBox_MF_center_loFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_MF_center_loFocusLost
        try {
            Double.parseDouble(jTextBox_MF_center_lo.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }         // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_MF_center_loFocusLost

    private void jTextBox_MH_POPFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_MH_POPFocusLost
        try {
            Double.parseDouble(jTextBox_MH_POP.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }         // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_MH_POPFocusLost

    private void jComboBox_MH_AlgoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_MH_AlgoActionPerformed
        switch (jComboBox_MH_Algo.getSelectedIndex()) {
            case 0: {
                System.out.println("Atrificial Bee Colony Training");
                MHAlgo = "ABC";
                //removing all panels
                jPanel_ChooseAlgo.removeAll();
                jPanel_ChooseAlgo.repaint();
                jPanel_ChooseAlgo.revalidate();
                //adding panels
                jPanel_ChooseAlgo.add(abcPanel);
                jPanel_ChooseAlgo.repaint();
                jPanel_ChooseAlgo.revalidate();
                break;
            }
            case 1: {
                System.out.println("Bacteria Foregging Optimization Training");
                MHAlgo = "BFO";
                //removing all panels
                jPanel_ChooseAlgo.removeAll();
                jPanel_ChooseAlgo.repaint();
                jPanel_ChooseAlgo.revalidate();
                //adding panels
                jPanel_ChooseAlgo.add(bfoPanel);
                jPanel_ChooseAlgo.repaint();
                jPanel_ChooseAlgo.revalidate();
                break;
            }
            case 2: {
                System.out.println("Differential Evolution Training");
                MHAlgo = "DE";
                //removing all panels
                jPanel_ChooseAlgo.removeAll();
                jPanel_ChooseAlgo.repaint();
                jPanel_ChooseAlgo.revalidate();
                //adding panels
                jPanel_ChooseAlgo.add(dePanel);
                jPanel_ChooseAlgo.repaint();
                jPanel_ChooseAlgo.revalidate();
                break;
            }
            case 3: {
                System.out.println("Gray Wolf Optimization Training");
                MHAlgo = "GWO";
                jPanel_ChooseAlgo.removeAll();
                jPanel_ChooseAlgo.repaint();
                jPanel_ChooseAlgo.revalidate();
                //adding panels
                jPanel_ChooseAlgo.add(gwoPanel);
                jPanel_ChooseAlgo.repaint();
                jPanel_ChooseAlgo.revalidate();
                break;
            }
            case 4: {
                System.out.println("Particle Swarm Optimization Training");
                MHAlgo = "PSO";
                jPanel_ChooseAlgo.removeAll();
                jPanel_ChooseAlgo.repaint();
                jPanel_ChooseAlgo.revalidate();
                //adding panels
                jPanel_ChooseAlgo.add(psoPanel);
                jPanel_ChooseAlgo.repaint();
                jPanel_ChooseAlgo.revalidate();
                break;
            }
        }//switch
        return;
    }//GEN-LAST:event_jComboBox_MH_AlgoActionPerformed

    private void jTextBox_GP_TSFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_GP_TSFocusLost
        try {
            Double.parseDouble(jTextBox_GP_TS.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }         // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_GP_TSFocusLost

    private void jTextBox_GP_POPFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_GP_POPFocusLost
        try {
            Double.parseDouble(jTextBox_GP_POP.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_GP_POPFocusLost

    private void jTextBox_Tree_ArityFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_Tree_ArityFocusLost
        try {
            Double.parseDouble(jTextBox_Tree_Arity.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_Tree_ArityFocusLost

    private void jTextBox_Tree_DepthFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_Tree_DepthFocusLost
        try {
            Double.parseDouble(jTextBox_Tree_Depth.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Operation failed! " + e);
            System.out.print(e);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_Tree_DepthFocusLost

    private void jTextBox_EN_MH_ITRFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_EN_MH_ITRFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_EN_MH_ITRFocusLost

    private void trainFNTModel_STOPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trainFNTModel_STOPActionPerformed
        if (isTrainingStarted) {
            answerWorker.cancel(true);
            answerWorker = null;
            isTrainingStarted = false;
            stopStatus.setText("  Stopped");
            jTextAreaRunTime.setText("");
            //Cleaning output folder (deleting files in folder Except the data files)
//            File outputFolder = new File(InitiatorFrame.absoluteFilePathOut);
//            final File[] files = outputFolder.listFiles();
//            System.out.println("The deleated output files..");
//            for (File f1 : files) {
//                System.out.println(f1.getName());
//                if (f1.getName().equalsIgnoreCase("filteredData.csv") || f1.getName().equalsIgnoreCase("normalizedData.csv")) {
//                    //ignore these data files DO NOT delete
//                } else {
//                    f1.delete();
//                }
//            }
        } else {
            JOptionPane.showMessageDialog(this, "Start Trainig Again");
        }
    }//GEN-LAST:event_trainFNTModel_STOPActionPerformed

    private void jTextBox_Max_GenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextBox_Max_GenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_Max_GenActionPerformed

    private void jTextBox_GP_POPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextBox_GP_POPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_GP_POPActionPerformed

    private void jTextField_ElitismActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_ElitismActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_ElitismActionPerformed

    private void jTextBox_GP_TSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextBox_GP_TSActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_GP_TSActionPerformed

    private void jTextBox_MF_width_loFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_MF_width_loFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_MF_width_loFocusLost

    private void jTextBox_MF_width_hiFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_MF_width_hiFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_MF_width_hiFocusLost

    private void jTextBox_MF_center_dev_loFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_MF_center_dev_loFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_MF_center_dev_loFocusLost

    private void jTextBox_MF_center_dev_hiFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_MF_center_dev_hiFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_MF_center_dev_hiFocusLost

    private void jTextBox_THEN_weight__dev_loFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_THEN_weight__dev_loFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_THEN_weight__dev_loFocusLost

    private void jTextBox_THEN_weight__dev_hiFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_THEN_weight__dev_hiFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_THEN_weight__dev_hiFocusLost

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        FSMFType = "Type-I";
        jComboBox_FunType.setSelectedIndex(4);
        jComboBox_FunType.setEnabled(true);
        jTextBox_MF_center_dev_lo.setEnabled(false);
        jTextBox_MF_center_dev_hi.setEnabled(false);
        MF_Dev_1.setEnabled(false);
        MF_Dev_2.setEnabled(false);
        MF_Dev_3.setEnabled(false);
        jTextBox_THEN_weight__dev_lo.setEnabled(false);
        jTextBox_THEN_weight__dev_hi.setEnabled(false);
        Rule_W_1.setEnabled(false);
        Rule_W_2.setEnabled(false);
        Rule_W_3.setEnabled(false);
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        FSMFType = "Type-II";
        jComboBox_FunType.setSelectedIndex(1);
        jComboBox_FunType.setEnabled(false);
        jTextBox_MF_center_dev_lo.setEnabled(true);
        jTextBox_MF_center_dev_hi.setEnabled(true);
        MF_Dev_1.setEnabled(true);
        MF_Dev_2.setEnabled(true);
        MF_Dev_3.setEnabled(true);
        Rule_W_1.setEnabled(true);
        jTextBox_THEN_weight__dev_lo.setEnabled(true);
        jTextBox_THEN_weight__dev_hi.setEnabled(true);
        Rule_W_1.setEnabled(true);
        Rule_W_2.setEnabled(true);
        Rule_W_3.setEnabled(true);
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jTextBox_tree_branch_weight_loFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_tree_branch_weight_loFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_tree_branch_weight_loFocusLost

    private void jTextBox_tree_branch_weight_hiFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBox_tree_branch_weight_hiFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBox_tree_branch_weight_hiFocusLost

    private void jComboBox_WeightsOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_WeightsOnlyActionPerformed
        if (jComboBox_WeightsOnly.getSelectedIndex() == 0) {
            jTextBox_tree_branch_weight_lo.setEnabled(false);
            jTextBox_tree_branch_weight_hi.setEnabled(false);
            Branch_W_1.setEnabled(false);
            Branch_W_2.setEnabled(false);
            Branch_W_3.setEnabled(false);
        } else {
            jTextBox_tree_branch_weight_lo.setEnabled(true);
            jTextBox_tree_branch_weight_hi.setEnabled(true);
            Branch_W_1.setEnabled(true);
            Branch_W_2.setEnabled(true);
            Branch_W_3.setEnabled(true);
        }
    }//GEN-LAST:event_jComboBox_WeightsOnlyActionPerformed

    private void jComboBox_Select_a_Data_FileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_Select_a_Data_FileActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox_Select_a_Data_FileActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FuzzyFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FuzzyFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Branch_W_1;
    private javax.swing.JLabel Branch_W_2;
    private javax.swing.JLabel Branch_W_3;
    private javax.swing.JLabel MF_Dev_1;
    private javax.swing.JLabel MF_Dev_2;
    private javax.swing.JLabel MF_Dev_3;
    private javax.swing.JLabel Rule_W_1;
    private javax.swing.JLabel Rule_W_2;
    private javax.swing.JLabel Rule_W_3;
    private javax.swing.JPanel abcPanel;
    private javax.swing.JPanel abcPanel1;
    private javax.swing.JMenuItem about_;
    private javax.swing.JMenuItem back2Main_;
    private javax.swing.JMenu back_;
    private javax.swing.JPanel bfoPanel;
    private javax.swing.JPanel bfoPanel1;
    private javax.swing.ButtonGroup buttonGroupRandomizedTest;
    private javax.swing.ButtonGroup buttonGroup_MODEL;
    private javax.swing.ButtonGroup buttonGroup_Model_Sel;
    private javax.swing.ButtonGroup buttonGroup_Objective;
    private javax.swing.ButtonGroup buttonGroup_RAND;
    private javax.swing.ButtonGroup buttonGroup_Randomized;
    private javax.swing.ButtonGroup buttonGroup_SaveData;
    private javax.swing.JLabel correlLabel;
    private javax.swing.JPanel dataSelect;
    private javax.swing.JPanel dePanel;
    private javax.swing.JPanel dePanel1;
    private javax.swing.JMenuItem developer_;
    private javax.swing.JMenuItem exit_;
    private javax.swing.JPanel fileBrowsPanel;
    private javax.swing.JPanel fntParamPanale;
    private javax.swing.ButtonGroup fuzzySetType;
    private javax.swing.JPanel gwoPanel;
    private javax.swing.JPanel gwoPanel1;
    private javax.swing.JMenu help;
    private javax.swing.JMenuItem help_;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox jComboBoxTestPercent;
    private javax.swing.JComboBox jComboBox_Ensemble_Candidates;
    private javax.swing.JComboBox jComboBox_Ensemble_Diversity;
    private javax.swing.JComboBox jComboBox_Ensemble_Method;
    private javax.swing.JComboBox jComboBox_FoldType;
    private javax.swing.JComboBox jComboBox_FunType;
    private javax.swing.JComboBox jComboBox_MHAlgo1;
    private javax.swing.JComboBox jComboBox_MH_Algo;
    private javax.swing.JComboBox jComboBox_Manual_TRN_Size;
    private javax.swing.JComboBox jComboBox_Select_a_Data_File;
    private javax.swing.JComboBox jComboBox_Select_a_Problem;
    private javax.swing.JComboBox jComboBox_WeightsOnly;
    private javax.swing.JComboBox jComboBox_abc_Food;
    private javax.swing.JComboBox jComboBox_abc_Food1;
    private javax.swing.JComboBox jComboBox_de_stat;
    private javax.swing.JComboBox jComboBox_de_stat1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelTestSize;
    private javax.swing.JLabel jLabel_EN_1;
    private javax.swing.JLabel jLabel_EN_2;
    private javax.swing.JLabel jLabel_EN_3;
    private javax.swing.JLabel jLabel_EN_4;
    private javax.swing.JLabel jLabel_EN_5;
    private javax.swing.JLabel jLabel_EN_6;
    private javax.swing.JLabel jLabel_EN_7;
    private javax.swing.JLabel jLabel_EN_8;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanel_ChooseAlgo;
    private javax.swing.JPanel jPanel_ChooseAlgoEN;
    private javax.swing.JPanel jPanel_Parameter;
    private javax.swing.JPanel jPanel_study_ref;
    private javax.swing.JProgressBar jProgressBarDefinite;
    private javax.swing.JProgressBar jProgressBarIndefinite;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JRadioButton jRadioButton9;
    private javax.swing.JRadioButton jRadioButton_GP_MO;
    private javax.swing.JRadioButton jRadioButton_GP_SO;
    private javax.swing.JRadioButton jRadioButton_RND_AUTO;
    private javax.swing.JRadioButton jRadioButton_RND_INPUT;
    private javax.swing.JRadioButton jRadioButton_TRN_NEW;
    private javax.swing.JRadioButton jRadioButton_TRN_OLD;
    private javax.swing.JRadioButton jRadioButton_current;
    private javax.swing.JRadioButton jRadioButton_old;
    private javax.swing.JRadioButton jRadioButton_sufuling_of_data_yes;
    private javax.swing.JRadioButton jRadioButton_tst_sufling_no;
    private javax.swing.JRadioButton jRadioButton_tst_sufuling_yes;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPaneDataselection;
    private javax.swing.JTabbedPane jTabbedPaneTraining;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextArea jTextArea6;
    private javax.swing.JTextArea jTextAreaRunTime;
    private javax.swing.JTextArea jTextArea_DataDisplay;
    private javax.swing.JTextArea jTextArea_TestModelInfo;
    private javax.swing.JTextField jTextBox_EN_MH_ITR;
    private javax.swing.JTextField jTextBox_EN_MH_POP;
    private javax.swing.JTextField jTextBox_GP_POP;
    private javax.swing.JTextField jTextBox_GP_TS;
    private javax.swing.JTextField jTextBox_MF_center_dev_hi;
    private javax.swing.JTextField jTextBox_MF_center_dev_lo;
    private javax.swing.JTextField jTextBox_MF_center_lo;
    private javax.swing.JTextField jTextBox_MF_width_hi;
    private javax.swing.JTextField jTextBox_MF_width_lo;
    private javax.swing.JTextField jTextBox_MH_ITR;
    private javax.swing.JTextField jTextBox_MH_POP;
    private javax.swing.JTextField jTextBox_Max_GP_ITR;
    private javax.swing.JTextField jTextBox_Max_Gen;
    private javax.swing.JTextField jTextBox_SEED_INPUT;
    private javax.swing.JTextField jTextBox_THEN_weight__dev_hi;
    private javax.swing.JTextField jTextBox_THEN_weight__dev_lo;
    private javax.swing.JTextField jTextBox_THEN_weight_hi;
    private javax.swing.JTextField jTextBox_THEN_weight_lo;
    private javax.swing.JTextField jTextBox_Tree_Arity;
    private javax.swing.JTextField jTextBox_Tree_Depth;
    private javax.swing.JTextField jTextBox__MF_center_hi;
    private javax.swing.JTextField jTextBox_enWTmax;
    private javax.swing.JTextField jTextBox_enWTmin;
    private javax.swing.JTextField jTextBox_normHigh;
    private javax.swing.JTextField jTextBox_normLow;
    private javax.swing.JTextField jTextBox_tree_branch_weight_hi;
    private javax.swing.JTextField jTextBox_tree_branch_weight_lo;
    private javax.swing.JTextField jTextField_Display_Massage;
    private javax.swing.JTextField jTextField_Elitism;
    private javax.swing.JTextField jTextField_GP_CR;
    private javax.swing.JTextField jTextField_GP_MR;
    private javax.swing.JTextField jTextField_abc_trail;
    private javax.swing.JTextField jTextField_abc_trail1;
    private javax.swing.JTextField jTextField_bfo_EL;
    private javax.swing.JTextField jTextField_bfo_EL1;
    private javax.swing.JTextField jTextField_bfo_NC;
    private javax.swing.JTextField jTextField_bfo_NC1;
    private javax.swing.JTextField jTextField_bfo_PR;
    private javax.swing.JTextField jTextField_bfo_PR1;
    private javax.swing.JTextField jTextField_bfo_RL;
    private javax.swing.JTextField jTextField_bfo_RL1;
    private javax.swing.JTextField jTextField_bfo_RS;
    private javax.swing.JTextField jTextField_bfo_RS1;
    private javax.swing.JTextField jTextField_bfo_SL;
    private javax.swing.JTextField jTextField_bfo_SL1;
    private javax.swing.JTextField jTextField_de_CR;
    private javax.swing.JTextField jTextField_de_CR1;
    private javax.swing.JTextField jTextField_de_F;
    private javax.swing.JTextField jTextField_de_F1;
    private javax.swing.JTextField jTextField_fileName;
    private javax.swing.JTextField jTextField_k_Value;
    private javax.swing.JTextField jTextField_pso_c0;
    private javax.swing.JTextField jTextField_pso_c1;
    private javax.swing.JTextField jTextField_pso_c2;
    private javax.swing.JTextField jTextField_pso_c4;
    private javax.swing.JTextField jTextField_pso_c5;
    private javax.swing.JTextField jTextField_pso_c6;
    private javax.swing.JTextField jTextField_testCORREL;
    private javax.swing.JTextField jTextField_testCORREL1;
    private javax.swing.JTextField jTextField_testRMSE;
    private javax.swing.JTextField jTextField_trainRes;
    private javax.swing.JMenuItem keyShortcut_;
    private javax.swing.JButton loadData_;
    private javax.swing.JButton plotTestModel_;
    private javax.swing.JButton plotTrainedModel_;
    private javax.swing.JPanel psoPanel;
    private javax.swing.JPanel psoPanel1;
    private javax.swing.JLabel r2Label;
    private javax.swing.JMenuItem referances_;
    private javax.swing.JButton resetParam_;
    private javax.swing.JLabel rmseLabel;
    private javax.swing.JButton saveTrainedModel_;
    private javax.swing.JMenuItem save_;
    private javax.swing.JButton setParamFromFile_;
    private javax.swing.JButton showTestModeStructure_;
    private javax.swing.JButton showTrainedModeStructure_;
    private javax.swing.JButton showTrainingResult_;
    private javax.swing.JButton showdata_;
    private javax.swing.JButton showdata_1;
    private javax.swing.JButton showdata_2;
    private javax.swing.JButton showdata_3;
    private javax.swing.JLabel stopStatus;
    private javax.swing.JPanel testPanel;
    private javax.swing.JButton testTrainedModel_;
    private javax.swing.JButton trainFNTModel_STOP;
    private javax.swing.JButton trainFNTModel_TRN;
    private javax.swing.JMenuItem version_;
    // End of variables declaration//GEN-END:variables
}//end FuzzyFNT Frame
