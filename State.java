import java.util.*;   
/* Weighted NFA State Class */
public abstract class State {
    int gstart, gend, lstart, lend, root;       /* codes for reachability (descendence) queries. g: for global coding ; l: for local coding */
                                                // lstart is the order in the set of states associated with the item used in compact bitset representation of the DFA state
                                                /* the root of the subtree: the begining of the itemset that contains this state */
    BitSet follow;                              /* the following items in the NFAutomaton */
        
    public State() {
        follow = new BitSet();
    }

    public abstract int getItem();

    public abstract boolean getType();

    public int getOrder() { return lstart;}

    public void setOrder(int n) { lstart = n;}
  
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

    public String toString() { return ((Integer) gstart).toString();}
}