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
    }

    public int getItem(){
        return item;
    }

    public void setItem(int i){
        item = i;
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

    // for reachability between two sets of states align them and check the descendance relation 
    public void Align(Iterator<State> xit, Iterator<State> yit, DfaState s, boolean jump) {
        State x = xit.next();
        State y = yit.next();
        do {
            if (x.getEnd() < y.getStart())  { if (xit.hasNext()) x = xit.next(); else break;}
            else if (y.getEnd() < x.getStart() || x.getStart() > y.getStart() && x.getEnd() < y.getEnd()) { if (yit.hasNext()) y = yit.next(); else break;}
            else {
                if (!jump) s.addState(y);
                else if (x.getRoot() == y.getRoot()) s.addState(y);
                if (yit.hasNext()) y = yit.next(); else break;
            }
        } while (true);
    }

    // compute the support of a set of IStates and collect the localprevious items in bs
    public int evalSupport(BitSet bs,Iterator<State> it) {
        State ref,s;
        int sprt = 0;
        ref = it.next();
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

    public BitSet clearBs(BitSet bs, int i) {
        for (int j = bs.nextSetBit(0); j > 0; j = bs.nextSetBit(j + 1)) {
            if (j <= i) bs.clear(j); 
        }
        return bs;
    }

    public DfaState delta_s(int a) {
        DfaState res_a = new DfaState(a);                  // res_a = delta(this,a)
        DfaState res_1 = new DfaState(WAutomaton.itemsetDelimiter);                  // res_1 = delta(res_a,itemsetDelimiter) 
        BitSet pr = new BitSet();                // local next items for subsequent (itemset) extensions
        TreeSet<State> list_delimiters = new TreeSet<State>();
        Align(this.listStates().iterator(), WAutomaton.wDFAStartState.getTransitions().get(a).listStates().iterator(), res_a, false);
        for (State st:res_a.listStates()) {
        list_delimiters.addAll(WAutomaton.wDFAStartState.getTransitions().get(WAutomaton.itemsetDelimiter).getStates(st.getRoot())); 
        }
        Align(res_a.listStates().iterator(), list_delimiters.iterator(),res_1,  false);
        int sprt = evalSupport(pr, res_1.listStates().iterator());
        res_a.setSupport(sprt);
        res_a.setFollow(clearBs(pr,a));
        res_a.addTransition(WAutomaton.itemsetDelimiter, res_1);
        res_1.setSupport(sprt);
        return res_a;
    }

    public DfaState delta(int a){
        DfaState res_a = new DfaState(a);                  // res_a = delta(this,a)
        DfaState res_1 = new DfaState(WAutomaton.itemsetDelimiter);                  // res_1 = delta(res_a,itemsetDelimiter) 
        BitSet pr = new BitSet();                // local next items for subsequent (itemset) extensions
        for (IState r: getStates().keySet() ){
            if (!WAutomaton.wDFAStartState.getTransitions().get(a).getStates().containsKey(r)) continue;
            Align(getStates(r).iterator(), WAutomaton.wDFAStartState.getTransitions().get(a).getStates(r).iterator(),res_a,  false);
        }
        Align(res_a.listStates().iterator(), this.getTransitions().get(WAutomaton.itemsetDelimiter).listStates().iterator(), res_1, true);         
        int sprt = evalSupport(pr, res_1.listStates().iterator());
        res_a.setSupport(sprt);
        res_a.setFollow(clearBs(pr, a));
        res_a.addTransition(WAutomaton.itemsetDelimiter, res_1);
        return res_a;
    }

    public String toString(){
        return (listStates()+" Supp: "+getSupport()); 
    }
}

