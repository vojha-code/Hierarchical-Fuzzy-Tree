/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FIS;

import MISC.FramesCall;
import Randoms.MersenneTwisterFast;
import java.util.Vector;

/**
 *
 * @author ojh0009
 */
public class MiscOperations {

    public void display(FuzzyFNT[] tree) {
        int i = 0;
        for (FuzzyFNT tree1 : tree) {
            System.out.print((i++) + ": ");
            System.out.print(tree1);
            System.out.print("  (" + tree1.getParametersCount() + ") :");
            System.out.printf(" %.3f", tree1.getFitness());
            System.out.print("  " + tree1.getSize());
            System.out.print("  " + tree1.getDiversity());
            System.out.print("   " + tree1.getRank());
            System.out.printf("  %.3f \n", tree1.getDistance());
            //tree1.printTree();
        }//for
    }//end Display FuzzyFNT

    public void displaySO(FuzzyFNT[] tree) {
        int i = 0;
        for (FuzzyFNT tree1 : tree) {
            System.out.print((i++) + ": ");
            System.out.print(tree1);
            System.out.print("  (" + tree1.getParametersCount() + ") :");
            System.out.printf(" %.3f", tree1.getFitness());
            System.out.print("  " + tree1.getSize());
            System.out.println("  " + tree1.getDiversity());
        }//for
    }//end Display FuzzyFNT

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

    FuzzyFNT[] mergePopulation(FuzzyFNT[] mainTree, FuzzyFNT[] offspringTree) {
        int mN = mainTree.length;
        int N = mN + offspringTree.length;

        FuzzyFNT[] newTree = new FuzzyFNT[N];
        for (int i = 0; i < N; i++) {
            if (i < mN) {
                newTree[i] = mainTree[i];
            } else {
                newTree[i] = offspringTree[i - mN];
            }//if
        }//for i
        return newTree;
    }//fin: mergeTree

    class Individual {

        int n;
        Vector p;

        Individual() {
            p = new Vector();
        }

        void set_p(Vector val) {
            p.addAll(val);
        }

        Vector get_p() {
            return p;
        }
    }//class: Individual

    FuzzyFNT[] nonDominationSort(FuzzyFNT[] x) {
        int N = x.length;
        int obj = 3;
        double[][] xObj = new double[N][obj];
        for (int i = 0; i < N; i++) {
            xObj[i][0] = x[i].getFitness();
            xObj[i][1] = x[i].getSize();
            xObj[i][2] = x[i].getDiversity();
        }
        //int frontNum = 1;
        int front = 0;
        Individual[] individual = new Individual[N];
        Vector F = new Vector();
        Vector f = new Vector();
        //Non-Dominated sort. 
        for (int i = 0; i < N; i++) {
            individual[i] = new Individual();
            // Number of individuals that dominate this individual
            individual[i].n = 0;
            //Individuals which this individual dominate
            Vector d = new Vector();
            for (int j = 0; j < N; j++) {
                int dom_less = 0;
                int dom_equl = 0;
                int dom_more = 0;
                for (int k = 0; k < obj; k++) {
                    if (xObj[i][k] < xObj[j][k]) {
                        dom_less = dom_less + 1;
                    } else if (xObj[i][k] == xObj[j][k]) {
                        dom_equl = dom_equl + 1;
                    } else {
                        dom_more = dom_more + 1;
                    }//if 
                }// for k
                if (dom_less == 0 && dom_equl != obj) {
                    // Number of individuals that dominate this individual                    
                    individual[i].n = individual[i].n + 1;
                } else if (dom_more == 0 && dom_equl != obj) {
                    //Individuals which this individual dominate
                    ////System.out.print(" "+j);
                    d.add(j);
                }
            }// for j
            individual[i].set_p(d);
            ////System.out.println();
            if (individual[i].n == 0) {
                x[i].setRank(front);
                f.add(i);
            }//if
        }// for i 
        //checking individuals
        /*for (int i = 0; i < N; i++) {
         // Number of individuals that dominate this individual
         ////System.out.println("Dominated: "+individual[i].n);
         //Individuals which this individual dominate
         //System.out.print("Dominates:");
         Vector p = (Vector)individual[i].get_p();
         for (int j = 0; j < p.size(); j++) {
         //System.out.print(" "+p.get(j));
         }// for j
         //System.out.println();
         }// for i*/

        F.add(front, f);
        // Find the subsequent fronts        
        for (int i = 0; i < F.size() && !((Vector) F.get(front)).isEmpty(); i++) {
            //System.out.print("Fornt" + " " + front + ": " + ((Vector) F.get(front)).size() + " >");
            f = new Vector();
            for (int j = 0; j < ((Vector) F.get(front)).size(); j++) {
                int indx = Integer.parseInt((((Vector) F.get(front))).get(j).toString());
                //System.out.print(" " + indx);
                if (!individual[indx].get_p().isEmpty()) {
                    for (int k = 0; k < individual[indx].get_p().size(); k++) {
                        int indIndex = Integer.parseInt(individual[indx].get_p().get(k).toString());
                        individual[indIndex].n = individual[indIndex].n - 1;
                        if (individual[indIndex].n == 0) {
                            x[indIndex].setRank(front + 1);
                            f.add(indIndex);
                        }//if                        
                    }//for k
                }//if
            }//for j
            front = front + 1;
            F.add(front, f);
            //System.out.println();
        }//for i 
        //Checking if any indivial has not been picked in front        
        f = new Vector();
        for (int i = 0; i < N; i++) {
            if (x[i].getRank() == -1) {
                x[i].setRank(front);
                f.add(i);
            }//if
        }//for  
        //front = front + 1;
        F.add(front, f);
        //System.out.print("Fornt" + " " + front + ": " + ((Vector) F.get(front)).size() + " >");
        for (int j = 0; j < ((Vector) F.get(front)).size(); j++) {
            int indx = Integer.parseInt((((Vector) F.get(front))).get(j).toString());
            //System.out.print(" " + indx);
        }//for j
        //System.out.println("\n");
        //display(x);
        //System.out.println("Front Based Sorting");
        //sorting according to front
        //copy rank
        double[] rank = new double[N];
        for (int i = 0; i < N; i++) {
            rank[i] = x[i].getRank();
        }//for
        int[] index_of_Fronts = sort2DarrayCol(rank);
        FuzzyFNT[] sorted_based_on_fronts = sort2DArray(x, index_of_Fronts);
        //display(sorted_based_on_fronts);
        //x = sort(x, dim, obj);

        //computing crrowding distance
        //System.out.println("Crowding distance computation:");
        int current_index = 0;
        FuzzyFNT[] z = new FuzzyFNT[N];
        for (int i = 0; i < F.size() && !((Vector) F.get(i)).isEmpty(); i++) {
            //System.out.print("Font" + i + ":");
            int length_Fi = ((Vector) F.get(i)).size();
            FuzzyFNT[] y = new FuzzyFNT[length_Fi];
            double[][] objVal = new double[length_Fi][obj];
            int previous_index = current_index;
            int pointer = 0;
            for (int j = 0; j < length_Fi; j++) {
                int indexInto = current_index + j;
                //System.out.print(" (" + j + "):" + indexInto);
                y[j] = sorted_based_on_fronts[indexInto];
                pointer++;
            }//for j 
            current_index = current_index + pointer;
            for (int j = 0; j < obj; j++) {
                //copy obj j
                double[] jObj = new double[length_Fi];
                if (j == 0) {
                    for (int k = 0; k < length_Fi; k++) {
                        jObj[k] = y[k].getFitness();
                    }//for k
                } else if (j == 1) {
                    for (int k = 0; k < length_Fi; k++) {
                        jObj[k] = y[k].getSize();
                    }//for k
                } else if (j == 2) {
                    for (int k = 0; k < length_Fi; k++) {
                        jObj[k] = y[k].getDiversity();
                    }//for k
                } //if   
                int[] index_of_obj = sort2DarrayCol(jObj);
                FuzzyFNT[] sorted_based_on_objectives = sort2DArray(y, index_of_obj);
                double f_max = 0.0;
                double f_min = 0.0;
                if (j == 0) {
                    f_max = sorted_based_on_objectives[length_Fi - 1].getFitness();
                    f_min = sorted_based_on_objectives[0].getFitness();
                } else if (j == 1) {
                    f_max = sorted_based_on_objectives[length_Fi - 1].getSize();
                    f_min = sorted_based_on_objectives[0].getSize();
                } else if (j == 2) {
                    f_max = sorted_based_on_objectives[length_Fi - 1].getDiversity();
                    f_min = sorted_based_on_objectives[0].getDiversity();
                }//if 
                ////System.out.println(f_max + " " + f_min);                
                objVal[length_Fi - 1][j] = Double.POSITIVE_INFINITY;//inifinity
                objVal[0][j] = Double.POSITIVE_INFINITY;//inifinity
                for (int k = 1; k < length_Fi - 1; k++) {
                    double next_obj = 0.0;
                    double previous_obj = 0.0;
                    if (j == 0) {
                        next_obj = sorted_based_on_objectives[k + 1].getFitness();
                        previous_obj = sorted_based_on_objectives[k - 1].getFitness();
                    } else if (j == 1) {
                        next_obj = sorted_based_on_objectives[k + 1].getSize();
                        previous_obj = sorted_based_on_objectives[k - 1].getSize();
                    } else if (j == 2) {
                        next_obj = sorted_based_on_objectives[k + 1].getDiversity();
                        previous_obj = sorted_based_on_objectives[k - 1].getDiversity();
                    } //if 
                    if (f_max - f_min == 0.0) {
                        objVal[k][j] = Double.POSITIVE_INFINITY;//inifinity
                    } else {
                        objVal[k][j] = (next_obj - previous_obj) / (f_max - f_min);
                        //System.out.printf("%.3f, %.3f, %.3f, %.3f, %.3f\n",next_obj,previous_obj,f_max,f_min,objVal[k][j]);
                    }//if                 
                }//for k
            }//for j
            double[] distance = new double[length_Fi];
            for (int j = 0; j < length_Fi; j++) {
                distance[j] = 0.0;
                for (int k = 0; k < obj; k++) {
                    distance[j] = distance[j] + objVal[j][k];
                }//for k                
                y[j].setDistance(distance[j]);
            }//for j 
            //copy y to z
            for (int j = 0; j < length_Fi; j++) {
                z[previous_index++] = y[j];
            }//for j 
            //System.out.println();
        }//for i
        System.arraycopy(z, 0, x, 0, N); //for j 
        //System.out.println();
        //display(x);
        return x;
    }//fun : non - dominant
    //Sorting ----------------------------------------------------

    public int[] sort2DarrayCol(double[] x) {
        int N = x.length;
        int[] indices = new int[N];
        indices[0] = 0;
        for (int i = 1; i < N; i++) {
            int j = i;
            for (; j >= 1 && x[j] < x[j - 1]; j--) {
                double temp = x[j];
                x[j] = x[j - 1];
                indices[j] = indices[j - 1];
                x[j - 1] = temp;
            }//for j
            indices[j] = i;
        }//for i
        /*for (int i = 0; i < N; i++) {
         //System.out.print(" " + indices[i]);
         }*/
        return indices;//indices of sorted elements
    }//inster sort

    private FuzzyFNT[] sort2DArray(FuzzyFNT[] array, int[] index) {
        int N = array.length;
        FuzzyFNT[] sortedArray = new FuzzyFNT[N];
        for (int i = 0; i < N; i++) {
            sortedArray[i] = array[index[i]];
        }//for i
        return sortedArray;
    }//sort 2D array 

    double[][] intermediate_population(double[][] main_population, double[][] offspring_population) {
        int mN = main_population.length;
        int D = main_population[0].length;
        int oN = offspring_population.length;
        double[][] intermediate_population = new double[mN + oN][D];
        for (int i = 0; i < mN + oN; i++) {
            for (int j = 0; j < D; j++) {
                if (i < mN) {
                    intermediate_population[i][j] = main_population[i][j];
                } else {
                    intermediate_population[i][j] = offspring_population[i - mN][j];
                }//if 
            }//for j
        }//for i
        return intermediate_population;
    }//fun: intermediate opulation

    FuzzyFNT[] replace_chromosome_SO(FuzzyFNT[] tree, int pop) {
        int N = tree.length;
        int obj = 2;
        FuzzyFNT[] replaced_pop = new FuzzyFNT[pop];
        //copy rank
        double[] fitness = new double[N];
        for (int i = 0; i < N; i++) {
            fitness[i] = tree[i].getFitness();
        }//for
        int[] fitness_index = sort2DarrayCol(fitness);
        FuzzyFNT[] sorted_based_on_fitness = sort2DArray(tree, fitness_index);
        //System.out.println("Rank based Sorting :Replace");
        //display(sorted_based_on_ranks);
        System.arraycopy(sorted_based_on_fitness, 0, replaced_pop, 0, pop); ////System.out.println(rank[i]);
        //for        
        return replaced_pop;
    }//fun: replace SO

    FuzzyFNT[] replace_chromosome(FuzzyFNT[] tree, int pop) {
        int N = tree.length;
        int obj = 2;
        FuzzyFNT[] replaced_pop = new FuzzyFNT[pop];
        //copy rank
        double[] rank = new double[N];
        for (int i = 0; i < N; i++) {
            rank[i] = tree[i].getRank();
        }//for
        int[] rank_index = sort2DarrayCol(rank);
        FuzzyFNT[] sorted_based_on_ranks = sort2DArray(tree, rank_index);
        //System.out.println("Rank based Sorting :Replace");
        //display(sorted_based_on_ranks);
        for (int i = 0; i < N; i++) {
            rank[i] = sorted_based_on_ranks[i].getRank();
            ////System.out.println(rank[i]);
        }//for   
        int max_rank = maxRank(rank);
        int previous_index = 0;
        for (int i = 0; i <= max_rank; i++) {
            //Get the index for current rank i.e the last the last element in the
            //sorted_chromosome with rank i. 
            int current_index = findMax(sorted_based_on_ranks, i);
            //Check to see if the population is filled if all the individuals with
            //rank i is added to the population. 
            //System.out.print(current_index + "]");
            if (current_index > pop) {
                //If so then find the number of individuals with in with current rank i.
                int remaining = pop - previous_index;
                //System.out.print(">" + remaining + ">");
                //Get information about the individuals in the current rank i.
                int temp_length = current_index - previous_index;
                FuzzyFNT[] temp_pop = new FuzzyFNT[temp_length];
                int idx_temp = 0;
                int copy_index = previous_index;
                double[] dist = new double[temp_length];
                for (; copy_index < current_index; copy_index++) {
                    temp_pop[idx_temp] = sorted_based_on_ranks[copy_index];
                    dist[idx_temp] = sorted_based_on_ranks[copy_index].getDistance();
                    idx_temp++;
                }//for 
                //ort the individuals with rank i in the descending order based on
                //the crowding distance.

                int[] indexDist = sort2DarrayColDecend(dist);
                temp_pop = sort2DArray(temp_pop, indexDist);
                for (int j = 0; j < remaining; j++) {
                    replaced_pop[previous_index] = temp_pop[j];
                    ////System.out.print((previous_index)+" ");
                    previous_index++;
                }//for
                ////System.out.println();
            } else if (current_index < pop) {
                //copy population with rank i
                for (; previous_index <= current_index; previous_index++) {
                    replaced_pop[previous_index] = sorted_based_on_ranks[previous_index];
                    ////System.out.print((previous_index)+" ");
                }//for
                ////System.out.println();
            } else if (current_index == pop) {
                //copy all population with rank i
                for (; previous_index < current_index; previous_index++) {
                    replaced_pop[previous_index] = sorted_based_on_ranks[previous_index];
                    ////System.out.print((previous_index)+" ");
                }//for
                ////System.out.println();
            }//if
        }//for        
        return replaced_pop;
    }//fun: replace

    int maxRank(double[] x) {
        int N = x.length;
        int rank = (int) x[0];
        for (int i = 1; i < N; i++) {
            if (x[i] > rank) {
                rank = (int) x[i];
            }//if
        }//for
        return rank;
    }//fun rank

    int findMax(FuzzyFNT[] x, int rank) {
        int N = x.length;
        int index = 0;
        for (int i = 0; i < N; i++) {
            if ((int) x[i].getRank() == rank) {
                index = i;
            }//if
        }//for
        return index;
    }//fun rank

    public int[] sort2DarrayColDecend(double[] x) {
        int N = x.length;
        int[] indices = new int[N];
        indices[0] = 0;
        for (int i = 1; i < N; i++) {
            int j = i;
            for (; j >= 1 && x[j] > x[j - 1]; j--) {
                double temp = x[j];
                x[j] = x[j - 1];
                indices[j] = indices[j - 1];
                x[j - 1] = temp;
            }//for j
            indices[j] = i;
        }//for i
        /*for (int i = 0; i < N; i++) {
         //System.out.print(" " + indices[i]);
         }*/
        return indices;//indices of sorted elements
    }//inster sort
    
    
  private boolean isequal(FuzzyFNT Tree1, FuzzyFNT Tree2) {
        boolean equal = true;
        double[] tree1 = new double[7];
        double[] tree2 = new double[7];
        tree1[0] = Tree1.getFitness();
        tree1[1] = Tree1.getSize();
        tree1[2] = Tree1.m_FunctChilds.size();
        tree1[3] = Tree1.m_LeafChilds.size();
        tree1[4] = Tree1.m_rank;
        tree1[5] = Tree1.m_dist;
        tree1[6] = Tree1.getDiversity();

        tree2[0] = Tree2.getFitness();
        tree2[1] = Tree2.getSize();
        tree2[2] = Tree2.m_FunctChilds.size();
        tree2[3] = Tree2.m_LeafChilds.size();
        tree2[4] = Tree2.m_rank;
        tree2[5] = Tree2.m_dist;
        tree1[6] = Tree2.getDiversity();

        for (int i = 0; i < 2; i++) {
            //System.out.print(" < " + tree1[i] + " " + tree2[i] + " >");
            if (tree1[i] != tree2[i]) {
                equal = false;
                break;
            }//if
        }//for
        //System.out.println("....................................................");
        return equal;
    }//faun: isqual
  
    FuzzyFNT[] replaceWithBestDistinct(FuzzyFNT[] tree, int pop) {
        //int[] indexF = FramesCall.selectIndividuals(tree, pop);
        System.out.println("Replacing With the Best Tree");
        int N = tree.length;
        int obj = 2;
        FuzzyFNT[] replaced_pop = new FuzzyFNT[pop];
        //copy rank
        double[] fitness = new double[N];
        for (int i = 0; i < N; i++) {
            fitness[i] = tree[i].getFitness();
        }//for
        int[] fitness_index = sort2DarrayCol(fitness);
        FuzzyFNT[] sorted_based_on_fitness = sort2DArray(tree, fitness_index);
        //System.out.println("Rank based Sorting :Replace");
        //display(sorted_based_on_ranks);
        //System.arraycopy(sorted_based_on_fitness, 0, replaced_pop, 0, pop); ////System.out.println(rank[i]);
        int indexBest = 0;
        replaced_pop[indexBest] = sorted_based_on_fitness[indexBest].copyTree();
        //bestFitness[indexBest] = sorted_based_on_fitness[indexBest].getFitness();
        System.out.println("Replaced With the Best Tree :> "+sorted_based_on_fitness[indexBest].getFitness());
        indexBest++;//increased by one
        for (int i = 1; i < N; i++) {
            boolean foundSimilar = false;
            for (int search = 0; search < indexBest; search++) {
                if (isequal(replaced_pop[search],sorted_based_on_fitness[i])){
                    foundSimilar = true;
                    break;
                }//if
            }//for search
            
            if (!foundSimilar && indexBest < pop) {
                replaced_pop[indexBest] = sorted_based_on_fitness[indexBest].copyTree();               
                indexBest++;
            }//if
        }//for 
        MersenneTwisterFast m_random = new MersenneTwisterFast();
        if (indexBest < pop) {
            System.out.println("  No more distinct trea:  Copying random trees");
            for (int fill = indexBest; fill < pop; fill++) {
                int randomIndex =  m_random.nextInt(N-1);
                replaced_pop[fill] = sorted_based_on_fitness[randomIndex].copyTree();
            }//for
        }//best possible distinct structure found
        return replaced_pop;
    }//best Distinct
}//class
