/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Varun and Vaslav
 */
import AdditionalFrames.InitiatorFrame;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        /*try {
         for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
         if ("Nimbus".equals(info.getName())) {
         UIManager.setLookAndFeel(info.getClassName());
         break;
         }
         }
         } catch (Exception e) {
         // If Nimbus is not available, you can set the GUI to another look and feel.
         }*/

        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        InitiatorFrame initFrame = new InitiatorFrame();
        initFrame.setVisible(true);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - initFrame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - initFrame.getHeight()) / 2);
        initFrame.setLocation(x, y);
    }

}
