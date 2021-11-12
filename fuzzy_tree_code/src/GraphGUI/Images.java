/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphGUI;

import MISC.FramesCall;
import AdditionalFrames.InitiatorFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 *
 * @author Varun
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
public class Images extends JFrame implements ActionListener {

    static JScrollPane scPane = new JScrollPane();
    static JLabel imageView = new JLabel();
    static JTextField imageNumber = new JTextField();
    static JButton btnNext = new JButton();
    static JButton btnPre = new JButton();
    static JButton btnInfo = new JButton();
    static JButton btnHelp = new JButton();
    static JButton btnClose = new JButton();
    static BufferedImage bufImages[];
    static Images myImage;
    int imageNum = 0;
    int modelNum = 1;
    int m_imageArraySize;
    int output = 1;
    int m_totalImages;
    int m_totalOutCol;

    public Images(int outputColumn, int imageArraySize, boolean isOld) {
        m_imageArraySize = imageArraySize;
        m_totalOutCol = outputColumn;
        m_totalImages = outputColumn * imageArraySize;
        output = 1;
        modelNum = 1;
        imageNum = 0;//set Number to 0 on each call
        setSize(800, 680);
        setResizable(false);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        bufImages = new BufferedImage[m_totalImages];
        String imageName = "";
        if (!isOld) {
            imageName = InitiatorFrame.absoluteFilePathOut + "SaveImage";
        } else {
            imageName = InitiatorFrame.absoluteFilePathOut + "SaveImageOld";
        }
        for (int j = 0; j < outputColumn; j++) {
            for (int k = 0; k < m_imageArraySize; k++) {
                File myFile = new File(imageName + "" + j + "" + k + ".png");
                if (myFile.exists()) {
                    try {
                        bufImages[imageNum] = ImageIO.read(myFile);
                        imageNum++;
                    } catch (Exception e) {
                        System.out.printf(" Error reading image" + e);
                    }
                } else {
                    System.out.printf("  image file doesnot exists");
                    return;
                }
            }//k images
        }//j outputs
        imageNum = 0;//re-set the index

        btnPre.setSize(100, 20);
        btnPre.setLocation(10, 20);
        btnPre.setText("Previous");
        btnPre.addActionListener(this);
        add(btnPre);

        btnNext.setSize(100, 20);
        btnNext.setLocation(130, 20);
        btnNext.setText("Next");
        btnNext.addActionListener(this);
        add(btnNext);

        imageNumber.setSize(150, 20);
        imageNumber.setLocation(250, 20);
        imageNumber.setEditable(false);
        imageNumber.setText("Target "+output+"/"+m_totalOutCol+" : Model " + modelNum + "/" + m_imageArraySize);
        imageNumber.setBorder(null);

        add(imageNumber);

        btnInfo.setSize(130, 20);
        btnInfo.setLocation(410, 20);
        btnInfo.setText("Tree Details");
        btnInfo.addActionListener(this);
        add(btnInfo);

        btnHelp.setSize(100, 20);
        btnHelp.setLocation(560, 20);
        btnHelp.setText("Help");
        btnHelp.addActionListener(this);
        add(btnHelp);

        btnClose.setSize(100, 20);
        btnClose.setLocation(680, 20);
        btnClose.setText("Close");
        btnClose.addActionListener(this);
        add(btnClose);

        ImageIcon icon = new ImageIcon(bufImages[imageNum]);
        imageView.setIcon(icon);

        scPane.setSize(785, 580);
        scPane.setLocation(5, 60);
        //scPane.setBackground(Color.WHITE);
        //scPane.setForeground(Color.WHITE);
        scPane.setViewportView(imageView);
        add(scPane);

        repaint();

        //Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        //int x = (int) ((dimension.getWidth() - 750) / 2);
        //int y = (int) ((dimension.getHeight() - 580) / 2);
        //setLocation(x, y);
        setVisible(true);
    }//Image Constructor

    public static void main(String args[]) {
        myImage = new Images(3, 5, false);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == btnNext) {
            if (imageNum < m_totalImages - 1) {
                imageNum++;
                modelNum++;
                if (modelNum > m_imageArraySize) {
                    modelNum = 1;
                    output++;
                }
                ImageIcon icon = new ImageIcon(bufImages[imageNum]);
                imageView.setIcon(icon);
                imageNumber.setText("Target "+output+"/"+m_totalOutCol+" : Model " + modelNum + "/" + m_imageArraySize);
            } else {
                //imageView.setText("Last image displayed! Click previous");
                //JOptionPane.showMessageDialog(null, "Last image displayed! Click previous");
            }
        } else if (event.getSource() == btnPre) {
            if (imageNum > 0) {
                imageNum--;
                modelNum--;
                if (modelNum < 1) {
                    modelNum = m_imageArraySize;
                    output--;
                    if (imageNum == 0) {
                        modelNum = 1;
                        output = 1;
                    }
                }
                ImageIcon icon = new ImageIcon(bufImages[imageNum]);
                imageView.setIcon(icon);
                imageNumber.setText("Target "+output+"/"+m_totalOutCol+" : Model " + modelNum + "/" + m_imageArraySize);
            } else {
                //imageView.setText("First image displayed! Click Next");
                //JOptionPane.showMessageDialog(null, "First image displayed! Click Next");
            }
        } else if (event.getSource() == btnInfo) {
            FramesCall.treeDescription();
        } else if (event.getSource() == btnHelp) {
            FramesCall.helpCall();
        } else {
            imageNum = 0;
            output = 1;
            modelNum = 1;
            this.dispose();
        }
    } // end method actionPerformed
}
