package zTestCodeFolder;

import java.util.ArrayList;
 
public class SimpleArrayListExample {
 
  public static void main(String[] args) {
   
    //create an ArrayList object
    ArrayList arrayList = new ArrayList();
   
    /*
       Add elements to Arraylist using
       boolean add(Object o) method. It returns true as a general behavior
       of Collection.add method. The specified object is appended at the end
       of the ArrayList.
    */
    int a = 1;
    double b = 2.0;
    int[] c = {1,2,3};
    double[] d = {1.0,2.0,3.0};
    String e = "A";
    boolean f = true;
    
    arrayList.add(a);
    arrayList.add(b);
    arrayList.add(c);
    arrayList.add(d);
    arrayList.add(e);
    arrayList.add(f);

    int a1 = (int) arrayList.get(0);
    double b1 = (double) arrayList.get(1);
    int[] c1 = (int[]) arrayList.get(2);
    double[] d1 = (double[]) arrayList.get(3);
    String e1 = (String )arrayList.get(4);
    boolean f1 = (boolean) arrayList.get(5);
    
    /*
      Use get method of Java ArrayList class to display elements of ArrayList.
      Object get(int index) returns and element at the specified index in
      the ArrayList    
    */
    System.out.println("Getting elements of ArrayList");
    //System.out.println(arrayList.get(0));
    System.out.println(a1);
    System.out.println(b1);
    for(int i = 0; i < c1.length; i++){
        System.out.print(c1[i]+" ");
    }
    System.out.println();
    for(int i = 0; i < d1.length; i++){
        System.out.print(d1[i]+" ");
    }
    System.out.println();
    System.out.println(e1);
    System.out.println(f1);

  }
}