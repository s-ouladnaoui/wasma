import java.util.*;
// one state of the wDFA is a set of states of the nfa used in the queue of the deteminizatiob module
public class DfaState {   
    ArrayList<Integer> pattern; 
    TreeSet<State> reference;
    TreeMap<State,TreeSet<State>> states;   // the set of states categorized by their (sub)roots
    HashMap<Integer,DfaState> transitions;   // the set of DFA transitions from this state by item
    DfaState root;
    BitSet follow;
    int support;                                                                                                                                                                                                                      

    public DfaState(){
        reference = new TreeSet<State>(State.BY_DESC );
        states = new TreeMap(State.BY_START);
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

    public TreeMap<State,TreeSet<State>> getStates(){
        return states;
    }

    public TreeSet<State> getStates(State r){
        return states.get(r);
    }

    public TreeSet<State> listStates() {
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (State rt: this.states.keySet() ){       
            r.addAll(this.getStates(rt));      
        }
        return r;
    }

    public TreeSet<State> getReferences(){
        return reference;
    }

    public Set<State> listRoots() {
        TreeSet<State> r = new TreeSet<>(State.BY_START);
        for (State s:states.keySet()) r.add(s);
        return r;
    }

    public void addState(State s){
        if (reference.add(s)) {
            this.setSupport(s.getWeight());
        } 
        if (states.containsKey(s.getRoot())) {
            states.get(s.getRoot()).add(s);
        } else {
            TreeSet<State> ss = new TreeSet<State>(State.BY_DESC);
            ss.add(s);
            states.put(s.getRoot(), ss);
        }
        this.setFollow(s.getFollow());
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
        return (states.toString());
    }

    public void Align(DfaState s, DfaState r, State ref) {
        Iterator<State> xit, yit;
        if (ref == null){
            xit = s.getReferences().iterator();
            yit = r.listRoots().iterator();
        } else {
            xit = s.getStates(ref).iterator();
            yit = r.getStates(ref).iterator();
        } 
        State x = xit.next();
        State y = yit.next();
        do {
            if (x.getEnd() < y.getStart())  { 
                if (xit.hasNext()){ x = xit.next();} else break;
            }
            else if (y.getEnd() < x.getStart()) { 
                if (yit.hasNext()) y = yit.next(); else break;
            } else {
                    if (ref != null ){
                        this.addState(y);
                    } else for(State m: r.getStates(y)){
                        this.addState(m);
                    }
                if (yit.hasNext()) y = yit.next(); else break;
            }
        } while (true);
    }

    public DfaState delta(int a, DfaState ref) {
        DfaState res = new DfaState();         // res = delta(this,a)
        if (this.getItem() == WAutomaton.itemsetDelimiter )  // this is an itemsetdelimiter a #_State
            res.Align(this,ref.getTransitions().get(a),null);     
        else {
            Set<State> l = this.listRoots();
            for (State r: l){                 // this is an itemsetState a \sigma_State
                if (ref.getTransitions().get(a).getStates().containsKey(r))
                res.Align(this,ref.getTransitions().get(a),r);   
            }
        }     
        return res;
    }
}