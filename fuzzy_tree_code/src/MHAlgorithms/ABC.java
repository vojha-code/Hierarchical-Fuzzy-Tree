package MHAlgorithms;

import FIS.GPFIS;
import MISC.TrainingModuleFIS;
import Function.Function;
import Randoms.*;
import DataReader.Pattern;
import AdditionalFrames.InitiatorFrame;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ABC {

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
    int population; //population of solution vectors //NP = population = The number of colony size (employed bees+onlooker bees)
    private int maxIter; //The number of cycles for foraging {a stopping criteria}
    private int num_Function_Evaluation = 0;
    //MH Priniting Set-up
    private int printStep;//number of steps
    private boolean isPrintSteps = false;//wether to print steps of MH
    private boolean isPrintFinal = false;//whteher to print final optimial

    /* Control Parameters of ABC algorithm*/
    private int FoodNumber; //The number of food sources equals the half of the colony size
    private int limit = 100;  //A food source which could not be improved through "limit" trials is abandoned by its employed bee
    private int dizi1[] = new int[10];
    private double Foods[][];              //Foods is the population of food sources. Each row of Foods matrix is a vector holding dimension parameters to be optimized. The number of rows of Foods matrix equals to the FoodNumber
    private double funFitness[];                    //funFitness is a vector holding objective function values associated with food sources 
    private double beeFitness[];              //beeFitness is a vector holding beeFitness (quality) values associated with food sources
    private double trial[];                //trial is a vector holding trial numbers through which solutions can not be improved
    private double prob[];                 //prob is a vector holding probabilities of food sources (solutions) to be chosen
    private double solution[];             //New solution (neighbour) produced by v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) j is a randomly chosen parameter and k is a randomlu chosen solution different from i
    public double ObjValSol;               //Objective function value of new solution
    public double FitnessSol;              //Fitness value of new solution*/
    public int neighbour, param2change;    //param2change corresponds to j, neighbour corresponds to k in equation v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij})
    public double GlobalMin;               //Optimum solution obtained by ABC algorithm
    private double GlobalParams[];         //Parameters of the optimum solution

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
        this.FoodNumber = (int) population / 2;
        FNTweight = new double[dimension];
        Foods = new double[FoodNumber][dimension];
        funFitness = new double[FoodNumber];
        beeFitness = new double[FoodNumber];
        trial = new double[FoodNumber];
        prob = new double[FoodNumber];
        solution = new double[dimension];
        GlobalParams = new double[dimension];
    }

    public void setAlgo(ArrayList algorithSetUp) {
        limit = (int) algorithSetUp.get(0);
        int percentofPop = 10 + (int) algorithSetUp.get(1) * 10;
        FoodNumber = (int) (population * (percentofPop / 100.0f));
        //System.out.println("Food No:"+FoodNumber);
        Foods = new double[FoodNumber][dimension];
        funFitness = new double[FoodNumber];
        beeFitness = new double[FoodNumber];
        trial = new double[FoodNumber];
        prob = new double[FoodNumber];
    }

    /*GlobalMins holds the GlobalMin of each run in multiple runs*/
    double r; /*a random number in the range [0,1)*/

    /*a function pointer returning double and taking a D-dimensional array as argument */
    /*If your function takes additional arguments then change function pointer definition and lines calling "...=function(solution);" in the code*/

    public double getBestFitness() {
        return GlobalMin;
    }

    public double[] getBestParmeter() {
        return GlobalParams;
    }
    
//Step 1 All food sources are initialized 
    void initial() {
        int i;
        for (i = 0; i < FoodNumber; i++) {
            init(i);
        }
        GlobalMin = funFitness[0];
        for (i = 0; i < dimension; i++) {
            GlobalParams[i] = Foods[0][i];
        }
    }

//Step 1.a Initializing the candidate solution  
    void init(int index) {
        /*Variables are initialized in the range [minX,maxX]. 
         If each parameter has different range, use arrays minX[j], maxX[j] instead of minX and maxX */
        /* Counters of food sources are also initialized in this function*/
        int j;
        for (j = 0; j < dimension; j++) {

            r = ((high[j] - low[j]) * randGen.random() + low[j]);//((double)Math.random()*32767 / ((double)32767+(double)(1)));
            if ((FunName.equals("FNT") || FunName.equals("FIS") || FunName.equals("CVFNT"))&& index == 0) {
                Foods[index][j] = FNTweight[j];//*(maxX-minX)+minX;
            } else {
                Foods[index][j] = r;//*(maxX-minX)+minX;
            }
            solution[j] = Foods[index][j];
        }
        funFitness[index] = calculateFunction(solution);
        beeFitness[index] = CalculateFitness(funFitness[index]);
        //System.out.println("Fitness:"+funFitness[0]);
        trial[index] = 0;
    }

//Step 1.a.1 evaluate function value of the candidate solution
    double calculateFunction(double sol[]) {
        double fitnessRet = 0.0;
        num_Function_Evaluation++;
        switch (FunName) {
            
            case "FIS":
                if (m_isGP) {
                    fitnessRet = ((GPFIS)m_GP).getMHfitness(sol);
                } else {
                    fitnessRet = ((TrainingModuleFIS)m_MH).getMHfitness(sol);
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
        }
        return fitnessRet;
    }

//Step 1.a.2  Fitness function based on the evaluated function value
    double CalculateFitness(double fun) {
        double result = 0;
        if (fun >= 0) {
            result = 1 / (fun + 1);
        } else {
            result = 1 + Math.abs(fun);
        }
        return result;
    }

//1 Initialization done....
//Step 2  The best food source is memorized - Preserving global best
    void MemorizeBestSource() {
        int i, j;
        for (i = 0; i < FoodNumber; i++) {
            if (funFitness[i] < GlobalMin) {
                GlobalMin = funFitness[i];//here we change global min
                for (j = 0; j < dimension; j++) {
                    GlobalParams[j] = Foods[i][j];//and the crrespondin parameters
                }
            }
        }
    }//Step 2 

//Step 3 - Send employee bee 
    void SendEmployedBees() {
        int i, j;
        /*Employed Bee Phase*/
        for (i = 0; i < FoodNumber; i++) {
            /*The parameter to be changed is determined randomly*/
            r = randGen.random();
            //r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
            param2change = (int) (r * dimension);

            /*A randomly chosen solution is used in producing a mutant solution of the solution i*/
            r = randGen.random();
            //r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
            neighbour = (int) (r * FoodNumber); // randomly chosen neighbour

            /*Randomly selected solution must be different from the solution i*/
            while (neighbour == i) {
                r = randGen.random();
                //r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
                neighbour = (int) (r * FoodNumber);
            }
            for (j = 0; j < dimension; j++) {
                solution[j] = Foods[i][j];
            }

            /* v_{ij} = x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
            r = randGen.random();
            //r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
            solution[param2change] = Foods[i][param2change] + (Foods[i][param2change] - Foods[neighbour][param2change]) * (r - 0.5) * 2;

            /*if generated parameter value is out of boundaries, it is shifted onto the boundaries*/
            if (solution[param2change] < low[param2change]) {
                solution[param2change] = low[param2change];
            }
            if (solution[param2change] > high[param2change]) {
                solution[param2change] = high[param2change];
            }
            ObjValSol = calculateFunction(solution);
            FitnessSol = CalculateFitness(ObjValSol);

            /*a greedy selection is applied between the current solution i and its mutant*/
            if (FitnessSol > beeFitness[i]) {
                /*If the mutant solution is better than the current solution i, 
                 replace the solution with the mutant and reset the trial counter of solution i*/
                trial[i] = 0;
                for (j = 0; j < dimension; j++) {
                    Foods[i][j] = solution[j];
                }
                funFitness[i] = ObjValSol;
                beeFitness[i] = FitnessSol;
            } else {   /*if the solution i can not be improved, increase its trial counter*/

                trial[i] = trial[i] + 1;
            }
        }
    }//End Step 3 - send of employed bee phase

//Step 4
	/* A food source is chosen with the probability which is proportional to its quality*/
    /*Different schemes can be used to calculate the probability values*/
    /*For example prob(i)=beeFitness(i)/sum(beeFitness)*/
    /*or in a way used in the metot below prob(i)=a*beeFitness(i)/max(beeFitness)+b*/
    /*probability values are calculated by using beeFitness values and normalized by dividing maximum beeFitness value*/
    void CalculateProbabilities() {
        int i;
        double maxfit;
        maxfit = beeFitness[0];
        for (i = 1; i < FoodNumber; i++) {
            if (beeFitness[i] > maxfit) {
                maxfit = beeFitness[i];
            }
        }
        for (i = 0; i < FoodNumber; i++) {
            prob[i] = (0.9 * (beeFitness[i] / maxfit)) + 0.1;
        }
    }//CalculateProbabilities

// Step 5	
    void SendOnlookerBees() {
        int i, j, t;
        i = 0;
        t = 0;
        /*onlooker Bee Phase*/
        while (t < FoodNumber) {
            r = randGen.random();
            //r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
            if (r < prob[i]) {/*choose a food source depending on its probability to be chosen*/

                t++;

                /*The parameter to be changed is determined randomly*/
                r = randGen.random();
                //r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
                param2change = (int) (r * dimension);

                /*A randomly chosen solution is used in producing a mutant solution of the solution i*/
                r = randGen.random();
                //r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
                neighbour = (int) (r * FoodNumber);

                /*Randomly selected solution must be different from the solution i*/
                while (neighbour == i) {
                    //System.out.println(Math.random()*32767+"  "+32767);
                    r = randGen.random();
                    //r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
                    neighbour = (int) (r * FoodNumber);
                }
                for (j = 0; j < dimension; j++) {
                    solution[j] = Foods[i][j];
                }

                /*v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
                r = randGen.random();
                //r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
                solution[param2change] = Foods[i][param2change] + (Foods[i][param2change] - Foods[neighbour][param2change]) * (r - 0.5) * 2;

                /*if generated parameter value is out of boundaries, it is shifted onto the boundaries*/
                if (solution[param2change] < low[param2change]) {
                    solution[param2change] = low[param2change];
                }
                if (solution[param2change] > high[param2change]) {
                    solution[param2change] = high[param2change];
                }
                ObjValSol = calculateFunction(solution);
                FitnessSol = CalculateFitness(ObjValSol);

                /*a greedy selection is applied between the current solution i and its mutant*/
                if (FitnessSol > beeFitness[i]) {
                    /*If the mutant solution is better than the current solution i, 
                     replace the solution with the mutant and reset the trial counter of solution i*/
                    trial[i] = 0;
                    for (j = 0; j < dimension; j++) {
                        Foods[i][j] = solution[j];
                    }
                    funFitness[i] = ObjValSol;
                    beeFitness[i] = FitnessSol;
                } else {   /*if the solution i can not be improved, increase its trial counter*/

                    trial[i] = trial[i] + 1;
                }
            } /*if */

            i++;
            if (i == FoodNumber) {
                i = 0;
            }
        }/*while*/

    }// End Step 5 -  end of onlooker bee phase     */

    /* Step 6 -  determine the food sources whose trial counter exceeds the "limit" value. 
     In Basic ABC, only one scout is allowed to occur in each cycle*/
    void SendScoutBees() {
        int maxtrialindex, i;
        maxtrialindex = 0;
        for (i = 1; i < FoodNumber; i++) {
            if (trial[i] > trial[maxtrialindex]) {
                maxtrialindex = i;
            }
        }
        if (trial[maxtrialindex] >= limit) {
            init(maxtrialindex);
        }
    }//SendScoutBees

    public ArrayList execute() {
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
        
        //Algorithm starts
        initial(); // Step 1		    
        MemorizeBestSource(); //Step 2 : : find global best
        int iter;
        for (iter = 0; iter < maxIter; iter++) {
            //printing steps
            if (isPrintSteps) {
                if (iter % printStep == 0) {
                    if (isOptimization) {
                        if (storeIndex < 101) {
                            storeBest[storeIndex] = GlobalMin;
                            stat = computFitnessStat();
                            storeMean[storeIndex] = stat[0];
                            storeStd[storeIndex] = stat[1];
                            storeIndex++;
                        }
                    }
                    System.out.printf("   MH algo It: %5d Best: %.9f Mean: %.9f Std: %.9f \n", iter, GlobalMin, stat[0], stat[1]);
                }//printsteps
            }//isPrintSteps

            SendEmployedBees(); //Step 3
            CalculateProbabilities(); //Step 4
            SendOnlookerBees(); //Step 5
            MemorizeBestSource(); // repeat Step 2 : find global best
            SendScoutBees(); //Step 6
            //if(iter%100 ==0){returnResult[iRet] = GlobalMin; iRet++;  System.out.printf("%d Value: %.3f \n",iter,GlobalMin);}
            //Termination criteria
            if (GlobalMin < 0.00000001) {
                //break;
            }
        }//finising main loop
        //printing final step
        if (isPrintFinal) {
            if (isOptimization) {
                if (storeIndex < 101) {
                    storeBest[storeIndex] = GlobalMin;
                    stat = computFitnessStat();
                    storeMean[storeIndex] = stat[0];
                    storeStd[storeIndex] = stat[1];
                    storeIndex++;
                }
            }
            System.out.printf("   MH algo It: %5d Best: %.9f Mean: %.9f Std: %.9f \nTotal Fun Evaluations: %d\n", iter, GlobalMin, stat[0], stat[1], num_Function_Evaluation);
        }
        ArrayList array = new ArrayList();
        array.add(storeBest);
        array.add(storeMean);
        array.add(storeStd);
        return array;
    }// exceute    

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

    public static void main(String[] args) {

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
        System.arraycopy(GlobalParams, 0, returnWeight[indexBest], 0, dimension);
        bestStructure[indexBest] = GlobalMin;
        indexBest++;//increment ibest
        if (indexBest < m_Ensemble_Candidates && m_Ensemble_Diversity.equals("Parametric")) {
            for (int i = 0; i < FoodNumber; i++) {
                boolean found = false;
                for (int search = 0; search < indexBest; search++) {
                    if (bestStructure[search] == funFitness[i]) {
                        found = true;
                        break;
                    }
                }
                if (!found && indexBest < m_Ensemble_Candidates) {
                    System.arraycopy(Foods[i], 0, returnWeight[indexBest], 0, dimension);
                    bestStructure[indexBest] = funFitness[i];
                    indexBest++;
                }//if not found
            }//for food number
        }//if parameteric
        if (indexBest < m_Ensemble_Candidates && m_Ensemble_Diversity.equals("Parametric")) {
            //System.out.print(" No more distinct trea:  Copying random trees");
            for (int fill = indexBest; fill < m_Ensemble_Candidates; fill++) {
                //int rInt = randGen.nextInt(FoodNumber - 1);
                int rInt = randGen.random(FoodNumber - 1);
                System.arraycopy(Foods[rInt], 0, returnWeight[fill], 0, dimension);
                bestStructure[fill] = funFitness[rInt];
            }
        }//best possible distinct parameters found

        for (int i = 0; i < m_Ensemble_Candidates; i++) {
            //System.out.println(funFitness[i]);
            System.arraycopy(Foods[i], 0, returnWeight[i], 0, dimension);
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
            try (FileReader fin = new FileReader(outputTrainFile)) { //readt output Train to make ensemble          
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
        System.arraycopy(GlobalParams, 0, returnWeight[0], 0, dimension);
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
                for (int i = 0; i < FoodNumber - j; i++) {
                    if (funFitness[i] > funFitness[i + 1]) {
                        tmpFit = funFitness[i];
                        System.arraycopy(Foods[i], 0, tmpVal, 0, dimension);
                        funFitness[i] = funFitness[i + 1];
                        System.arraycopy(Foods[i + 1], 0, Foods[i], 0, dimension);
                        funFitness[i + 1] = tmpFit;
                        System.arraycopy(tmpVal, 0, Foods[i + 1], 0, dimension);
                        swapped = true;
                    }
                }
            }
            tmpVal = null;
            //test the sorting
            /*for (int i = 0; i < FoodNumber ; i++){ 
             System.out.println("Fit:"+funFitness[i]);
             }  */
        } catch (Exception e) {
            System.out.print("\nError Sorting FNT:" + e);
        }//end Sorting
    }
}
