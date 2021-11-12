package FIS;

import java.io.*;

public class LeafNode extends Node {

    int m_InputNumber;

    public LeafNode() {
    }

    public LeafNode(double weight, int inputNumber, FunctNode parentNode) {
        m_InputNumber = inputNumber;

        m_Weight = weight;
        m_ParentNode = parentNode;
        //System.out.print("Leaf:");
    }//end constructor Leaf Node

    public double getOutput(double[] inputs, int depth) {
        for (int i = 0; i < depth; i++) {
            System.out.print("--");
        }
        System.out.println(" l:" + inputs[m_InputNumber] + " inputN:" + m_InputNumber);
        return inputs[m_InputNumber];
    }

    public void setInputNumber(int inputNumber) {
        m_InputNumber = inputNumber;
    }

    public int getInputNumber() {
        return m_InputNumber;
    }

    @Override
    public void inspect(FuzzyFNT tree, int depth) {
        if (depth > tree.getDepth()) {
            tree.setDepth(depth);
        }
    }//end LeanNode inspect

    @Override
    public double getOutput(double[] inputs) {
        //System.out.println("Leaf node"+m_InputNumber);
        return inputs[m_InputNumber];
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void print(int depth) {
        for (int i = 0; i < depth; i++) {
            System.out.print("-");
        }
        System.out.println(" :" + m_InputNumber);
    }

    @Override
    public void printFile(int depth, PrintWriter file) {
        //for(int i = 0; i < depth; i++)
        {
            //file.print("");
        }
        file.println(m_InputNumber + "," + m_Weight);
    }//end printfile

    @Override
    Node copyNode(FunctNode parentNode) {
        LeafNode node = new LeafNode(m_Weight, m_InputNumber, parentNode);
        return node;
    }

}//end Leaf Node
