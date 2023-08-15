import java.util.BitSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;

public class DfaState {   // one state of the wDFA is a set of states of the nfa 
    HashMap<IState,TreeSet<State>> states;   // the set of states categorized by their (sub)roots
    HashMap<Integer,DfaState> transitions;   // the set of DFA transitions from this state by item
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
    
    public void addState(State s){
        if (states.containsKey(s.getRoot())) {
            states.get(s.getRoot()).add(s);
        }else {
            TreeSet<State> ss = new TreeSet<State>();
            ss.add(s);
            states.put(s.getRoot(), ss);
        }
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int sprt) {
        support += sprt;
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
        return ("States: "+getStates()+" Supp: "+getSupport()); 
    }

    public DfaState delta(int a){
        DfaState res = new DfaState();                  // res = delta(this,a)
        TreeSet<State> list = new TreeSet<State>();     // set of the states of res 
        BitSet br = new BitSet();
        for (IState r: getStates().keySet() ){
            Iterator<State> xit = getStates(r).iterator();
            Iterator<State> yit;
            if (WAutomaton.wDFAStartState.getTransitions().get(a).getStates().containsKey(r)) {
                yit = WAutomaton.wDFAStartState.getTransitions().get(a).getStates(r).iterator();
            } else continue;
            State x = xit.next();
            State y = yit.next();
            do {
                if (x.getEnd() < y.getStart())  { if (xit.hasNext()) x = xit.next(); else break;}
                else if (y.getEnd() < x.getStart()) { if (yit.hasNext()) y = yit.next(); else break;}
                else {
                    list.add(y);
                    br.set(WAutomaton.wNFAStates.indexOf(y));
                    if (yit.hasNext()) y = yit.next(); else break;
                }
            } while (true);
        }
        State ref, s;
        Iterator<State> it = list.iterator();
        ref = it.next();
        res.addState(ref);
        setFollow(((IState) ref).getPrior());
        res.setSupport(((IState) ref).getWeight());
        while (it.hasNext()){
            s = (State) it.next();
            res.addState(s);
            if (s.getStart() > ref.getEnd()) {
                ref = s;
                res.setSupport(((IState) ref).getWeight());
                setFollow(((IState) ref).getPrior());
            }
        }
        if (a == WAutomaton.itemsetDelimiter && res.getSupport() > WAutomaton.min_supp) {
            this.setSupport(res.getSupport());
            if(!WAutomaton.stateMap.containsKey(br))  WAutomaton.stateMap.put(br, res);
        }
        return res;
    }    
}