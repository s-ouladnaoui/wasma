import java.util.*;   
/* Weighted NFA State Class */
public class State {
    public static final Comparator<State> BY_START = new ByStart();  // comparator using start number
    public static final Comparator<State> BY_DESC = new ByDesc(); // coparator using descendance relation
    boolean type;                                   /* flag : the state is an itemset delimiter */
    int start, end;                                 /* codes for reachability (descendence) queries */
    int weight,                                     /* the frequency of the prefix from the startstate to this state */ 
        root;                                       /* the root of the subtree: the begining of the itemset that contains this state */
    BitSet follow;                                  /* the following items in the NFAutomaton */
    int num,ord;                                    /* index in the NFA adjList and id in the set of states associated with the item */
    
    private static class ByStart implements Comparator<State>  // the natural state order is based on start code used in DFA state alignment
    {
        public int compare(State p,State q){
            return p.start - q.start;         
        }
    }
    
    private static class ByDesc implements Comparator<State>    // the second order is based on reachability relation used in support calculation 
    {
        public int compare(State p,State q){
            if (p.end < q.start) return -1;                     // p is at the left of q so is less than q
            if (p.start > q.end) return +1;                     // p is at the right of q so is greater than q
            return 0;                                           // p and q have a descendance relationship, they are equal
        }                          
    }

    public State(boolean stateType) {
        type = stateType;
        follow = new BitSet();
    }

    public int getNum(){ return num;}

    public void setNum(int n) { num = n;}

    public int getOrder(){ return ord;}

    public void setOrder(int n) { ord = n;}

    public int getWeight() { return weight;}

    public void setWeight(int w){ weight += w;}

    public boolean getType() { return type;}
  
    public int getStart() { return start;}

    public void setStart(int s) { start = s;}

    public int getEnd() { return end;}

    public void setEnd(int e) { end = e;}

    public BitSet getFollow() { return follow;}

    public void setFollow(BitSet b) { follow.or(b);}

    public int getRoot() { return root;}

    public void setRoot(int r) { root = r;}

    public String toString() { return ((Integer) start).toString();}
}
