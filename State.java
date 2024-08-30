import java.util.*;   
/* Weighted NFA State Class */
public class State {
    int item;
    boolean type;                                           /* flag: the state is an itemset delimiter when is true*/
    int gstart, gend, lstart, lend;                         /* codes for reachability (descendence) queries. g: for global coding ; l: for local coding */
                                                            // lstart is the order in the set of states associated with the item used in compact bitset representation of the DFA state
    int weight,                                             /* the frequency of the prefix from the startstate to this state */ 
        delimiter,                                          /* next Statedelimiter */
        root;                                               /* the root of the subtree: the begining of the itemset that contains this state */
    BitSet follow;                                          /* the following items in the NFAutomaton */
        
    public State(boolean stateType, int i) {
        item = i;
        type = stateType;
        follow = new BitSet();
        lstart = -1;
    }

    public int getItem() { return item;}

    public int getOrder() { return lstart;}

    public void setOrder(int n) { lstart = n;}

    public int getWeight() { return weight;}

    public void setWeight(int w) { weight += w;}

    public boolean getType() { return type;}
  
    public int getStart() { return gstart;}

    public void setStart(int s) { gstart = s;}

    public int getEnd() { return gend;}

    public void setEnd(int e) { gend = e;}

    public int getlEnd() { return lend;}

    public void setlEnd(int e) { lend = e;}

    public BitSet getFollow() { return follow;}

    public void setFollow(BitSet b) { follow.or(b);}

    public int getRoot() { return root;}

    public void setRoot(int r) { root = r;}
    
    public int getDelim() { return delimiter;}

    public void setDelim(int d) { delimiter = d;}
    
    public String toString() { return ((Integer) gstart).toString();}
}