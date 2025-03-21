import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

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
                    int r = rand3.nextInt(alphabet_size);
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