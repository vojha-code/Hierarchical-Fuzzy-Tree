/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Randoms;

/**
 *
 * @author ojh0009
 */
public class RandomTest {
    public static void main(String args[]){
        MersenneTwisterFast m_Random = new MersenneTwisterFast();
        JavaRand m_Random1 = new JavaRand();
        int countMin = 0;
        int countMax = 0;
        int countMin1 = 0;
        int countMax1 = 0;
        for(int i = 0;i <  5000;i++){
            int r = m_Random.nextInt(5) ;
            int r1 = m_Random1.random(5,true);
            if(r== 5){                
                countMax++;
            }else if(r == 0){
                countMin++;
            }//if
            if(r1== 5){                
                countMax1++;
            }else if(r1 == 0){
                countMin1++;
            }//if
        }//for
        System.out.println(" MT min "+countMin);
        System.out.println(" MT Max "+countMax);
        System.out.println(" JR min "+countMin1);
        System.out.println(" JR Max "+countMax1);   
        System.out.println(" Random Vector Test "+countMax1);   
        int a[] = m_Random.randomIntVector(0, 10);
        for(int i = 0; i < a.length;i++){
            System.out.println(i+" "+a[i]);
        }//for
        
    }//main    
}//class
