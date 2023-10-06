import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.List;
// one state of the wDFA is a set of states of the nfa used in the queue of the deteminizatiob module
public class DfaState {   
    ArrayList<Integer> pattern; 
    HashMap<State,TreeSet<State>> states;   // the set of states categorized by their (sub)roots
    HashMap<Integer,DfaState> transitions;   // the set of DFA transitions from this state by item
    DfaState root;
    BitSet follow;
    int support;                                                                                                                                                                                                                      

    public DfaState(){
        states = new HashMap<State,TreeSet<State>>();
        transitions = new HashMap<Integer,DfaState>();
        follow = new BitSet(); 
        pattern =  new ArrayList<Integer>();
    }

    public int getItem() {
        return pattern.get(pattern.size()-1);
    }

    public ArrayList<Integer> getPattern() {
        return pattern;
    }

    public void extendPattern(int i) {
        pattern.add(i);
    }

    public HashMap<State,TreeSet<State>> getStates(){
        return states;
    }

    public TreeSet<State> getStates(State r){
        return states.get(r);
    }

    public TreeSet<State> listStates() {
        TreeSet<State> r = new TreeSet<State>();
        for (State rt: this.getStates().keySet() ){       
            r.addAll(getStates(rt));      
        }
        return r;
    }

    public TreeSet<State> listStateRoots() {
        TreeSet<State> r = new TreeSet<State>();
        for (State rt: this.getStates().keySet() ){       
            r.add(rt);      
        }
        return r;
    }

    public void addState(State s){
        if (states.containsKey(s.getRoot())) {
            states.get(s.getRoot()).add(s);
        } else {
            TreeSet<State> ss = new TreeSet<State>();
            ss.add(s);
            states.put(s.getRoot(), ss);
        }
        this.setFollow(s.getFollow());
    }

    public void addStates(State r){
        for (State s: getStates(r)){
            addState(s);
        }
    }

    public void addStates(List<State> l){
        for (State s: l){
            addState(s);
        }
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

    public DfaState getRoot(){
        return root;
    }

    public void setRoot(DfaState r){
        root = r;
    }

    public String toString(){
        return (listStates().toString());
    }

    // Reachability between an itemsetdelimiter setstate and an item setstate 
    public void Align(DfaState s, DfaState r) {
        Iterator<State> xit = s.listStates().iterator();
        Iterator<State> yit = r.listStateRoots().iterator();
        State x = xit.next();
        State ref1 = x;
        State y = yit.next();
        State ref2 = y;
        boolean first = true;
        int sprt = 0;
        do {
            if (ref1.getEnd() < y.getStart())  { 
                if (xit.hasNext()){
                        while(x.getStart() < ref1.getEnd()) { if (xit.hasNext()) x = xit.next(); else break;}
                        ref1 = x;
                } else break;
            }
            else if (y.getEnd() < ref1.getStart() || ref1.getStart() > y.getStart() && ref1.getEnd() < y.getEnd()) { 
                if (yit.hasNext()) y = yit.next(); else break;
            } else {
                if (first ) {
                    ref2 = y;
                    for (State m:r.getStates(y)){
                        this.addState(m);
                        sprt += m.getWeight();
                    }
                    first = false;
                } else {
                    for (State m:r.getStates(y)){
                        this.addState(m);
                        sprt += m.getWeight();
                    }
                    if ( y.getStart() > ref2.getEnd()) {
                    ref2 = y;
                }
            }
                if (yit.hasNext()) y = yit.next(); else break;
            }
        } while (true);
        this.setSupport(sprt);
    }

    // for reachability between two sets of states align them and check the descendance relation 
    // 2 cases : between item setstate and an itmesetdelimiter setstate
    // or        between 2 items setstates   
    public void Align(DfaState s, DfaState r, State m) {
        Iterator<State> xit = s.getStates(m).iterator();
        Iterator<State> yit = r.getStates(m).iterator();
        State x = xit.next();
        State ref1 = x;
        State y = yit.next();
        State ref2 = y;
        boolean first = true;
        int sprt = 0;
        do {
            if (ref1.getEnd() < y.getStart())  { 
                if (xit.hasNext()){
                    ref1 = xit.next(); 
                } else break;
            }
            else if (y.getEnd() < ref1.getStart() || ref1.getStart() > y.getStart() && ref1.getEnd() < y.getEnd()) { 
                if (yit.hasNext()) y = yit.next(); else break;
            } else {
                if (first ) {
                    ref2 = y;
                    this.addState(y);
                    sprt += y.getWeight();
                    first = false;
                } else {
                    this.addState(y);
                    if ( y.getStart() > ref2.getEnd()) {
                    ref2 = y;
                    sprt += y.getWeight();
                }
            }
                if (yit.hasNext()) y = yit.next(); else break;
            }
        } while (true);
        this.setSupport(sprt);
    }

    public DfaState delta(int a, DfaState ref) {
        DfaState res = new DfaState();                          // res = delta(this,a)
        if (this.getItem() == WAutomaton.itemsetDelimiter ) 
            res.Align(this,ref.getTransitions().get(a));     
        else 
            for (State r: getStates().keySet() )
                if (ref.getTransitions().get(a).getStates().containsKey(r))
                    res.Align(this,ref.getTransitions().get(a),r);
        return res;
    }
}