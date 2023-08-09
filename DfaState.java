import java.util.HashMap;
import java.util.TreeSet;

public class DfaState {   // one state of the wdfa is a set of states of the nfa 
    int item;
    HashMap<IState,TreeSet<State>> states;   // the set of states categorized by their roots
    HashMap<Integer,DfaState> transitions;   // the set of transitions in the dfa from this state
    int support;                                                                                                                                                                                                                      

    public DfaState(int i) {
        item = i;
        states = new HashMap<>();
    }

    public TreeSet<State> getEtats(IState r) {
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

    public void setSupport(int support) {
        this.support = support;
    }
}