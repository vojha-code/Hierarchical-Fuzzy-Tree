/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FIS;

import Randoms.MersenneTwisterFast;
import java.util.ArrayList;

/**
 *
 * @author ojh0009
 */
public class TournamentSelection {

    MersenneTwisterFast rand;

    FuzzyFNT[] tournament_selection_SO(MersenneTwisterFast m_Random, FuzzyFNT[] x, int pool_size, int tour_size) {
        rand = m_Random;
        int N = x.length;
        FuzzyFNT[] matingPool = new FuzzyFNT[pool_size];
        //Until the mating pool is filled, perform tournament selection
        for (int i = 0; i < pool_size; i++) {
            //Select n individuals at random, where n = tour_size
            int[] candidate = randomIntVector(0, N);
            double[] c_obj_rank = new double[tour_size];
            //Collect information about the selected candidates.
            for (int j = 0; j < tour_size; j++) {
                //System.out.print(" " + candidate[j]);
                c_obj_rank[j] = x[candidate[j]].getFitness();
            }//for j           
            int[] minRanks = min(c_obj_rank); //System.out.print("  :");for (int j = 0; j < minRanks.length; j++) { //System.out.print(" " + candidate[minRanks[j]]);}
            //check is more than one candidates have
            if (minRanks.length > 1) {
                //check crowding distance
                int maxDist = rand.random(minRanks.length);
                matingPool[i] = x[candidate[minRanks[maxDist]]];
            } else {
                matingPool[i] = x[candidate[minRanks[0]]];
            }//if 
            //System.out.println();
        }//for i  
        //System.out.println("Meating Pool Finished");
        //System.exit(0);
        return matingPool;
    }//fun: tournament SO

    FuzzyFNT[] tournament_selection(MersenneTwisterFast m_Random, FuzzyFNT[] x, int pool_size, int tour_size) {
        rand = m_Random;
        int N = x.length;
        FuzzyFNT[] matingPool = new FuzzyFNT[pool_size];
        //Until the mating pool is filled, perform tournament selection
        for (int i = 0; i < pool_size; i++) {
            //Select n individuals at random, where n = tour_size
            int[] candidate = rand.randomIntVector(0, N);
            double[] c_obj_rank = new double[tour_size];
            double[] c_obj_distance = new double[tour_size];
            //Collect information about the selected candidates.
            for (int j = 0; j < tour_size; j++) {
                //System.out.print(" " + candidate[j]);
                c_obj_rank[j] = x[candidate[j]].getRank();
                c_obj_distance[j] = x[candidate[j]].getDistance();
            }//for j           
            int[] minRanks = min(c_obj_rank); //System.out.print("  :");for (int j = 0; j < minRanks.length; j++) { //System.out.print(" " + candidate[minRanks[j]]);}
            //check is more than one candidates have
            if (minRanks.length > 1) {
                //check crowding distance
                int[] maxDist = max(c_obj_distance, minRanks);//System.out.print("  :");for (int j = 0; j < maxDist.length; j++) {//System.out.print(" " + candidate[maxDist[j]]);}
                matingPool[i] = x[candidate[minRanks[maxDist[0]]]];
            } else {
                matingPool[i] = x[candidate[minRanks[0]]];
            }//if 
            //System.out.println();
        }//for i  
        //System.out.println("Meating Pool Finished");
        //System.exit(0);
        return matingPool;
    }//fun: tournament

    private int[] randomIntVector(int min, int max) {
        int a[] = new int[max];
        for (int i = 0; i < max; i++) {
            if (i == 0) {
                a[i] = (int) ((max - min) * Math.random() + min);
            } else {
                while (true) {
                    boolean flag = false;
                    int r = (int) ((max - min) * Math.random() + min);
                    for (int j = 0; j < i; j++) {
                        if (r == a[j]) {
                            flag = true;
                            break;
                        }//if	
                    }//for
                    if (!flag) {
                        a[i] = r;
                        break;
                    }//if   
                }//while				
            }//else 	
            ////System.out.println(i + " - " + a[i] + " ");
            //a[i] = i;//System.out.println(i+" - "+a[i]+" ");
        }//for
        return a;
    }//permutation

    private int[] min(double[] c_obj) {
        double min = c_obj[0];
        int length = c_obj.length;
        int[] minIndex = new int[length];
        minIndex[0] = 0;
        for (int i = 1; i < length; i++) {
            minIndex[i] = -1;//assigning to test others same min
            if (c_obj[i] < min) {
                min = c_obj[i];
                minIndex[0] = i;//index into objective
            }//if
        }//for i

        //check is there is same rank min exists
        for (int i = 0; i < length; i++) {
            if (i != minIndex[0]) {//not the selected index
                if (c_obj[i] == c_obj[minIndex[0]]) {//check for equal rank
                    minIndex[i] = i;//index into c_obj
                }//if
            }//if
        }//for i
        int count = 0;// cont equal ranks index
        for (int i = 0; i < length; i++) {
            if (minIndex[i] != -1) {
                count++;
            }
        }
        int[] totalSameRanks = new int[count];
        count = 0;
        for (int i = 0; i < length; i++) {
            if (minIndex[i] != -1) {
                totalSameRanks[count] = minIndex[i];
                count++;
            }//if
        }//for
        return totalSameRanks;
    }//min

    private int[] max(double[] dist, int[] rank) {
        //fileter distance
        int length = rank.length;
        double[] c_obj = new double[length];
        int count = 0;
        for (int i = 0; i < dist.length; i++) {
            if (rank[count] == i) {
                c_obj[count] = dist[i];//copy dist index
                //System.out.print(" (" + c_obj[count] + ")");
                count++;
            }//if  
        }//for        
        double max = c_obj[0];
        int[] maxIndex = new int[length];
        maxIndex[0] = 0;
        for (int i = 1; i < length; i++) {
            maxIndex[i] = -1;
            if (c_obj[i] > max) {//computing max
                max = c_obj[i];
                maxIndex[0] = i;//index into rank
                //System.out.println("New Max: "+max+" at: "+maxIndex[0]);
            }//if
        }//for i
        for (int i = 0; i < length; i++) {
            if (i != maxIndex[0]) {//not the selected index
                if (c_obj[maxIndex[0]] == c_obj[i]) {//check for equal distance
                    //System.out.println("True");
                    maxIndex[i] = i;//index of rank
                }
            }//if
        }//for i
        count = 0;
        for (int i = 0; i < length; i++) {
            if (maxIndex[i] != -1) {
                count++;
            }
        }
        //System.out.println("count"+count);
        int[] totalSameDist = new int[count];
        count = 0;
        for (int i = 0; i < length; i++) {
            if (maxIndex[i] != -1) {
                totalSameDist[count] = maxIndex[i];
                count++;
            }//if
        }//for
        return totalSameDist;
    }//max

    FuzzyFNT[] tournament_selection_SO(MersenneTwisterFast m_Random, FuzzyFNT[] x, ArrayList gpParameters) {
        rand = m_Random;
        int N = x.length;
        int ELITISM = (int) gpParameters.get(1);//double GP Elitism
        double MUTATION_PROB = (double) gpParameters.get(2);//double GP Mutation
        double CROSSOVER_PROB = (double) gpParameters.get(3);//double GP Crossover
        int TOURNAMENT_SIZE = (int) gpParameters.get(4);//int GP Tournament

        int rN = N - ELITISM;
        int xover = (int) (rN * CROSSOVER_PROB);
        int mutat = rN - xover;
        int pool_size = ELITISM + 2 * xover + mutat;
        FuzzyFNT[] matingPool = new FuzzyFNT[pool_size];

        //Until the mating pool is filled, perform tournament selection
        int i = 0;//ppol index
        FuzzyFNT[] sortedPOP = sorting_SO_Population(x);
        for (int j = 0; j < ELITISM; j++) {
            matingPool[i++] = sortedPOP[j];
        }
        while (i < pool_size) {//select 2*xover + mutat individuals
            if (i < ELITISM + 2 * xover) {

                int[] candidate = randomIntVector(0, N);
                double[] c_obj_rank = new double[TOURNAMENT_SIZE];
                //Collect information about the selected candidates.
                for (int j = 0; j < TOURNAMENT_SIZE; j++) {
                    //System.out.print(" " + candidate[j]);
                    c_obj_rank[j] = x[candidate[j]].getFitness();
                }//for j           
                int[] minRanks = min(c_obj_rank); //System.out.print("  :");for (int j = 0; j < minRanks.length; j++) { //System.out.print(" " + candidate[minRanks[j]]);}
                //check is more than one candidates have
                int parent1 = candidate[minRanks[0]];
                matingPool[i] = x[parent1];
                i++;
                //repeat for parent second parent
                candidate = randomIntVector(0, N);
                for (int j = 0; j < TOURNAMENT_SIZE; j++) {
                    //System.out.print(" " + candidate[j]);
                    c_obj_rank[j] = x[candidate[j]].getFitness();
                }//for j 
                int parent2 = candidate[minRanks[0]];
                while (true) {
                    if (!isequal(x, parent1, parent2)) {//parents are not equal
                        break;
                    } else {
                        candidate = randomIntVector(0, N);
                        for (int j = 0; j < TOURNAMENT_SIZE; j++) {
                            //System.out.print(" " + candidate[j]);
                            c_obj_rank[j] = x[candidate[j]].getFitness();
                        }//for j 
                        parent2 = candidate[minRanks[0]];
                    }
                }
                matingPool[i] = x[parent2];
                i++;
            } else {//take cadidates for mutation
                int[] candidate = randomIntVector(0, N);
                double[] c_obj_rank = new double[TOURNAMENT_SIZE];
                //Collect information about the selected candidates.
                for (int j = 0; j < TOURNAMENT_SIZE; j++) {
                    //System.out.print(" " + candidate[j]);
                    c_obj_rank[j] = x[candidate[j]].getFitness();
                }//for j           
                int[] minRanks = min(c_obj_rank); //System.out.print("  :");for (int j = 0; j < minRanks.length; j++) { //System.out.print(" " + candidate[minRanks[j]]);}
                //check is more than one candidates have
                int parent1 = candidate[minRanks[0]];
                matingPool[i] = x[parent1];
                i++;
            }
            //System.out.println();
        }//for i  
        System.out.println("Meating Pool Finished Length" + pool_size + " = " + i);
        System.exit(0);
        return matingPool;
    }//tornament selection

    public FuzzyFNT[] sorting_SO_Population(FuzzyFNT[] tree) {
        try {
            int populationSize = tree.length;
            boolean swapped = true;
            int j = 0;
            FuzzyFNT tmp = null;
            while (swapped) {
                swapped = false;
                j++;
                for (int i = 0; i < populationSize - j; i++) {
                    if (tree[i].getFitness() > tree[i + 1].getFitness()) {
                        tmp = tree[i];
                        tree[i] = tree[i + 1];
                        tree[i + 1] = tmp;
                        swapped = true;
                    }//if
                }//for i
            }//wgile
        } catch (Exception e) {
            System.out.print("\nError Sorting FNT:" + e);
        }
        return tree;
    }//fun: Sorting SO Population

    private boolean isequal(FuzzyFNT[] tree, int parent1, int parent2) {
        boolean equal = true;
        double[] tree1 = new double[7];
        double[] tree2 = new double[7];
        tree1[0] = tree[parent1].getFitness();
        tree1[1] = tree[parent1].getSize();
        tree1[2] = tree[parent1].m_FunctChilds.size();
        tree1[3] = tree[parent1].m_LeafChilds.size();
        tree1[4] = tree[parent1].m_rank;
        tree1[5] = tree[parent1].m_dist;
        tree1[6] = tree[parent1].getDiversity();

        tree2[0] = tree[parent2].getFitness();
        tree2[1] = tree[parent2].getSize();
        tree2[2] = tree[parent2].m_FunctChilds.size();
        tree2[3] = tree[parent2].m_LeafChilds.size();
        tree2[4] = tree[parent2].m_rank;
        tree2[5] = tree[parent2].m_dist;
        tree1[6] = tree[parent2].getDiversity();

        for (int i = 0; i < 4; i++) {
            //System.out.print(" < " + tree1[i] + " " + tree2[i] + " >");
            if (tree1[i] != tree2[i]) {
                equal = false;
                break;
            }//if
        }//for
        //System.out.println("....................................................");

        return equal;
    }//faun: isqual
}//class
