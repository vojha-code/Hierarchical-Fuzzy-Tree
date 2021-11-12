package DataReader;

import java.util.*;
public class AttrClass {
 private String AttrName;
 private String AttrType;
 private double Range[];
 private Vector strAttrVector;

 public String getAttrName() {
 return AttrName;
 }
 public void setAttrName(String AttrName) {
 this.AttrName = AttrName;
 }
 public String getAttrType() {
 return AttrType;
 }
 public void setAttrType(String AttrType) {
 this.AttrType = AttrType;
 }
 
 public double[] getAttrRange() {
 return Range;
 }
 public void setAttrRange(double Range[]) {
 this.Range = Range;
 }
 
  public Vector getStrAttrVal() {
 return strAttrVector;
 }
 public void setStrAttrVal(Vector strAttrVector) {
 this.strAttrVector = strAttrVector;
 }
}