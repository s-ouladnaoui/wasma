/* Weighted NFA item State Class */

import java.util.BitSet;

public class itemState extends State {
    int item ;                  /* The item of State */
    int weight;                 /* the frequency of the prefix from the startstate to this state */ 
    int delimiter;              /* next Delimiter State */
    BitSet follow;              /* the following items in the NFAutomaton */

    public itemState(int i) { 
        item = i; 
        follow = new BitSet();
        
    }
    
    public int getItem() { return item;}
    
    public boolean getType() { return false;}       /* flag: the state is an itemset delimiter when is true otherwise is an itemstate*/
    
    public int getDelim() { return delimiter;}

    public void setDelim(int d) { delimiter = d;}

    public int getWeight() { return weight;}

    public void setWeight(int w) { weight += w;}
    
    public BitSet getFollow() { return follow;}
    
    public void setFollow(BitSet b) { follow.or(b);}

}