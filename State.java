import java.util.*;
/* Weighted Automaton State Class */
    public class State implements Comparable<State> {
    boolean type;                                         /* flag : the state is an itemset delimiter */
    BitSet follow;                                        /* les items suivants */
    final HashMap<Integer,State> transitions;             /* Immediate transition list */
    private int weight;                                   /* output weight of the state */
    int start, end;                                       /* codes for reachability (descendence) queries */
   // static int code = 0;
    State root;                /* the begining of the itemset that contains this state (root)*/
    public State() {
        type = false;
        transitions = new HashMap<>();
        //start = code++;
       // end = code++;
    }

    public boolean getType(){ return type;}
    public BitSet getFollow()
    {
        return follow;
    }

    public void setFollow(BitSet s)
    {
        follow.or(s);
    }
    protected Map<Integer, State > getTransitions() {
        return transitions;
    }

    public int getWeight() {
        return weight;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public State getRoot() {
        return root;
    }
    public void setRoot(State r)
    {
        this.root = r;
    }

    public  void addTransition(int item, State dest) {
        transitions.put(item,dest);
    }

    public void setWeight(int w) {
        weight = w;
    }

    public void setStart(int s) {
        start = s;
    }

    public void setEnd(int e) {
        end = e;
    }

    public String toString() {
        return " ( "+ getType()+", "+getStart()+", "+getEnd()+"; w = "+getWeight()+" trans: "+transitions+")" ;
    }

    public int compareTo(State t) {
        if (t.getStart() > this.getEnd())   return -1;
        if (t.getEnd() < this.getStart())   return +1;
        else return 0;
    }
}