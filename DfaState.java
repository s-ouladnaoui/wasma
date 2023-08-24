import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;
// one state of the wDFA is a set of states of the nfa used in the queue of the deteminizatiob module
public class DfaState {   
    int item;
    ArrayList<Integer> pattern; 
    HashMap<IState,TreeSet<State>> states;   // the set of states categorized by their (sub)roots
    HashMap<Integer,DfaState> transitions;   // the set of DFA transitions from this state by item
    BitSet follow;
    int support;                                                                                                                                                                                                                      

    public DfaState(int i){
        item = i;
        states = new HashMap<IState,TreeSet<State>>();
        transitions = new HashMap<Integer,DfaState>();
        follow = new BitSet(); 
        pattern =  new ArrayList<Integer>();
    }

    public int getItem(){
        return item;
    }

    public void setItem(int i){
        item = i;
    }

    public ArrayList<Integer> getPattern() {
        return pattern;
    }

    public void setPattern(int i) {
        pattern.add(i);
    }

    public HashMap<IState,TreeSet<State>> getStates(){
        return states;
    }

    public TreeSet<State> getStates(IState r){
        return states.get(r);
    }

    public TreeSet<State> listStates(){
        TreeSet<State> r = new TreeSet<State>();
        for (IState rt: this.getStates().keySet() ){
            r.addAll(getStates(rt));
        }
        return r;
    }

    public BitSet listStateBits() {
        BitSet r = new BitSet(listStates().size());
        for (State s:listStates()){
            r.set(WAutomaton.wNFAStates.indexOf(s));
        }
        return r;
    }
    
    public void addState(State s){
        if (states.containsKey(s.getRoot())) {
            states.get(s.getRoot()).add(s);
        }else {
            TreeSet<State> ss = new TreeSet<State>();
            ss.add(s);
            states.put(s.getRoot(), ss);
        }
        if (s.getType()) this.setFollow(((IState) s).getFollow());
    }

    public int getSupport(){
        return support;
    }

    public void setSupport(int sprt){
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

    // compute the support of a set of IStates and collect the local next items in bs
    public static int evalSupport(BitSet bs,Iterator<State> it) {
        int sprt = 0;
        if (!it.hasNext()) return 0;
        State ref = it.next(),s;
        bs.or(((IState) ref).getPrior());
        sprt += ((IState) ref).getWeight();
        while (it.hasNext()){
            s = (State) it.next();
            bs.or(((IState) s).getPrior());
            if (s.getStart() > ref.getEnd()) {
                ref = s;
                sprt += ((IState) ref).getWeight();
            }      
        }
        return sprt;
    } 

    public DfaState delta(int a) {
        DfaState res = new DfaState(a);    // res_a = delta(this,a)
        BitSet pr = new BitSet();       // local next items for subsequent (itemset) extensions
        ArrayList<Integer> p = new ArrayList<>(this.getPattern());
        p.add(a); 
        res.pattern = p;
        if (a == WAutomaton.itemsetDelimiter)  {
            for (IState r: getStates().keySet() ){
                WAutomaton.Align(getStates(r).iterator(), WAutomaton.wDFAStartState.getTransitions().get(a).getStates(r).iterator(),res,  false);            
            }
        int sprt = evalSupport(pr, res.listStates().iterator());
        res.setSupport(sprt);
        this.setFollow(WAutomaton.clearBs(pr, this.getItem()));
        return res;
        }
        else {
            if (this.getItem() == WAutomaton.itemsetDelimiter) {
                WAutomaton.Align(listStates().iterator(), WAutomaton.wDFAStartState.getTransitions().get(a).listStates().iterator(),res,  false);       
            } else {
                for (IState r: getStates().keySet() ){
                    if (!WAutomaton.wDFAStartState.getTransitions().get(a).getStates().containsKey(r)) continue;
                    WAutomaton.Align(getStates(r).iterator(), WAutomaton.wDFAStartState.getTransitions().get(a).getStates(r).iterator(),res,  false);
                }  
            }
            return res;
        }
    }
    
    public String toString(){
        return (listStates().toString());//+" Supp: "+getSupport()); 
    }
}

