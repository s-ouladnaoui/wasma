import java.util.BitSet;
import java.util.HashMap;
import java.util.TreeSet;

public class DfaState {   // one state of the wdfa is a set of states of the nfa 
    HashMap<IState,TreeSet<State>> states;   // the set of states categorized by their roots
    HashMap<Integer,DfaState> transitions;   // the set of transitions in the dfa from this state
    BitSet follow;
    int support;                                                                                                                                                                                                                      

    public DfaState() {
        states = new HashMap<IState,TreeSet<State>>();
        transitions = new HashMap<Integer,DfaState>();
        follow = new BitSet(); 
    }

    public HashMap<IState,TreeSet<State>> getStates() {
        return states;
    }
    public TreeSet<State> getStates(IState r) {
        return states.get(r);
    }
    
    public void addState(IState r, State s){
        if (this.states.containsKey(r)) {
            this.states.get(r).add(s);
        } else {
            TreeSet<State> ss = new TreeSet<State>();
            ss.add(s);
            states.put(r, ss);
        }
        
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int sprt) {
        this.support = sprt;
    }

    public BitSet getfollow(){
        return follow;
    }

    public void setfollow(BitSet b){
        follow.or(b);
    }
    
    public HashMap<Integer,DfaState> gettransitions(){
        return transitions;
    }

    public void addTransition(int i, DfaState d){
        transitions.put(i, d);
    }

    public String toString(){
        return ("States: "+getStates()); 
    }

}