import java.util.*;
/* Weighted Automaton State Class */
    public class State implements Comparable<State> {

    boolean type;                                  /* flag : the state is an itemset delimiter */
    final HashMap<Integer,State> transitions;      /* Immediate transition list */
    private int start, end;                        /* codes for reachability (descendence) queries */
    IState root;                /* the root of the subtree: the begining of the itemset that contains this state */
    
    public State(boolean stateType) {
        type = stateType;
        transitions = new HashMap<>();
    }

    public boolean getType() { return type;}

    public void setType(boolean t) { type = t; }
  
    protected Map<Integer, State > getTransitions() { return transitions; }

    public int getStart() { return start; }

    public int getEnd() { return end; }

    public IState getRoot() { return root; }
 
    public void setRoot(IState r) { root = r; }

    public  void addTransition(int item, State dest) { transitions.put(item,dest); }

    public void setStart(int s) { start = s; }

    public void setEnd(int e) { end = e; }

    public String toString() {
        return //" ( "+ //getType()+", "+getStart()+", "+getEnd()+" )" ;
               ((Integer) WAutomaton.wNFAStates.indexOf(this)).toString();
                //+" )";
    }

    public int compareTo(State t)
    {   
        if (getStart() < t.getStart()) return -1;
        else if (getStart() > t.getStart()) return +1;
        else return 0;
    }
}