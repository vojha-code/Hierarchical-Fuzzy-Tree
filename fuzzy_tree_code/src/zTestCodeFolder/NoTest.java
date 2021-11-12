package zTestCodeFolder;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Varun
 */
public class NoTest {
    public static void main(String args[]){
        int el = 2;
        double mr = 0.2;
        double cr = 0.8;
        int N  = 20;
        int rN = N-el; 
        int xover = (int)(rN*cr);
        int mutat = rN-xover;
        int pool_size = el + 2*xover+ mutat;
        System.out.println(N);
        System.out.println(el);
        System.out.println(xover);
        System.out.println(mutat);
        System.out.println(pool_size);
        
        int mt = pool_size - el;
        System.out.println(mt);
        int ct = (int)(mt*cr/2.0);
        System.out.println(ct);
        
        
    }
}
