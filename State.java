import java.util.*;   
/* Weighted NFA State Class */
public class State {
    public static final Comparator<State> BY_START = new ByStart();  // comparator using start code 
    public static final Comparator<State> BY_DESC = new ByDesc();   // comparator using descendance relation
    public static final Comparator<State> BY_ROOT = new ByRoot();   // comparator using the root and then start code 
    public static final Comparator<State> BY_lSTAET = new BylStart();   // comparator using the root and then start code 
    int item;
    boolean type;                                            /* flag: the state is an itemset delimiter when is true*/
    int gstart, lstart, gend,lend;            /* codes for reachability (descendence) queries */
                                                // g: for global coding ; l: for local coding
    int weight,                                              /* the frequency of the prefix from the startstate to this state */ 
        root;                                                /* the root of the subtree: the begining of the itemset that contains this state */
    BitSet follow;                                           /* the following items in the NFAutomaton */
    //int    ord;                                              /* id in the "Compact" BitSet representing the set of states associated with the item */
    
    private static class ByStart implements Comparator<State> {  // the natural state order is based on start code used in DFA state alignment
        public int compare(State p,State q){
            return p.getStart() - q.getStart();         
        }
    }

    private static class BylStart implements Comparator<State> {  // the natural state order is based on start code used in DFA state alignment
        public int compare(State p,State q){
            return p.getOrder() - q.getOrder();         
        }
    }
    
    private static class ByDesc implements Comparator<State> {   // the second order is based on reachability relation used in support calculation 
        public int compare(State p,State q){
            if (p.getEnd() < q.getStart()) return -1;                     // p is at the left of q so is less than q
            if (p.getStart() > q.getEnd()) return +1;                     // p is at the right of q so is greater than q
            return 0;                                           // p and q have a descendance relationship, they are equal
        }                          
    }

    private static class ByRoot implements Comparator<State> {  // the root ordering followed by the natural state order (start code) used in DFA state alignment
        public int compare(State p,State q){
            if (p.getRoot() != q.getRoot()) return p.getRoot() - q.getRoot();
            else return p.getStart()- q.getStart();         
        }
    }

    public State(boolean stateType, int i) {
        item = i;
        type = stateType;
        follow = new BitSet();
        lstart = -1;
    }

    public int getOrder(){ return lstart;}

    public void setOrder(int n) { lstart = n;}

    public int getWeight() { return weight;}

    public void setWeight(int w){ weight += w;}

    public boolean getType() { return type;}
  
    public int getStart() { return gstart;}

    public void setStart(int s) { gstart = s;}

    public int getEnd() { return gend;}

    public void setEnd(int e) { gend = e;}
    public void setlEnd(int e) { lend = e;}

    public BitSet getFollow() { return follow;}

    public void setFollow(BitSet b) { follow.or(b);}

    public int getRoot() { return root;}

    public void setRoot(int r) { root = r;}

    public String toString() { return ((Integer) gstart).toString();}

    public int compare(State y,Comparator<State> c) {
        return this.compare(y, c);
    }
}