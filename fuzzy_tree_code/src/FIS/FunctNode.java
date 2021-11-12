package FIS;

import Randoms.*;
import java.io.*;
import java.util.*;

class FunctNode extends Node {

    Vector m_Children;
    private String m_FSType;
    double[][] m_A;
    double[][] m_B;
    double[][] m_C;
    double[][] m_D;
    double[][] m_E;
    int m_input_part;
    int m_Arity;
    int m_actFun;
    MersenneTwisterFast m_RNG;

    public FunctNode() {
    }

    public FunctNode(String FSType, double weight, int input_part, double[][] a, double[][] b, double[][] c, double[][] d, double[][] e, int actFun, int arity, FunctNode parentNode, MersenneTwisterFast random) {
        m_FSType = FSType;//Type of node
        m_Weight = weight;//@Override
        m_input_part = input_part;
        m_A = a;
        m_B = b;
        m_C = c;
        m_D = d;
        m_E = e;
        m_actFun = actFun;
        m_Arity = arity;
        m_Children = new Vector();
        m_ParentNode = parentNode;//@Override
        m_RNG = random;
    }//end constructor

    @Override
    public double getOutput(double[] inputs) {
        double netn = 0;
        double[] x = new double[m_Arity];
        int idx = 0;
        for (Object m_Children1 : m_Children) {
            Node n = (Node) m_Children1;
            //netn = n.getWeight() * n.getOutput(inputs); //if we want to take weight input then this is ok
            x[idx++] = n.getOutput(inputs);
        }
        double y = 10e+10; //initialize with a big no.
        if (m_FSType.equalsIgnoreCase("Type-II")) {
            y = FIST2.computeFIS(m_input_part, m_Arity, x, m_A, m_B, m_C, m_D, m_E);//Type 2 FIS output
        } else {
            y = FIST1.computeFIS(m_input_part, m_Arity, x, m_A, m_B, m_C);//Type 1 FIS output
        }
        return y;
    }

    //public double getOutput(double[] inputs, int depth){	}
    public void generateChildren(int nInputs, int depth, int maxHeight, ArrayList treeParameters) {
        int maxDepth = maxHeight;// int depth of tree
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

        if (depth < maxDepth) {
            for (int i = 0; i < m_Arity; i++) {
                double weight = m_RNG.random(range_weight[0], range_weight[1]);
                if (weightsOnly.equalsIgnoreCase("Rules_Parmeters")) {
                    weight = 1.0; //keep weights eqaul to one
                }

                int in_part = 2;
                if (inPart[0] == 0) {//Max
                    in_part = 2 + m_RNG.random(inPart[1]);
                } else {
                    in_part = inPart[1];//Total
                }

                int actFun = m_funType;
                if (0 == actFun) {//0 indicates random selection
                    actFun = 1 + m_RNG.random(7);//random selection currently only 7 function are implemented
                }

                int minArity = 2;
                int range = nInputs + maxArity - minArity;
                int num = m_RNG.random(range);
                if (num < nInputs) { //leaf node
                    LeafNode n = new LeafNode(weight, num, this);
                    m_Children.add(n);
                    //System.out.print(" D"+depth+"LN:"+num);
                } else {// func node
                    int arity = num - nInputs + minArity;

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
                    FunctNode n = new FunctNode(FSType, weight, in_part, a, b, c, d, e, actFun, arity, this, m_RNG);
                    n.generateChildren(nInputs, depth + 1, maxDepth, treeParameters);
                    m_Children.add(n);
                } //if
            }// for arity
        }//
        else { // if max depth only leafs
            for (int i = 0; i < m_Arity; i++) {
                double weight = m_RNG.random(range_weight[0], range_weight[1]);
                int range = nInputs - 1;
                int num = m_RNG.random(range);
                LeafNode n = new LeafNode(weight, num, this);
                m_Children.add(n);
                //System.out.print(" D"+depth+"LN:"+i+"A"+num);				
            }
        }
    }//end generate Child

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

    public void readChildren(BufferedReader brProb) {
        try {

            String dataline;
            double weight = 0.0;
            int parts = 2;
            double[][] a = new double[][]{{}};
            double[][] b = new double[][]{{}};
            double[][] devp = new double[][]{{}};
            double[][] c = new double[][]{{}};
            double[][] devc = new double[][]{{}};
            int actFun = 0;
            int arity = 0;
            int num = 0;
            String FSType =  "Type-I";
            
            for (int i = 0; i < m_Arity; i++) {
                if ((dataline = brProb.readLine()) != null) {
                    if (!dataline.contains("f")) {//read a child as leaf node
                        double[] d = new double[2];
                        String[] tokens = dataline.split(",");
                        for (int t = 0; t < tokens.length; t++) {
                            d[t] = Double.parseDouble(tokens[t]);
                            //System.out.println(tokens[t] + "==" + d[t]);
                        }
                        num = (int) d[0];
                        weight = d[1];
                        LeafNode n = new LeafNode(weight, num, this);
                        m_Children.add(n);
                    } else { //read the function Node
                        double[] d = new double[4];
                        int j = 0;
                        String[] tokens = dataline.split(",");
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
                        FunctNode n = new FunctNode(FSType, weight, parts, a, b, c, devp, devc, actFun, arity, this, m_RNG);
                        n.readChildren(brProb);
                        m_Children.add(n);
                    }
                }//if data is null do nothing
            }//end reading arity	
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error Read Child" + e);
        }
    }//end generate Child

    @Override
    FunctNode getParentNode() {
        //System.out.println("returnParrent:"+m_ParentNode);
        return m_ParentNode;
    }

    @Override
    public void inspect(FuzzyFNT tree, int depth) {
        try {
            for (Object m_Children1 : m_Children) {
                Node n = (Node) m_Children1;
                if (n.isLeaf()) {
                    n.inspect(tree, depth + 1);
                    tree.addLeafChild((LeafNode) n);
                } else {
                    n.inspect(tree, depth + 1);
                    tree.addFunctChild((FunctNode) n);
                }
            }
        } catch (Exception e) {
            System.out.print("Error Inspection FuncNode" + e);
        }
    }//end Function Inspect

    @Override
    public boolean isLeaf() {
        return false;
    }

    public String getNodeType() {
        return m_FSType;
    }

    public int getArity() {
        return m_Arity;
    }

    public int getInputPart() {
        return m_input_part;
    }

    public void setA(double[][] x) {

        m_A = x;
    }

    public void setB(double[][] x) {
        m_B = x;
    }

    public void setC(double[][] x) {
        m_C = x;
    }

    public void setD(double[][] x) {
        m_D = x;
    }

    public void setE(double[][] x) {
        m_E = x;
    }

    public double[][] getA() {
        //System.out.println("A " + m_A.length + " " + m_A[0].length);
        return m_A;
    }

    public double[][] getB() {
        //System.out.println("B " + m_B.length + " " + m_B[0].length);
        return m_B;
    }

    public double[][] getC() {
        //System.out.println("C " + m_C.length + " " + m_C[0].length);
        return m_C;
    }

    public double[][] getD() {
        //System.out.println("C " + m_C.length + " " + m_C[0].length);
        return m_D;
    }

    public double[][] getE() {
        //System.out.println("C " + m_C.length + " " + m_C[0].length);
        return m_E;
    }

    public void replace(FunctNode oldNode, FunctNode newNode) {// delete oldNode?
        try {
            //System.out.print(m_Children.size()+": "+m_Children.contains(oldNode)+": "+m_Children.indexOf(oldNode));
            if (m_Children.contains(oldNode)) {
                m_Children.removeElement(oldNode); //remove old
                m_Children.add(newNode);
                //System.out.println(" "+m_Children.size()+": "+m_Children.contains(oldNode));
            } else {
                return;
            }
        } catch (Exception e) {
            System.out.print("Error Replaccing:" + e);
        }
    }

    public void removeAndReplace(FunctNode node, LeafNode newLeafNode) {
        try {
            //System.out.print(m_Children.size()+": "+m_Children.contains(node)+": "+m_Children.indexOf(node));
            if (m_Children.contains(node)) {
                m_Children.removeElement(node); //remove old
                m_Children.add(newLeafNode);
                //System.out.println(" "+m_Children.size()+": "+m_Children.contains(node));
            } else {
                return;
            }
        } catch (Exception e) {
            System.out.print("Error RemoveAndReplace-" + e);
        }
    }

    public void removeAndGrow(LeafNode toRemove, int maxDepth, int nInputs, ArrayList treeParameters) {
        try {
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
            //System.out.print(m_Children.size()+": "+m_Children.contains(toRemove)+": "+m_Children.indexOf(toRemove));
            if (m_Children.contains(toRemove)) {
                m_Children.removeElement(toRemove);

                int in_part = 2;
                if (inPart[0] == 0) {//Max
                    in_part = 2 + m_RNG.random(inPart[1]);
                } else {//Total
                    in_part = inPart[1];
                }

                int actFun = m_funType;
                if (0 == actFun) {
                    actFun = 1 + m_RNG.random(7);//random selection currently only four function are implemented
                }
                int minArity = 2;
                int range = maxArity - minArity;
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

                FunctNode newNode = new FunctNode(FSType, weight, in_part, a, b, c, d, e, actFun, arity, this, m_RNG);
                newNode.generateChildren(nInputs, 0, maxDepth, treeParameters);
                m_Children.add(newNode);
            } else {
                return;
            }
        } catch (Exception e) {
            System.out.print("Error RemoveAndGraow-" + e);
        }
    }

    @Override
    public void print(int depth) {
        for (int i = 0; i < depth; i++) {
            System.out.print("-");
        }
        System.out.println(" +" + m_Arity);

        for (Object m_Children1 : m_Children) {
            Node node = (Node) m_Children1;
            //System.out.print(" "+node.m_Weight);
            node.print(depth + 1);
        }
    }//end print

    @Override
    public void printFile(int depth, PrintWriter file) {
        int type = 1;
        if(m_FSType.equalsIgnoreCase("Type-II")){
            type = 2;
        }
        file.print("f" + "," + m_actFun + "," + m_Arity + "," + m_Weight + "," + m_input_part+ "," + type);
        for (int ii = 0; ii < m_A.length; ii++) {
            for (int jj = 0; jj < m_A[0].length; jj++) {
                file.print("," + m_A[ii][jj]);
            }
        }
        for (int ii = 0; ii < m_B.length; ii++) {
            for (int jj = 0; jj < m_B[0].length; jj++) {
                file.print("," + m_B[ii][jj]);
            }
        }
        for (int ii = 0; ii < m_C.length; ii++) {
            for (int jj = 0; jj < m_C[0].length; jj++) {
                file.print("," + m_C[ii][jj]);
            }
        }
        if (m_FSType.equalsIgnoreCase("Type-II")) {
            for (int ii = 0; ii < m_D.length; ii++) {
                for (int jj = 0; jj < m_D[0].length; jj++) {
                    file.print("," + m_D[ii][jj]);
                }
            }
            for (int ii = 0; ii < m_E.length; ii++) {
                for (int jj = 0; jj < m_E[0].length; jj++) {
                    file.print("," + m_E[ii][jj]);
                }
            }
        }//if Type-II
        file.println();
        for (Object m_Children1 : m_Children) {
            Node node = (Node) m_Children1;
            //System.out.print(" "+node.m_Weight);
            node.printFile(depth + 1, file);
        }
    }//end printFile

    // public void printFile(int depth, std::fstream* file);
    @Override
    Node copyNode(FunctNode parentNode) {
        FunctNode node = new FunctNode(m_FSType, m_Weight, m_input_part, m_A, m_B, m_C, m_D, m_E, m_actFun, m_Arity, parentNode, m_RNG);
        for (Object m_Children1 : m_Children) {
            node.m_Children.add(((Node) m_Children1).copyNode(node));
        }
        return node;
    }//end CopyNode
}//end function Node
