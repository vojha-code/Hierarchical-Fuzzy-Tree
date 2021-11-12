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

public class GWO {

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

    //GWO Algorithm parameters
    private double Wolf[][];
    private double wolfScore[];
    private double bestWolf[];
    private double bestScore;
    private double alpha[];
    private double beta[];
    private double delta[];

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
        Wolf = new double[population][dimension]; //[population][dimension]
        wolfScore = new double[population];//hold the fitness of all wolves
        bestWolf = new double[dimension];
        alpha = new double[dimension];
        beta = new double[dimension];
        delta = new double[dimension];
    }

    public double getBestFitness() {
        return alphaScore;
    }

    public double[] getBestParmeter() {
        return alpha;
    }

    public double alphaScore;
    public double betaScore;
    public double deltaScore;

    //Random initial solutions
    public void initiator() {
        //System.out.println("Initialization----------------------");
        for (int i = 0; i < population; i++) {
            for (int j = 0; j < dimension; j++) {
                if ((FunName.equals("FNT") || FunName.equals("FIS") || FunName.equals("CVFNT")) && i == 0) {
                    Wolf[i][j] = FNTweight[j];
                } else {
                    Wolf[i][j] = ((high[j] - low[j]) * randGen.random() + low[j]);
                }
                //System.out.printf(" %.3f",nest[i][j]);
            }//for
            wolfScore[i] = fitness(Wolf[i]);
            //System.out.printf(" - %.3f\n",wolfScore[i]);
        }//for

        //computing best fitness
        //System.out.printf(" --------------------------------- \n ");
        int index = minfitness();
        bestScore = wolfScore[index];
        System.arraycopy(Wolf[index], 0, bestWolf, 0, dimension); //preserving bestNest
        //System.out.printf(" %.3f",bestNest[j]);
        //for
        //System.out.print(FunName);
        //System.out.printf("  Initial best : %.5f \n",bestScore);	
        //System.exit(1);
        //System.out.printf(" ---------------------------------- \n ");	   
    }//initiator  

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
        double best = wolfScore[0];
        //System.out.println(best);
        int bestIndex = 0;
        for (int i = 1; i < population; i++) {
            //System.out.printf("\n %.3f   <  %.3f",fitness[i],best);
            if (wolfScore[i] < best) {
                //System.out.println("  Found best at "+i+"  "+fitness[i]);
                best = wolfScore[i];
                bestIndex = i;
            }//if
        }//for		
        return bestIndex;
    }// minfitness  

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
        //System.out.print("Initialization of the population of the Wolves");
        // Find the best fitness and best wolf
        initiator();
        sorting();
        //finding alpha
        System.arraycopy(Wolf[0], 0, alpha, 0, dimension);
        alphaScore = wolfScore[0];//System.out.printf(" - %.3f\n",alphaScore);	
        //returnResult[2] = alphaScore;		 
        //finding beta
        System.arraycopy(Wolf[1], 0, beta, 0, dimension);
        betaScore = wolfScore[1];//System.out.printf(" - %.3f\n",betaScore);		
        //finding delta
        System.arraycopy(Wolf[2], 0, delta, 0, dimension);
        deltaScore = wolfScore[2];//System.out.printf(" - %.3f\n",deltaScore);

        int iter = 0;
        while (iter < maxIter) {
            //printing steps
            if (isPrintSteps) {
                if (iter % printStep == 0) {
                    if (isOptimization) {
                        if (storeIndex < 101) {
                            storeBest[storeIndex] = alphaScore;
                            stat = computFitnessStat();
                            storeMean[storeIndex] = stat[0];
                            storeStd[storeIndex] = stat[1];
                            storeIndex++;
                        }
                    }
                    System.out.printf("   MH algo It: %5d Best: %.9f Mean: %.9f Std: %.9f \n", iter, alphaScore, stat[0], stat[1]);
                }//printsteps
            }//isPrintSteps
            for (int i = 0; i < population; i++) {
                //Return back the search agents that go beyond the boundaries of the search space
                for (int b = 0; b < dimension; b++) {
                    Wolf[i][b] = simplebounds(Wolf[i][b], b);
                }

                //Calculate fitness of the wolf
                wolfScore[i] = fitness(Wolf[i]);

                //Update Alpha, Beta, and Delta
                if (wolfScore[i] < alphaScore) {
                    System.arraycopy(Wolf[i], 0, alpha, 0, dimension);
                    alphaScore = wolfScore[i];
                }//update alpha

                if ((wolfScore[i] > alphaScore) && (wolfScore[i] < betaScore)) {
                    System.arraycopy(Wolf[i], 0, beta, 0, dimension);
                    betaScore = wolfScore[i];
                }//Update beta

                if ((wolfScore[i] > alphaScore) && (wolfScore[i] > betaScore) && (wolfScore[i] < deltaScore)) {
                    System.arraycopy(Wolf[i], 0, delta, 0, dimension);
                    deltaScore = wolfScore[i];
                }//update Delta
            }

            double a = 2 - iter * ((2) / maxIter); // a decreases linearly from 2 to 0

            //Update the Position of search agents including omegas
            for (int i = 0; i < population; i++) {
                for (int j = 0; j < dimension; j++) {

                    double r1 = randGen.random();//rand(); //r1 is a random number in [0,1]
                    double r2 = randGen.random();//rand(); //r2 is a random number in [0,1]

                    double A1 = 2 * a * r1 - a; //Equation (3.3)
                    double C1 = 2 * r2; //Equation (3.4)

                    double D_alpha = Math.abs(C1 * alpha[j] - Wolf[i][j]); //Equation (3.5)-part 1
                    double X1 = alpha[j] - A1 * D_alpha; //Equation (3.6)-part 1

                    r1 = randGen.random();
                    r2 = randGen.random();

                    double A2 = 2 * a * r1 - a; // Equation (3.3)
                    double C2 = 2 * r2; // Equation (3.4)

                    double D_beta = Math.abs(C2 * beta[j] - Wolf[i][j]); //Equation (3.5)-part 2
                    double X2 = beta[j] - A2 * D_beta; //Equation (3.6)-part 2       

                    r1 = randGen.random();
                    r2 = randGen.random();

                    double A3 = 2 * a * r1 - a; //Equation (3.3)
                    double C3 = 2 * r2; //Equation (3.4)

                    double D_delta = Math.abs(C3 * delta[j] - Wolf[i][j]); //Equation (3.5)-part 3
                    double X3 = delta[j] - A3 * D_delta; //Equation (3.5)-part 3             

                    Wolf[i][j] = (X1 + X2 + X3) / 3; //Equation (3.7)
                }
            }
            //if(l%100 ==0){returnResult[iRet] = alphaScore; iRet++; }//System.out.printf(" %d - %.3f\n",l,alphaScore);		}	
            iter = iter + 1;
            //Termination criteria
            if (alphaScore < 0.0000001) {
                //System.out.println("treshold" + alphaScore);
                //break;
            }
        }// While
        //printing final step
        if (isPrintFinal) {
            if (isOptimization) {
                if (storeIndex < 101) {
                    storeBest[storeIndex] = alphaScore;
                    stat = computFitnessStat();
                    storeMean[storeIndex] = stat[0];
                    storeStd[storeIndex] = stat[1];
                    storeIndex++;
                }
            }
            System.out.printf("   MH algo It: %5d Best: %.9f Mean: %.9f Std: %.9f \nTotal Fun Evaluations: %d\n", iter, alphaScore, stat[0], stat[1], num_Function_Evaluation);
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
        for (int i = 0; i < wolfScore.length; i++) {
            sum = sum + wolfScore[i];
        }
        double mean = (double) (sum / wolfScore.length);
        //System.out.println("Computation Done mean:" + mean);
        double var = 0.0;
        for (int i = 0; i < wolfScore.length; i++) {
            var = var + Math.pow((wolfScore[i] - mean), 2);
        }
        var = Math.sqrt(var / wolfScore.length);//it is standard diviation becuase we take sqrt
        //System.out.println("Computation Done std:"+var);
        stat[0] = mean;
        stat[1] = var;
        //System.out.println("Computation Done");
        return stat;
    }//retrun

    // Application of simple constraints
    public double simplebounds(double param, int indx) {
        if (param < low[indx]) {
            return low[indx];
        } else if (param > high[indx]) {
            return high[indx];
        } else {
            return param;
        }
    }//Simplebouds

    //Sorting ----------------------------------------------------
    public void sorting() {//Sorting according to fitness of wolves
        for (int k = 1; k < population; k++) {
            for (int i = 0; i < population - k; i++) {
                double fitness1 = wolfScore[i];
                double fitness2 = wolfScore[i + 1];

                double tempCost = wolfScore[i];
                double temp[] = new double[dimension];
                System.arraycopy(Wolf[i], 0, temp, 0, dimension);
                if (fitness1 >= fitness2) {
                    System.arraycopy(Wolf[i + 1], 0, Wolf[i], 0, dimension);
                    wolfScore[i] = wolfScore[i + 1];

                    System.arraycopy(temp, 0, Wolf[i + 1], 0, dimension);
                    wolfScore[i + 1] = tempCost;
                }//if
            }//for
        }//for
    }//Sorting

    public static void main(String args[]) {
    }//main

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

    public void setStep(int m_PrintStepPSO) {
        printStep = m_PrintStepPSO;
    }

    public double[][] getWeight() {
        double[][] returnWeight = new double[m_Ensemble_Candidates][dimension];
        sortingArray();
        //finding disting parameters
        double[] bestStructure = new double[m_Ensemble_Candidates];
        int indexBest = 0;
        System.arraycopy(alpha, 0, returnWeight[indexBest], 0, dimension);
        bestStructure[indexBest] = alphaScore;
        indexBest++;
        if (indexBest < m_Ensemble_Candidates && m_Ensemble_Diversity.equals("Parametric")) {
            for (int i = 0; i < population; i++) {
                boolean found = false;
                for (int search = 0; search < indexBest; search++) {
                    if (bestStructure[search] == wolfScore[i]) {
                        found = true;
                        break;
                    }
                }
                if (!found && indexBest < m_Ensemble_Candidates) {
                    System.arraycopy(Wolf[i], 0, returnWeight[indexBest], 0, dimension);
                    bestStructure[indexBest] = wolfScore[i];
                    indexBest++;
                }
            }
        }
        if (indexBest < m_Ensemble_Candidates && m_Ensemble_Diversity.equals("Parametric")) {
            //System.out.print(" No more distinct trea:  Copying random trees");
            for (int fill = indexBest; fill < m_Ensemble_Candidates; fill++) {
                int rInt = randGen.random(population - 1);
                System.arraycopy(Wolf[rInt], 0, returnWeight[fill], 0, dimension);
                bestStructure[fill] = wolfScore[rInt];
            }
        }//best possible distinct parameters found

        for (int i = 0; i < m_Ensemble_Candidates; i++) {
            //System.out.println(f[i]);
            System.arraycopy(Wolf[i], 0, returnWeight[i], 0, dimension);
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
        System.arraycopy(alpha, 0, returnWeight[0], 0, dimension);
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
                    if (wolfScore[i] > wolfScore[i + 1]) {
                        tmpFit = wolfScore[i];
                        System.arraycopy(Wolf[i], 0, tmpVal, 0, dimension);
                        wolfScore[i] = wolfScore[i + 1];
                        System.arraycopy(Wolf[i + 1], 0, Wolf[i], 0, dimension);
                        wolfScore[i + 1] = tmpFit;
                        System.arraycopy(tmpVal, 0, Wolf[i + 1], 0, dimension);
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

}//GWO
