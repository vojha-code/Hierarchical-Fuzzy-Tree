package FIS;

import DataReader.Pattern;
import java.io.PrintWriter;
import java.util.ArrayList;

public class EvaluationFunction {

    Pattern[] patTrain;
    Pattern[] patTest;
    int m_itemsCount;
    int m_TrainItemsCount;
    int m_TestItemsCount;
    int m_InputsCount;
    int m_OutputsCount;
    
    public double trainedResult;
    public double testedResult;
    private final double[] deTargetMin;
    private final double[] deTargetMax;
    private final double normalizedLow;
    private final double normalizedHigh;
    private final boolean m_isClassification;
    private final boolean needDenormalization;

    /**
     * 
     * @param dataFileInfo 
     */
    public EvaluationFunction(ArrayList dataFileInfo) {
        m_isClassification = (boolean) dataFileInfo.get(4);//propble type
        normalizedLow = (double) dataFileInfo.get(5);// normalization min
        normalizedHigh = (double) dataFileInfo.get(6);// normalization max
        deTargetMin = (double[]) dataFileInfo.get(9);// target min vector
        deTargetMax = (double[]) dataFileInfo.get(10);// target max vector
        needDenormalization = (boolean) dataFileInfo.get(12);// need denormalization
        //System.out.println(" What is here: "+needDenormalization);
    }

    /**
     * Loading the training and test data into the system
     *
     * @param patTrain training patterns/example
     * @param input number of input features
     * @param output number of output features
     * @return true if data loaded
     */
    public boolean loadDataTrain(Pattern[] patTrain, int input, int output) {
        this.patTrain = patTrain;
        m_InputsCount = input;
        m_OutputsCount = output;
        m_TrainItemsCount = patTrain.length;
        return true;
    }

    /**
     * Loading the only test data into the system
     *
     * @param patTest test patters/examples
     * @param input number of input features
     * @param output number of output features
     * @return true if data loaded
     */
    public boolean loadDataTest(Pattern[] patTest, int input, int output) {
        this.patTest = patTest;
        m_InputsCount = input;
        m_OutputsCount = output;
        m_TestItemsCount = patTest.length;
        return true;
    }

    /**
     * Evaluate the fitness of the tree model
     *
     * @param tree the model to be tasted over training data
     * @param m_OutputColumn output column to be trained
     * @return returns the fitness value
     */
    public double evaluateFitness(FuzzyFNT tree, int m_OutputColumn) { //MSE
        double predicted = 0.0, error = 0.0, errorSum = 0.0;
        int countTrue = 0;//count for true classification
        //tree.printTree();
        for (int i = 0; i < m_TrainItemsCount; i++) {
            predicted = tree.getOutput(patTrain[i].input, m_InputsCount);
            if (!m_isClassification) {
                double p = denormalize(predicted, deTargetMin[m_OutputColumn], deTargetMax[m_OutputColumn], normalizedLow, normalizedHigh, needDenormalization);
                double t = denormalize(patTrain[i].target[m_OutputColumn], deTargetMin[m_OutputColumn], deTargetMax[m_OutputColumn], normalizedLow, normalizedHigh, needDenormalization);
                error = p - t;
                errorSum = errorSum + (error * error);
                //System.out.println(predicted+" = "+p +" - "+ t);
                //System.exit(0);
            } else {
                double p = (predicted < 0.5) ? 0.0 : 1.0;
                double t = patTrain[i].target[m_OutputColumn];
                //System.out.println(t+" - "+p+"  ("+predicted+")");
                if (p == t) {
                    //System.out.println(t+" - "+p);
                    countTrue++;
                }
            }
        }
        //System.out.println("Total matches "+countTrue);
        double ret = 10e10;
        if (!m_isClassification) {
            //ret = errorSum / (2.0*m_TrainItemsCount);//MSE
            ret = Math.sqrt(errorSum / m_TrainItemsCount);//RMSE
            if(Double.isNaN(ret)){
                //System.out.print(m_TrainItemsCount+"Error Sum is a Problem: "+errorSum);
                //System.exit(0);
            }
        } else {
            ret = (1.0 - ((double) countTrue / (double) m_TrainItemsCount)) + 0.000000000000000001;//error rate
        }
        //System.exit(0);
        return ret;
    }//end evaluate fitness

    /*//evaluate fintness directely from file
    public double evaluateFitness(FuzzyFNT tree, String fileName) { //MSE
        double predicted = 0.0, error = 0.0, errorSum = 0.0;
        try {
            FileReader fin = new FileReader(fileName);
            try (BufferedReader br = new BufferedReader(fin)) {
                String readLine;
                for (int i = 0; i < m_TrainItemsCount; i++) {
                    if ((readLine = br.readLine()) != null) {
                        String[] tokens = readLine.split(",");
                        double[] inputs = new double[m_InputsCount];
                        double target;
                        if (tokens.length == m_InputsCount + 1) {
                            int j = 0;
                            for (j = 0; j < m_InputsCount; j++) {
                                inputs[j] = Double.parseDouble(tokens[j]);
                            }
                            target = Double.parseDouble(tokens[j]);
                        } else {
                            System.out.println("Error");
                            break;
                        }
                        predicted = tree.getOutput(inputs, m_InputsCount);
                        error = predicted - target;
                        errorSum = errorSum + (error * error);
                    }
                }
                br.close();
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Evaluate Fitness from File" + e);
        }
        return Math.sqrt(errorSum / m_TrainItemsCount);//RMSE
    }//end evaluate fitness

    /**
     * Returns model fitness result over test data
     *
     * @param tree is a trained model
     * @return training fitness over training data
     */
    public double[] test(FuzzyFNT tree) {
        double predicted = 0.0, error = 0.0, errorSum = 0.0;
        double[] recieveResTest = new double[m_TestItemsCount];
        try {
            for (int i = 0; i < m_TestItemsCount; i++) {
                predicted = tree.getOutput(patTest[i].input, m_InputsCount);
                error = predicted - patTest[i].target[0];//target 0 because of regression single target column
                errorSum = errorSum + (error * error);
                recieveResTest[i] = predicted;
            }
            testedResult = (errorSum / m_TestItemsCount);
            System.out.println(" Test ( " + (errorSum / m_TestItemsCount) + " )");
        } catch (Exception e) {
            System.out.print("Error test" + e);
        }
        return recieveResTest;
    }//end test

    /**
     * Returns model result over training data
     *
     * @param tree is a trained model
     * @return training fitness over training data
     */
    public double[] testTrain(FuzzyFNT tree) {
        double predicted = 0.0, error = 0.0, errorSum = 0.0;
        double[] recieveResTrain = new double[m_TrainItemsCount];
        try {
            for (int i = 0; i < m_TrainItemsCount; i++) {
                predicted = tree.getOutput(patTrain[i].input, m_InputsCount);
                error = predicted - patTrain[i].target[0];//target 0 because of regression single target column
                errorSum = errorSum + (error * error);
                recieveResTrain[i] = predicted;
            }
            trainedResult = (errorSum / m_TrainItemsCount);
            System.out.print(" Train ( " + trainedResult + " )");
        } catch (Exception e) {
            System.out.print("Error test:" + e);
        }
        return recieveResTrain;
    }//end testTrain */

    /**
     * Returns model fitness result over test data
     *
     * @param tree is a trained model
     * @param m_Ensemble_CandidatesInt
     * @param file where the test data will be printed
     * @return training fitness over training data
     */
    public double[][] test(FuzzyFNT[][] tree, int m_Ensemble_CandidatesInt, PrintWriter file) {
        double[][] errorSum = new double[m_OutputsCount][m_Ensemble_CandidatesInt];
        int[][] errorRate = new int[m_OutputsCount][m_Ensemble_CandidatesInt];
        for (int j = 0; j < m_OutputsCount; j++) {
            for (int k = 0; k < m_Ensemble_CandidatesInt; k++) {
                errorSum[j][k] = 0.0;
                errorRate[j][k] = 0;
            }
        }//initialize of error to 0.0
        try {
            for (int i = 0; i < m_TestItemsCount; i++) {
                for (int j = 0; j < m_OutputsCount; j++) {
                    double t = patTest[i].target[j];
                    //System.out.print(t+" = ");
                    if (!m_isClassification) {//regression
                        t = denormalize(t, deTargetMin[j], deTargetMax[j], normalizedLow, normalizedHigh, needDenormalization);
                    }
                    if (j == 0) {//regression
                        file.print(t);
                    } else {
                        file.print("," + t);
                    }
                    for (int k = 0; k < m_Ensemble_CandidatesInt; k++) {
                        double predicted = tree[j][k].getOutput(patTest[i].input, m_InputsCount);
                        if (!m_isClassification) {//regression
                            double p = denormalize(predicted, deTargetMin[j], deTargetMax[j], normalizedLow, normalizedHigh, needDenormalization);
                            file.print("," + p);
                            double error = p - t;//target 0 because of regression single target column
                            //System.out.println(t+" => "+p);
                            errorSum[j][k] = errorSum[j][k] + (error * error);
                        } else {//classification
                            double p = (predicted < 0.5) ? 0.0 : 1.0;
                            file.print("," + p);
                            if (p == t) {
                                errorRate[j][k]++;//count true classification
                            }//if
                        }//if  
                    }//models
                }//output columns
                file.println();
            }//for

            for (int j = 0; j < m_OutputsCount; j++) {
                for (int k = 0; k < m_Ensemble_CandidatesInt; k++) {
                    if (!m_isClassification) {//regression
                        //errorSum[j][k] = errorSum[j][k] / (2.0*m_TestItemsCount);//MSE
                        errorSum[j][k] = Math.sqrt(errorSum[j][k] / m_TestItemsCount);//RMSE
                    } else {//classification
                        errorSum[j][k] = (1.0 - ((double) errorRate[j][k] / (double) m_TestItemsCount))  + 0.0000000000000001;
                    }//if
                }//for models
            }//RMSE calculated for every output every models
        } catch (Exception e) {
            System.out.print("Error test" + e);
        }
        return errorSum;//RMSE OR MSE
    }//end test

    /**
     * Returns model result over training data
     *
     * @param tree is a trained model
     * @param m_Ensemble_CandidatesInt number of trees
     * @param file where the data will be printed
     * @return training fitness over training data
     */
    public double[][] testTrain(FuzzyFNT[][] tree, int m_Ensemble_CandidatesInt, PrintWriter file) {
        double[][] errorSum = new double[m_OutputsCount][m_Ensemble_CandidatesInt];
        int[][] errorRate = new int[m_OutputsCount][m_Ensemble_CandidatesInt];
        for (int j = 0; j < m_OutputsCount; j++) {
            for (int k = 0; k < m_Ensemble_CandidatesInt; k++) {
                errorSum[j][k] = 0.0;
                errorRate[j][k] = 0;
            }
        }//initialize of error to 0.0
        try {
            for (int i = 0; i < m_TrainItemsCount; i++) {
                for (int j = 0; j < m_OutputsCount; j++) {
                    double t = patTrain[i].target[j];                    
                    if (!m_isClassification) {//regression
                        t = denormalize(t, deTargetMin[j], deTargetMax[j], normalizedLow, normalizedHigh, needDenormalization);                       
                    }
                    if (j == 0) {
                        file.print(t);
                    } else {
                        file.print("," + t);
                    }
                    for (int k = 0; k < m_Ensemble_CandidatesInt; k++) {
                        double predicted = tree[j][k].getOutput(patTrain[i].input, m_InputsCount);
                        if (!m_isClassification) {//regression
                            double p = denormalize(predicted, deTargetMin[j], deTargetMax[j], normalizedLow, normalizedHigh, needDenormalization);
                            file.print("," + p);
                            double error = p - t;//target 0 because of regression single target column                            
                            errorSum[j][k] = errorSum[j][k] + (error * error);
                        } else {//classification
                            double p = (predicted < 0.5) ? 0.0 : 1.0;
                            file.print("," + p);
                            if (p == t) {
                                errorRate[j][k]++;//count true classification
                            }
                        }//if
                    }//k models
                }//j outputs
                file.println();
            }//i patterns

            for (int j = 0; j < m_OutputsCount; j++) {
                for (int k = 0; k < m_Ensemble_CandidatesInt; k++) {
                    if (!m_isClassification) {//regression
                        //errorSum[j][k] = errorSum[j][k] / (2*m_TrainItemsCount);//MSE
                        errorSum[j][k] = Math.sqrt(errorSum[j][k] / m_TrainItemsCount);//RMSE
                    } else {//classification
                        errorSum[j][k] = (1.0 - ((double) errorRate[j][k] / (double) m_TrainItemsCount))  + 0.0000000000000001;
                    }//if
                }//for models
            }//RMSE calculated for every output every models 
        } catch (Exception e) {
            System.out.print("Error train:" + e);
        }
        return errorSum;//RMSE or MSE
    }//end testTrain

    public double denormalize(double x, double dataLow, double dataHigh, double normalizedLow, double normalizedHigh, boolean needDenormalization) {
        if(needDenormalization){
            return ((dataLow - dataHigh) * x - normalizedHigh * dataLow + dataHigh * normalizedLow) / (normalizedLow - normalizedHigh);
        }            
        else{
            return x;
        }
    }//denormalization

    //Evaluation finctions for Classifications
}//end evaluate function
