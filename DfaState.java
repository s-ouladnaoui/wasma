import java.util.BitSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;

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
        }else {
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

    public BitSet getFollow(){
        return follow;
    }

    public void setFollow(BitSet b){
        follow.or(b);
    }
    
    public HashMap<Integer,DfaState> getTransitions(){
        return transitions;
    }

    public void addTransition(int i, DfaState s){
        transitions.put(i, s);
    }

    public String toString(){
        return ("States: "+getStates()); 
    }

    public DfaState delta(int a){
        DfaState res = new DfaState();
        for (IState r: this.getStates().keySet() ){
            Iterator<State> xit = this.getStates(r).iterator();
            Iterator<State> yit;
            if (WAutomaton.wdfaStartState.getTransitions().get(a).getStates().containsKey(r)) {
                yit = WAutomaton.wdfaStartState.getTransitions().get(a).getStates(r).iterator();
            } else continue;
            State x = xit.next();
            State y = yit.next();
            do {
                if (x.getEnd() < y.getStart())  { if (xit.hasNext()) x = xit.next();}
                else if (y.getEnd() < x.getStart()) { if (yit.hasNext()) y = yit.next();}
                else {
                    res.addState(r,y);
                    if (yit.hasNext()) y = yit.next();
                }
            } while (xit.hasNext() && yit.hasNext());
        }
        return res;
    }
}