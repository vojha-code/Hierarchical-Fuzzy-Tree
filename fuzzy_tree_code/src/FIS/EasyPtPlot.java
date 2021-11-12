package FIS;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import ptolemy.plot.*; // Import plotting package.

public class EasyPtPlot {

    /**
     * 
     * @param fileName
     * @param type
     * @param title
     * @param points
     * @param indexPointer 
     */
    public EasyPtPlot(String fileName, String type, String title,int points,int indexPointer) {
        // Create ptplot ‘‘Plot’’ object
        indexPointer = indexPointer * 2;//2 for two columns for each output
        try {
            double[] plotX;
            double[] plotY;
            int pointLength = 0;

            String line;
            try (FileReader fin = new FileReader(fileName)) {
                BufferedReader br = new BufferedReader(fin);
                pointLength = 0;
                while ((line = br.readLine()) != null) {
                    pointLength++;
                }  
                pointLength = (int)((points/100.0) * pointLength);//percent of total points
                System.out.println("Total points ploted are:"+pointLength);
                plotX = new double[pointLength];
                plotY = new double[pointLength];
                br.close();
                fin.close();
            }
            try (FileReader fin1 = new FileReader(fileName)) {
                BufferedReader br1 = new BufferedReader(fin1);
                int i = 0;
                while (i < pointLength ){
                    line = br1.readLine();
                    String[] tokens = line.split(",");
                    plotX[i] = Double.parseDouble(tokens[indexPointer]);//target is in x
                    plotY[i] = Double.parseDouble(tokens[indexPointer+1]);//predicted in y
                    i++;//increament i for next row
                }
                br1.close();
                fin1.close();
            }

            switch (type) {
                case "prediction": {
                    Plot myPlot = new Plot();
                    myPlot.setTitle(title);
                    myPlot.setXLabel("data points");
                    myPlot.setYLabel("data values");
                    //myPlot.setMarksStyle("points");
                    for (int i = 0; i < pointLength; i++) {
                        double x = (double) i;
                        double y = plotX[i];//Target
                        double z = plotY[i];//Predicted
                        myPlot.addPoint(0, x, y, true);//target red
                        myPlot.addPoint(1, x, z, true);//predicted blue
                    }       // Create PlotApplication to display Plot object
                    new PlotApplication(myPlot);
                    break;
                } //end prediction  plot
                case "regression": {
                    Plot myPlot = new Plot();
                    myPlot.setTitle(title);
                    myPlot.setXLabel("target");
                    myPlot.setYLabel("predicted");
                    myPlot.setMarksStyle("points");
                    myPlot.setConnected(false);
                    for (int i = 0; i < pointLength; i++) {

                        double x = plotX[i];//Target
                        double y = plotY[i];//Predicted
                        myPlot.addPoint(0, x, y, true);
                        //myPlot.addPoint(1, x, z, true);
                    }       // Create PlotApplication to display Plot object
                    new PlotApplication(myPlot);
                    break;
                }//end regression  plot
            }//end switch case
        } catch (IOException | NumberFormatException e) {
            System.out.print("Error file Train Test printing:" + e);
        }
    }//end EasyPlot method
}//end easyPlot class
