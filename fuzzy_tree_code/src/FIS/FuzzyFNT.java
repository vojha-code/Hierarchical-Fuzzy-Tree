package FIS;

import Randoms.*;
import java.io.*;
import java.util.*;

public class FuzzyFNT {

    //FNT variables
    FunctNode m_Root;
    double m_Fitness;//obj = 1
    int m_Size;   //obj = 2
    int m_Diversity;//obj = 3
    int m_Depth;//obj = 4

    int m_rank = -1;
    double m_dist = -1.0;

    Vector m_FunctChilds;
    Vector m_LeafChilds;

    String m_WeightsOnly;
    //JavaRand m_RNG;
    MersenneTwisterFast m_RNG;
    //MersenneTwisterFast m_RNG;
    public int m_InputsCount;
    public int m_MaxArity;

    /**
     *
     * @param random
     *
     */
    public FuzzyFNT(MersenneTwisterFast random) {
        m_Root = null;
        m_FunctChilds = new Vector();
        m_LeafChilds = new Vector();
        m_Depth = 0;
        m_WeightsOnly = "";// setWeightsOnly is set by a fucntion later
        m_RNG = random;
        //System.out.println("FuzzyFNT Constructor:Done");
    }//end Constructor FuzzyFNT

    /**
     * Random construction of the trees using recursion
     *
     * @param inputsCount (Integer) indicated the total number of input features
     * @param treeParameters
     *
     */
    public void randomConstruction(int inputsCount, ArrayList treeParameters) {//int maxArity, int maxDepth, int m_funType, double nodeMin, double nodeMax, double edgeMin, double edgeMax) {
        int maxDepth = (int) treeParameters.get(1);// int depth of tree
        int maxArity = (int) treeParameters.get(2);//int arity of a tree
        int m_funType = (int) treeParameters.get(3); // int  activation function type        
        double[] range_a = (double[]) treeParameters.get(4);//double[] min max a
        double[] range_b = (double[]) treeParameters.get(5);//double[] min max a
        double[] range_c = (double[]) treeParameters.get(6);//double[] min max c
        double[] range_weight = (double[]) treeParameters.get(7);//double[] min max weight
        int[] inPart = (int[]) treeParameters.get(8);//int[] rules
        String weightsOnly = (String) treeParameters.get(9);//int[] rules
        double[] range_d = (double[]) treeParameters.get(10);//double[] deviation param
        double[] range_e = (double[]) treeParameters.get(11);//double[] deviation consequent
        String FSType = (String) treeParameters.get(12);//int[] Type of node

        m_InputsCount = inputsCount;
        m_MaxArity = maxArity;
        int in_part = 2;//partition aleaset two
        if (inPart[0] == 0) {//Max
            in_part = 2 + m_RNG.random(inPart[1]);//partition aleaset two
        } else {
            in_part = inPart[1];//Total fixed
        }

        int actFun = m_funType;
        if (0 == actFun) {
            actFun = 1 + m_RNG.random(7);//random selection currently only 7 function are implemented
        }

        //System.out.print(actFun);
        int minArity = 2;
        int range = maxArity - minArity;
        if (range == 0) {
            range = 1;
        }
        int arity = minArity + m_RNG.random(range);

        double[][] a = new double[arity][in_part];
        a = ruleParm(a, m_RNG, range_a[0], range_a[1]);
        double[][] b = new double[arity][in_part];
        b = ruleParm(b, m_RNG, range_b[0], range_b[1]);
        double[][] d;
        int no_rules = (int) Math.pow(in_part, arity);
        //System.out.println(no_rules+" "+arity);  
        double[][] c = new double[no_rules][arity + 1];//c0,c1,c2 if arity is 2 for each rule
        double[][] e;
        c = ruleParm(c, m_RNG, range_c[0], range_c[1]);
        if (FSType.equalsIgnoreCase("Type-II")) {
            d = new double[arity][in_part];
            d = ruleParm(d, m_RNG, range_d[0], range_d[1]);//change dev
            e = new double[no_rules][arity + 1];//c0,c1,c2 if arity is 2 for each rule
            e = ruleParm(e, m_RNG, range_e[0], range_e[1]);//change dev
        } else {
            d = new double[][]{{}};
            e = new double[][]{{}};
        }
        double weight = m_RNG.random(range_weight[0], range_weight[1]);
        if (weightsOnly.equalsIgnoreCase("Rules_Parmeters")) {
            weight = 1.0; //keep weights eqaul to one
        }
        m_Root = new FunctNode(FSType, weight, in_part, a, b, c, d, e, actFun, arity, m_Root, m_RNG);
        //System.out.println("Root:"+this+" has arity "+arity);
        m_Root.generateChildren(inputsCount, 0, maxDepth, treeParameters);
        inspectChilds();
        //System.out.println("FuzzyFNT random Constructor:Done");
    }//end randomConstruction

    private double[][] ruleParm(double[][] x, MersenneTwisterFast m_RNG, double min, double max) {
        int N = x.length;
        int D = x[0].length;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < D; j++) {
                x[i][j] = m_RNG.random(min, max);
            }
        }
        return x;
    }//set A B C

    /**
     * Inspects he the created tree for retrieving its function and leaf child's
     * information
     */
    public void inspectChilds() {
        try {
            m_FunctChilds.clear();
            m_LeafChilds.clear();
            m_Depth = 0;
            m_Root.inspect(this, 0);
            setDiversity();
        } catch (Exception e) {
            System.out.print("Error Inspection FNT" + this.getClass().getName() + " " + e);
        }
    }//end inspect FuzzyFNT

    /**
     * Set "true" if wants to optimize the tree weights only
     *
     * @param weightsOnly true/false
     */
    public void setWeightsOnly(String weightsOnly) {
        m_WeightsOnly = weightsOnly;
    }

    public void setFitness(double fitness) {
        m_Fitness = fitness;
    }

    public double getFitness() {
        return m_Fitness;
    }

    public void setRank(int rank) {
        m_rank = rank;
    }

    public int getRank() {
        return m_rank;
    }

    public void setDistance(double dist) {
        m_dist = dist;
    }

    public double getDistance() {
        return m_dist;
    }

    public void setDiversity() {
        Vector div = new Vector();
        for (Object m_FunctChild : m_FunctChilds) {
            FunctNode cur = (FunctNode) m_FunctChild;
            div.add(cur.m_actFun);
        }//for
        int[] arr = new int[div.size()];
        for (int i = 0; i < div.size(); i++) {
            arr[i] = Integer.parseInt(div.get(i).toString());
        }//for i
        int diversity = countDistinctElements(arr);
        m_Diversity = -1 * diversity;
    }//fun

    public int countDistinctElements(int[] arr) {
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            boolean isDistinct = false;
            for (int j = 0; j < i; j++) {
                if (arr[i] == arr[j]) {
                    isDistinct = true;
                    break;
                }//if
            }//for
            if (!isDistinct) {
                count++;
                //System.out.print(arr[i]+" ");
            }//if
        }//for
        return count;
    }//fun

    public int getDiversity() {
        return m_Diversity;
    }

    public Vector getFunctionChild() {
        return m_FunctChilds;
    }

    public Vector getLeafChild() {
        return m_LeafChilds;
    }

    /**
     * Returns the node activation value
     *
     * @param inputs an array of input features
     * @param inputsCount total number of input features
     * @return activation value
     */
    public double getOutput(double[] inputs, int inputsCount) {
//        for(int i = 0;i < inputs.length;i++){
//            System.out.println(inputs[i]);
//        }
        return m_Root.getOutput(inputs);
    }

    /**
     * Adding function node in a vector of function nodes
     *
     * @param functNode function node of the tree
     */
    public void addFunctChild(FunctNode functNode) {
        //System.out.println("fun child"); 
        m_FunctChilds.add(functNode);
    }

    /**
     * Adding leaf node in a vector of leaf nodes
     *
     * @param leafNode leaf node of the tree
     */
    public void addLeafChild(LeafNode leafNode) {
        //System.out.println("leaf child");
        m_LeafChilds.add(leafNode);
    }

    /**
     * Returns height of the tree
     *
     * @return height of the tree
     */
    public int getDepth() {
        return m_Depth;
    }

    /**
     * Set height of the tree
     *
     * @param depth height of the tree
     */
    public void setDepth(int depth) {
        m_Depth = depth;
    }

    /**
     * Size of the tree in terms of total number of nodes
     *
     * @return total number of node
     */
    public int size() {
        return (m_FunctChilds.size() + m_LeafChilds.size());
    }

    public int getSize() {
        m_Size = getParametersCount();
        //m_Size = (m_FunctChilds.size() + m_LeafChilds.size());
        return m_Size;
    }

    /**
     * Print tree for console
     */
    public void printTree() {
        m_Root.print(0);
    }

    /**
     * Print tree to a data/text file
     *
     * @param fileName name of the file tree to be printed
     */
    public void printTreeFile(String fileName) {
        try {
            FileWriter fw = new FileWriter(fileName);
            BufferedWriter bw = new BufferedWriter(fw);
            try (PrintWriter file = new PrintWriter(bw)) {
                m_Root.printFile(0, file);
            }
            bw.close();
        } catch (Exception e) {
            System.out.print("Error PrintFile:" + this.getClass().getName() + " " + e);
        }
    }

    /**
     * Read a saved tree model
     *
     * @param fileName the filename of the saved tree model
     */
    public void readSavedFNTmodel(String fileName) {
        //System.out.println("Model Reading " + fileName);
        try {
            FileReader fin = new FileReader(fileName);
            try (BufferedReader brProb = new BufferedReader(fin)) {
                String rootData;// = brProb.readLine();
                double weight = 0.0;
                int parts = 2;
                double[][] a = new double[][]{{}};
                double[][] b = new double[][]{{}};
                double[][] devp = new double[][]{{}};
                double[][] c = new double[][]{{}};
                double[][] devc = new double[][]{{}};
                int actFun = 0;
                int arity = 0;
                String FSType = "Type-I";//1 for type ot type 2

                if ((rootData = brProb.readLine()) != null) {
                    if (rootData.contains("f")) {
                        double[] d = new double[4];
                        
                        String[] tokens = rootData.split(",");
                        int t = 1;//at t =0 values is "f" indicates root as a function node
                        actFun = Integer.parseInt(tokens[t++]);//at t = 1 values is of activation func type
                        arity = (int) Double.parseDouble(tokens[t++]);//at t = 2 values is of activation func type
                        weight = Double.parseDouble(tokens[t++]);//at t = 3 values is of weight
                        parts = (int) Double.parseDouble(tokens[t++]);//at t = 4 values is of parts
                        int type = (int) Double.parseDouble(tokens[t++]);//at t = 4 values is of parts
                        int rules = (int) Math.pow(parts, arity);//derived from parts and arity information
                        FSType = (type == 1) ? "Type-I" : "Type-II";

                        a = new double[arity][parts];
                        b = new double[arity][parts];
                        c = new double[rules][arity + 1];
                        for (int ii = 0; ii < a.length; ii++) {
                            for (int jj = 0; jj < a[0].length; jj++) {
                                a[ii][jj] = Double.parseDouble(tokens[t++]);
                            }
                        }
                        for (int ii = 0; ii < b.length; ii++) {
                            for (int jj = 0; jj < b[0].length; jj++) {
                                b[ii][jj] = Double.parseDouble(tokens[t++]);
                            }
                        }
                        for (int ii = 0; ii < c.length; ii++) {
                            for (int jj = 0; jj < c[0].length; jj++) {
                                c[ii][jj] = Double.parseDouble(tokens[t++]);
                            }
                        }

                        if (FSType.equalsIgnoreCase("Type-II")) {
                            System.out.println("Reading Type 2 node");
                            devp = new double[arity][parts];
                            devc = new double[rules][arity + 1];
                            for (int ii = 0; ii < devp.length; ii++) {
                                for (int jj = 0; jj < devp[0].length; jj++) {
                                    devp[ii][jj] = Double.parseDouble(tokens[t++]);
                                }
                            }
                            for (int ii = 0; ii < devc.length; ii++) {
                                for (int jj = 0; jj < devc[0].length; jj++) {
                                    devc[ii][jj] = Double.parseDouble(tokens[t++]);
                                }
                            }
                        }//if Type-II
                    }
                }//if
                m_Root = new FunctNode(FSType, weight, parts, a, b, c, devp, devc, actFun, arity, m_Root, m_RNG);
                //System.out.println("Root:"+this+" has arity "+arity);
                //m_Root.readChildren(brProb,0);
                m_Root.readChildren(brProb);
                inspectChilds();
                brProb.close();
                //System.out.println("FuzzyFNT random Constructor:Done");
            } // = brProb.readLine();
        } catch (IOException | NumberFormatException e) {
            System.out.println("Model Reading Error" + this.getClass().getName() + " " + e);
        }
    }//end Model reading

    /**
     * Print function nodes only to console
     */
    public void printFunctNodes() {
        System.out.print("{");
        for (Object m_FunctChild : m_FunctChilds) {
            FunctNode cur = (FunctNode) m_FunctChild;
            System.out.print("+" + cur.getArity() + "(" + cur.getWeight() + "," + cur.getA() + "," + cur.getB() + "," + cur.getC() + "," + cur.getD() + ") , ");
        }
        System.out.print("}\n");
    }//end printFunction

    /**
     * Prints leaf nodes only to console
     */
    public void printLeafNodes() {
        System.out.print("{");
        for (Object m_LeafChild : m_LeafChilds) {
            LeafNode cur = (LeafNode) m_LeafChild;
            System.out.print(cur.getInputNumber() + "(" + cur.getWeight() + ") , ");
        }
        System.out.print("} \n");
    }//end printLeaf

    /**
     * Copy the current object of the tree to another object to preserve and
     * protect from other operations
     *
     * @return a new FuzzyFNT with new object
     */
    public FuzzyFNT copyTree() {
        FuzzyFNT tree = new FuzzyFNT(m_RNG);
        tree.m_MaxArity = m_MaxArity;
        tree.m_InputsCount = m_InputsCount;
        tree.m_Fitness = m_Fitness;
        tree.m_WeightsOnly = m_WeightsOnly;
        tree.m_Root = (FunctNode) m_Root.copyNode(m_Root);
        tree.inspectChilds();
        return tree;
    }//end copyTree

    /**
     * Returns the tree count real values weights and activation function
     * parameters
     *
     * @return return the tree parameters count
     */
    public int getParametersCount() {
        int nParamCount = 0;
        try {
            if (m_WeightsOnly.equalsIgnoreCase("Rules_Parmeters_And_Input_Weights")) {
                nParamCount = 0;
                int parts = m_Root.getInputPart();
                int arity = m_Root.getArity();
                int no_rules = (int) Math.pow(parts, arity);
                if (m_Root.getNodeType().equalsIgnoreCase("Type-II")) {
                    //root A[arity][parts] + B[arity][parts] + devp[arity][parts] + C[rules][arity+1] + devc[rules][arity+1]
                    nParamCount = 3 * (arity * parts) + 2 * (no_rules * (arity + 1));
                } else {
                    //root A[arity][parts] + B[arity][parts] + C[rules][arity+1]
                    nParamCount = 2 * (arity * parts) + no_rules * (arity + 1);
                }

                for (Object m_FunctChild : m_FunctChilds) {
                    FunctNode cur = (FunctNode) m_FunctChild;
                    nParamCount += 1;//for the weights 
                    parts = cur.getInputPart();
                    arity = cur.getArity();
                    no_rules = (int) Math.pow(parts, arity);
                    if (cur.getNodeType().equalsIgnoreCase("Type-II")) {
                        //root A[arity][parts] + B[arity][parts] + devp[arity][parts] + C[rules][arity+1] + devc[rules][arity+1]
                        nParamCount += 3 * (arity * parts) + 2 * (no_rules * (arity + 1));
                    } else {
                        //root A[arity][parts] + B[arity][parts] + C[rules][arity+1]
                        nParamCount += 2 * (arity * parts) + no_rules * (arity + 1);
                    }
                }
                for (Object m_LeafChild : m_LeafChilds) {
                    nParamCount += 1;//for the weights 
                }
            } else if (m_WeightsOnly.equalsIgnoreCase("Inputs_Weights")) {
                nParamCount = 0;
                nParamCount += m_FunctChilds.size(); // weights
                nParamCount += m_LeafChilds.size();
            } else {//Nodes only
                nParamCount = 0;
                //m_Root.print(0);//Check the parameters of the tree
                int parts = m_Root.getInputPart();
                int arity = m_Root.getArity();
                int no_rules = (int) Math.pow(parts, arity);
                //System.out.println(rules + " - " + rules);
                if (m_Root.getNodeType().equalsIgnoreCase("Type-II")) {
                    //root A[arity][parts] + B[arity][parts] + devp[arity][parts] + C[rules][arity+1] + devc[rules][arity+1]
                    nParamCount = 3 * (arity * parts) + 2 * (no_rules * (arity + 1));
                } else {
                    //root A[arity][parts] + B[arity][parts] + C[rules][arity+1]
                    nParamCount = 2 * (arity * parts) + no_rules * (arity + 1);
                }
                for (Object m_FunctChild : m_FunctChilds) {
                    FunctNode cur = (FunctNode) m_FunctChild;
                    //nParamCount += 1;//increase it for the weights (Do not take it as a parameter)
                    parts = cur.getInputPart();
                    arity = cur.getArity();
                    no_rules = (int) Math.pow(parts, arity);
                    //System.out.println(rules + " - " + rules);
                    if (cur.getNodeType().equalsIgnoreCase("Type-II")) {
                        //root A[arity][parts] + B[arity][parts] + devp[arity][parts] + C[rules][arity+1] + devc[rules][arity+1]
                        nParamCount += 3 * (arity * parts) + 2 * (no_rules * (arity + 1));
                    } else {
                        //root A[arity][parts] + B[arity][parts] + C[rules][arity+1]
                        nParamCount += 2 * (arity * parts) + no_rules * (arity + 1);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting paramter count " + e);
            //System.exit(0);
        }
        return nParamCount;
    }//end ParameterCount

    /**
     * Set the tree real values weights and activation function parameters
     *
     * @param parameters total number of parameters
     * @return return the tree parameters count
     */
    public double[] getParameters(int parameters) {
        double mhWeights[] = new double[parameters];
        try {
            double[][] nodeVectorA;
            double[][] nodeVectorB;
            double[][] nodeVectorC;
            double[][] nodeVectorD = new double[][]{{}};
            double[][] nodeVectorE = new double[][]{{}};
            if (m_WeightsOnly.equalsIgnoreCase("Rules_Parmeters_And_Input_Weights")) {
                int idx = 0;
                nodeVectorA = m_Root.getA();
                nodeVectorB = m_Root.getB();
                nodeVectorC = m_Root.getC();
                for (int i = 0; i < nodeVectorA.length; i++) {
                    for (int j = 0; j < nodeVectorA[0].length; j++) {
                        mhWeights[idx] = nodeVectorA[i][j];//IF part MF values
                        //System.out.print(" " + mhWeights[idx]);
                        idx++;
                    }
                }
                for (int i = 0; i < nodeVectorB.length; i++) {
                    for (int j = 0; j < nodeVectorB[0].length; j++) {
                        mhWeights[idx] = nodeVectorB[i][j];//THEN part MF values
                        //System.out.print(" " + mhWeights[idx]);
                        idx++;
                    }
                }
                for (int i = 0; i < nodeVectorC.length; i++) {
                    for (int j = 0; j < nodeVectorC[0].length; j++) {
                        mhWeights[idx] = nodeVectorC[i][j];//THEN part MF values
                        //System.out.print(" " + mhWeights[idx]);
                        idx++;
                    }
                }
                if (m_Root.getNodeType().equalsIgnoreCase("Type-II")) {
                    nodeVectorD = m_Root.getD();
                    nodeVectorE = m_Root.getE();
                    for (int i = 0; i < nodeVectorD.length; i++) {
                        for (int j = 0; j < nodeVectorD[0].length; j++) {
                            mhWeights[idx] = nodeVectorD[i][j];//THEN part MF values
                            //System.out.print(" " + mhWeights[idx]);
                            idx++;
                        }
                    }
                    for (int i = 0; i < nodeVectorE.length; i++) {
                        for (int j = 0; j < nodeVectorE[0].length; j++) {
                            mhWeights[idx] = nodeVectorE[i][j];//THEN part MF values
                            //System.out.print(" " + mhWeights[idx]);
                            idx++;
                        }
                    }
                }
                //System.out.println("\n" + idx + "\n");
                for (Object m_FunctChild : m_FunctChilds) {
                    FunctNode cur = (FunctNode) m_FunctChild;
                    //cur.setWeight(parameters[idx]/* *2 - 1*/);
                    mhWeights[idx] = cur.getWeight();
                    idx++;
                    nodeVectorA = cur.getA();
                    nodeVectorB = cur.getB();
                    nodeVectorC = cur.getC();
                    for (int i = 0; i < nodeVectorA.length; i++) {
                        for (int j = 0; j < nodeVectorA[0].length; j++) {
                            mhWeights[idx] = nodeVectorA[i][j];//IF part MF values
                            idx++;
                        }
                    }
                    for (int i = 0; i < nodeVectorB.length; i++) {
                        for (int j = 0; j < nodeVectorB[0].length; j++) {
                            mhWeights[idx] = nodeVectorB[i][j];//THEN part MF values
                            idx++;
                        }//for j
                    }//for i
                    for (int i = 0; i < nodeVectorC.length; i++) {
                        for (int j = 0; j < nodeVectorC[0].length; j++) {
                            mhWeights[idx] = nodeVectorC[i][j];//THEN part MF values
                            idx++;
                        }//for j
                    }//for i
                    if (cur.getNodeType().equalsIgnoreCase("Type-II")) {
                        nodeVectorD = cur.getD();
                        nodeVectorE = cur.getE();
                        for (int i = 0; i < nodeVectorD.length; i++) {
                            for (int j = 0; j < nodeVectorD[0].length; j++) {
                                mhWeights[idx] = nodeVectorD[i][j];//THEN part MF values
                                //System.out.print(" " + mhWeights[idx]);
                                idx++;
                            }
                        }//for
                        for (int i = 0; i < nodeVectorE.length; i++) {
                            for (int j = 0; j < nodeVectorE[0].length; j++) {
                                mhWeights[idx] = nodeVectorE[i][j];//THEN part MF values
                                //System.out.print(" " + mhWeights[idx]);
                                idx++;
                            }
                        }//for
                    }//if Type-II
                }//for cur
                for (Object m_LeafChild : m_LeafChilds) {
                    LeafNode cur = (LeafNode) m_LeafChild;
                    mhWeights[idx] = cur.getWeight();
                    idx++;
                }
                //System.out.println("\n" + idx + "\n");
            } else if (m_WeightsOnly.equalsIgnoreCase("Inputs_Weights")) {
                int idx = 0;
                for (Object m_FunctChild : m_FunctChilds) {
                    FunctNode cur = (FunctNode) m_FunctChild;
                    mhWeights[idx] = cur.getWeight();
                    idx++;
                }
                for (Object m_LeafChild : m_LeafChilds) {
                    LeafNode cur = (LeafNode) m_LeafChild;
                    mhWeights[idx] = cur.getWeight();
                    idx++;
                }
            } else {// Nodes only
                int idx = 0;
                nodeVectorA = m_Root.getA();
                nodeVectorB = m_Root.getB();
                nodeVectorC = m_Root.getC();
                for (int i = 0; i < nodeVectorA.length; i++) {
                    for (int j = 0; j < nodeVectorA[0].length; j++) {
                        mhWeights[idx] = nodeVectorA[i][j];//IF part MF values
                        //System.out.print(" " + mhWeights[idx]);
                        idx++;
                    }
                }
                for (int i = 0; i < nodeVectorB.length; i++) {
                    for (int j = 0; j < nodeVectorB[0].length; j++) {
                        mhWeights[idx] = nodeVectorB[i][j];//THEN part MF values
                        //System.out.print(" " + mhWeights[idx]);
                        idx++;
                    }
                }
                for (int i = 0; i < nodeVectorC.length; i++) {
                    for (int j = 0; j < nodeVectorC[0].length; j++) {
                        mhWeights[idx] = nodeVectorC[i][j];//THEN part MF values
                        //System.out.print(" " + mhWeights[idx]);
                        idx++;
                    }
                }
                if (m_Root.getNodeType().equalsIgnoreCase("Type-II")) {
                    nodeVectorD = m_Root.getD();
                    nodeVectorE = m_Root.getE();
                    for (int i = 0; i < nodeVectorD.length; i++) {
                        for (int j = 0; j < nodeVectorD[0].length; j++) {
                            mhWeights[idx] = nodeVectorD[i][j];//THEN part MF values
                            //System.out.print(" " + mhWeights[idx]);
                            idx++;
                        }
                    }//for
                    for (int i = 0; i < nodeVectorE.length; i++) {
                        for (int j = 0; j < nodeVectorE[0].length; j++) {
                            mhWeights[idx] = nodeVectorE[i][j];//THEN part MF values
                            //System.out.print(" " + mhWeights[idx]);
                            idx++;
                        }
                    }//for
                }//if Type-II
                for (Object m_FunctChild : m_FunctChilds) {
                    FunctNode cur = (FunctNode) m_FunctChild;
                    //cur.setWeight(parameters[idx]/* *2 - 1*/);
                    //mhWeights[idx] = cur.getWeight(); // (DO NOT TAKE WEIGHTS)
                    //idx++;
                    nodeVectorA = cur.getA();
                    nodeVectorB = cur.getB();
                    nodeVectorC = cur.getC();
                    for (int i = 0; i < nodeVectorA.length; i++) {
                        for (int j = 0; j < nodeVectorA[0].length; j++) {
                            mhWeights[idx] = nodeVectorA[i][j];//IF part MF values
                            //System.out.print(" " + mhWeights[idx]);
                            idx++;
                        }
                    }
                    for (int i = 0; i < nodeVectorB.length; i++) {
                        for (int j = 0; j < nodeVectorB[0].length; j++) {
                            mhWeights[idx] = nodeVectorB[i][j];//THEN part MF values
                            //System.out.print(" " + mhWeights[idx]);
                            idx++;
                        }//for j
                    }//for i
                    for (int i = 0; i < nodeVectorC.length; i++) {
                        for (int j = 0; j < nodeVectorC[0].length; j++) {
                            mhWeights[idx] = nodeVectorC[i][j];//THEN part MF values
                            //System.out.print(" " + mhWeights[idx]);
                            idx++;
                        }//for j
                    }//for i
                    if (cur.getNodeType().equalsIgnoreCase("Type-II")) {
                        nodeVectorD = cur.getD();
                        nodeVectorE = cur.getE();
                        for (int i = 0; i < nodeVectorD.length; i++) {
                            for (int j = 0; j < nodeVectorD[0].length; j++) {
                                mhWeights[idx] = nodeVectorD[i][j];//THEN part MF values
                                //System.out.print(" " + mhWeights[idx]);
                                idx++;
                            }
                        }//for
                        for (int i = 0; i < nodeVectorE.length; i++) {
                            for (int j = 0; j < nodeVectorE[0].length; j++) {
                                mhWeights[idx] = nodeVectorE[i][j];//THEN part MF values
                                //System.out.print(" " + mhWeights[idx]);
                                idx++;
                            }
                        }//for
                    }//if Type-II
                }//for cur
                //System.out.println("\n" + idx + "\n");
            }//if
        } catch (Exception e) {
            System.out.println("Error getting paramter " + this.getClass().getName() + " " + e);
            //System.exit(0);
        }
        return mhWeights;
    }//end getParameters	

    /**
     * Set the tree real values weights and activation function parameters
     *
     * @param parameters total number of parameters
     * @param treeParameters tree parameters
     * @return return the tree parameters count
     */
    public double[][] getParametersRange(int parameters, ArrayList treeParameters) {
        double range[][] = new double[parameters][2];
        double[] range_a = (double[]) treeParameters.get(4);//double[] min max a
        double[] range_b = (double[]) treeParameters.get(5);//double[] min max a
        double[] range_c = (double[]) treeParameters.get(6);//double[] min max c
        double[] range_weight = (double[]) treeParameters.get(7);//double[] min max weight
        double[] range_d = (double[]) treeParameters.get(10);//double[] deviation param
        double[] range_e = (double[]) treeParameters.get(11);//double[] deviation consequent
        try {
            double[][] nodeVectorA;
            double[][] nodeVectorB;
            double[][] nodeVectorC;
            double[][] nodeVectorD = new double[][]{{}};
            double[][] nodeVectorE = new double[][]{{}};
            if (m_WeightsOnly.equalsIgnoreCase("Rules_Parmeters_And_Input_Weights")) {
                int idx = 0;
                nodeVectorA = m_Root.getA();
                nodeVectorB = m_Root.getB();
                nodeVectorC = m_Root.getC();
                for (int i = 0; i < nodeVectorA.length; i++) {
                    for (int j = 0; j < nodeVectorA[0].length; j++) {
                        range[idx][0] = range_a[0];
                        range[idx][1] = range_a[1];
                        //System.out.print(" " + mhWeights[idx]);
                        idx++;
                    }
                }
                for (int i = 0; i < nodeVectorB.length; i++) {
                    for (int j = 0; j < nodeVectorB[0].length; j++) {
                        range[idx][0] = range_b[0];
                        range[idx][1] = range_b[1];
                        //System.out.print(" " + mhWeights[idx]);
                        idx++;
                    }
                }
                for (int i = 0; i < nodeVectorC.length; i++) {
                    for (int j = 0; j < nodeVectorC[0].length; j++) {
                        range[idx][0] = range_c[0];
                        range[idx][1] = range_c[1];
                        //System.out.print(" " + mhWeights[idx]);
                        idx++;
                    }
                }
                if (m_Root.getNodeType().equalsIgnoreCase("Type-II")) {
                    nodeVectorD = m_Root.getD();
                    nodeVectorE = m_Root.getE();
                    for (int i = 0; i < nodeVectorD.length; i++) {
                        for (int j = 0; j < nodeVectorD[0].length; j++) {
                            range[idx][0] = range_d[0];
                            range[idx][1] = range_d[1];
                            //System.out.print(" " + mhWeights[idx]);
                            idx++;
                        }
                    }
                    for (int i = 0; i < nodeVectorE.length; i++) {
                        for (int j = 0; j < nodeVectorE[0].length; j++) {
                            range[idx][0] = range_e[0];
                            range[idx][1] = range_e[1];
                            //System.out.print(" " + mhWeights[idx]);
                            idx++;
                        }
                    }
                }//if Type-II
                //System.out.println("\n" + idx + "\n");
                for (Object m_FunctChild : m_FunctChilds) {
                    FunctNode cur = (FunctNode) m_FunctChild;
                    //cur.setWeight(parameters[idx]/* *2 - 1*/);
                    range[idx][0] = range_weight[0];
                    range[idx][1] = range_weight[1];
                    idx++;
                    nodeVectorA = cur.getA();
                    nodeVectorB = cur.getB();
                    nodeVectorC = cur.getC();
                    for (int i = 0; i < nodeVectorA.length; i++) {
                        for (int j = 0; j < nodeVectorA[0].length; j++) {
                            range[idx][0] = range_a[0];
                            range[idx][1] = range_a[1];
                            idx++;
                        }
                    }
                    for (int i = 0; i < nodeVectorB.length; i++) {
                        for (int j = 0; j < nodeVectorB[0].length; j++) {
                            range[idx][0] = range_b[0];
                            range[idx][1] = range_b[1];
                            idx++;
                        }//for j
                    }//for i
                    for (int i = 0; i < nodeVectorC.length; i++) {
                        for (int j = 0; j < nodeVectorC[0].length; j++) {
                            range[idx][0] = range_c[0];
                            range[idx][1] = range_c[1];
                            idx++;
                        }//for j
                    }//for i
                    if (cur.getNodeType().equalsIgnoreCase("Type-II")) {
                        nodeVectorD = cur.getD();
                        nodeVectorE = cur.getE();
                        for (int i = 0; i < nodeVectorD.length; i++) {
                            for (int j = 0; j < nodeVectorD[0].length; j++) {
                                range[idx][0] = range_d[0];
                                range[idx][1] = range_d[1];
                                //System.out.print(" " + mhWeights[idx]);
                                idx++;
                            }
                        }
                        for (int i = 0; i < nodeVectorE.length; i++) {
                            for (int j = 0; j < nodeVectorE[0].length; j++) {
                                range[idx][0] = range_e[0];
                                range[idx][1] = range_e[1];
                                //System.out.print(" " + mhWeights[idx]);
                                idx++;
                            }
                        }
                    }//if Type-II
                }//for cur
                for (Object m_LeafChild : m_LeafChilds) {
                    LeafNode cur = (LeafNode) m_LeafChild;
                    range[idx][0] = range_weight[0];
                    range[idx][1] = range_weight[1];
                    idx++;
                }
                //System.out.println("\n" + idx + "\n");
            } else if (m_WeightsOnly.equalsIgnoreCase("Inputs_Weights")) {
                int idx = 0;
                for (Object m_FunctChild : m_FunctChilds) {
                    FunctNode cur = (FunctNode) m_FunctChild;
                    range[idx][0] = range_weight[0];
                    range[idx][1] = range_weight[1];
                    idx++;
                }
                for (Object m_LeafChild : m_LeafChilds) {
                    LeafNode cur = (LeafNode) m_LeafChild;
                    range[idx][0] = range_weight[0];
                    range[idx][1] = range_weight[1];
                    idx++;
                }
            } else {// Nodes only
                int idx = 0;
                nodeVectorA = m_Root.getA();
                nodeVectorB = m_Root.getB();
                nodeVectorC = m_Root.getC();
//                System.out.println("Root Node");
//                System.out.println("A " + nodeVectorA.length + "  x " + nodeVectorA[0].length);
//                System.out.println("B " + nodeVectorB.length + "  x " + nodeVectorB[0].length);
//                System.out.println("C " + nodeVectorC.length + "  x " + nodeVectorC[0].length);

                for (int i = 0; i < nodeVectorA.length; i++) {
                    for (int j = 0; j < nodeVectorA[0].length; j++) {
                        range[idx][0] = range_a[0];
                        range[idx][1] = range_a[1];
                        //System.out.print(" " + mhWeights[idx]);
                        idx++;
                    }
                }
                for (int i = 0; i < nodeVectorB.length; i++) {
                    for (int j = 0; j < nodeVectorB[0].length; j++) {
                        range[idx][0] = range_b[0];
                        range[idx][1] = range_b[1];
                        //System.out.print(" " + mhWeights[idx]);
                        idx++;
                    }
                }
                for (int i = 0; i < nodeVectorC.length; i++) {
                    for (int j = 0; j < nodeVectorC[0].length; j++) {
                        range[idx][0] = range_c[0];
                        range[idx][1] = range_c[1];
                        //System.out.print(" " + mhWeights[idx]);
                        idx++;
                    }
                }
                if (m_Root.getNodeType().equalsIgnoreCase("Type-II")) {
                    nodeVectorD = m_Root.getD();
                    nodeVectorE = m_Root.getE();
                    for (int i = 0; i < nodeVectorD.length; i++) {
                        for (int j = 0; j < nodeVectorD[0].length; j++) {
                            range[idx][0] = range_d[0];
                            range[idx][1] = range_d[1];
                            //System.out.print(" " + mhWeights[idx]);
                            idx++;
                        }
                    }
                    for (int i = 0; i < nodeVectorE.length; i++) {
                        for (int j = 0; j < nodeVectorE[0].length; j++) {
                            range[idx][0] = range_e[0];
                            range[idx][1] = range_e[1];
                            //System.out.print(" " + mhWeights[idx]);
                            idx++;
                        }
                    }
                }//if Type-II
                //System.out.println("\n" + idx + "\n");
                for (Object m_FunctChild : m_FunctChilds) {
                    FunctNode cur = (FunctNode) m_FunctChild;
                    //cur.setWeight(parameters[idx]/* *2 - 1*/);
                    //mhWeights[idx] = cur.getWeight(); // (DO NOT TAKE WEIGHTS)
                    //idx++;
                    nodeVectorA = cur.getA();
                    nodeVectorB = cur.getB();
                    nodeVectorC = cur.getC();
//                    System.out.println("Fun Node");
//                    System.out.println("A " + nodeVectorA.length + "  x " + nodeVectorA[0].length);
//                    System.out.println("B " + nodeVectorB.length + "  x " + nodeVectorB[0].length);
//                    System.out.println("C " + nodeVectorC.length + "  x " + nodeVectorC[0].length);
                    for (int i = 0; i < nodeVectorA.length; i++) {
                        for (int j = 0; j < nodeVectorA[0].length; j++) {
                            range[idx][0] = range_a[0];
                            range[idx][1] = range_a[1];
                            idx++;
                        }
                    }
                    for (int i = 0; i < nodeVectorB.length; i++) {
                        for (int j = 0; j < nodeVectorB[0].length; j++) {
                            range[idx][0] = range_b[0];
                            range[idx][1] = range_b[1];
                            idx++;
                        }//for j
                    }//for i
                    for (int i = 0; i < nodeVectorC.length; i++) {
                        for (int j = 0; j < nodeVectorC[0].length; j++) {
                            range[idx][0] = range_c[0];
                            range[idx][1] = range_c[1];
                            idx++;
                        }//for j
                    }//for i
                    if (cur.getNodeType().equalsIgnoreCase("Type-II")) {
                        nodeVectorD = cur.getD();
                        nodeVectorE = cur.getE();
                        for (int i = 0; i < nodeVectorD.length; i++) {
                            for (int j = 0; j < nodeVectorD[0].length; j++) {
                                range[idx][0] = range_d[0];
                                range[idx][1] = range_d[1];
                                //System.out.print(" " + mhWeights[idx]);
                                idx++;
                            }
                        }
                        for (int i = 0; i < nodeVectorE.length; i++) {
                            for (int j = 0; j < nodeVectorE[0].length; j++) {
                                range[idx][0] = range_e[0];
                                range[idx][1] = range_e[1];
                                //System.out.print(" " + mhWeights[idx]);
                                idx++;
                            }
                        }
                    }//if Type-II
                }//for cur
                //System.out.println("\n" + idx + "\n");
            }//if
        } catch (Exception e) {
            System.out.println("Error getting paramter range " + e);
        }
        return range;
    }//end getParameters	

    /**
     * Setting the optimized parameters to the tree
     *
     * @param parameters the optimized parameter using meta-heuristic
     */
    public void setParameters(double[] parameters) {
        try {
            //System.out.print("Set Parameter"+m_FunctChilds.size());
            if (m_WeightsOnly.equalsIgnoreCase("Rules_Parmeters_And_Input_Weights")) {
                int idx = 0;
                int parts = m_Root.getInputPart();
                int arity = m_Root.getArity();
                int no_rules = (int) Math.pow(parts, arity);

                double[][] nodeVectorA = new double[arity][parts];//
                for (int i = 0; i < nodeVectorA.length; i++) {
                    for (int j = 0; j < nodeVectorA[0].length; j++) {
                        nodeVectorA[i][j] = parameters[idx];
                        idx++;
                    }
                }
                m_Root.setA(nodeVectorA);//Setting A of Root
                double[][] nodeVectorB = new double[arity][parts];//
                for (int i = 0; i < nodeVectorB.length; i++) {
                    for (int j = 0; j < nodeVectorB[0].length; j++) {
                        nodeVectorB[i][j] = parameters[idx];
                        idx++;
                    }
                }
                m_Root.setB(nodeVectorB);//Setting B of Root
                double[][] nodeVectorC = new double[no_rules][arity + 1];//
                for (int i = 0; i < nodeVectorC.length; i++) {
                    for (int j = 0; j < nodeVectorC[0].length; j++) {
                        nodeVectorC[i][j] = parameters[idx];
                        idx++;
                    }
                }
                m_Root.setC(nodeVectorC);//Setting C of Root
                if (m_Root.getNodeType().equalsIgnoreCase("Type-II")) {
                    double[][] nodeVectorD = new double[arity][parts];//
                    for (int i = 0; i < nodeVectorD.length; i++) {
                        for (int j = 0; j < nodeVectorD[0].length; j++) {
                            nodeVectorD[i][j] = parameters[idx];
                            idx++;
                        }
                    }
                    m_Root.setD(nodeVectorD);//Setting D of Root
                    double[][] nodeVectorE = new double[no_rules][arity + 1];//
                    for (int i = 0; i < nodeVectorE.length; i++) {
                        for (int j = 0; j < nodeVectorE[0].length; j++) {
                            nodeVectorE[i][j] = parameters[idx];
                            idx++;
                        }
                    }
                    m_Root.setE(nodeVectorE);//Setting E of Root
                }
                //set the function nodes  
                for (Object m_FunctChild : m_FunctChilds) {
                    FunctNode cur = (FunctNode) m_FunctChild;
                    cur.setWeight(parameters[idx]);//it is ok to increatse parameter here beacuse each fun node has one weight (be it 1.0 or else)
                    idx++;
                    parts = cur.getInputPart();
                    arity = cur.getArity();
                    no_rules = (int) Math.pow(parts, arity);

                    nodeVectorA = new double[arity][parts];//
                    for (int i = 0; i < nodeVectorA.length; i++) {
                        for (int j = 0; j < nodeVectorA[0].length; j++) {
                            nodeVectorA[i][j] = parameters[idx];
                            idx++;
                        }
                    }
                    cur.setA(nodeVectorA);//Setting A of cur function
                    nodeVectorB = new double[arity][parts];//
                    for (int i = 0; i < nodeVectorB.length; i++) {
                        for (int j = 0; j < nodeVectorB[0].length; j++) {
                            nodeVectorB[i][j] = parameters[idx];
                            idx++;
                        }
                    }
                    cur.setB(nodeVectorB);//Setting B of cur function
                    nodeVectorC = new double[no_rules][arity + 1];//
                    for (int i = 0; i < nodeVectorC.length; i++) {
                        for (int j = 0; j < nodeVectorC[0].length; j++) {
                            nodeVectorC[i][j] = parameters[idx];
                            idx++;
                        }
                    }
                    cur.setC(nodeVectorC);//Setting C of cur function
                    if (cur.getNodeType().equalsIgnoreCase("Type-II")) {
                        double[][] nodeVectorD = new double[arity][parts];//
                        for (int i = 0; i < nodeVectorD.length; i++) {
                            for (int j = 0; j < nodeVectorD[0].length; j++) {
                                nodeVectorD[i][j] = parameters[idx];
                                idx++;
                            }
                        }
                        cur.setD(nodeVectorD);//Setting D of cur function
                        double[][] nodeVectorE = new double[no_rules][arity + 1];//
                        for (int i = 0; i < nodeVectorE.length; i++) {
                            for (int j = 0; j < nodeVectorE[0].length; j++) {
                                nodeVectorE[i][j] = parameters[idx];
                                idx++;
                            }
                        }
                        cur.setE(nodeVectorE);//Setting E of cur function
                    }//Type-II
                }//for all function
                for (Object m_LeafChild : m_LeafChilds) {
                    LeafNode cur = (LeafNode) m_LeafChild;
                    cur.setWeight(parameters[idx]);
                    idx++;
                }
            } else if (m_WeightsOnly.equalsIgnoreCase("Inputs_Weights")) {
                int idx = 0;
                for (Object m_FunctChild : m_FunctChilds) {
                    FunctNode cur = (FunctNode) m_FunctChild;
                    cur.setWeight(parameters[idx]);
                    idx++;
                }
                for (Object m_LeafChild : m_LeafChilds) {
                    LeafNode cur = (LeafNode) m_LeafChild;
                    cur.setWeight(parameters[idx]);
                    idx++;
                }
            } else {//if parameters of the rules only
                int idx = 0;
                int parts = m_Root.getInputPart();
                int arity = m_Root.getArity();
                int no_rules = (int) Math.pow(parts, arity);

                double[][] nodeVectorA = new double[arity][parts];//
                for (int i = 0; i < nodeVectorA.length; i++) {
                    for (int j = 0; j < nodeVectorA[0].length; j++) {
                        nodeVectorA[i][j] = parameters[idx];
                        idx++;
                    }
                }
                m_Root.setA(nodeVectorA);//Setting A of Root
                double[][] nodeVectorB = new double[arity][parts];//
                for (int i = 0; i < nodeVectorB.length; i++) {
                    for (int j = 0; j < nodeVectorB[0].length; j++) {
                        nodeVectorB[i][j] = parameters[idx];
                        idx++;
                    }
                }
                m_Root.setB(nodeVectorB);//Setting B of Root
                double[][] nodeVectorC = new double[no_rules][arity + 1];//
                for (int i = 0; i < nodeVectorC.length; i++) {
                    for (int j = 0; j < nodeVectorC[0].length; j++) {
                        nodeVectorC[i][j] = parameters[idx];
                        idx++;
                    }
                }
                m_Root.setC(nodeVectorC);//Setting C of Root
                if (m_Root.getNodeType().equalsIgnoreCase("Type-II")) {
                    double[][] nodeVectorD = new double[arity][parts];//
                    for (int i = 0; i < nodeVectorD.length; i++) {
                        for (int j = 0; j < nodeVectorD[0].length; j++) {
                            nodeVectorD[i][j] = parameters[idx];
                            idx++;
                        }
                    }
                    m_Root.setD(nodeVectorD);//Setting D of Root
                    double[][] nodeVectorE = new double[no_rules][arity + 1];//
                    for (int i = 0; i < nodeVectorE.length; i++) {
                        for (int j = 0; j < nodeVectorE[0].length; j++) {
                            nodeVectorE[i][j] = parameters[idx];
                            idx++;
                        }
                    }
                    m_Root.setE(nodeVectorE);//Setting E of Root
                }
                for (Object m_FunctChild : m_FunctChilds) {
                    FunctNode cur = (FunctNode) m_FunctChild;// No weight paratemter
                    parts = cur.getInputPart();
                    arity = cur.getArity();
                    no_rules = (int) Math.pow(parts, arity);

                    nodeVectorA = new double[arity][parts];//
                    for (int i = 0; i < nodeVectorA.length; i++) {
                        for (int j = 0; j < nodeVectorA[0].length; j++) {
                            nodeVectorA[i][j] = parameters[idx];
                            idx++;
                        }
                    }
                    cur.setA(nodeVectorA);//Setting A of cur function
                    nodeVectorB = new double[arity][parts];//
                    for (int i = 0; i < nodeVectorB.length; i++) {
                        for (int j = 0; j < nodeVectorB[0].length; j++) {
                            nodeVectorB[i][j] = parameters[idx];
                            idx++;
                        }
                    }
                    cur.setB(nodeVectorB);//Setting B of cur function
                    nodeVectorC = new double[no_rules][arity + 1];//
                    for (int i = 0; i < nodeVectorC.length; i++) {
                        for (int j = 0; j < nodeVectorC[0].length; j++) {
                            nodeVectorC[i][j] = parameters[idx];
                            idx++;
                        }
                    }
                    cur.setC(nodeVectorC);//Setting C of cur function
                    if (cur.getNodeType().equalsIgnoreCase("Type-II")) {
                        double[][] nodeVectorD = new double[arity][parts];//
                        for (int i = 0; i < nodeVectorD.length; i++) {
                            for (int j = 0; j < nodeVectorD[0].length; j++) {
                                nodeVectorD[i][j] = parameters[idx];
                                idx++;
                            }
                        }
                        cur.setD(nodeVectorD);//Setting D of cur function
                        double[][] nodeVectorE = new double[no_rules][arity + 1];//
                        for (int i = 0; i < nodeVectorE.length; i++) {
                            for (int j = 0; j < nodeVectorE[0].length; j++) {
                                nodeVectorE[i][j] = parameters[idx];
                                idx++;
                            }
                        }
                        cur.setE(nodeVectorE);//Setting E of cur function
                    }//Type-II
                }//for all function
            }
        } catch (Exception e) {
            System.out.println("Error Setting paramter at File " + this.getClass().getName() + " " + e);
            System.exit(0);
        }
    }//end set paramters       
}//end class FuzzyFNT
