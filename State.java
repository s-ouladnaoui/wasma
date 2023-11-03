import java.util.*;   
/* Weighted Automaton State Class */
public class State {
    public static final Comparator<State> BY_START = new ByStart();  // comparator using start number
    public static final Comparator<State> BY_DESC = new ByDesc(); // coparator using descendance relation
    boolean type;                                    /* flag : the state is an itemset delimiter */
    TreeMap<Integer,State> transitions;              /* Immediate transition list */
    int start, end;                                  /* codes for reachability (descendence) queries */
    int weight;                                     /* the frequency of the prefix from the startstate to this state */ 
    State root; 
    BitSet follow;                                    /* the root of the subtree: the begining of the itemset that contains this state */
    
    private static class ByStart implements Comparator<State>
    {
        public int compare(State p,State q){
            return p.getStart() - q.getStart();         // the natural order of start codes
        }
    }
    
    private static class ByDesc implements Comparator<State>
    {
        public int compare(State p,State q){
            if (p.getEnd() < q.getStart()) return -1;   // p is at the left of q so is less than q
            if (p.getStart() > q.getEnd()) return +1;   // p is at the right of q so is greater than q
            return 0;        }                          // p and q have a descendance relationship, they are equal
    }
    public State(boolean stateType) {
        type = stateType;
        transitions = new TreeMap<>();
        follow = new BitSet();
    }

    public int getWeight() { return weight;}

    public void setWeight(int w){ weight += w;}

    public boolean getType() { return type;}

    public void setType(boolean t) { type = t;}
  
    protected Map<Integer, State > getTransitions() { return transitions;}

    public int getStart() { return start;}

    public void setStart(int s) { start = s;}

    public int getEnd() { return end;}

    public void setEnd(int e) { end = e;}

    public BitSet getFollow() {for (int i:transitions.keySet()) if (i >= 0) follow.set(i); return follow;}

    public void setFollow(BitSet b) { follow.or(b);}

    public State getRoot() { return root;}

    public void setRoot(State r) { root = r;}

    public  void addTransition(int item, State dest) { transitions.put(item,dest);}

    public String toString() { return (((Integer)getStart()).toString());}
}