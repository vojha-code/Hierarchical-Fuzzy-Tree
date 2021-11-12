/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zTestCodeFolder;

import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author Varun
 */
public class TestDeleteFolder {

    public static void main(String[] args) {
        //JFileChooser f = new JFileChooser();
        //f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //f.showSaveDialog(null);

        File outputFolder = new File("C:\\Users\\Varun\\Desktop\\Fridman\\T1SO\\FIS_Type-1_FRD_SO (6)");
        //        f.getSelectedFile();
        //System.out.println(f.getCurrentDirectory());
       // System.out.println(f.getSelectedFile());
        final File[] files = outputFolder.listFiles();
        for (File f1 : files) {
            System.out.println(f1.getName());
            //f1.delete();
        }
        //outputFolder.delete();
    }

}
