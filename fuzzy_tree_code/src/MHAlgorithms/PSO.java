package MHAlgorithms;

import AdditionalFrames.InitiatorFrame;
import FIS.GPFIS;
import Randoms.*;
import DataReader.Pattern;
import Function.Function;
import MISC.TrainingModuleFIS;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PSO {

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

    // PSO parameters
    public double c0; // inertia weight
    public double c1; // cognitive/local weight
    public double c2; // social/global weight
    public double m_BestGlobalFitness;
    public double[] m_BestGlobalPosition;
    public double[] funFitness;//vector of all fitness values

    Particle[] m_Swarm;
    public double m_MinV[];
    public double m_MaxV[];

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
        //seting arrays of PSO
        FNTweight = new double[dimension];
        c0 = 0.729; // inertia weight
        c1 = 1.49445; // cognitive/local weight
        c2 = 1.49445; // social/global weight
        m_MinV = new double[dimension];
        m_MaxV = new double[dimension];
        m_BestGlobalPosition = new double[dimension];
        funFitness = new double[population];
    }

    public void setAlgo(ArrayList algorithSetUp) {
        c0 = (double) algorithSetUp.get(0); // inertia weight
        c1 = (double) algorithSetUp.get(1); // cognitive/local weight
        c2 = (double) algorithSetUp.get(2); // social/global weight
    }

    public double getBestFitness() {
        return m_BestGlobalFitness;
    }

    public double[] getBestParmeter() {
        return m_BestGlobalPosition;
    }

    public void RandomInitialization() {
        //m_BestGlobalPosition = new double[dimension];
        m_Swarm = new Particle[population];

        for (int i = 0; i < population; i++) { // initialize each Particle in the m_Swarm
            double[] randomPosition = new double[dimension];
            double[] randomVelocity = new double[dimension];
            double[] lbestPosition = new double[dimension];

            for (int j = 0; j < dimension; j++) {
                if ((FunName.equals("FNT") || FunName.equals("FIS") || FunName.equals("CVFNT")) && i == 0) {
                    randomPosition[j] = FNTweight[j];
                } else {
                    randomPosition[j] = low[j] + randGen.random() * (high[j] - low[j]);
                }
                m_MinV[j] = -1.0 * Math.abs(high[j] - low[j]);
                m_MaxV[j] = Math.abs(high[j] - low[j]);
                randomVelocity[j] = m_MinV[j] + randGen.random() * (m_MaxV[j] - m_MinV[j]);
                lbestPosition[j] = randomPosition[j];
            }

            double fitness = computetFitness(randomPosition);
            funFitness[i] = fitness;
            m_Swarm[i] = new Particle(randomPosition, fitness, randomVelocity, lbestPosition, fitness);

            if (m_Swarm[i].m_fitness < m_BestGlobalFitness) { // does current Particle have global best position/solution?
                m_BestGlobalFitness = m_Swarm[i].m_fitness;
                System.arraycopy(m_Swarm[i].m_position, 0, m_BestGlobalPosition, 0, dimension);
                //System.out.println("Global "+m_BestGlobalFitness+" == "+m_Swarm[i].m_fitness);
            }
        }//total number of particles 

    }//end random initialization

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

        //System.out.println("Pso methodn problem"); 
        m_BestGlobalFitness = 10e10;
        RandomInitialization();

        double newFitness = 10e10;
        double[] newVelocity = new double[dimension];
        double[] newPosition = new double[dimension];
        int iteration = 0;
        //m_BestGlobalFitness = 10000000.0; 
        while (iteration < maxIter) {
            //printing steps
            if (isPrintSteps) {
                if (iteration % printStep == 0) {
                    if (isOptimization) {
                        if (storeIndex < 101) {
                            storeBest[storeIndex] = m_BestGlobalFitness;
                            stat = computFitnessStat();
                            storeMean[storeIndex] = stat[0];
                            storeStd[storeIndex] = stat[1];
                            storeIndex++;
                        }
                    }
                    System.out.printf("   MH algo It: %5d Best: %.9f Mean: %.9f Std: %.9f \n", iteration, m_BestGlobalFitness, stat[0], stat[1]);
                }//printsteps
            }//isPrintSteps
            for (int i = 0; i < population; i++) // each Particle
            {
                Particle currParticle = (Particle) m_Swarm[i];

                for (int j = 0; j < dimension; j++) {// each x value of the velocity
                    double r1 = randGen.random();
                    double r2 = randGen.random();

                    newVelocity[j] = (c0 * currParticle.m_velocity[j])
                            + (c1 * r1 * (currParticle.m_bestPosition[j] - currParticle.m_position[j]))
                            + (c2 * r2 * (m_BestGlobalPosition[j] - currParticle.m_position[j]));

                    /* if (Math.abs(newVelocity[j]) > m_MaxV) {
                     if (newVelocity[j] < m_MinV) {
                     newVelocity[j] = m_MinV;
                     } else if (newVelocity[j] > m_MaxV) {
                     newVelocity[j] = m_MaxV;
                     }
                     }*/
                    newPosition[j] = currParticle.m_position[j] + newVelocity[j];
                    //check bound 
                    if (newPosition[j] < low[j]) {
                        newPosition[j] = low[j];
                        newVelocity[j] = 0.0;
                    } else if (newPosition[j] > high[j]) {
                        newPosition[j] = high[j];
                        newVelocity[j] = 0.0;
                    }

                    currParticle.m_velocity[j] = newVelocity[j];
                    currParticle.m_position[j] = newPosition[j];
                }//dimension
                newFitness = computetFitness(newPosition);

                currParticle.m_fitness = newFitness;
                funFitness[i] = newFitness;//update fitness vector

                if (newFitness < currParticle.m_bestFitness) {
                    System.arraycopy(newPosition, 0, currParticle.m_bestPosition, 0, dimension);
                    currParticle.m_bestFitness = newFitness;
                }

                if (newFitness < m_BestGlobalFitness) {
                    System.arraycopy(newPosition, 0, m_BestGlobalPosition, 0, dimension);
                    m_BestGlobalFitness = newFitness;
                }
            } // each Particle
            if (m_BestGlobalFitness < 0.0000001) {
                //System.out.println("treshold" + m_BestGlobalFitness);
                //break;
            }
            iteration++;
        } // while
        //printing final step
        if (isPrintFinal) {
            if (isOptimization) {
                if (storeIndex < 101) {
                    storeBest[storeIndex] = m_BestGlobalFitness;
                    stat = computFitnessStat();
                    storeMean[storeIndex] = stat[0];
                    storeStd[storeIndex] = stat[1];
                    storeIndex++;
                }
            }
            System.out.printf("   MH algo It: %5d Best: %.9f Mean: %.9f Std: %.9f \nTotal Fun Evaluations: %d\n", iteration, m_BestGlobalFitness, stat[0], stat[1], num_Function_Evaluation);
        }
        ArrayList array = new ArrayList();
        array.add(storeBest);
        array.add(storeMean);
        array.add(storeStd);

        newPosition = null;
        m_Swarm = null;
        return array;
    }//end compute

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

    private double computetFitness(double[] sol) {
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
                if (!isClassification) {//RMSE
                    fitnessRet = Function.computeteRMSE(sol, target, predictor, num_predictors, pattern_Length);
                } else {//Accuracy
                    fitnessRet = Function.computeteAccuracy(sol, target, predictor, num_predictors, pattern_Length);
                }
                //System.out.println(" fitness:Computed");
                break;
            default:
                fitnessRet = Function.computeteFunction(sol, FunName);
                break;
        }
        return fitnessRet;
    }

    public double[] getFinalParm() {
        return m_BestGlobalPosition;
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
    }

    public double[][] getWeight() {
        double[][] m_BestGlobalPositionArray = new double[m_Ensemble_Candidates][dimension];
        //finding disting parameters
        double[] bestStructure = new double[m_Ensemble_Candidates];
        int indexBest = 0;
        System.arraycopy(m_BestGlobalPosition, 0, m_BestGlobalPositionArray[indexBest], 0, dimension);
        bestStructure[indexBest] = m_BestGlobalFitness;
        indexBest++;
        if (indexBest < m_Ensemble_Candidates && m_Ensemble_Diversity.equals("Parametric") && !FunName.equals("RMSE")) {
            for (int i = 0; i < population; i++) {
                boolean found = false;
                for (int search = 0; search < indexBest; search++) {
                    if (bestStructure[search] == m_Swarm[i].m_bestFitness) {
                        found = true;
                        break;
                    }
                }
                if (!found && indexBest < m_Ensemble_Candidates) {
                    System.arraycopy(m_Swarm[i].m_bestPosition, 0, m_BestGlobalPositionArray[indexBest], 0, dimension);
                    bestStructure[indexBest] = m_Swarm[i].m_bestFitness;
                    indexBest++;
                }
            }
        }
        if (indexBest < m_Ensemble_Candidates && m_Ensemble_Diversity.equals("Parametric") && !FunName.equals("RMSE")) {
            //System.out.print(" No more distinct trea:  Copying random trees");
            for (int fill = indexBest; fill < m_Ensemble_Candidates; fill++) {
                int rInt = randGen.random(population - 1);
                System.arraycopy(m_Swarm[rInt].m_bestPosition, 0, m_BestGlobalPositionArray[fill], 0, dimension);
                bestStructure[fill] = m_Swarm[rInt].m_bestFitness;
            }
        }//best possible distinct parameters found
        return m_BestGlobalPositionArray;
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
        System.arraycopy(m_BestGlobalPosition, 0, returnWeight[0], 0, dimension);
        return returnWeight;
    }

    public void sortingArray() {
        try {//Sorting population
            try {//Sorting population
                Particle tmp;
                boolean swapped = true;
                int j = 0;
                while (swapped) {
                    swapped = false;
                    j++;
                    for (int i = 0; i < population - j; i++) {
                        if (((Particle) m_Swarm[i]).m_bestFitness > ((Particle) m_Swarm[i + 1]).m_bestFitness) {
                            tmp = (Particle) m_Swarm[i];
                            ((Particle) m_Swarm[i]).m_bestFitness = ((Particle) m_Swarm[i + 1]).m_bestFitness;
                            System.arraycopy(((Particle) m_Swarm[i + 1]).m_bestPosition, 0, ((Particle) m_Swarm[i]).m_bestPosition, 0, dimension);
                            ((Particle) m_Swarm[i + 1]).m_bestFitness = tmp.m_bestFitness;
                            System.arraycopy(tmp.m_bestPosition, 0, ((Particle) m_Swarm[i + 1]).m_bestPosition, 0, dimension);
                            swapped = true;
                        }
                    }
                }
                tmp = null;
            } catch (Exception e) {
                System.out.print("\nError Sorting FNT:" + e);
            }//end Sorting
        } catch (Exception e) {
            System.out.print("\nError Sorting FNT:" + e);
        }//end Sorting
    }
}//end PSO
