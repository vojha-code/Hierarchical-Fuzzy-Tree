/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MISC;

import AdditionalFrames.KeyFrame;
import AdditionalFrames.ConfisionMat;
import AdditionalFrames.ShowResult;
import AdditionalFrames.IndividualSelector;
import AdditionalFrames.InitiatorFrame;
import AdditionalFrames.AboutFrame;
import AdditionalFrames.DeveloperInfo;
import AdditionalFrames.VersionInfo;
import AdditionalFrames.TreeDescription;
import AdditionalFrames.ReferenceFrame;
import FIS.FuzzyFNT;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.Vector;
import javax.swing.JOptionPane;

/**
 *
 * @author ojh0009
 */
public class FramesCall {

    public static void helpCall() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(InitiatorFrame.absoluteFilePathInp + "helpFile.pdf"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Operation failed! Cannot open file!");
                System.out.print(e);
            }
        }
    }//help  

    public static void statisticsCall() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(InitiatorFrame.absoluteFilePathInp + "statistics.pdf"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Operation failed! Cannot open file!");
                System.out.print(e);
            }
        }
    }//help  

    public static void ensembleTrainCall() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(InitiatorFrame.absoluteFilePathOut + "ensembleTrain.csv"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Operation failed! Cannot open file!");
                System.out.print(e);
            }
        }
    }//helpEnsemble

    public static void ensembleTestCall() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(InitiatorFrame.absoluteFilePathOut + "ensembleTest.csv"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Operation failed! Cannot open file!");
                System.out.print(e);
            }
        }
    }//helpEnsemble

    public static void filterData() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(InitiatorFrame.absoluteFilePathOut + "filteredData.csv"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Operation failed! Cannot open file!");
                System.out.print(e);
            }
        }
    }//filterdata

    public static void normalizedData() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(InitiatorFrame.absoluteFilePathOut + "normalizedData.csv"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Operation failed! Cannot open file!");
                System.out.print(e);
            }
        }
    }//normalizedData

    public static void treeDescription() {
        TreeDescription treeframe = new TreeDescription();
        treeframe.setVisible(true);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - treeframe.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - treeframe.getHeight()) / 2);
        treeframe.setLocation(x, y);
    }//key

    public static void keyShoutcut() {
        KeyFrame keyframe = new KeyFrame();
        keyframe.setVisible(true);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - keyframe.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - keyframe.getHeight()) / 2);
        keyframe.setLocation(x, y);
    }//key

    public static void aboutTool() {
        AboutFrame aboutframe = new AboutFrame();
        aboutframe.setVisible(true);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - aboutframe.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - aboutframe.getHeight()) / 2);
        aboutframe.setLocation(x, y);
    }//about

    public static void developers() {
        DeveloperInfo devInfoframe = new DeveloperInfo();
        devInfoframe.setVisible(true);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - devInfoframe.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - devInfoframe.getHeight()) / 2);
        devInfoframe.setLocation(x, y);
    }//developers   

    public static void versions() {
        VersionInfo versionInfoframe = new VersionInfo();
        versionInfoframe.setVisible(true);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - versionInfoframe.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - versionInfoframe.getHeight()) / 2);
        versionInfoframe.setLocation(x, y);
    }//version

    public static void referance() {
        ReferenceFrame refFrame = new ReferenceFrame();
        refFrame.setVisible(true);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - refFrame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - refFrame.getHeight()) / 2);
        refFrame.setLocation(x, y);
    }//referance

    public static void initiator() {
        InitiatorFrame initFrame = new InitiatorFrame();
        initFrame.setVisible(true);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - initFrame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - initFrame.getHeight()) / 2);
        initFrame.setLocation(x, y);
    }//initiator    

    public static void showResult(String fntTrainParam, String executionReport, String modelStatistic, Vector holdFeatures, String[] nameAtr, String dataInfo, boolean isClassification) {
        ShowResult resframe = new ShowResult();
        resframe.setVisible(true);
        resframe.setPerson();
        resframe.setProblem(dataInfo);
        resframe.setAlgoParam(fntTrainParam);
        resframe.setResStat(modelStatistic);
        resframe.setReport(executionReport);
        resframe.setResFeature(holdFeatures, nameAtr);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - resframe.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - resframe.getHeight()) / 2);
        resframe.setLocation(x, y);
    }//show result regression

    public static void showMatrix(String mat, boolean isTest) {
        ConfisionMat resframe = new ConfisionMat();
        resframe.setVisible(true);
        if(!isTest){
            resframe.setMatrix(mat,"Confusion Matrix of Training Result:");
        }else{
            resframe.setMatrix(mat,"Confusion Matrix of Test Result:");
        }
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - resframe.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - resframe.getHeight()) / 2);
        resframe.setLocation(x, y);
    }//show result regression

   
    public static int[] selectIndividuals(FuzzyFNT[] mainPopulation, int m_Ensemble_Candidates) {
        System.out.print("Selector"+mainPopulation.length);
        int[] selecedIndiviudal = new int[m_Ensemble_Candidates];
        IndividualSelector indSel = new IndividualSelector();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - indSel.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - indSel.getHeight()) / 2);
        indSel.setLocation(x, y);
        indSel.setVisible(true);
        indSel.setTable(mainPopulation,m_Ensemble_Candidates);
        while(true){
            if(indSel.isSubmited == true){
                selecedIndiviudal = indSel.returnSele;
                break;
            }
            System.out.print("");
        }
        indSel.dispose();
        //for(int i =0;i<selecedIndiviudal.length;i++ ){
           // System.out.println("Submited :"+selecedIndiviudal[i]);
        //}
        //System.out.println("Submited");        
        return selecedIndiviudal;
    }
}
