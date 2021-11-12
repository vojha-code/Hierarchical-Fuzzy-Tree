/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AdditionalFrames;

import static AdditionalFrames.InitiatorFrame.absoluteFilePathInp;
import static AdditionalFrames.InitiatorFrame.absoluteFilePathOut;
import static AdditionalFrames.InitiatorFrame.personData;
import FIS.FuzzyFrame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

/**
 *
 * @author Varun
 */
public class InitiatorFrame extends javax.swing.JFrame {

    public static String absoluteFilePathInp = System.getProperty("user.dir") + File.separator + "input" + File.separator;
    public static String absoluteFilePathOut = System.getProperty("user.dir") + File.separator + "output" + File.separator;

    public static String personData;
    public String personName;
    public String personEmail;
    public String personExperiment;

    private void callModels() {
        setFields();
        FuzzyFrame frame = new FuzzyFrame();
        frame.setVisible(true);
        this.dispose();// setVisible(false);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);

    }

    private boolean setFields() {
        personName = jTextFieldPersonName.getText();
        personEmail = jTextFieldPersonEmail.getText();
        personExperiment = jTextFieldPersonExp.getText();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String dateValue = dateFormat.format(date);

        personData = personName + "\n" + personEmail + "\n" + personExperiment + "\n" + dateValue;
        boolean personFill = false;
        if (!personName.isEmpty() && !personEmail.isEmpty() && !personExperiment.isEmpty()) {
            personFill = true;
            if (personEmail.contains("@") && personEmail.contains(".")) {
                personFill = true;
            } else {
                personFill = false;
                JOptionPane.showMessageDialog(this, "Enter a valid email!");
            }
        } else {
            personFill = false;
            JOptionPane.showMessageDialog(this, "Enter and valid data!");
        }
        return personFill;
    }//check fields

    private void setFilePath() {
        String workingDirectory = System.getProperty("user.dir");
        absoluteFilePathInp = workingDirectory + File.separator + "input" + File.separator;
        absoluteFilePathOut = workingDirectory + File.separator + "output" + File.separator;
        System.out.println("Final filepath Input  : " + absoluteFilePathInp);
        System.out.println("Final filepath Output : " + absoluteFilePathOut);
    }

    public InitiatorFrame() {
        setFilePath();
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel13 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        exitCall = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jTextFieldPersonExp = new javax.swing.JTextField();
        jTextFieldPersonEmail = new javax.swing.JTextField();
        jTextFieldPersonName = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        startModel = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        main_image = new javax.swing.JLabel();

        jLabel13.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(0, 0, 102));
        jLabel13.setText("Experiment Details:");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Adaptive Approximation Tool");
        setResizable(false);

        jPanel1.setLayout(null);

        jLabel8.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 102));
        jLabel8.setText("Or");
        jPanel1.add(jLabel8);
        jLabel8.setBounds(130, 240, 12, 14);

        exitCall.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        exitCall.setForeground(new java.awt.Color(0, 0, 102));
        exitCall.setText("Exit");
        exitCall.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        exitCall.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                exitCallMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exitCallMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exitCallMouseExited(evt);
            }
        });
        jPanel1.add(exitCall);
        exitCall.setBounds(90, 230, 40, 30);

        jLabel10.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 102));
        jLabel10.setText("Email:");
        jPanel1.add(jLabel10);
        jLabel10.setBounds(90, 124, 27, 30);

        jLabel11.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(51, 51, 51));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("<html><center>This work was funded by the European Union’s Seventh Framework Programme for <br> research, technological development and demonstration under grant agreement No. 316555.</center></html>");
        jLabel11.setToolTipText("");
        jLabel11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel1.add(jLabel11);
        jLabel11.setBounds(30, 290, 530, 40);

        jTextFieldPersonExp.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jTextFieldPersonExp.setForeground(new java.awt.Color(0, 0, 102));
        jTextFieldPersonExp.setText("Write a title for the experiment");
        jTextFieldPersonExp.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextFieldPersonExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldPersonExpActionPerformed(evt);
            }
        });
        jPanel1.add(jTextFieldPersonExp);
        jTextFieldPersonExp.setBounds(220, 158, 210, 30);

        jTextFieldPersonEmail.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jTextFieldPersonEmail.setForeground(new java.awt.Color(0, 0, 102));
        jTextFieldPersonEmail.setText("vkojha@ieee.org");
        jTextFieldPersonEmail.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextFieldPersonEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldPersonEmailActionPerformed(evt);
            }
        });
        jPanel1.add(jTextFieldPersonEmail);
        jTextFieldPersonEmail.setBounds(220, 130, 210, 20);

        jTextFieldPersonName.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jTextFieldPersonName.setForeground(new java.awt.Color(0, 0, 102));
        jTextFieldPersonName.setText("Varun Kumar Ojha");
        jTextFieldPersonName.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextFieldPersonName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldPersonNameActionPerformed(evt);
            }
        });
        jPanel1.add(jTextFieldPersonName);
        jTextFieldPersonName.setBounds(220, 100, 210, 20);

        jLabel7.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 0, 102));
        jLabel7.setText("Name: ");
        jPanel1.add(jLabel7);
        jLabel7.setBounds(90, 90, 33, 30);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(51, 51, 51));
        jLabel1.setText("Developed At: VSB Technical University of Ostrava, Czech Republic and Machine Intelligence Labs, USA");
        jPanel1.add(jLabel1);
        jLabel1.setBounds(40, 260, 510, 30);

        jLabel2.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 51, 51));
        jLabel2.setText("  Function Aprroximation And Feature Selection");
        jPanel1.add(jLabel2);
        jLabel2.setBounds(20, 60, 350, 14);

        startModel.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        startModel.setForeground(new java.awt.Color(0, 0, 102));
        startModel.setText("Start Experiments");
        startModel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        startModel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                startModelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startModelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                startModelMouseExited(evt);
            }
        });
        jPanel1.add(startModel);
        startModel.setBounds(220, 230, 130, 30);

        jLabel14.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(0, 0, 102));
        jLabel14.setText("Experiment Details:");
        jPanel1.add(jLabel14);
        jLabel14.setBounds(90, 170, 110, 14);

        jLabel15.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(0, 0, 102));
        jLabel15.setText("The Algorithm:                    Multiobjective Type-1 and Type-2 Fuzzy Tree");
        jPanel1.add(jLabel15);
        jLabel15.setBounds(90, 200, 370, 14);

        main_image.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/main_page.png"))); // NOI18N
        main_image.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel1.add(main_image);
        main_image.setBounds(-3, -1, 600, 340);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldPersonEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldPersonEmailActionPerformed

    }//GEN-LAST:event_jTextFieldPersonEmailActionPerformed

    private void jTextFieldPersonNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldPersonNameActionPerformed

    }//GEN-LAST:event_jTextFieldPersonNameActionPerformed

    private void jTextFieldPersonExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldPersonExpActionPerformed

    }//GEN-LAST:event_jTextFieldPersonExpActionPerformed

    private void exitCallMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitCallMouseExited
        exitCall.setForeground(new java.awt.Color(0, 0, 102));
    }//GEN-LAST:event_exitCallMouseExited

    private void exitCallMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitCallMouseEntered
        exitCall.setForeground(Color.red);
    }//GEN-LAST:event_exitCallMouseEntered

    private void exitCallMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitCallMouseClicked
        System.exit(0);
    }//GEN-LAST:event_exitCallMouseClicked

    private void startModelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_startModelMouseClicked
        callModels();
    }//GEN-LAST:event_startModelMouseClicked

    private void startModelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_startModelMouseEntered
        startModel.setForeground(Color.red);
    }//GEN-LAST:event_startModelMouseEntered

    private void startModelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_startModelMouseExited
        startModel.setForeground(new java.awt.Color(0, 0, 102));
    }//GEN-LAST:event_startModelMouseExited

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(InitiatorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InitiatorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InitiatorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InitiatorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new InitiatorFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel exitCall;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextFieldPersonEmail;
    private javax.swing.JTextField jTextFieldPersonExp;
    private javax.swing.JTextField jTextFieldPersonName;
    private javax.swing.JLabel main_image;
    private javax.swing.JLabel startModel;
    // End of variables declaration//GEN-END:variables
}
