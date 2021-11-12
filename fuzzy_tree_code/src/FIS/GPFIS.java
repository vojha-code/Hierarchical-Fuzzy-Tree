package FIS;

import MISC.TrainingModuleFIS;
import Randoms.MersenneTwisterFast;
import java.util.ArrayList;

public class GPFIS {

    //Global Variables
    //Random Algorithm
    MersenneTwisterFast m_Random;
    //JavaRand m_Random;
    //GP -tree Setup variables
    EvaluationFunction m_EvaluationFunction;

    private int m_MaxIterationsGeneral;
    private int m_MaxTreeDepth;
    public int m_funType;
    private final int m_Ensemble_Candidates;
    private final String m_Ensemble_Diversity;

    public int TOURNAMENT_SIZE;//8;
    private int Pool_Size;
    private String m_Objective_Type;
    //GP parameters
    private int m_PopulationGP;
    private int m_IterationsGP;
    private int m_PrintStepGP;
    private double m_DesiredFitnessGP;
    private double MUTATION_PROB;
    private double CROSSOVER_PROB;
    private int ELITISM;

    //MH-Algorithm parameters
    private String m_MHAlgo;
    private String weightsOnly;
    private ArrayList mhParameters;
    private final ArrayList algoParameter;
    //Training variables
    private FuzzyFNT[] m_BestWeightsTreeArray;
    private int indexFit = 0;
    private int m_OutputColumn;
    private final int m_MaxTreeArity;

    private final ArrayList treeParameters;

    /**
     * This is FuzzyFNT parameter setup function/constructor
     *
     * @param random random number generator MT
     * @param evaluationFun Function Evaluation
     * @param treeParams tree parameters
     * @param gpParameters gp parameter settings
     * @param mhParams mh parameter setting
     * @param algoParams individual algorithm parameter
     * @param ensembleParameters ensemble parameters
     */
    public GPFIS(MersenneTwisterFast random, EvaluationFunction evaluationFun, ArrayList treeParams, ArrayList gpParameters, ArrayList mhParams, ArrayList algoParams, ArrayList ensembleParameters) {
        m_Random = random;
        m_EvaluationFunction = evaluationFun;

        treeParameters = treeParams;
        m_MaxIterationsGeneral = (int) treeParameters.get(0);//  int general iteration
        m_MaxTreeDepth = (int) treeParameters.get(1);// int depth of tree
        m_MaxTreeArity = (int) treeParameters.get(2);//int arity of a tree
        m_funType = (int) treeParameters.get(3);// int  activation function type        
        weightsOnly = (String) treeParameters.get(9);//String weight only type

        m_Ensemble_Candidates = (int) ensembleParameters.get(0);// int number of candidates for ensemble
        m_Ensemble_Diversity = (String) ensembleParameters.get(1);//String ensemble diversity type   

        //setting GPFIS parameter
        m_PopulationGP = (int) gpParameters.get(0);//int GPFIS population
        ELITISM = (int) gpParameters.get(1);//double GPFIS Elitism
        MUTATION_PROB = (double) gpParameters.get(2);//double GPFIS Mutation
        CROSSOVER_PROB = (double) gpParameters.get(3);//double GPFIS Crossover
        TOURNAMENT_SIZE = (int) gpParameters.get(4);//int GPFIS Tournament
        m_IterationsGP = (int) gpParameters.get(5);//int GPFIS iteration
        m_Objective_Type = (String) gpParameters.get(6);//String GPFIS training mode/single/multi
        m_PrintStepGP = (int) (0.2 * m_IterationsGP);
        m_DesiredFitnessGP = 0.000000001;

        //seting MH parameters
        mhParameters = mhParams;
        m_MHAlgo = (String) mhParameters.get(0);//String algorithm
        algoParameter = algoParams;
    }//Counstructor: tree set-up

    /**
     * This is FuzzyFNT main loop function
     *
     * @param inputsCount total number of input columns
     * @param Ot total number of output columns
     * @return
     */
    public FuzzyFNT[] doEvolution(int inputsCount, int Ot) {
        Pool_Size = (int) (m_PopulationGP * (50 / 100.0));
        FuzzyFNT m_BestGlobalTree = null;
        FuzzyFNT m_BestLocalTree = null;
        FuzzyFNT m_BestWeightsTree = null;
        FuzzyFNT[] m_BestGlobalTreeArray = new FuzzyFNT[m_Ensemble_Candidates];
        FuzzyFNT[] m_BestLocalTreeArray = new FuzzyFNT[m_Ensemble_Candidates];
        m_BestWeightsTreeArray = new FuzzyFNT[m_Ensemble_Candidates];//used to compute fitenss hence declered outside
        m_OutputColumn = Ot;

        try {
            //The loop for training GENERAL ITERATION:
            for (int i = 0; i < m_MaxIterationsGeneral; i++) {
                //JTextAreaPrintStream.initGUI ();
                System.out.println("INITIALIZATION : Flexible Neural Tree Model");
                System.out.printf("  Total Tree %d  each may have MAX %d feature %d siblings %d heights\n", m_PopulationGP, inputsCount, m_MaxTreeArity, m_MaxTreeDepth);

                //Random Initialization of trees
                FuzzyFNT[] mainPopulation = randomInitialization(inputsCount);
                mainPopulation = fitnessEvaluation(mainPopulation);
                System.out.println("Sorted Population:" + mainPopulation.length);
                displayFNTPopulation(mainPopulation);
                mainPopulation = sortingPopulation(mainPopulation);
                //System.out.println("Sorted Population:" + mainPopulation.length);
                displayFNTPopulation(mainPopulation);

                if (i == 0) {
                    //Preserving a population of size m_Ensemble_Candidates           
                    for (int ibest = 0; ibest < m_Ensemble_Candidates; ibest++) {

                        m_BestGlobalTreeArray[ibest] = mainPopulation[ibest];
                        m_BestGlobalTreeArray[ibest] = m_BestGlobalTreeArray[ibest].copyTree();
                        m_BestLocalTreeArray[ibest] = mainPopulation[ibest];
                        m_BestLocalTreeArray[ibest] = m_BestLocalTreeArray[ibest].copyTree();
                        ////System.out.println("    Initial Global Best Model(s) :"+m_BestGlobalTreeArray[ibest]);

                    }//for

                    m_BestGlobalTree = findBestTree(mainPopulation);
                    ////System.out.print("Best Tree:"+m_BestGlobalTree.getFitness()+"=="+m_BestGlobalTreeArray[0].getFitness());//+m_BestGlobalTree+" :"+bestFit);
                    ////System.out.println("FN->"+m_BestGlobalTree.m_FunctChilds.size()+" LN->"+m_BestGlobalTree.m_LeafChilds.size()+" FIT->"+m_BestGlobalTree.getFitness());
                    ////System.out.println("FN->"+m_BestGlobalTree.m_FunctChilds.size()+" LN->"+m_BestGlobalTree.m_LeafChilds.size()+" FIT->"+m_BestGlobalTree.getFitness());
                    m_BestGlobalTree = m_BestGlobalTree.copyTree();
                    //double bestFit = m_BestGlobalTree.getFitness();
                    m_BestLocalTree = m_BestGlobalTree;
                    m_BestLocalTree = m_BestLocalTree.copyTree();
                    //m_BestGlobalTree.printTree();
                }//if i == 0

                System.out.println(" Structure Optimization : GP loop ");
                int j = 0;
                int noImprovement = 0;
                //Genetic programming
                do {
                    //print iteration and stor best
                    //Initializing/re-Initializing bestTree
                    //System.out.println(j + " generation\n Main population:" + mainPopulation.length);
                    FuzzyFNT bestTree = findBestTree(mainPopulation);
                    bestTree = bestTree.copyTree();
                    if ((j+1) % m_PrintStepGP == 0 || j == 0) {
                        System.out.printf("ITR: Gen  %d GP %5d FIT: Tree: %.9f  (%3d)  \n", i, j, bestTree.getFitness(), bestTree.size());
                    }//if
                    //Checking better structre (if any)
                    if ((bestTree.getFitness() + 0.000001 < m_BestLocalTree.getFitness()) || j == 0) {
                        //System.out.printf("ITR: Gen  %d Better Structure: OLD: %.9f  (%3d)  NEW: %.9f (%3d) \n", i, m_BestLocalTree.getFitness(), m_BestLocalTree.size(), bestTree.getFitness(), bestTree.size());
                        m_BestLocalTree = null;
                        m_BestLocalTree = bestTree;
                        m_BestLocalTree = m_BestLocalTree.copyTree();
                        bestTree = null;
                    }//if

                    //structure optimization by GPFIS
                    if (m_Objective_Type.equals("Single Objective")) {
                        FuzzyFNT[] pool = matingPool(mainPopulation, mainPopulation.length);
                        FuzzyFNT[] offspring = createNewPopulation(pool);
                        offspring = fitnessEvaluation(offspring);
                        System.arraycopy(offspring, 0, mainPopulation, 0, mainPopulation.length);
                        mainPopulation = sortingPopulation(mainPopulation);
                    } else {
                        FuzzyFNT[] pool = matingPool(mainPopulation, Pool_Size);
                        FuzzyFNT[] offspring = createNewPopulation(pool);
                        offspring = fitnessEvaluation(offspring);
                        FuzzyFNT[] intermediatePop = mergePopulation(mainPopulation, offspring);
                        intermediatePop = sortingPopulation(intermediatePop);
                        mainPopulation = reducePopulation(intermediatePop);
                        //mainPopulation = distinctlyReducedPopulaton(intermediatePop);
                    }//if                    
                    j++;//increament loop
                    if (noImprovement >= 10000) {
                        System.out.println(" No Improvement");
                        break;
                    } else {
                        noImprovement++;
                    }
                } while (j < m_IterationsGP);
                //System.out.println("Best Local Population:");
                //displayFNTPopulation(mainPopulation);

                //finding distinct FNTs for Parameter training
                m_BestLocalTreeArray = findDistinctBest(mainPopulation, m_BestLocalTree);
                //weights optimization by Metaheuristic	
                parameterOptimization(m_BestLocalTreeArray, weightsOnly);
                //finding best Global tree weight tree
                FuzzyFNT[] tempGlobal = findGLobalBestTree(m_BestGlobalTreeArray);//it murge with previous global and find the best one
                //m_BestWeightsTree.printTree();
                m_BestWeightsTree = null;
                m_BestWeightsTree = m_BestLocalTreeArray[0];
                m_BestWeightsTree = m_BestWeightsTree.copyTree();
                System.out.printf("  Best Global Fitness: %.9f  Best Local fitness:  %.9f \n", m_BestGlobalTree.getFitness(), m_BestWeightsTreeArray[0].getFitness());

                //better solution found
                for (int ibest = 0; ibest < m_Ensemble_Candidates; ibest++) {
                    if (tempGlobal[ibest].getFitness() < m_BestGlobalTreeArray[ibest].getFitness()) {
                        System.out.printf("  Better Global tree OLD: %.9f  NEW: %.9f \n", m_BestGlobalTreeArray[ibest].getFitness(), tempGlobal[ibest].getFitness());
                        m_BestGlobalTreeArray[ibest] = null;
                        m_BestGlobalTreeArray[ibest] = tempGlobal[ibest];
                        m_BestGlobalTreeArray[ibest] = m_BestGlobalTreeArray[ibest].copyTree();
                    } else {
                        System.out.printf("  Better Global tree OLD: %.9f  NO Replacement \n", m_BestGlobalTreeArray[ibest].getFitness());
                    }
                }//for
                if (m_BestWeightsTree.getFitness() < m_BestGlobalTree.getFitness()) {
                    ////System.out.println("  Better Global tree found, Before: " + m_BestGlobalTree.getFitness() + " After: " + m_BestWeightsTreeArray[0].getFitness());
                    //report = report + ("  Better Global tree found, Before: " + m_BestGlobalTree.getFitness() + " After: " + m_BestWeightsTreeArray[0].getFitness()) + "\n";
                    m_BestGlobalTree = null;
                    m_BestGlobalTree = m_BestWeightsTreeArray[0];
                    m_BestGlobalTree = m_BestGlobalTree.copyTree();
                }
                if (m_BestGlobalTree.getFitness() <= m_DesiredFitnessGP) {
                    break;
                }
            }//next General Iteration  
        } catch (Exception e) {
            System.out.println("Error Gen Itr:" + e);
            System.exit(0);
        }
        m_BestWeightsTree = null;
        m_BestWeightsTreeArray = null;
        m_BestLocalTreeArray = null;
        //JTextAreaPrintStream.show();
        return m_BestGlobalTreeArray;
    }//end evolution

    FuzzyFNT[] randomInitialization(int inputsCount) {
        FuzzyFNT[] retTree = new FuzzyFNT[m_PopulationGP];
        for (int i = 0; i < m_PopulationGP; i++) {
            retTree[i] = new FuzzyFNT(m_Random);
            do {
                //tree.randomConstruction(inputsCount, m_MaxTreeArity, m_MaxTreeDepth, m_funType);
                retTree[i].randomConstruction(inputsCount, treeParameters);
                retTree[i].setWeightsOnly(weightsOnly);
            } while (retTree[i].m_FunctChilds.size() < 1);
        }
        return retTree;
    }//fun: Random Initialization 

    /**
     * Computes the fitness of N trees
     *
     * @param tree array of tree
     * @return Array of tree with computed fitness values
     */
    private FuzzyFNT[] fitnessEvaluation(FuzzyFNT[] tree) {
        int N = tree.length;
        for (int i = 0; i < N; i++) {
            tree[i].setFitness(fitnessEvaluation(tree[i]));
        }//for
        return tree;
    }//fun: fitness Evaluation

    /**
     * Computes the fitness of a tree
     *
     * @param tree an individual tree
     * @return fitness value
     */
    private double fitnessEvaluation(FuzzyFNT tree) {
        //double d = m_EvaluationFunction.evaluateFitness(tree);
        ////System.out.println(tree+"  "+d);
        return m_EvaluationFunction.evaluateFitness(tree, m_OutputColumn);
    }//fun: fitness evaluation

    /**
     * Finds best tree from an array of tree
     *
     * @param tree an array of tree
     * @return the best tree or fittest tree
     */
    private FuzzyFNT findBestTree(FuzzyFNT[] tree) {
        FuzzyFNT bestTree = null;
        try {
            double bestFitness = 10e10;
            for (FuzzyFNT tree1 : tree) {
                double currFitness = tree1.getFitness();
                if (currFitness < bestFitness) {
                    bestTree = tree1;
                    bestFitness = currFitness;
                }
            } //for
            ////System.out.println("Best FuzzyFNT has fitness:"+bestFitness);		
        } catch (Exception e) {
            //System.out.print("Error findong Best tree\n" + e);
        }
        return bestTree;
    }//fun: find best tree 

    private void displayFNTPopulation(FuzzyFNT[] tree) {
        MiscOperations miscOP = new MiscOperations();
        if (m_Objective_Type.equals("Single Objective")) {
            miscOP.displaySO(tree);
        } else {
            miscOP.display(tree);
        }//if       
    }//end Display FuzzyFNT

    private FuzzyFNT[] sortingPopulation(FuzzyFNT[] tree) {
        MiscOperations miscOP = new MiscOperations();
        if (m_Objective_Type.equals("Single Objective")) {
            return miscOP.sorting_SO_Population(tree);
        } else {
            return miscOP.nonDominationSort(tree);
        }//if
    }//fun:Soring

    private FuzzyFNT[] matingPool(FuzzyFNT[] tree, int poolSize) {
        //System.out.println(" Pool Size: " + poolSize);
        TournamentSelection ts = new TournamentSelection();
        if (m_Objective_Type.equals("Single Objective")) {
            return ts.tournament_selection_SO(m_Random, tree, poolSize, TOURNAMENT_SIZE);
        } else {
            return ts.tournament_selection(m_Random, tree, poolSize, TOURNAMENT_SIZE);
        }//if              
    }//fun: mating pool

    private FuzzyFNT[] createNewPopulation(FuzzyFNT[] pool) {
        Genetic_Operator genetic = new Genetic_Operator();
        return genetic.geneticOperators(m_Random, pool, MUTATION_PROB, treeParameters);
    }//fun: new Population created

    FuzzyFNT[] mergePopulation(FuzzyFNT[] tree, FuzzyFNT[] tree1) {
        MiscOperations miscOP = new MiscOperations();
        return miscOP.mergePopulation(tree, tree1);
    }//fun: mergeTree

    private FuzzyFNT[] reducePopulation(FuzzyFNT[] tree) {
        MiscOperations miscOP = new MiscOperations();
        if (m_Objective_Type.equals("Single Objective")) {
            return miscOP.replace_chromosome_SO(tree, m_PopulationGP);
        } else {
            return miscOP.replace_chromosome(tree, m_PopulationGP);
        }//if 
    }//fun : reducedPopulation

    private FuzzyFNT[] distinctlyReducedPopulaton(FuzzyFNT[] tree) {
        MiscOperations miscOP = new MiscOperations();
        return miscOP.replaceWithBestDistinct(tree, m_PopulationGP);
    }//return distinct population

    public double getMHfitness(double[] position) {
        m_BestWeightsTreeArray[indexFit].setParameters(position);
        //fitnessEvaluation(m_BestWeightsTree);
        return fitnessEvaluation(m_BestWeightsTreeArray[indexFit]);//m_BestWeightsTree.getFitness();
    }

    private FuzzyFNT[] findDistinctBest(FuzzyFNT[] mainPopulation, FuzzyFNT m_BestLocalTree) {
        //preprocessing before finding best local trees
        //FNT[] m_bstlclTA = new FuzzyFNT[m_Ensemble_Candidates];
        if (m_BestLocalTree.getFitness() < findBestTree(mainPopulation).getFitness()) {
            FuzzyFNT[] diffBest = new FuzzyFNT[1];
            diffBest[0] = m_BestLocalTree.copyTree();
            
            mainPopulation = mergePopulation(mainPopulation, diffBest);
            mainPopulation = sortingPopulation(mainPopulation);
            //System.out.println("Best Local Population:");
            //displayFNTPopulation(mainPopulation);
        }//if
        MiscOperations miscOP = new MiscOperations();
        return miscOP.replaceWithBestDistinct(mainPopulation, m_Ensemble_Candidates);
    }//fun : find best local tree

    private void parameterOptimization(FuzzyFNT[] m_BestLocalTreeArray, String weightsOnly) {
        System.out.println(" Parameter Optimization : MH  ");
        TrainingModuleFIS mhAlgo = new TrainingModuleFIS();
        double[][] bestPosition;//recieve best paramters from MH

        if (m_Ensemble_Candidates == 1 || m_Ensemble_Diversity.equals("Parametric")) {
            indexFit = 0;//used for computing MH fitness 
            for (int k = 0; k < m_Ensemble_Candidates; k++) {
                m_BestWeightsTreeArray[k] = null;
                m_BestWeightsTreeArray[k] = m_BestLocalTreeArray[0].copyTree();//in papametric takes only best tree
                m_BestWeightsTreeArray[k].setWeightsOnly(weightsOnly);
            }
            int mhDimension = m_BestWeightsTreeArray[0].getParametersCount();
            mhParameters.set(1, mhDimension);//seting dimension
            double[][] mhParameterRange = m_BestWeightsTreeArray[0].getParametersRange(mhDimension, treeParameters);
            double[] mhBestParameter = m_BestWeightsTreeArray[0].getParameters(mhDimension);
            bestPosition = new double[m_Ensemble_Candidates][mhDimension];

            System.out.println("  " + m_MHAlgo + " dimension : " + mhDimension + " = " + mhParameterRange.length + " = " + mhBestParameter.length);
            bestPosition = mhAlgo.MHAlgorithmsReturn(m_Random, mhParameters, algoParameter, this, mhBestParameter, mhParameterRange, m_Ensemble_Candidates, m_Ensemble_Diversity);
            // take best weights and set
            for (int k = 0; k < m_Ensemble_Candidates; k++) {
                m_BestWeightsTreeArray[k].setWeightsOnly(weightsOnly);
                m_BestWeightsTreeArray[k].setParameters(bestPosition[k]);
                m_BestWeightsTreeArray[k].setFitness(fitnessEvaluation(m_BestWeightsTreeArray[k]));
                System.out.println("Parameter best fitenss : " + m_BestWeightsTreeArray[k].getFitness());
            }//for: candidates
            bestPosition = null;//dispose bestPostion
        } else if (m_Ensemble_Diversity.equals("Structural")) {
            //System.out.println();
            for (int k = 0; k < m_Ensemble_Candidates; k++) {
                indexFit = k;//used for computing MH fitness 
                //copy best local tree
                ////System.out.print("\n\nProcessing" + k + "th Tree");
                m_BestWeightsTreeArray[k] = null;
                m_BestWeightsTreeArray[k] = m_BestLocalTreeArray[k].copyTree();
                m_BestWeightsTreeArray[k].setWeightsOnly(weightsOnly);
                int mhDimension = m_BestWeightsTreeArray[k].getParametersCount();
                double[][] mhParameterRange = m_BestWeightsTreeArray[k].getParametersRange(mhDimension, treeParameters);
                double[] mhBestParameter = m_BestWeightsTreeArray[k].getParameters(mhDimension);
                bestPosition = new double[1][mhDimension];
                //System.out.print("  " + m_MHAlgo + " dimension: " + mhDimension);
                mhParameters.set(1, mhDimension);//seting dimension at postion 1
                bestPosition = mhAlgo.MHAlgorithmsReturn(m_Random, mhParameters, algoParameter, this, mhBestParameter, mhParameterRange, 1, m_Ensemble_Diversity);//1 beacuase we are trainig single structure
                ////System.out.print("\n\nProcessing " + k);
                // take best weights and set
                m_BestWeightsTreeArray[k].setWeightsOnly(weightsOnly);
                m_BestWeightsTreeArray[k].setParameters(bestPosition[0]);//'0' for the first weights (and there is onl one weight) 
                m_BestWeightsTreeArray[k].setFitness(fitnessEvaluation(m_BestWeightsTreeArray[k]));
                ////System.out.print("\n\nfinished");
                bestPosition = null;//dispose bestPostion
            }//for: candidates optimization
            ////System.out.print("\n\nProcessing all structure OK");
        }//if structure
        mhAlgo = null;//dispose MH algo variable
    }//fun: parameter Optimization

    private FuzzyFNT[] findGLobalBestTree(FuzzyFNT[] globalTreeLocal) {
        MiscOperations miscOP = new MiscOperations();
        FuzzyFNT[] tree = miscOP.mergePopulation(globalTreeLocal, m_BestWeightsTreeArray);
        return miscOP.replaceWithBestDistinct(tree, m_Ensemble_Candidates);
        /*MiscOperations miscOP = new MiscOperations();
         FuzzyFNT[] tree = miscOP.mergePopulation(globalTree, m_BestWeightsTreeArray);
         return miscOP.replace_chromosome_SO(tree, m_Ensemble_Candidates);*/
    }

}//end GPFIS class
