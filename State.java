/* Weighted NFA State base Class */

public abstract class State {
    int gstart, gend, lstart, lend, root;       /* codes for reachability (descendence) queries. g: for global coding ; l: for local coding */
                                                // lstart is the order in the set of states associated with the item used in compact bitset representation of the DFA state
                                                /* the root of the subtree: the begining of the itemset that contains this state */                                      
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

    public int getRoot() { return root;}

    public void setRoot(int r) { root = r;}

    public String toString() { return ((Integer) gstart).toString();}    
}