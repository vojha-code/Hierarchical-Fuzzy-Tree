
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphGUI;

import AdditionalFrames.InitiatorFrame;
import java.awt.Color;
import java.io.*;
import java.util.*;

/**
 *
 * @author Varun
 */
class TreeNode {

    int nodeIndex;
    int nodeValue;
    int nodeLevel;

}

class TreeNodeFun extends TreeNode {

    int nodeIndex;
    int nodeValue;
    int nodeLevel;
    static int indx = 0;

    public void deleteStatic() {
        indx = 0;
    }

    public TreeNodeFun(int nodeIdx, int nodeVal, int nodeLvl) {
        nodeIndex = nodeIdx;
        nodeValue = nodeVal;
        nodeLevel = nodeLvl;
    }

    public void readChildrenStructure(BufferedReader brProb, PrintWriter pr, int depth, int parent) {
        try {
            String dataline;

            int val;
            String actFun;
            for (int i = 0; i < nodeValue; i++) {
                if ((dataline = brProb.readLine()) != null) {
                    if (!dataline.contains("f")) {
                        //read a child as leaf node
                        indx++;
                        String[] tokens = dataline.split(",");
                        val = (int) Double.parseDouble(tokens[0]);
                        /*System.out.print(val);
                         System.out.print(" " + indx);
                         System.out.print(" " + depth);
                         System.out.println(" " + parent);*/

                        pr.print(depth + ",");
                        pr.print(val + ",");
                        pr.print(parent + ",");//parent
                        pr.print(indx + ",");//child
                        pr.print("L" + ",");//node type
                        pr.println("-1");

                        TreeNodeLeaf n = new TreeNodeLeaf(indx, val, depth);
                    } else {
                        //read the function Node
                        indx++;
                        String[] tokens = dataline.split(",");
                        actFun = tokens[1];
                        val = (int) Double.parseDouble(tokens[2]);
                        /*System.out.print(val);
                         System.out.print(" " + indx);
                         System.out.print(" " + depth);
                         System.out.println(" " + parent);*/

                        pr.print(depth + ",");
                        pr.print(val + ",");
                        pr.print(parent + ",");//parent
                        pr.print(indx + ",");//child
                        pr.print("F" + ",");//node type
                        pr.println(actFun);//function type

                        TreeNodeFun n = new TreeNodeFun(indx, val, depth);
                        n.readChildrenStructure(brProb, pr, depth + 1, indx);
                    }
                }//if data is null do nothing
            }//end reading arty	
        } catch (Exception e) {
            System.out.println("Error Read Child" + e);
        }
        //System.out.println("Max Depth:"+depth);
    }//end generate Child

}

class TreeNodeLeaf extends TreeNode {

    public TreeNodeLeaf(int nodeIdx, int nodeVal, int nodeLvl) {
        nodeIndex = nodeIdx;
        nodeValue = nodeVal;
        nodeLevel = nodeLvl;
    }
}

public class testGraphDraw {

    //Here is some example syntax for the GraphDraw class
    //public static void main(String[] args) {
    /**
     * @param filename the file name to be read
     * @param Oidx output index
     * @param Midx is ensemble candidate index
     * @param isOld is testing an OLd model
     */
    public void drawTree(String filename, int Oidx, int Midx, boolean isOld) {
        TreeNodeFun rootNode;
        try {
            FileReader fin = new FileReader(filename);
            BufferedReader brProb = new BufferedReader(fin);
            String treeFile = "";
            if(!isOld){
                treeFile = InitiatorFrame.absoluteFilePathOut + "New_FNT_Structure.txt";
            }else{
                treeFile = InitiatorFrame.absoluteFilePathOut + "Old_FNT_Structure.txt";
            }          
            FileWriter fw = new FileWriter(treeFile);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pr = new PrintWriter(bw);

            String rootData;
            int val = 0;
            String actFun = "";
            if ((rootData = brProb.readLine()) != null) {
                if (rootData.contains("f")) {
                    String[] tokens = rootData.split(",");
                    //System.out.print(tokens[1]);
                    actFun = tokens[1];
                    val = Integer.parseInt(tokens[2]);
                    /*System.out.print(" " + 0);
                     System.out.print(" " + 0);
                     System.out.println(" " + -1);*/
                    pr.print(0 + ",");//level
                    pr.print(val + ",");//value
                    pr.print(-1 + ",");//parent
                    pr.print(0 + ",");//index/child
                    pr.print("F" + ",");//node type
                    pr.println(actFun);//function type
                }
            }
            rootNode = new TreeNodeFun(0, val, 0);
            rootNode.readChildrenStructure(brProb, pr, 1, 0);
            brProb.close();
            pr.close();
            bw.close();
            fw.close();
            rootNode.deleteStatic();

            fin = new FileReader(treeFile);
            brProb = new BufferedReader(fin);

            Vector retriveDepth = new Vector();
            Vector Yval = new Vector();//the lavel value of the node
            Vector Dval = new Vector();//the node data value
            Vector Pval = new Vector();//the parent of the node
            Vector Ival = new Vector();//the index value of a node
            Vector Tval = new Vector();// Type of node
            Vector Aval = new Vector();//activation finction type

            int count = 0;
            while ((rootData = brProb.readLine()) != null) {
                String[] tokens = rootData.split(",");
                val = Integer.parseInt(tokens[0]);
                Yval.add(val);//retrieve lavel of the node
                Dval.add(Integer.parseInt(tokens[1]));//retrive the data value of the node
                Pval.add(Integer.parseInt(tokens[2]));//retrieve the parent index value 
                Ival.add(Integer.parseInt(tokens[3]));//retrive index of node itself
                Tval.add(tokens[4]);//retrive if function node or leafe node
                Aval.add(tokens[5]);//retrieve Transfer function type

                if (count == 0) {
                    retriveDepth.add(val);
                } else {
                    boolean match = false;
                    for (int i = 0; i < retriveDepth.size(); i++) {
                        if (val == Integer.parseInt(retriveDepth.get(i).toString())) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        retriveDepth.add(val);//add a disting lavel value
                    }
                }
                count++;
            }
            brProb.close();

            /*for (int i = 0; i < retriveDepth.size(); i++) {
             System.out.print(retriveDepth.get(i));
             }

             System.out.println("\ndsdgfs");
             for (int i = 0; i < Yval.size(); i++) {
             System.out.print(Yval.get(i));
             }*/
            int maxTreeDepth = retriveDepth.size();

            int nodeCountAtDepth[] = new int[maxTreeDepth];
            for (int i = 0; i < maxTreeDepth; i++) {
                nodeCountAtDepth[i] = 0;
            } //System.out.println("\ndsdgfs");
            Vector Xval = new Vector();//
            for (int i = 0; i < Yval.size(); i++) {
                int value = Integer.parseInt(Yval.get(i).toString());
                nodeCountAtDepth[value] = nodeCountAtDepth[value] + 1;
                Xval.add(nodeCountAtDepth[value]);//
                //System.out.println(Yval.get(i) + " " + Xval.get(i) + " " + Dval.get(i) + " " + Pval.get(i) + " " + Ival.get(i));
            }
            int maxWidth = 1;
            for (int i = 0; i < Xval.size(); i++) {
                int value = Integer.parseInt(Xval.get(i).toString());
                if (maxWidth < value) {
                    maxWidth = value;
                }
            }
            //System.out.println("\n" + maxWidth);
            //re adjust xVal
           /* int midPoint = 0;
            if(maxWidth%2 == 0){
                midPoint = maxWidth/2;
            }else{
                midPoint = (maxWidth+1)/2;
            }//mid point decided
            
            for (int depthSearch = 0; depthSearch < maxTreeDepth; depthSearch++) {
                int midPoindModifier = (int)nodeCountAtDepth[depthSearch]/2;
                midPoindModifier = -1*midPoindModifier;//make modifier negetive
                for (int i = 0; i < Yval.size(); i++) {
                    int Depthvalue = Integer.parseInt(Yval.get(i).toString());
                    if (Depthvalue == depthSearch) {
                        int Xupdate = midPoint +  midPoindModifier;
                        Xval.set(i, Xupdate);
                        System.out.print(Xupdate+" ");
                        midPoindModifier++;//increase modifier value
                    }
                }
                System.out.println();
            }//x value modified */
            

            GraphDraw frame = new GraphDraw();
            int maxFrameX = maxWidth * 100 + 100;
            int maxFrameY = maxTreeDepth * 100 + 100;
            frame.setSize(maxFrameX, maxFrameY);
            frame.setBackground(Color.WHITE);
            frame.setForeground(Color.WHITE);
            frame.setOpaque(true);
            frame.setMaxWinSize(maxFrameX, maxFrameY);

            for (int i = 0; i < Yval.size(); i++) {
                String Nval = Dval.get(i).toString();
                int NyVal = Integer.parseInt(Yval.get(i).toString());
                int NxVal = Integer.parseInt(Xval.get(i).toString());
                String NodeType = Tval.get(i).toString();
                int actFunNumber = Integer.parseInt((Aval.get(i)).toString());
                frame.addNode(Nval, (NxVal * 100 + 10), (NyVal * 100 + 100), NodeType, actFunNumber);
                //System.out.println(Yval.get(i));
            }
            //System.out.println("\n I got the print right" );

            for (int i = 1; i < Dval.size(); i++) {
                int NPVal = Integer.parseInt(Pval.get(i).toString());
                int NIVal = Integer.parseInt(Ival.get(i).toString());
                frame.addEdge(NPVal, NIVal);
                //System.out.println(NPVal+" "+NIVal);
            }
            //System.out.println("\n I got the print right" );
            //frame.setVisible(true);
            frame.callSaveImage(Oidx, Midx, isOld);

            //Disposing variables
            Yval = null;
            Xval = null;
            Dval = null;
            Pval = null;
            Ival = null;
            Tval = null;
            Aval = null;

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
