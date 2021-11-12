/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphGUI;

import AdditionalFrames.InitiatorFrame;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 *
 * @author Varun
 */
import javax.swing.*;

public class GraphDraw extends JPanel {

    /**
     * Creates new form GraphDraw
     */
    int width;
    int height;

    ArrayList<Node> nodes;
    ArrayList<edge> edges;
    private int maxWidth;
    private int maxHeight;

    public GraphDraw() {
        nodes = new ArrayList<Node>();
        edges = new ArrayList<edge>();
        width = 30;
        height = 30;
    }

    void setMaxWinSize(int maxFrameX, int maxFrameY) {
        this.maxWidth = maxFrameX;
        this.maxHeight = maxFrameY;
    }

    class Node {

        int x, y, actFun;
        String name, type;

        public Node(String myName, int myX, int myY, String mytype, int myactFun) {
            x = myX;
            y = myY;
            name = myName;
            type = mytype;
            actFun = myactFun;
        }
    }

    class edge {

        int i, j;

        public edge(int ii, int jj) {
            i = ii;
            j = jj;
        }
    }

    public void addNode(String name, int x, int y, String nodeType, int actFun) {
        //add a node at pixel (x,y)
        nodes.add(new Node(name, x, y, nodeType, actFun));
        //this.repaint();
        repaint();
        //jPanel1.repaint();
    }

    public void addEdge(int i, int j) {
        //add an edge between nodes i and j
        edges.add(new edge(i, j));
        //this.repaint();
        repaint();
        //jPanel1.repaint();
    }

    @Override
    public void paintComponent(Graphics gg) {
        Graphics2D g;// = (Graphics2D)jPanel1.getGraphics();
        g = (Graphics2D) gg;
        FontMetrics f = g.getFontMetrics();

        int nodeHeight = Math.max(height, f.getHeight());
        //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //g.setStroke(new BasicStroke(1.5f)); // set the thickness of polygon line
        //g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.00f));
        //g.setPaint(Color.black);//color of the polygon line
        //g.clearRect(0, 0, maxWidth, maxHeight);
        //g.setFont(new Font("TimesRoman", Font.PLAIN, 10)); 
        g.setBackground(Color.white);
        g.setColor(Color.white);
        g.fillRect(0 , 0, maxWidth, maxHeight);
        //g.setColor(Color.darkGray);

        g.setColor(Color.BLACK);
        //g.drawString("Press Ctrl+S to save image", 80, 80);
        //System.out.println("Edge Size" + edges.size() + " Node Size" + nodes.size());
        for (edge e : edges) {
            //System.out.println(e.i + ">" + e.j);
            g.drawLine(nodes.get(e.i).x, nodes.get(e.i).y, nodes.get(e.j).x, nodes.get(e.j).y);
            //System.out.println("8");
        }
        //System.out.println("Ok");

        for (Node n : nodes) {
            int nodeWidth = Math.max(width, f.stringWidth(n.name) + width / 2);
            if (n.type.equals("F")) {
                if (n.actFun == 1) {
                    g.setColor(Color.GRAY);
                } else if (n.actFun == 2) {
                    g.setColor(Color.MAGENTA);
                } else if (n.actFun == 3) {
                    g.setColor(Color.CYAN);
                } else if (n.actFun == 4) {
                    g.setColor(Color.ORANGE);
                } else if (n.actFun == 5) {
                    g.setColor(Color.PINK);
                } else if (n.actFun == 6) {
                    g.setColor(Color.red);
                } else if (n.actFun == 7) {
                    g.setColor(Color.green);
                }
                g.fillOval(n.x - nodeWidth / 2, n.y - nodeHeight / 2, nodeWidth, nodeHeight);
                g.setColor(Color.BLACK);
                g.drawOval(n.x - nodeWidth / 2, n.y - nodeHeight / 2, nodeWidth, nodeHeight);
                g.setColor(Color.white);
                g.drawString(n.name, n.x - f.stringWidth(n.name) / 2, n.y + f.getHeight() / 2);
            } else {
                g.setColor(Color.lightGray);
                g.fillRect(n.x - nodeWidth / 2, n.y - nodeHeight / 2, nodeWidth, nodeHeight);
                g.setColor(Color.BLACK);
                g.drawRect(n.x - nodeWidth / 2, n.y - nodeHeight / 2, nodeWidth, nodeHeight);
                g.setColor(Color.DARK_GRAY);
                g.drawString(n.name, n.x - f.stringWidth(n.name) / 2, n.y + f.getHeight() / 2);
            }
        }
    }

    public static BufferedImage getScreenShot(Component componenet) {
        BufferedImage image = new BufferedImage(componenet.getWidth(), componenet.getHeight(), BufferedImage.TYPE_INT_RGB);
        componenet.paint(image.getGraphics());
        return image;
    }

    public static void SaveScreeShot(Component component, String filename) {
        try {
            BufferedImage img = getScreenShot(component);
            ImageIO.write(img, "png", new File(filename));
            img.flush();
            img = null;//clean img variable
        } catch (Exception e) {

        }

    }

    public void callSaveImage(int Oidx,int Midx,boolean isOld) {
        try {
            if(!isOld){
                SaveScreeShot(this, InitiatorFrame.absoluteFilePathOut+"SaveImage"+Oidx+""+Midx+".png");
            }else{
                SaveScreeShot(this, InitiatorFrame.absoluteFilePathOut+"SaveImageOld"+Oidx+""+Midx+".png");
            }
            
        } catch (Exception e) {

        }
    }
}
