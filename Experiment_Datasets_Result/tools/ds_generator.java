import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
/*

This module is a random generator of sequential dataset folowwing the spmf format.
It admits five parameters:
    1. The alphabet size (a) or the number (positive integer) of items used in the generation
    2. The max length for itemsets (i): if positive we generate itemsets where the length is between 1 and i
                                        otherwise the length is fixed to i   
    3. The max size for a sequence in terms of the number of itemsets composing the sequence:   
                                        if positive we generate sequences where the size is is between 1 and s
                                        otherwise the sequence size is fixed to s  
    4. The size of the dataset: represent the number of transations (rows) of the generated dataset
    5. The name of the output file 
    Example
    java ds_generator 100 4 10 1000 ds1k => generates a random sequence dataset:
                                            over 100 items 
                                            where the itemset length belongs randomly to [1,4]
                                            the number of itemsets per transaction randomly peeked from [1,10]
                                            containing 1k transaction called ds1k
    java ds_generator 1000 -2 -8 10000 ds10k => generates a random sequence dataset:
                                            over 1000 items 
                                            where the itemset length is exactly 2 for the entire dataset
                                            the number of itemsets per transaction is fixed to 8
                                            containing 10k transaction called ds10k
    */

public class ds_generator {
public static void main(String[] args) throws IOException  {
        int itemsetDelimiter= -1;
        int transactionDelimiter = -2;
        int  alphabet_size = Integer.parseInt(args[0]);
        int  itemset_max_size = Integer.parseInt(args[1]);
        int  sequence_max_size = Integer.parseInt(args[2]);
        int  dataset_size = Integer.parseInt(args[3]);
        BufferedWriter out = new BufferedWriter(new FileWriter(args[4]));
        int i = 0, j, k;
        Random rand1 = new Random(),
               rand2 = new Random(),
               rand3 = new Random();
        ArrayList<Integer> itemset, transaction;
        String stransaction;
        while (i < dataset_size) { 
            j = 0;
            transaction = new ArrayList<Integer>();
            int sms = (sequence_max_size > 0)? rand1.nextInt(sequence_max_size)+1:-1 * sequence_max_size; 
            while (j < sms) {
                k = 0;
                itemset = new ArrayList<Integer>();
                int ims = (itemset_max_size > 0)? rand2.nextInt(itemset_max_size)+1:-1 * itemset_max_size;  
                while (k < ims) {
                    int r = rand3.nextInt(alphabet_size)+1;
                    if (!itemset.contains(r)) {
                        itemset.add(r);
                        k++;
                    }
                }
                Collections.sort(itemset);;
                itemset.add(itemsetDelimiter);
                transaction.addAll(itemset);
                j++;
            }
            transaction.add(transactionDelimiter);                
            stransaction = "";
            for (int m = 0; m < transaction.size()-1; m++){
                stransaction = stransaction.concat(Integer.toString(transaction.get(m)));
                stransaction = stransaction.concat(" ");
            }
            stransaction = stransaction.concat(Integer.toString(transactionDelimiter));    
            out.write(stransaction+"\n");
            i++;
        }
        out.close();
    }
}