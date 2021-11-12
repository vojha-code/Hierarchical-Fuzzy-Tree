package MHAlgorithms;


import FIS.GPFIS;
import Function.Function;
import Randoms.*;
//import NeuralNetwork.*;
import DataReader.Pattern;
import AdditionalFrames.InitiatorFrame;
import MISC.TrainingModuleFIS;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DEvolution {

    //RANDOM algo Parameters
    MersenneTwisterFast randGen;

    public void setSeed(MersenneTwisterFast rand) {
        randGen = rand;
    }
    //JavaRand randGen = new JavaRand();
    //FNT Parameters
    private double[] FNTweight; //used for FNT
    private Object m_GP;
    private Object m_MH;
    private boolean m_isGP;
    private int m_Ensemble_Candidates;
    private String m_Ensemble_Diversity = "";
    private boolean isClassification = false;
    //FNT Ensemble parameters
    private int num_predictors;
    private double[][] predictor;
    private double[] target;
    private int pattern_Length;
    //NN Network Parameters
    private Pattern[] patTrain;
    //OPT Function fun; 
    private String FunName;
    private boolean isOptimization = false;
    //MH Parameters
    private int dimension;//dimension of solution vector
    private double low[]; //lower bound 
    private double high[]; // upper bound
    private int population; //population of solution vectors //NP = population = The number of colony size (employed bees+onlooker bees)
    private int maxIter; //The number of cycles for foraging {a stopping criteria}
    private int num_Function_Evaluation = 0;
    //MH Priniting Set-up
    private int printStep;//number of steps
    private boolean isPrintSteps = false;//wether to print steps of MH
    private boolean isPrintFinal = false;//whteher to print final optimial
    //DE parameters
    private int currGen;
    private double fx;//holds the best cost

    public double F = 0.7; // 0.5
    /**
     * weight factor (default 0.7)
     */
    public double CR = 0.9 /*1.0*/;
    /**
     * Crossing over factor (default 0.9)
     */
    // Helper variable
    private int numr = 3; // for stragey DE/rand-to-best/1/bin
    private String Strategy = "DE/rand-to-best/1/bin";
    private int[] r;
    // Population data
    private double trialCost;
    private double funFitness[];
    private double trialVector[];
    private double bestSolution[];
    private double currentPopulation[][];
    private double nextPopulation[][];

    //seting optimization
    public void setFunction(String FunName) {
        this.FunName = FunName;
    }

    public void setOptimization(boolean optimizationTask) {
        isOptimization = optimizationTask;
    }

    public void setTrainingPattern(Pattern[] patTrain) {
        this.patTrain = patTrain;
    }

    //setting MH parameters
    public void setMHparameters(ArrayList mhParam) {
        this.dimension = (int) mhParam.get(0);//dimension;
        this.population = (int) mhParam.get(1);//population;        
        this.maxIter = (int) mhParam.get(2);//maxIter;
        this.printStep = (int) mhParam.get(3);// printSteps;
        isPrintSteps = (boolean) mhParam.get(4);//printStep;
        isPrintFinal = (boolean) mhParam.get(5);//printFinal;
        this.low = new double[dimension];
        this.high = new double[dimension];
        this.low = (double[]) mhParam.get(6);//low;
        this.high = (double[]) mhParam.get(7);//high;
        //seting arrays of ABC
        FNTweight = new double[dimension];
        funFitness = new double[population];//hold cost vector or function fitness
        trialVector = new double[dimension];
        bestSolution = new double[dimension];//holds best vector
        currentPopulation = new double[population][dimension];//holds current population
        nextPopulation = new double[population][dimension];
        r = new int[numr];
    }

    public void setAlgo(ArrayList algorithSetUp) {
        CR = (double) algorithSetUp.get(0);
        F = (double) algorithSetUp.get(1);
        Strategy = (String) algorithSetUp.get(2);
        switch (Strategy) {
            case "DE/rand-to-best/1/bin":
                numr = 3;
                break;
            case "DE/rand-to-best/2/bin":
                numr = 3;
                break;
            case "DE/best/2/bin":
                numr = 5;
        }
        r = new int[numr];

    }

    public double getBestFitness() {
        return fx;
    }

    public double[] getBestParmeter() {
        return bestSolution;
    }
    /**
     * variable controlling print out, default value = 0 (0 -> no output, 1 ->
     * print final value, 2 -> detailed map of optimization process)
     */
    public int prin = 1;

    // implementation of abstract method 
    public ArrayList execute() {//(MultivariateFunction func, double[] xvec, double tolfx, double tolx)  
        num_Function_Evaluation = 0;
        double[] stat = {0.0, 0.0};
        double[] storeBest = null;
        double[] storeMean = null;
        double[] storeStd = null;
        if (isOptimization) {
            storeBest = new double[101];
            storeMean = new double[101];
            storeStd = new double[101];
            //System.out.println("Initialization Done");
        }
        int storeIndex = 0;

        //f = func;
        //bestSolution = xvec;
        // Create first generation
        firstGeneration();
        //System.out.println(iRet+" "+returnResult[iRet]);
        //stopCondition(fx, bestSolution, tolfx, tolx, true);

        //main iteration loop
        while (true) {
            boolean xHasChanged;
            do {
                //printing steps
                if (isPrintSteps) {
                    if (currGen % printStep == 0) {
                        if (isOptimization) {
                            if (storeIndex < 101) {
                                storeBest[storeIndex] = fx;
                                stat = computFitnessStat();
                                storeMean[storeIndex] = stat[0];
                                storeStd[storeIndex] = stat[1];
                                storeIndex++;
                            }
                        }
                        System.out.printf("   MH algo It: %5d Best: %.9f Mean: %.9f Std: %.9f \n", currGen, fx, stat[0], stat[1]);
                    }//printsteps
                }//isPrintSteps

                xHasChanged = nextGeneration();
                if (currGen >= maxIter) {//if (maxFun > 0 && numFun > maxFun)
                    break;
                }
                //if (prin > 1 && currGen % 20 == 0)
                //printStatistics();
            } while (!xHasChanged);
            //if (stopCondition(fx, bestSolution, tolfx, tolx, false) || (maxFun > 0 && numFun > maxFun))
            if (currGen >= maxIter) {
                break;
            }
            if (fx < 0.0000001) {
                //System.out.println("treshold" + fx);
                //break;
            }
        }//while
        //printing final step
        if (isPrintFinal) {
            if (isOptimization) {
                if (storeIndex < 101) {
                    storeBest[storeIndex] = fx;
                    stat = computFitnessStat();
                    storeMean[storeIndex] = stat[0];
                    storeStd[storeIndex] = stat[1];
                    storeIndex++;
                }
            }
            System.out.printf("   MH algo It: %5d Best: %.9f Mean: %.9f Std: %.9f \nTotal Fun Evaluations: %d\n", currGen, fx, stat[0], stat[1], num_Function_Evaluation);
        }

        ArrayList array = new ArrayList();
        array.add(storeBest);
        array.add(storeMean);
        array.add(storeStd);
        return array;
    }//main finction

    private double[] computFitnessStat() {
        //System.out.println("Computation Starts");
        double[] stat = new double[2];
        double sum = 0.0;
        for (int i = 0; i < funFitness.length; i++) {
            sum = sum + funFitness[i];
        }
        double mean = (double) (sum / funFitness.length);
        //System.out.println("Computation Done mean:" + mean);
        double var = 0.0;
        for (int i = 0; i < funFitness.length; i++) {
            var = var + Math.pow((funFitness[i] - mean), 2);
        }
        var = Math.sqrt(var / funFitness.length);//it is standard diviation becuase we take sqrt
        //System.out.println("Computation Done std:"+var);
        stat[0] = mean;
        stat[1] = var;
        //System.out.println("Computation Done");
        return stat;
    }//retrun

    private void printStatistics() {
        // Compute mean
        double meanCost = 0.0;
        for (int i = 0; i < population; i++) {
            meanCost += funFitness[i];
        }
        meanCost = meanCost / population;

        // Compute variance
        double varCost = 0.0;
        for (int i = 0; i < population; i++) {
            double tmp = (funFitness[i] - meanCost);
            varCost += tmp * tmp;
        }
        varCost = varCost / (population - 1);

        // System.out.println();
        // System.out.println();
        // System.out.println();
        //System.out.println(FunName+" value: " + fx);
        // System.out.println();
        // for (int k = 0; k < dimension; k++)
        // {
        // System.out.println("bestSolution[" + k + "] = " + bestSolution[k]);
        // }
        // System.out.println();
        // System.out.println("Current Generation: " + currGen);
        // System.out.println("Function evaluations: " + numFun);
        // System.out.println("Populations size (population): " + population);
        // System.out.println("Average value: " + meanCost);
        // System.out.println("Variance: " + varCost);
        // System.out.println("Weight factor (F): " + F);
        // System.out.println("Crossing-over (CR): " + CR);
        // System.out.println();
    }

    // Generate starting population
    private void firstGeneration() {
        currGen = 0;//initializing current generation
        // Construct population random start vectors
        for (int i = 0; i < population; i++) {
            for (int j = 0; j < dimension; j++) {
                // Uniformly distributed sample points
                if ((FunName.equals("FNT") || FunName.equals("FIS") || FunName.equals("CVFNT")) && i == 0) {
                    currentPopulation[i][j] = FNTweight[j];//*(maxX-minX)+minX;
                } else {
                    currentPopulation[i][j] = ((high[j] - low[j]) * randGen.random() + low[j]);
                }
            }//for dimension
            funFitness[i] = evaluate(currentPopulation[i]);
        }//for population
        //numFun += population;

        findSmallestCost();
    }

    public double evaluate(double[] sol) {
        num_Function_Evaluation++;
        double fitnessRet = 0.0;
        switch (FunName) {
            
            case "FIS":
                if (m_isGP) {
                    fitnessRet = ((GPFIS) m_GP).getMHfitness(sol);
                } else {
                    fitnessRet = ((TrainingModuleFIS) m_MH).getMHfitness(sol);
                    //System.out.println("Fit: "+fitnessRet);
                }
                break;
            
            case "RMSE":
                //System.out.println("Computing beeFitness:");
                if (!isClassification) {//RMSE
                    fitnessRet = Function.computeteRMSE(sol, target, predictor, num_predictors, pattern_Length);
                } else {//Accuracy
                    fitnessRet = Function.computeteAccuracy(sol, target, predictor, num_predictors, pattern_Length);
                }
                //System.out.println(" beeFitness:Computed");
                break;
            default:
                fitnessRet = Function.computeteFunction(sol, FunName);
                break;
        }//switch
        return fitnessRet;
    }//switch
    // check whether a parameter is out of range
    private double checkBounds(double param, int i) {
        if (param < low[i]) {
            return low[i];
        } else if (param > high[i]) {
            return high[i];
        } else {
            return param;
        }
    }

    // Generate next generation
    private boolean nextGeneration() {
        boolean updateFlag = false;
        int best = 0; // to avoid compiler complaints
        double[][] swap;

        currGen++;

        // Loop through all population vectors
        for (int r0 = 0; r0 < population; r0++) {
            // Choose ri so that r0 != r[1] != r[2] != r[3] != r[4] ...
            r[0] = r0;
            for (int k = 1; k < numr; k++) {
                r[k] = randomInteger(population - k);
                for (int l = 0; l < k; l++) {
                    if (r[k] >= r[l]) {
                        r[k]++;
                    }
                }
            }

            copy(trialVector, currentPopulation[r0]);
            int n = randomInteger(dimension);

            for (int i = 0; i < dimension; i++) { // perform binomial trials
                // change at least one parameter                
                if (randGen.random() < CR || i == dimension - 1) {
                    //String deStrategy;
                    switch (Strategy) {
                        case "DE/rand-to-best/1/bin":
                            // DE/rand-to-best/1/bin
                            // (change to 'numr=3' in constructor when using this strategy)
                            trialVector[n] = trialVector[n]
                                    + F * (bestSolution[n] - trialVector[n])
                                    + F * (currentPopulation[r[1]][n] - currentPopulation[r[2]][n]);
                            break;
                        case "DE/rand-to-best/2/bin":
                            //DE/rand-to-best/2/bin 
                            double K = randGen.random();
                            trialVector[n] = trialVector[n]
                                    + K * (bestSolution[n] - trialVector[n])
                                    + F * (currentPopulation[r[1]][n] - currentPopulation[r[2]][n]);
                            break;
                        case "DE/best/2/bin":
                            // DE/best/2/bin
                            // (change to 'numr=5' in constructor when using this strategy)
                            trialVector[n] = bestSolution[n]
                                    + (currentPopulation[r[1]][n] + currentPopulation[r[2]][n]
                                    - currentPopulation[r[3]][n] - currentPopulation[r[4]][n]) * F;
                            break;
                    }//switch
                }//if
                n = (n + 1) % dimension;
            }//dimension

            // make sure that trial vector obeys boundaries
            for (int i = 0; i < dimension; i++) {
                trialVector[i] = checkBounds(trialVector[i], i);
            }

            // Test this choice
            trialCost = evaluate(trialVector);
            if (trialCost < funFitness[r0]) {
                // Better than old vector
                funFitness[r0] = trialCost;
                copy(nextPopulation[r0], trialVector);

                // Check for new best vector
                if (trialCost < fx) {
                    fx = trialCost;
                    best = r0;
                    updateFlag = true;
                }//if 
            }//if
            else {
                // Keep old vector
                copy(nextPopulation[r0], currentPopulation[r0]);
            }
        }
		//numFun += population;

        // Update best vector
        if (updateFlag) {
            copy(bestSolution, nextPopulation[best]);
        }

        // Switch pointers
        swap = currentPopulation;
        currentPopulation = nextPopulation;
        nextPopulation = swap;

        return updateFlag;
    }

    // Determine vector with smallest cost in current population
    private void findSmallestCost() {
        int best = 0;
        fx = funFitness[0];
        for (int i = 1; i < population; i++) {
            if (funFitness[i] < fx) {
                fx = funFitness[i];
                best = i;
            }
        }
        //System.arraycopy(currentPopulation[best], 0, bestSolution, 0, dimension);
        copy(bestSolution, currentPopulation[best]);
    }

    //copy vector a into b
    public void copy(double a[], double b[]) {
        System.arraycopy(b, 0, a, 0, dimension);
    }

    // draw random integer in the range from 0 to n-1
    private int randomInteger(int n) {
        return (int) (randGen.random() * (n - 1));
    }

    public static void main(String args[]) {
        //DEvolution de = new DEvolution();
        //System.out.println(de.FunName + " value: " + Arrays.toString(de.optimize()));
    }

    //FNT functions
    public void setWeight(Object obj, double[] weights, int ensemble_Candidates, String ensemble_Diversity, boolean isGP) {
        m_isGP = isGP;
        if (m_isGP) {
            m_GP = obj;
            isPrintSteps = !ensemble_Diversity.equals("Structural");//do not print output for cross validation
            isPrintFinal = true;
        } else {
            m_MH = obj;
        }
        System.arraycopy(weights, 0, FNTweight, 0, dimension);
        m_Ensemble_Candidates = ensemble_Candidates;
        m_Ensemble_Diversity = ensemble_Diversity;
    }

    public double[][] getWeight() {
        double[][] returnWeight = new double[m_Ensemble_Candidates][dimension];
        sortingArray();
        //finding disting parameters
        double[] bestStructure = new double[m_Ensemble_Candidates];
        int indexBest = 0;
        System.arraycopy(bestSolution, 0, returnWeight[indexBest], 0, dimension);
        bestStructure[indexBest] = fx;
        indexBest++;
        if (indexBest < m_Ensemble_Candidates && m_Ensemble_Diversity.equals("Parametric")) {
            for (int i = 0; i < population; i++) {
                boolean found = false;
                for (int search = 0; search < indexBest; search++) {
                    if (bestStructure[search] == funFitness[i]) {
                        found = true;
                        break;
                    }
                }
                if (!found && indexBest < m_Ensemble_Candidates) {
                    System.arraycopy(currentPopulation[i], 0, returnWeight[indexBest], 0, dimension);
                    bestStructure[indexBest] = funFitness[i];
                    indexBest++;
                }
            }
        }
        if (indexBest < m_Ensemble_Candidates && m_Ensemble_Diversity.equals("Parametric")) {
            //System.out.print(" No more distinct trea:  Copying random trees");
            for (int fill = indexBest; fill < m_Ensemble_Candidates; fill++) {
                int rInt = randGen.random(population - 1);
                System.arraycopy(currentPopulation[rInt], 0, returnWeight[fill], 0, dimension);
                bestStructure[fill] = funFitness[rInt];
            }
        }//best possible distinct parameters found

        for (int i = 0; i < m_Ensemble_Candidates; i++) {
            //System.out.println(f[i]);
            System.arraycopy(currentPopulation[i], 0, returnWeight[i], 0, dimension);
        }

        return returnWeight;
    }//getWeight

   public void setEnsemble(int predictorsNum, int index, boolean classification) {
        String outputTrainFile = InitiatorFrame.absoluteFilePathOut + "outputTrain.csv";
        System.out.println("Reading from : " + index);
        isPrintSteps = true;
        isPrintFinal = true;
        isClassification = classification;
        try {
            int length;
            num_predictors = predictorsNum;

            String line;
            try (FileReader fin = new FileReader(outputTrainFile)) { //read output Train to make ensemble          
                BufferedReader br = new BufferedReader(fin);
                length = 0;
                while ((line = br.readLine()) != null) {
                    length++;
                }
                br.close();
                fin.close();
            }
            pattern_Length = length;
            predictor = new double[pattern_Length][num_predictors];
            target = new double[pattern_Length];
            try (FileReader fin1 = new FileReader(outputTrainFile)) { //readt output Train to make ensemble           
                BufferedReader br1 = new BufferedReader(fin1);
                int i = 0;
                while ((line = br1.readLine()) != null) {
                    String[] tokens = line.split(",");
                    int indexPointer = index;//initilze the index into the line of string
                    target[i] = Double.parseDouble(tokens[indexPointer]);
                    indexPointer++;//read next double
                    //System.out.print(target[i]+",");
                    for (int j = 0; j < num_predictors; j++) {
                        predictor[i][j] = Double.parseDouble(tokens[indexPointer]);
                        indexPointer++;//read next double
                        //System.out.print(predictor[i][j]+",");
                    }
                    i++;//Not incrementing i was  the bulder
                    //System.out.println();
                }
                br1.close();
            }
            //System.out.println();
        } catch (IOException | NumberFormatException e) {
            System.out.print("Error file Train Test printing:" + e);
        }
        //System.out.println("Target paterrn    " + target.length);
        //System.out.println("Predictor paterrn " + predictor.length);
        //System.out.println("Predictor paterrn " + predictor[0].length);
        //System.out.println("Set up success");
    }//ensembel

    public double[][] getWeightEnsemble() {
        double[][] returnWeight = new double[1][dimension];
        System.arraycopy(bestSolution, 0, returnWeight[0], 0, dimension);
        return returnWeight;
    }

    public void sortingArray() {
        try {//Sorting population
            double[] tmpVal = new double[dimension];
            double tmpFit;
            boolean swapped = true;
            int j = 0;
            while (swapped) {
                swapped = false;
                j++;
                for (int i = 0; i < population - j; i++) {
                    if (funFitness[i] > funFitness[i + 1]) {
                        tmpFit = funFitness[i];
                        System.arraycopy(currentPopulation[i], 0, tmpVal, 0, dimension);
                        funFitness[i] = funFitness[i + 1];
                        System.arraycopy(currentPopulation[i + 1], 0, currentPopulation[i], 0, dimension);
                        funFitness[i + 1] = tmpFit;
                        System.arraycopy(tmpVal, 0, currentPopulation[i + 1], 0, dimension);
                        swapped = true;
                    }
                }
            }
            tmpVal = null;
            //test the sorting
            /*for (int i = 0; i < FoodNumber ; i++){ 
             System.out.println("Fit:"+f[i]);
             }  */
        } catch (Exception e) {
            System.out.print("\nError Sorting FNT:" + e);
        }//end Sorting
    }//sorting

}
