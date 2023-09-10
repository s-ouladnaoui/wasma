import java.util.*;   
/* Weighted Automaton State Class */
public class State implements Comparable<State> {
    boolean type;                                    /* flag : the state is an itemset delimiter */
    HashMap<Integer,State> transitions;              /* Immediate transition list */
    int start, end;                                  /* codes for reachability (descendence) queries */
    int weight ;                                     /* the frequency of the prefix from the startstate to this state */ 
    State root; 
    BitSet follow;                                    /* the root of the subtree: the begining of the itemset that contains this state */
    public State(boolean stateType) {
        type = stateType;
        transitions = new HashMap<>();
        weight = 0;
        follow = new BitSet();
    }

    public int getWeight(){
        return weight;
    }

    public void setWeight(int w){
        weight += w;
    }

    public boolean getType() { return type;}

    public void setType(boolean t) { type = t; }
  
    protected Map<Integer, State > getTransitions() { return transitions; }

    public int getStart() { return start; }

    public void setStart(int s) { start = s; }

    public int getEnd() { return end; }

    public void setEnd(int e) { end = e; }

    public BitSet getFollow() { 
        for (int i:transitions.keySet()) if (i >= 0) follow.set(i);
        return follow; 
    }

    public void setFollow(BitSet b) { follow.or(b); }

    public State getRoot() { return root; }

    public void setRoot(State r) { root = r; }

    public  void addTransition(int item, State dest) { 
        transitions.put(item,dest); 
    }

    public String toString() {
        return (((Integer)getStart()).toString());
    }

    public int compareTo(State t)
    {   
        if (getStart() < t.getStart()) return -1;
        else if (getStart() > t.getStart()) return +1;
        else return 0;
    }
}