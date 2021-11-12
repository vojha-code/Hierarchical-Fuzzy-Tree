/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FIS;

import Randoms.MersenneTwisterFast;
import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author ojh0009
 */
public class Genetic_Operator {

    MersenneTwisterFast m_RNG;
    private int m_MaxTreeDepth;
    private int m_funType;
    private ArrayList treeParameters;
    private MersenneTwisterFast rand;

    FuzzyFNT[] geneticOperators(MersenneTwisterFast m_Random, FuzzyFNT[] pool, double MUTATION_PROB, ArrayList treeParams) {
        m_RNG = m_Random;

        treeParameters = treeParams;
        m_MaxTreeDepth = (int) treeParameters.get(1); // int depth of tree
        m_funType = (int) treeParameters.get(3); // int  activation function type        

        //System.out.print("Create new population ");
        int N = pool.length;
        Vector child = new Vector();
        int indx = 0;
        while (indx < N) {
            double r = m_RNG.nextDouble();
            if (r < MUTATION_PROB) {//mutation  
                int parent1 = m_RNG.random(N);
                FuzzyFNT Tree = pool[parent1];
                Object newTree = mutation(Tree);
                if (newTree != null) {
                    //System.out.println(indx + " M " + newTree);
                    child.add((FuzzyFNT) newTree);
                    indx++;//increase pool size
                } else {
                    //System.out.println(newPopIndex + " M " + newTree);
                }//if
            } else { //crossover 
                int[] parents = m_RNG.randomIntVector(0, N);
                //Make sure both the parents are not the same
                int parent1 = parents[0];
                int parent2 = parents[1];
                int parent2Index = 1;
                boolean uniqueParent = true;
                while (true) {
                    if (parent2Index != N) {
                        if (!isequal(pool, parent1, parents[parent2Index])) {
                            parent2 = parents[parent2Index];//parents are not equal hense proced
                            break;
                        } else {
                            parent2Index++;//try next pares
                        }
                    } else {
                        uniqueParent = false;
                        //System.out.println("Error is no unique candidate found");
                        break;
                    }
                }//while 
                if (uniqueParent) {
                    Vector newTree = crossover(pool[parent1], pool[parent2]);
                    if (!newTree.isEmpty()) {
                        for (Object newTree1 : newTree) {
                            //System.out.println(indx + " C " + (FuzzyFNT)newTree1);
                            child.add((FuzzyFNT) newTree1);
                            indx++;
                        } //for                
                    } else {
                        //System.out.println("Empty Vector");
                    }
                }//if found
            }//if            
        }//while

        //copying child
        int offspringLength = child.size();
        FuzzyFNT[] newPopulation = new FuzzyFNT[offspringLength];
        for (int i = 0; i < child.size(); i++) {
            newPopulation[i] = (FuzzyFNT) child.get(i);
        }//for

        ////System.out.println(": Done");
        return newPopulation;
    }//fun

    private Vector crossover(FuzzyFNT first, FuzzyFNT second) {
        Vector newTree = new Vector();
        //copy tree  
        first = first.copyTree();
        second = second.copyTree();

        if (first.m_FunctChilds.isEmpty() || second.m_FunctChilds.isEmpty()) {
            //tree has no subtrees, only leaf nodes
            first = null;
            second = null;
            return newTree;
        }

        int firstSubIndex = first.m_FunctChilds.size();
        int secondSubIndex = second.m_FunctChilds.size();

        firstSubIndex = m_RNG.random(firstSubIndex);
        secondSubIndex = m_RNG.random(secondSubIndex);

        //pick subtrees
        FunctNode firstSubtree = (FunctNode) first.m_FunctChilds.get(firstSubIndex);
        FunctNode secondSubtree = (FunctNode) second.m_FunctChilds.get(secondSubIndex);

        //pick parents
        FunctNode firstParent = firstSubtree.getParentNode();
        FunctNode secondParent = secondSubtree.getParentNode();

        //replace
        firstParent.replace(firstSubtree, secondSubtree);
        secondParent.replace(secondSubtree, firstSubtree);

        //change parents
        firstSubtree.setParentNode(secondParent);
        secondSubtree.setParentNode(firstParent);

        //inspection
        first.inspectChilds();
        second.inspectChilds();

        //append to new population
        if (first.getDepth() <= m_MaxTreeDepth) {
            //newTree[newTreeIndex++] = first;
            newTree.add(first);
            //print tree for debuging
            ////System.out.print(first+": ");
            ////System.out.print("FN->"+first.m_FunctChilds.size()+"LN->"+first.m_LeafChilds.size()+" FIT->"+first.getFitness()+"\n");
            //first.printTree();	
        } else {
            first = null;
        }

        if (second.getDepth() <= m_MaxTreeDepth) {
            //newTree[newTreeIndex++] = second;
            newTree.add(second);
            //print tree for debuging
            ////System.out.print(second+": ");
            ////System.out.print("FN->"+second.m_FunctChilds.size()+"LN->"+second.m_LeafChilds.size()+" FIT->"+second.getFitness()+"\n");
            //second.printTree();	
        } else {
            second = null;
        }
        return newTree;
    }//end crossover

    private FuzzyFNT mutation(FuzzyFNT Tree) {
        Tree = Tree.copyTree();
        try {
            ////System.out.print("Mutation");
            int i = m_RNG.random(4);
            if (i == 0) {
                mutationOneLeaf(Tree);
            } else if (i == 1) {
                mutationAllLeafs(Tree);
            } else if (i == 2) {
                mutationPruning(Tree);
            } else if (i == 3) {
                mutationGrowing(Tree);
            } else {
                return null;
            }
            Tree.inspectChilds();
            ////System.out.println(": Done"+Tree.getDepth());	
            if (Tree.getDepth() < m_MaxTreeDepth) {
                //newPopulation.add(Tree);

                //print tree for debuging
                ////System.out.print(Tree+": ");
                ////System.out.print("FN->"+Tree.m_FunctChilds.size()+"LN->"+Tree.m_LeafChilds.size()+" FIT->"+Tree.getFitness()+"\n");
                //Tree.printTree();
            } else {
                Tree = null;
            }
            ////System.out.println(": Done"+Tree.getDepth());
        } catch (Exception e) {
            //System.out.print("\nError mutation:" + e);
        }
        return Tree;
    }//end mutation

    private void mutationOneLeaf(FuzzyFNT tree) {
        try {
            int index = m_RNG.random(tree.m_LeafChilds.size());
            LeafNode node = (LeafNode) tree.m_LeafChilds.get(index);
            int newInputNumber = m_RNG.random(tree.m_InputsCount);
            ////System.out.print(" mutationOneLeaf:"+index+":"+newInputNumber); 
            node.setInputNumber(newInputNumber);
        } catch (Exception e) {
            //System.out.print("mutationOneLeaf-" + e);
        }
    }//mutationOneleaf end

    private void mutationAllLeafs(FuzzyFNT tree) {
        try {
            for (Object m_LeafChild : tree.m_LeafChilds) {
                int newInputNumber = m_RNG.random(tree.m_InputsCount - 1);
                LeafNode node = (LeafNode) m_LeafChild;
                ////System.out.println(" mutationaLLLeaf:"+i+":"+newInputNumber);
                node.setInputNumber(newInputNumber);
            }
        } catch (Exception e) {
            //System.out.print("mutationAllLeafs-" + e);
        }
    }//mutationAllLeafs end

    private void mutationPruning(FuzzyFNT tree) {
        try {
            double weight = m_RNG.random();
            if (tree.m_FunctChilds.size() > 0) {
                int index = m_RNG.random(tree.m_FunctChilds.size());
                FunctNode node = (FunctNode) tree.m_FunctChilds.get(index);
                FunctNode parent = node.getParentNode();
                int inputNumber = m_RNG.random(tree.m_InputsCount - 1);
                LeafNode newLeafNode = new LeafNode(weight, inputNumber, parent);
                parent.removeAndReplace(node, newLeafNode);
            }
        } catch (Exception e) {
            //System.out.print("mutationPruning-" + e);
        }
    }//mutationPruning ends

    private void mutationGrowing(FuzzyFNT tree) {
        try {
            ////System.out.print("\n mutationGrowing:");
            int maxDepth = 2;
            int index = tree.m_LeafChilds.size();
            index = m_RNG.random(index);
            LeafNode node = (LeafNode) tree.m_LeafChilds.get(index);
            FunctNode parent = node.getParentNode();
            ////System.out.print("--------->"+index+"----->"+node.m_InputNumber);//"------>"+parent.getArity());
            parent.removeAndGrow(node, maxDepth, tree.m_InputsCount, treeParameters);
        } catch (Exception e) {
            //System.out.print("mutationGrowing-" + e);
        }
    }//mutationGrowing end

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

    private FuzzyFNT reproduction(FuzzyFNT indv) {
        ////System.out.print("Reproduction-");
        //FNT indv = tournamentSelection(tree, TOURNAMENT_SIZE);
        indv = indv.copyTree();
        //newPopulation.add(indv);

        //print tree for debuging
        ////System.out.print(indv+": ");
        ////System.out.print("FN->"+indv.m_FunctChilds.size()+"LN->"+indv.m_LeafChilds.size()+" FIT->"+indv.getFitness());
        //indv.printTree();
        return indv;
    }//end reproduction

    /*       //Elitism: Copy the fittest individual to new population
     int numElite = (int) (ELITISM * N);
     int eliteIndex = 0;
     do {
     newPopulation[newPopIndex] = tree[eliteIndex].copyTree();
     //print tree for debuging
     //System.out.print("\nSaved Elite   :" + tree[eliteIndex] + " > ");
     //System.out.print(newPopulation[newPopIndex] + ": ");
     //System.out.printf("%.3f", newPopulation[newPopIndex].getFitness());
     //System.out.print(" " + newPopulation[newPopIndex].size());
     //System.out.print(" " + newPopulation[newPopIndex].getRank());
     //System.out.println(" " + newPopulation[newPopIndex].getDistance());
     newPopIndex++;
     eliteIndex++;
     } while (eliteIndex < numElite); */
    FuzzyFNT[] newPopulation(MersenneTwisterFast m_Random, FuzzyFNT[] x, ArrayList gpParameters, ArrayList treeParams) {
        m_RNG = m_Random;

        treeParameters = treeParams;
        m_MaxTreeDepth = (int) treeParameters.get(1); // int depth of tree
        m_funType = (int) treeParameters.get(3); // int  activation function type  
        
        rand = m_Random;
        int N = x.length;
        int ELITISM = (int) gpParameters.get(1);//double GP Elitism
        double CROSSOVER_PROB = (double) gpParameters.get(3);//double GP Crossover
        int TOURNAMENT_SIZE = (int) gpParameters.get(4);//int GP Tournament

        int rN = N - ELITISM;
        int xover = (int) (rN * CROSSOVER_PROB);
        int mutat = rN - xover;
        int pool_size = ELITISM + 2 * xover + mutat;
        //FNT[] matingPool = new FuzzyFNT[pool_size];

        //Until the mating pool is filled, perform tournament selection
        int i = 0;//ppol index
        ArrayList child = new ArrayList();
        FuzzyFNT[] sortedPOP = sorting_SO_Population(x);
        for (int j = 0; j < ELITISM; j++) {
            //matingPool[i++] = sortedPOP[j];
            child.add(sortedPOP[j]);
            i++;
            //System.out.println(" "+i);
        }
        while (i < pool_size) {//select 2*xover + mutat individuals
            if (i < ELITISM + 2 * xover) {//for crossover
                int[] candidate = randomIntVector(0, N);
                double[] c_obj_rank = new double[TOURNAMENT_SIZE];
                //Collect information about the selected candidates.
                for (int j = 0; j < TOURNAMENT_SIZE; j++) {
                    //System.out.print(" " + candidate[j]);
                    c_obj_rank[j] = x[candidate[j]].getFitness();
                }//for j           
                int[] minRanks = min(c_obj_rank); //System.out.print("  :");for (int j = 0; j < minRanks.length; j++) { System.out.print(" " + candidate[minRanks[j]]);}
                //check is more than one candidates have
                int parent1 = candidate[minRanks[0]];
                //matingPool[i] = x[parent1];
                FuzzyFNT tree1 = x[parent1];
                //System.out.println(" Tree 1 "+tree1.getFitness());
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
                }//while a distinct candiadete found
                //matingPool[i] = x[parent2];
                FuzzyFNT tree2 = x[parent2];
                //System.out.println(" Tree 2 "+tree2.getFitness());
                Vector newTree = crossover(tree1, tree2);
                if (!newTree.isEmpty()) {
                    for (Object newTree1 : newTree) {
                        //System.out.println(indx + " C " + (FuzzyFNT)newTree1);
                        child.add((FuzzyFNT) newTree1);
                        i++;
                    } //for  
                }
                //System.out.println(" "+i);
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
                //matingPool[i] = x[parent1];
                FuzzyFNT Tree = x[parent1];
                Object newTree = mutation(Tree);
                if (newTree != null) {
                    //System.out.println(indx + " M " + newTree);
                    child.add((FuzzyFNT) newTree);
                    i++;//increase pool size
                } else {
                    //System.out.println(newPopIndex + " M " + newTree);
                }//if
                //System.out.println(" "+i);
            }
            //System.out.println();
        }//while
        
        //System.out.println("Meating Pool Finished Length" + pool_size + " = " + i);
        //copying child
        int offspringLength = child.size();
        FuzzyFNT[] newPopulation = new FuzzyFNT[offspringLength];
        for (i = 0; i < child.size(); i++) {
            newPopulation[i] = (FuzzyFNT) child.get(i);
        }//for

        ////System.out.println(": Done");
        return newPopulation;
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
}//class
