package FIS;

import java.io.*;

public class Node {

    double m_Weight;
    FunctNode m_ParentNode;

    public Node() {
    }

    public Node(double weight, FunctNode parentNode) {
        m_Weight = weight;
        m_ParentNode = parentNode;
        //System.out.println("parent ->"+parentNode);
    }

    public double getWeight() {
        return m_Weight;
    }

    public void setWeight(double weight) {
        m_Weight = weight;
    }

    double getOutput(double[] inputs) {
        System.out.println("Should be override");
        return 0.0;
    }

    //public double getOutput(double[] inputs, int depth) { System.out.println("Node"); return 0.0; }

    void print(int depth) {
        System.out.println("Should be override");
    }

    public void printFile(int depth, PrintWriter file) {
        System.out.println("Should be override");
    }

    void inspect(FuzzyFNT tree, int depth) {
        System.out.println("Should be override");
    }

    boolean isLeaf() {
        System.out.println("Should be override");
        return false;
    }

    Node copyNode(FunctNode parentNode) {
        System.out.println("Node");
        return null;
    }

    FunctNode getParentNode() {
        return m_ParentNode;
    }

    public void setParentNode(FunctNode parentNode) {
        m_ParentNode = parentNode;
    }
}// End Node
