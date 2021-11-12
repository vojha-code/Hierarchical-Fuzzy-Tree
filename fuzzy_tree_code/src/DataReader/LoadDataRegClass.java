package DataReader;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.*;//import input output
import java.util.Scanner;
import java.util.*;
import javax.swing.JOptionPane;

public class LoadDataRegClass {

    public String DataSet = ""; 		//Public beacuse of outside : the relation name 
    public int dataSetLength;
    private Vector catName = new Vector();	//the string of the class category - for classification only
    private Vector inputName = new Vector();	//name of the input features
    private Vector outputName = new Vector();	//name of the output features
    private Vector attrVector = new Vector();	//holds information of all available attributes 
    private AttrClass attrCls;			//hold one atttibure information
    private TrainPat tp;			//temporarily used to training pattern
    private Vector Data = new Vector();		//temporarily holds the comple data examples
    private double dataX[];			//temporarily holds input features vector
    private double dataY[];			//temporarily holds output featuers vector
    private double x[][];				//temporarily holds input features
    private double y[][];				//temporarily holds output featuers
    private double t[];					//temporarily hold class category - for classification only
    private boolean isClassification = false;	        //check if classification problem
    private boolean isTimeSerise = false;		//check is time serise problem
    private double normalizedLow;
    private double normalizedHigh;
    private Object currentFrame;
    private boolean notARFF = false;

    public void setNormalizationRange(double low, double high) {
        normalizedLow = low;
        normalizedHigh = high;
    }

    public String getDatasetName() {
        return DataSet;
    }

    public int getDatasetSize() {
        return dataSetLength;
    }

    public Vector getcatName() {
        return catName;
    }

    public Vector getInputName() {
        return inputName;
    }	//name of the input features

    public Vector getOutputName() {
        return outputName;
    }

    public Vector getAttributes() {
        return attrVector;
    }	//holds total available attributes 

    public String getProblemType() {
        if (isClassification) {
            return "Classification";
        } else if (isTimeSerise) {
            return "TimeSerise";
        } else {
            return "Regression";
        }
    }

    //Normalizing data value
    public double normalize(double x, double dataLow, double dataHigh, double normalizedLow, double normalizedHigh) {
        return ((x - dataLow) / (dataHigh - dataLow)) * (normalizedHigh - normalizedLow) + normalizedLow;
    }

    //De-normalizing data value
    public double denormalize(double x, double dataLow, double dataHigh, double normalizedLow, double normalizedHigh) {
        return ((dataLow - dataHigh) * x - normalizedHigh * dataLow + dataHigh * normalizedLow) / (normalizedLow - normalizedHigh);
    }

    //check data is numeric
    public boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    //check data attribute is a class  type
    public boolean isClass(String s) {
        return (s.equalsIgnoreCase("CLASS") || s.equals("Type"));
    }

    //Featching dataset name
    public void callRelation(String Rel, PrintWriter pr) {
        try {
            //System.out.println(Rel);
            Scanner sr = new Scanner(Rel);
            while (sr.hasNext()) {
                //System.out.println(sr.next());
                String relVal = sr.next();

                if (relVal.equalsIgnoreCase("@RELATION")) {
                    //Do nothing just print relation Name
                    DataSet = sr.next();
                    System.out.println("Dataset is " + DataSet);
                } else {
                    throw new Error("Error: Bad Arff file");
                }
            }
            //pr.println(Rel); //optional
        } catch (Exception e) {
            System.out.println("Error in reading dataset name: \n"+e);
        }
    }//end callRelation

    //Fetching attribute information
    public void callAttribute(String Attr, PrintWriter pr) {
        try {
            boolean isStringAtr = false;
            if (Attr.contains("{") || Attr.contains("}")) {
                isStringAtr = true;
            }

            String delims = "[\\[\\]\\{\\}\\s*, ]+";//
            String[] tokens = Attr.split(delims);
            //System.out.println(tokens.length);
            int i = 0;
            String attrName = tokens[i];
            i++; //counted a token
            if (attrName.equalsIgnoreCase("@ATTRIBUTE")) {
                //Token is  an attribute
                System.out.print(" " + attrName);

                if (isClass(tokens[i])) {//if attribute is a class?  
                    //System.out.println("Attr Name:"+tokens[i]);
                    i++; //counted a token
                    int j = 0;
                    for (; i < tokens.length; i++) {
                        catName.add(tokens[i]);
                        //catVal.add(j);
                        System.out.print(" " + catName.get(j));//+" Cat Val"+ catVal.get(j)); 
                        //System.out.println("Cat Name:"+tokens[i]+" Cat Val"+ j);
                        j++; //increase value j
                    }
                    isClassification = true;//set classification problem true		   
                }//end if class-check
                else { //Token is not a class attribute 
                    attrCls = new AttrClass();
                    attrName = tokens[i];
                    attrCls.setAttrName(attrName);
                    System.out.print(" " + attrCls.getAttrName());
                    //System.out.println("Attr Name:"+attrName);
                    i++;//
                    if (!isStringAtr) {//Attribute is not string type
                        String attrType = tokens[i];
                        attrCls.setAttrType(attrType);
                        System.out.print(" " + attrCls.getAttrType());
                        //System.out.println("Attr Type:"+attrType);
                        i++;
                        double range[] = new double[2];
                        int j = 0;
                        for (; i < tokens.length; i++) {
                            if (isNumeric(tokens[i])) {
                                double attrVal = (double) Double.parseDouble(tokens[i]);
                                range[j] = attrVal;
                                //System.out.println("Token Val:"+(range[j]));//attrVal));
                                j++;
                            }
                        }
                        attrCls.setAttrRange(range);
                        System.out.print(" " + attrCls.getAttrRange()[0]);//attrVal));
                        System.out.print(" " + attrCls.getAttrRange()[1]);//attrVal));
                    }//end Check for string attribute
                    else {
                        Vector strattrVector = new Vector();
                        String attrType = "String";//tokens[i];
                        attrCls.setAttrType(attrType);
                        System.out.print(" " + attrCls.getAttrType());
                        //System.out.println("Attr Type:"+attrType);
                        //i++;
                        double range[] = new double[2];
                        int j = 0;
                        range[0] = 0;
                        for (; i < tokens.length; i++) {
                            strattrVector.add(tokens[i]);
                            j++;
                        }
                        range[1] = j - 1;
                        attrCls.setAttrRange(range);
                        attrCls.setStrAttrVal(strattrVector);
                        for (int l = 0; l < j; l++) {
                            System.out.print(" " + attrCls.getStrAttrVal().get(l));
                        }
                        System.out.print(" " + attrCls.getAttrRange()[0]);//attrVal));
                        System.out.print(" " + attrCls.getAttrRange()[1]);//attrVal));

                    }//end if - else
                    attrVector.add(attrCls);
                }//end else  
                System.out.println();
            } else {
                throw new Error("Error: Bad Arff file");
            }

        } catch (Exception e) {
            System.out.println("Error in reading attributes name \n" + e);
            JOptionPane.showMessageDialog((Component) currentFrame, "Error :" + e + "!\n Check if the @attributes are OK!");
            return;
        }//try-catch 
        //pr.println(Attr); //optional
    }//end callAttribute

    //Fetching input featuers name
    public void callInput(String Attr, PrintWriter pr) {
        try {
            String delims = "[\\[\\]\\{\\}\\s*, ]+";//
            String[] tokens = Attr.split(delims);
            //System.out.println(tokens.length);
            int i = 0;
            String attrInput = tokens[i];
            i++; //counted a token
            System.out.print(" @Input ");
            if (attrInput.equalsIgnoreCase("@INPUTS")) {
                //System.out.println("Attr Name:"+tokens[i]);
                int j = 0;
                for (; i < tokens.length; i++) {
                    inputName.add(tokens[i]);
                    //catVal.add(j);
                    System.out.print(" " + inputName.get(j));//+" Cat Val"+ catVal.get(j)); 
                    //System.out.println("Cat Name:"+tokens[i]+" Cat Val"+ j);
                    j++; //increase value j
                }
                System.out.println();
            } else {
                throw new Error("Error: Bad Arff file");
            }
            //pr.println(Attr); //optional
        } catch (Exception e) {
            System.out.println("Error in reading inputs name \n" + e);
            JOptionPane.showMessageDialog((Component) currentFrame, "Error :" + e + "!\n Check the @inputss attributes");
            return;
        }//tru-catch
    }//end callInput

    //Fetching output featuers name  
    public void callOutput(String Attr, PrintWriter pr) {
        try {
            String delims = "[\\[\\]\\{\\}\\s*, ]+";//
            String[] tokens = Attr.split(delims);
            //System.out.println(tokens.length);
            int i = 0;
            String attrOutput = tokens[i];
            i++; //counted a token
            System.out.print(" @Output ");
            if (attrOutput.equalsIgnoreCase("@OUTPUTS")) {
                //System.out.println("Attr Name:"+tokens[i]);
                int j = 0;
                for (; i < tokens.length; i++) {
                    outputName.add(tokens[i]);
                    //catVal.add(j);
                    System.out.println(" " + outputName.get(j));//+" Cat Val"+ catVal.get(j)); 
                    //System.out.println("Cat Name:"+tokens[i]+" Cat Val"+ j);
                    j++; //increase value j
                }
                System.out.println();
            } else {
                throw new Error("Error: Bad Arff file");
            }
            //pr.println(Attr); //optional
        } catch (Exception e) {
            System.out.println("Error in reading outputs name \n" + e);
            JOptionPane.showMessageDialog((Component) currentFrame, "Error :" + e + "!\n Check the @outputs attributes");
            return;
        }//try-catch 
    }//end callOutput

    //collecting data from the file
    public void callDataColl(String DATA, PrintWriter pr) {
        try {
            String delims = "[\\[\\]\\{\\}\\s*, ]+";//
            String[] tokens = DATA.split(delims);
            int attrLen = attrVector.size();//for classification problem 
            int attrLenIn = inputName.size();//for regression problem
            int attrLenOt = outputName.size();//for regression problem

            //Find attribute values
            tp = new TrainPat();
            int i;
            dataX = new double[attrLenIn];
            dataY = new double[attrLenOt];
            if (isClassification) {//collect data for classification
                for (i = 0; (i < attrLen && i < tokens.length); i++) {
                    attrCls = (AttrClass) attrVector.get(i);
                    String typeAtr = attrCls.getAttrType();
                    if (!(typeAtr.equalsIgnoreCase("String"))) {
                        if (isNumeric(tokens[i])) {
                            double attrVal = (double) Double.parseDouble(tokens[i]);
                            dataX[i] = attrVal;  //System.out.print(" "+attrVal);//attrVal));
                            pr.print(dataX[i] + ",");
                        }
                    } else {
                        Vector strAtrValVector = attrCls.getStrAttrVal();
                        String attrValstr = tokens[i];
                        double attrVal = findStrVal(strAtrValVector, attrValstr);
                        dataX[i] = attrVal;  //System.out.print(" "+attrVal);
                        pr.print(dataX[i] + ",");
                    }
                }
                tp.setX(dataX);//setting X
                if (i < tokens.length) {//Find attribute category
                    String attrCat = tokens[i];
                    int attrCatVal = findCat(attrCat);
                    tp.setCls(attrCatVal);//setting Y
                    //System.out.print(" "+attrCatVal);
                    pr.print(attrCatVal + ",");
                }
                //System.out.println();
            }//end loading class data
            else {//It is regression problem
                attrCls = (AttrClass) attrVector.get(0);
                String atrNameZero = attrCls.getAttrName();
                if (atrNameZero.equalsIgnoreCase("TimeStamp")) {//time serise data
                    isTimeSerise = true;
                    //System.out.print("I am in Time Serise loading");
                    int t = 0;
                    for (i = 0; i < tokens.length; i++) {
                        attrCls = (AttrClass) attrVector.get(i);
                        String typeAtr = attrCls.getAttrType();
                        String atrNameI = attrCls.getAttrName();
                        if (atrNameI.equalsIgnoreCase("TimeStamp")) {
                            //do not load first coulmn
                            //System.out.print("Time Serise"+tokens.length);
                        } else {
                            if (!(typeAtr.equalsIgnoreCase("String"))) {
                                if (isNumeric(tokens[i])) {
                                    double attrVal = (double) Double.parseDouble(tokens[i]);
                                    //System.out.println("Token Val:"+attrVal);			
                                    if (t < attrLenIn) {
                                        dataX[t] = attrVal;
                                        pr.print(dataX[t] + ",");
                                        //System.out.print(dataX[t]+" ");
                                    } else {
                                        dataY[(t - attrLenIn)] = attrVal;
                                        pr.print(dataY[t - attrLenIn] + ",");
                                        //System.out.print(dataY[t-attrLenIn]+" ");
                                    }
                                }
                            } else {
                                Vector strAtrValVector = attrCls.getStrAttrVal();
                                String attrValstr = tokens[i];
                                double attrVal = findStrVal(strAtrValVector, attrValstr);
                                if (t < attrLenIn) {
                                    dataX[t] = attrVal;
                                    pr.print(dataX[t] + ",");
                                    //System.out.print(dataX[t]+" ");
                                } else {
                                    dataY[(t - attrLenIn)] = attrVal;
                                    pr.print(dataY[t - attrLenIn] + ",");
                                    //System.out.print(dataY[t-attrLenIn]+" ");
                                }
                            }
                            t++;
                            //System.out.println();	
                        }//end loading time					
                    }//end for		
                } else {// go for regression loading
                    for (i = 0; i < tokens.length; i++) {
                        attrCls = (AttrClass) attrVector.get(i);
                        String typeAtr = attrCls.getAttrType();
                        if (!(typeAtr.equalsIgnoreCase("String"))) {
                            if (isNumeric(tokens[i])) {
                                double attrVal = (double) Double.parseDouble(tokens[i]);
                                //System.out.println("Token Val:"+attrVal);			
                                if (i < attrLenIn) {
                                    dataX[i] = attrVal;
                                    pr.print(dataX[i] + ",");
                                    //System.out.print(dataX[i]+" ");
                                } else {
                                    dataY[(i - attrLenIn)] = attrVal;
                                    pr.print(dataY[i - attrLenIn] + ",");
                                    //System.out.print(dataY[i-attrLenIn]+" ");
                                }
                            }
                        } else {
                            Vector strAtrValVector = attrCls.getStrAttrVal();
                            String attrValstr = tokens[i];
                            double attrVal = findStrVal(strAtrValVector, attrValstr);
                            if (i < attrLenIn) {
                                dataX[i] = attrVal;
                                pr.print(dataX[i] + ",");
                                //System.out.print(dataX[i]+" ");
                            } else {
                                dataY[(i - attrLenIn)] = attrVal;
                                pr.print(dataY[i - attrLenIn] + ",");
                                //System.out.print(dataY[i-attrLenIn]+" ");
                            }
                        }
                    }//end for		
                }
                //System.out.println();
                tp.setX(dataX);
                tp.setY(dataY);
            }//end loading regression data	
            Data.add(tp);// Adding the training patter
            pr.println();// go to next line data are filtered
        } catch (Exception e) {
            System.out.println("Error in reading data values \n" + e);
            JOptionPane.showMessageDialog((Component) currentFrame, "Error loading data values \n" + e + "\n Check data values in the class");
            return;
            //System.exit(0);
        }
    }//end callData

    //resolving the catogary name to number
    public int findCat(String attrCat) {
        int i;
        int catVal = 0;
        try {
            boolean foundCat = false;
            int catSize = catName.size();
            for (i = 0; i < catSize; i++) {
                //System.out.println(attrCat+" = "+catName.get(i));
                if (attrCat.equals(catName.get(i))) {
                    catVal = i;
                    foundCat = true;
                    break;
                }
            }
            if (!foundCat) {
                throw new Error("Bad arrf file");
            }
        } catch (Exception e) {
            System.out.println("Error findCat:" + e);
            JOptionPane.showMessageDialog((Component) currentFrame, "Error in string value retrival \n" + e + "!\n Check data column for class");
            return 0;
            //System.exit(0);
        }
        return catVal;
    }//end findCatagory

    //String attr/data value
    int findStrVal(Vector atrStrVector, String attrStrVal) {
        int i;
        int atrVal = 0;
        try {
            int atrStrVectorSize = atrStrVector.size();
            for (i = 0; i < atrStrVectorSize; i++) {

                if (attrStrVal.equals(atrStrVector.get(i))) {
                    atrVal = i;
                    break;
                }
            }
            //cout << atrVal;
        } catch (Exception e) {
            System.out.println("Error findStrVal:" + e);
            JOptionPane.showMessageDialog((Component) currentFrame, "Error in string value retrival \n" + e + "!\n Check string values in the file");
            return 0;
            //System.exit(0);
        }
        return atrVal;
    }//end findatr

    //loading data 
    public void loadData(String fileName) {
        try {
            //Normalized data
            FileWriter fwrite = new FileWriter(fileName);
            PrintWriter pr = new PrintWriter(fwrite);
            int noPatterns = Data.size();
            dataSetLength = noPatterns;//datasetlength
            int noInputFeatures = inputName.size();//attrVector.size();
            int noOutputFeatures = outputName.size();//attrVector.size();
            int noFeatures;

            if (isTimeSerise) {
                noFeatures = attrVector.size() - 1;
            } else {
                noFeatures = attrVector.size();
            }

            x = new double[noPatterns][noInputFeatures];
            y = new double[noPatterns][noOutputFeatures];
            t = new double[noPatterns];

            double dataLow;
            double dataHigh;

            double valX;
            double valY;

            int i, j, k = 0;
            if (isClassification) {//load data for classification
                for (i = 0; i < noPatterns; i++) {
                    for (j = 0; j < noFeatures; j++) {
                        dataLow = ((AttrClass) attrVector.get(j)).getAttrRange()[0];
                        dataHigh = ((AttrClass) attrVector.get(j)).getAttrRange()[1];
                        valX = ((TrainPat) Data.get(i)).getX()[j];
                        x[i][j] = normalize(valX, dataLow, dataHigh, normalizedLow, normalizedHigh);
                        pr.print(x[i][j] + ",");
                        //System.out.printf(" %.2f",x[i][j]);
                    }
                    valY = ((TrainPat) Data.get(i)).getCls();
                    t[i] = valY;
                    pr.println(t[i] + ",");//go to next line
                    //System.out.println(" "+t[i]);
                }//end for
                System.out.println("Classification loaded Successfully!");
                System.out.println("Problem has " + noPatterns + " examples " + noFeatures + " Attributes and " + catName.size() + " classes");
            }//end loading classification data
            else {//load data for regression
                int time = 0;
                if (isTimeSerise) {
                    time = 1;
                }//if
                for (i = 0; i < noPatterns; i++) {
                    for (j = 0; j < noFeatures; j++) {
                        dataLow = ((AttrClass) attrVector.get(j + time)).getAttrRange()[0];
                        dataHigh = ((AttrClass) attrVector.get(j + time)).getAttrRange()[1];
                        if (j < noInputFeatures) {
                            valX = ((TrainPat) Data.get(i)).getX()[j];
                            x[i][j] = normalize(valX, dataLow, dataHigh, normalizedLow, normalizedHigh);
                            pr.print(x[i][j] + ",");
                            //System.out.printf(" %.2f",x[i][j]);
                        } else {
                            valY = ((TrainPat) Data.get(i)).getY()[j - noInputFeatures];
                            y[i][(j - noInputFeatures)] = normalize(valY, dataLow, dataHigh, normalizedLow, normalizedHigh);
                            pr.print(y[i][(j - noInputFeatures)] + ",");
                            //System.out.printf(" %.2f",y[i][(j-noInputFeatures)]);
                        }
                    }//noFeatures
                    pr.println();
                    //System.out.println();
                }//noPatterns
                System.out.println("Regression loaded Successfully!");
                System.out.println("Problem has " + noPatterns + " examples " + noInputFeatures + " input and " + noOutputFeatures + " Output features");
            }//end loading regression data
            pr.close();
        } catch (Exception e) {
            System.out.println("Loadding Data Error:" + e);
            JOptionPane.showMessageDialog((Component) currentFrame, "Error file access \n" + e + "\n Check/Close if the file open/exists");
            return;
            //System.exit(0);
        }//catch 
    }//end loadData

    //Loading data into pattern
    public Pattern[] runProblem() {
        int totalPat = Data.size();
        int catSize = catName.size();

        Pattern[] pat = new Pattern[totalPat];
        try {
            int In = 0, Ot = 0;
            int i, j;//loop variables
            double InP[];//input pattern
            double TrP[];//target pattern
            if (isClassification) {//run problem for classification
                In = attrVector.size();
                Ot = catName.size();
                for (i = 0; i < totalPat; i++) {
                    InP = new double[In];
                    TrP = new double[Ot];
                    for (j = 0; j < In; j++) {
                        InP[j] = x[i][j];
                        //System.out.printf("%1.2f ",InP[j]);
                    }
                    for (j = 0; j < Ot; j++) {
                        if (j == t[i]) {
                            TrP[j] = 1.0;
                        } else {
                            TrP[j] = 0.0;
                        }
                        //System.out.printf("%d ",(int)TrP[j]);
                    }
                    //System.out.println("");
                    pat[i] = new Pattern(InP, TrP);
                    //if(i < trainPat){patTrain[i] = new Pattern(InP,TrP);}   
                    //else{patTest[i - trainPat] = new Pattern(InP,TrP);}   
                }//end totalPat 
            }//end classification
            else {//run problem for regression
                In = inputName.size();
                Ot = outputName.size();
                for (i = 0; i < totalPat; i++) {
                    InP = new double[In];
                    TrP = new double[Ot];
                    for (j = 0; j < In; j++) {
                        InP[j] = x[i][j]; //System.out.printf("%1.2f ",InP[j]);
                    }
                    for (j = 0; j < Ot; j++) {
                        TrP[j] = y[i][j]; //System.out.print(TrP[j] +" ");
                    }
                    pat[i] = new Pattern(InP, TrP);
                    //if(i < trainPat){patTrain[i] = new Pattern(InP,TrP);}   
                    //else{patTest[i - trainPat] = new Pattern(InP,TrP);}   
                }//end totalPat 
            }//end regression
            //clean-up
            for (Object Data1 : Data) {
                tp = (TrainPat) Data1;
                tp = null;
            } //for
            Data = null;//release memory 	 
            x = null;//release memory 
            t = null;//release memory 
            y = null;//release memory	
        } catch (Exception e) {
            System.out.print("Error runProblem:" + e);
            JOptionPane.showMessageDialog((Component) currentFrame, "Error runProblem\n" + e + "!");
            return null;
            //System.exit(0);
        }
        return pat;
    }//end run program

    //passing data information
    public int[] dataInfo() {
        int netInfo[] = new int[4];
        int In = 0, Ot = 0, catSize = catName.size(), classification = 0;
        if (isClassification) {
            In = attrVector.size();
            Ot = catName.size();
            classification = 1;
        } else {
            In = inputName.size();
            Ot = outputName.size();
        }
        netInfo[0] = In;
        netInfo[1] = Ot;
        netInfo[2] = catSize;
        netInfo[3] = classification;

        return netInfo;
    }//end network info

    public void loadARFFforrmatFile(String fileName, String absolutepath) {
        boolean arff = false;
        try {
	   //InputStreamReader in = new InputStreamReader(System.in);
            //BufferedReader br = new BufferedReader(in);

            //System.out.print("\nEnter a Problem/Training (eg. iris.dat/iris.arff :");
            String FILE_Prob = fileName;//br.readLine();

            //FileReader  fin = new FileReader("./Dataset/"+FILE_Prob);
            FileReader fin = new FileReader(FILE_Prob);
            BufferedReader brProb = new BufferedReader(fin);

            //Filtered data 
            FileWriter fwrite = new FileWriter(absolutepath + "filteredData.csv");
            PrintWriter pr = new PrintWriter(fwrite);

            int example = 0;//Integer.parseInt(br.readLine());
            Scanner s = null;
            String Data;

            while ((Data = brProb.readLine()) != null) {
                if (Data.isEmpty()) {
                    continue;
                } else {
                    s = new Scanner(Data);//.useDelimiter("\\s*,\\s*");
                    //System.out.println(s.hasNext("Iris"));

                    if (s.hasNext("%")) {
                        //Do nothing
                        //System.out.println("%");
                        continue;
                    }
                    if (s.hasNext("@RELATION") || s.hasNext("@relation") || s.hasNext("@Relation")) {
                        //Operate on Relation 
                        //srp.callRelation(Data,pr);
                        callRelation(Data, pr);
                        //System.out.println("@RELATION");
                        arff = true;
                        continue;
                    }
                    if (s.hasNext("@ATTRIBUTE") || s.hasNext("@attribute") || s.hasNext("@Attribute")) {
                        //Operate on attributes 
                        //System.out.println("@ATTRIBUTE");
                        //srp.callAttribute(Data,pr);
                        callAttribute(Data, pr);
                        continue;
                    }
                    if (s.hasNext("@INPUTS") || s.hasNext("@inputs") || s.hasNext("@Inputs")) {
                        //Operate on attributes 
                        //System.out.println("@INPUTS");
                        //srp.callInput(Data,pr);
                        callInput(Data, pr);
                        continue;
                    }
                    if (s.hasNext("@OUTPUTS") || s.hasNext("@outputs") || s.hasNext("@Outputs")) {
                        //Operate on attributes 
                        //System.out.println("@OUTPUTS");
                        //srp.callOutput(Data,pr);
                        callOutput(Data, pr);
                        continue;
                    }
                    if (s.hasNext("@DATA") || s.hasNext("@data") || s.hasNext("@Data")) {
                        //Operate on attributes 
                        System.out.println("@Data loading..............");
                        continue;
                    } else if (s.hasNext()) {
                        if (arff) {
                            //srp.callDataColl(Data,pr);
                            callDataColl(Data, pr);
                        } else {
                            s = s.useDelimiter("\\s*,\\s*");
                            while (s.hasNext()) {
                                String valString = s.next();
                                pr.print(valString + " ");
                            }
                            pr.println();
                        }
                        example++;
                    }
                }
            }
            pr.close();
            brProb.close();
            loadData(absolutepath + "normalizedData.csv");
            //System.out.println("Problem has "+example+ " examples"); 
            //srp.runProblem();	   
        } catch (Exception e) {
            System.out.println("Error " + e);
            //System.exit(0);
            JOptionPane.showMessageDialog((Component) currentFrame, "Error accessing file:" + e + "!\n Check/Close if the file open/exists");
            return;
        }//try- catch
    }

    public void loadMISCforrmatFile(String fileName, String absolutepath) {
        boolean arff = false;
        try {
	   //InputStreamReader in = new InputStreamReader(System.in);
            //BufferedReader br = new BufferedReader(in);

            //System.out.print("\nEnter a Problem/Training (eg. iris.dat/iris.arff :");
            String FILE_Prob = fileName;//br.readLine();

            //FileReader  fin = new FileReader("./Dataset/"+FILE_Prob);
            FileReader fin = new FileReader(FILE_Prob);
            BufferedReader brProb = new BufferedReader(fin);

            //Filtered data 
            FileWriter fwrite = new FileWriter(absolutepath + "filteredData.csv");
            PrintWriter pr = new PrintWriter(fwrite);

            int example = 0;//Integer.parseInt(br.readLine());
            Scanner s = null;
            String Data;
            //reading first line of column names
            String firstColumn = brProb.readLine();

            String delims = "[, ]+";
            String[] tokens = firstColumn.split(delims);
            System.out.println("Total Attrributes: " + tokens.length);
            for (int i = 0; i < tokens.length - 1; i++) {
                inputName.add(tokens[i]);
                System.out.println("Intput " + i + " " + inputName.get(i));
            }
            outputName.add(tokens[tokens.length - 1]);
            System.out.println("Output " + 0 + " " + outputName.get(0));

            while ((Data = brProb.readLine()) != null) {
                if (Data.isEmpty()) {
                    continue;
                } else {
                    pr.println(Data);
                    example++;
                }
            }
            dataSetLength = example;
            pr.close();
            fwrite.close();
            fin.close();
            brProb.close();

            int attrLength = inputName.size() + outputName.size();
            double[] attrMin = new double[attrLength];
            double[] attrMax = new double[attrLength];
            x = new double[example][inputName.size()];
            y = new double[example][outputName.size()];
            //read filtered file
            FileReader finNew = new FileReader(absolutepath + "filteredData.csv");
            BufferedReader brProbNew = new BufferedReader(finNew);
            int exampleIndex = 0;
            while ((Data = brProbNew.readLine()) != null) {
                delims = "[, ]+";
                tokens = Data.split(delims);
                System.out.print(exampleIndex + " : ");
                int j = 0;
                for (; j < tokens.length; j++) {
                    if (isNumeric(tokens[j])) {
                        double attrVal = (double) Double.parseDouble(tokens[j]);
                        //find min max of attribute
                        if (exampleIndex == 0) {
                            attrMin[j] = attrVal;
                            attrMax[j] = attrVal;
                        } else {
                            if (attrVal < attrMin[j]) {
                                attrMin[j] = attrVal;
                            }//if min
                            if (attrVal > attrMax[j]) {
                                attrMax[j] = attrVal;
                            }//if min
                        }//else
                        //store minmax value
                        if (j < tokens.length - 1) {
                            x[exampleIndex][j] = attrVal;
                            System.out.print(x[exampleIndex][j] + " ");
                        } else if (j == tokens.length - 1) {
                            y[exampleIndex][0] = (double) Double.parseDouble(tokens[tokens.length - 1]);
                            System.out.print(y[exampleIndex][0] + " ");
                        }//if j == tokens.length - 1
                    } else {//if numeric attribute
                        //System.exit(0);
                        JOptionPane.showMessageDialog((Component) currentFrame, "Error file reading: \n Suggetion: For string valued attribute use ARRF formte");
                        return;
                    }
                }//for                
                System.out.println();
                exampleIndex++;
            }//while
            finNew.close();
            brProbNew.close();

            //stored attribute information
            for (int j = 0; j < attrLength; j++) {
                attrCls = new AttrClass();
                if (j < attrLength - 1) {
                    attrCls.setAttrName(inputName.get(j).toString());
                } else if (j == tokens.length - 1) {
                    attrCls.setAttrName(outputName.get(0).toString());
                }
                double[] range = new double[2];
                range[0] = attrMin[j];
                range[1] = attrMax[j];
                attrCls.setAttrRange(range);
                attrCls.setAttrType("real");
                System.out.println("Attribute " + j + " " + range[0] + " - " + range[1]);
                attrVector.add(attrCls);
            }

            System.out.println("\nNormalized dataset ");
            //Normalized data
            FileWriter fwriteNorm = new FileWriter(absolutepath + "normalizedData.csv");
            PrintWriter prNorm = new PrintWriter(fwriteNorm);
            for (int i = 0; i < exampleIndex; i++) {
                System.out.print(i + " : ");
                for (int j = 0; j < attrLength; j++) {
                    if (j < attrLength - 1) {
                        x[i][j] = normalize(x[i][j], attrMin[j], attrMax[j], normalizedLow, normalizedHigh);
                        System.out.print(x[i][j] + " ");
                        prNorm.print(x[i][j] + ",");
                    } else if (j == attrLength - 1) {
                        y[i][0] = normalize(y[i][0], attrMin[j], attrMax[j], normalizedLow, normalizedHigh);
                        System.out.print(y[i][0] + " ");
                        prNorm.print(y[i][0] + ",");
                    }//output
                }//for all attribute   
                System.out.println();
                prNorm.println();
            }//for all example
            prNorm.close();
            fwriteNorm.close();
            System.out.println("Problem has " + example + " examples\n");
            //srp.runProblem();	   
        } catch (IOException | NumberFormatException | HeadlessException e) {
            System.out.println("Error " + e);
            //System.exit(0);
            JOptionPane.showMessageDialog((Component) currentFrame, "Error accessing file:" + e + "!\n Check/Close if the file open/exists");
            return;
        }//try- catch
    }

    //public static void main(String args[]){
    public void loadDataRegressionClassification(String path, String fileName, Object obj) {
        //LoadDataRegClass srp = new LoadDataRegClass();

        String delims = "[. ]+";//
        String[] tokens = fileName.split(delims);
        if (tokens[tokens.length - 1].equalsIgnoreCase("dat") || tokens[tokens.length - 1].equalsIgnoreCase("arff")) {
            notARFF = true;
            loadARFFforrmatFile(fileName, path);
        } else {
            String fileNameNew = fileName;
            fileNameNew = fileNameNew.replace("\\", "/");
            String[] splittedFileName = fileNameNew.split("/");
            DataSet = splittedFileName[splittedFileName.length - 1];
            notARFF = false;
            System.out.print("The code for loding data exists " + notARFF);
            loadMISCforrmatFile(fileName, path);
        }

        currentFrame = obj;
    }//main or loadRegression

}//class LoadDataRegClass

