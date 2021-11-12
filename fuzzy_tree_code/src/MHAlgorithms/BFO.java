package MHAlgorithms;


import FIS.GPFIS;

import Function.Function;

import Randoms.*;
import DataReader.Pattern;
import AdditionalFrames.InitiatorFrame;
import MISC.TrainingModuleFIS;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class BFO {

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
    private int maxIter;
    private int num_Function_Evaluation = 0;
    //MH Priniting Set-up
    private int printStep;//number of steps
    private boolean isPrintSteps = false;//wether to print steps of MH
    private boolean isPrintFinal = false;//whteher to print final optimial  
    //BFO Algorithm parameters           
    private int Nc = 10;                         // Number of chemotactic steps 
    private int Ns = 5;                     // Limits the length of a swim 
    private int Nre = 10;                   // The number of reproduction steps 
    private int Ned = 10;                   // The number of elimination-dispersal events 
    private double Sr = population / 2;     // The number of bacteria reproductions (splits) per generation 
    private double Ped = 0.25;              // The probability that each bacteria will be eliminated/dispersed 
    private double Ci = 0.05;               // the run length  

    // variable related to bacteria
    private double Bacteria[][]; //[population][dimension]
    private double health[];//hold helth of each bacteria computed based on the fitness of the function
    private double funFitness[];//holds the fitness of the function of entire p[opulation
    private double prevCost[];
    private double bestCost;//the best costr or ftness finction
    private double bestBacteria[];// the test solution

    //stetting variables
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
        this.Nc = (int) mhParam.get(2);//maxIter;
        this.printStep = (int) mhParam.get(3);// printSteps;
        isPrintSteps = (boolean) mhParam.get(4);//printStep;
        isPrintFinal = (boolean) mhParam.get(5);//printFinal;
        this.low = new double[dimension];
        this.high = new double[dimension];
        this.low = (double[]) mhParam.get(6);//low;
        this.high = (double[]) mhParam.get(7);//high;
        //seting arrays of ABC
        FNTweight = new double[dimension];
        Bacteria = new double[population][dimension]; //[population][dimension]
        health = new double[population];
        funFitness = new double[population];
        prevCost = new double[population];
        bestBacteria = new double[dimension];
    }

    public void setAlgo(ArrayList algorithSetUp) {
        Nc = (int) algorithSetUp.get(0); // Number of chemotactic steps 
        Ns = (int) algorithSetUp.get(1);// Limits the length of a swim 
        Nre = (int) algorithSetUp.get(2);// The number of reproduction steps 
        Ned = (int) algorithSetUp.get(3);// The number of elimination-dispersal events 
        Ped = (double) algorithSetUp.get(4); // The probability that each bacteria will be eliminated/dispersed 
        Ci = (double) algorithSetUp.get(5);// the run length  
    }

    public double getBestFitness() {
        return bestCost;
    }

    public double[] getBestParmeter() {
        return bestBacteria;
    }

    //Random initial solutions
    public void initiator() {
        //System.out.println("Initialization----------------------");
        for (int i = 0; i < population; i++) {
            for (int j = 0; j < dimension; j++) {
                if ((FunName.equals("FNT") || FunName.equals("FIS") || FunName.equals("CVFNT")) && i == 0) {
                    Bacteria[i][j] = FNTweight[j];
                } else {
                    Bacteria[i][j] = ((high[j] - low[j]) * randGen.random() + low[j]);
                }
                funFitness[i] = fitness(Bacteria[i]);
                //health[i] = 0.0; 
                //System.out.printf(" - %.3f\n",fitness[i]);
            }
            //computing best fitness
            //System.out.printf(" --------------------------------- \n ");
            int index = minfitness();
            bestCost = funFitness[index];
            System.arraycopy(Bacteria[index], 0, bestBacteria, 0, dimension); //preserving bestNest
            //System.out.printf(" %.3f",bestNest[j]);
            //System.out.printf("Initial best funFitness  : %.3f \n",bestCost,index);
            //System.out.printf(" ---------------------------------- \n ");	   
        }
    }

    public double fitness(double[] sol) {
        num_Function_Evaluation++;
        double fitnessRet = 0.0;
        switch (FunName) {
            
            case "FIS":
                if (m_isGP) {
                    fitnessRet = ((GPFIS) m_GP).getMHfitness(sol);
                } else {
                    fitnessRet = ((TrainingModuleFIS) m_MH).getMHfitness(sol);
                }
                break;
            
            case "RMSE":
                //System.out.println("Computing fitness:");
                fitnessRet = Function.computeteRMSE(sol, target, predictor, num_predictors, pattern_Length);
                //System.out.println(" fitness:Computed");
                break;
            default:
                fitnessRet = Function.computeteFunction(sol, FunName);
                break;
        }
        return fitnessRet;
    }


    //Get the current best
    public int minfitness() {
        double best = funFitness[0];
        //System.out.println(best);
        int bestIndex = 0;
        for (int i = 1; i < population; i++) {
            //System.out.printf("\n %.3f   <  %.3f",fitness[i],best);
            if (funFitness[i] < best) {
                //System.out.println("  Found best at "+i+"  "+fitness[i]);
                best = funFitness[i];
                bestIndex = i;
            }
        }
        return bestIndex;
    }//minfitness

    //The main optimization loop
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
        int iter = 0;

        // Initialization of the population of the bacteria
        // Find the best fitness and best bacteria
        initiator();
        //Elimination and dispersal loop 
        for (int ell = 0; ell < Ned; ell++) {
            //Reproduction loop
            for (int K = 0; K < Nre; K++) {
                // swim/tumble(chemotaxis)loop   
                for (int j = 0; j < Nc; j++) {
                    //printing steps
                    if (isPrintSteps) {
                        if (iter % printStep == 0) {
                            if (isOptimization) {
                                if (storeIndex < 101) {
                                    storeBest[storeIndex] = bestCost;
                                    stat = computFitnessStat();
                                    storeMean[storeIndex] = stat[0];
                                    storeStd[storeIndex] = stat[1];
                                    storeIndex++;
                                }
                            }
                            System.out.printf("   MH algo It: %5d Best: %.9f Mean: %.9f Std: %.9f \n", iter, bestCost, stat[0], stat[1]);
                        }//printsteps
                    }//isPrintSteps
                    //Setting each bacteria health to 0.0
                    for (int h = 0; h < population; h++) {
                        health[h] = 0.0;
                    }
                    // Process each bacteria
                    for (int i = 0; i < population; i++) {
                        // Tumble  
                        double[] tumble = new double[dimension];
                        for (int p = 0; p < dimension; ++p) {
                            tumble[p] = 2.0 * randGen.random() - 1.0;
                        }
                        double rootProduct = 0.0;
                        for (int p = 0; p < dimension; ++p) {
                            rootProduct += (tumble[p] * tumble[p]);
                        }
                        for (int p = 0; p < dimension; ++p) {
                            Bacteria[i][p] = simplebounds((Bacteria[i][p] + (Ci * tumble[p]) / Math.sqrt(rootProduct)), p);
                            //Bacteria[i][p] = Bacteria[i][p] + (Ci * tumble[p]) / Math.sqrt(rootProduct);
                        }

                        prevCost[i] = funFitness[i];
                        funFitness[i] = fitness(Bacteria[i]);
                        health[i] += funFitness[i];
                        if (funFitness[i] < bestCost) {
                            //System.out.printf("\nNew best solution found by bacteria %d, at time = %d with value %.3f",i,iter,bestCost); 
                            bestCost = funFitness[i];
                            System.arraycopy(Bacteria[i], 0, bestBacteria, 0, dimension);
                        }

                        //Swim 
                        int m = 0; // Initialize counter for swim length 
                        while (m < Ns) {//While length of swim 
                            m = m + 1;
                            if (funFitness[i] < prevCost[i]) {
                                prevCost[i] = funFitness[i];
                                for (int p = 0; p < dimension; ++p) {
                                    Bacteria[i][p] = simplebounds((Bacteria[i][p] + (Ci * tumble[p]) / Math.sqrt(rootProduct)), p);
                                    //Bacteria[i][p] = Bacteria[i][p] + (Ci * tumble[p]) / Math.sqrt(rootProduct);
                                }
                                funFitness[i] = fitness(Bacteria[i]);
                                if (funFitness[i] < bestCost) {
                                    //System.out.printf("\nNew best solution found by bacteria %d, at time = %d with value %.3f",i,iter,bestCost); 
                                    bestCost = funFitness[i];
                                    System.arraycopy(Bacteria[i], 0, bestBacteria, 0, dimension);
                                }
                            } else {
                                m = Ns;
                            }
                        }
                        //if(bestCost<0.0001) break;	// for tumbling and swim						 
                    }// Go to next bacterium
                    //if(bestCost<0.0001) break; // for chemotoxic
                    //terminating critera
                    if (bestCost < 0.000000000001) {
                        //System.out.println("treshold" + bestCost);
                        //reportMH = reportMH + ("treshold" + bestCost) + "\n";
                        //break;
                    }
                    iter++;//next iteartion i.e repeat opearation of the population again
                }// Go to the next chemotactic             
                //Reproduction   
                sorting();
                for (int left = 0; left < population / 2; left++) {
                    int right = left + population / 2;
                    //Replace right half poor bacteria with left half best Bacteria
                    System.arraycopy(Bacteria[left], 0, Bacteria[right], 0, dimension);
                    funFitness[right] = funFitness[left];
                    prevCost[right] = prevCost[left];
                    health[right] = health[left];
                }
            }//Go to next reproduction    
            //Eliminatoin and dispersal
            for (int i = 0; i < population; ++i) {
                double prob = randGen.random();
                if (prob < Ped) {
                    for (int p = 0; p < dimension; ++p) {
                        Bacteria[i][p] = ((high[p] - low[p]) * randGen.random() + low[p]);
                    }
                    funFitness[i] = fitness(Bacteria[i]);
                    prevCost[i] = funFitness[i];
                    health[i] = 0.0;
                    if (funFitness[i] < bestCost) {
                        //System.out.printf("\nNew best solution found by bacteria %d, at time = %d with value %.3f",i,iter,bestCost); 
                        bestCost = funFitness[i];
                        System.arraycopy(Bacteria[i], 0, bestBacteria, 0, dimension);
                    }
                } //if (prob < Ped)
                // elimination dispersal
            } //for
        } //Go to next elimination and dispersal 
        //printing final step
        if (isPrintFinal) {
            if (isOptimization) {
                if (storeIndex < 101) {
                    storeBest[storeIndex] = bestCost;
                    stat = computFitnessStat();
                    storeMean[storeIndex] = stat[0];
                    storeStd[storeIndex] = stat[1];
                    storeIndex++;
                }
            }
            System.out.printf("   MH algo It: %5d Best: %.9f Mean: %.9f Std: %.9f \nTotal Fun Evaluations: %d\n", iter, bestCost, stat[0], stat[1], num_Function_Evaluation);
        }
        ArrayList array = new ArrayList();
        array.add(storeBest);
        array.add(storeMean);
        array.add(storeStd);
        return array;
    }//End of optimization

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

    // Application of simple constraints
    public double simplebounds(double param, int i) {
        if (param < low[i]) {
            return low[i];
        } else if (param > high[i]) {
            return high[i];
        } else {
            return param;
        }
    }

    //Sorting ----------------------------------------------------
    public void sorting() {//Sorting according to health of bacteria
        for (int k = 1; k < population; k++) {
            for (int i = 0; i < population - k; i++) {
                double fitness1 = health[i];//funFitness[i];
                double fitness2 = health[i + 1];//funFitness[i+1];;

                double tempCost = funFitness[i];
                double tempPrevCost = prevCost[i];
                double tempHealth = health[i];
                double temp[] = new double[dimension];
                System.arraycopy(Bacteria[i], 0, temp, 0, dimension);
                if (fitness1 >= fitness2) {
                    System.arraycopy(Bacteria[i + 1], 0, Bacteria[i], 0, dimension);
                    funFitness[i] = funFitness[i + 1];
                    prevCost[i] = prevCost[i + 1];
                    health[i] = health[i + 1];

                    System.arraycopy(temp, 0, Bacteria[i + 1], 0, dimension);
                    funFitness[i + 1] = tempCost;
                    prevCost[i + 1] = tempPrevCost;
                    health[i + 1] = tempHealth;
                }
            }
        }
    }

    public static void main(String args[]) {
    }

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
    }//setWeight

    public double[][] getWeight() {
        double[][] returnWeight = new double[m_Ensemble_Candidates][dimension];
        sortingArray();
        //finding disting parameters
        double[] bestStructure = new double[m_Ensemble_Candidates];
        int indexBest = 0;
        System.arraycopy(bestBacteria, 0, returnWeight[indexBest], 0, dimension);
        bestStructure[indexBest] = bestCost;
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
                    System.arraycopy(Bacteria[i], 0, returnWeight[indexBest], 0, dimension);
                    bestStructure[indexBest] = funFitness[i];
                    indexBest++;
                }
            }
        }
        if (indexBest < m_Ensemble_Candidates && m_Ensemble_Diversity.equals("Parametric")) {
            //System.out.print(" No more distinct trea:  Copying random trees");
            for (int fill = indexBest; fill < m_Ensemble_Candidates; fill++) {
                //int rInt = randGen.nextInt(population - 1);
                int rInt = randGen.random(population - 1);
                System.arraycopy(Bacteria[rInt], 0, returnWeight[fill], 0, dimension);
                bestStructure[fill] = funFitness[rInt];
            }
        }//best possible distinct parameters found

        for (int i = 0; i < m_Ensemble_Candidates; i++) {
            //System.out.println(f[i]);
            System.arraycopy(Bacteria[i], 0, returnWeight[i], 0, dimension);
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
        System.arraycopy(bestBacteria, 0, returnWeight[0], 0, dimension);
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
                        System.arraycopy(Bacteria[i], 0, tmpVal, 0, dimension);
                        funFitness[i] = funFitness[i + 1];
                        System.arraycopy(Bacteria[i + 1], 0, Bacteria[i], 0, dimension);
                        funFitness[i + 1] = tmpFit;
                        System.arraycopy(tmpVal, 0, Bacteria[i + 1], 0, dimension);
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
