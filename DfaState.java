import java.util.*;
// one state of the wDFA is a set of states of the nfa used in the queue of the deteminizatiob module
public class DfaState {   
    ArrayList<Integer> pattern; 
    TreeSet<State> reference;         // the min antichain of the stateset of the object
    TreeMap<State,TreeSet<State>> states;   // the set of states categorized by their (sub)roots
    HashMap<Integer,DfaState> transitions;   // the set of DFA transitions from this state by item
    DfaState root;
    BitSet follow;
    int support;                                                                                                                                                                                                                      

    public DfaState(){
        reference = new TreeSet<State>(State.BY_DESC );
        states = new TreeMap<State,TreeSet<State>>(State.BY_START);
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

    /*public TreeSet<State> listStates(){
        TreeSet<State> r = new TreeSet<>(State.BY_START);
        for (State t:states.keySet()) r.addAll(this.getStates(t));
        return r;
    }*/

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

    public TreeSet<State> getRoots(int min, int max) {
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (State s:states.keySet()) if (s.getStart() >= min && s.getEnd() <= max) r.add(s);   
        return r;
    }

    public TreeSet<State> getRoots() {
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (State s:states.keySet()) r.add(s);   
        return r;
    }
    
    public String toString(){
        return (states.toString());
    }

    /*private static final int borne1(TreeSet<State> l, TreeSet<State> m){
        return (Math.max(l.first().getStart(),m.first().getStart()));
    }

    private static final int borne2(TreeSet<State> l, TreeSet<State> m){
        return (l.last().getStart() < m.last().getStart())?  l.last().getEnd(): m.last().getEnd();
    }*/

    public TreeSet<State> getReferences(){
        return reference; 
    }

    public TreeSet<State> getReferences(int i, int [] border){
        TreeSet<State> r = new TreeSet<>(State.BY_DESC);
        for(State s:reference) if (s.getFollow().get(i)) r.add(s);
        border[0] = r.first().getStart();
        border[1] = r.last().getEnd();
        return r; 
    }

    public void addState(State s, boolean compute_weight) {      // add state to the stateset and consider its weight if it's the case 
        if (states.containsKey(s.getRoot())) states.get(s.getRoot()).add(s);
        else {
            TreeSet<State> ss = new TreeSet<State>(State.BY_START);
            ss.add(s);
            states.put(s.getRoot(), ss);
        }
        this.setFollow(s.getFollow());
        if (compute_weight && reference.add(s)) {
            this.setSupport(s.getWeight());
         if (s.getType() && s.getFollow().isEmpty())  reference.remove(s);
        } 
    }
    
    public void Align_from_itemsetDelimiter(DfaState s, DfaState r,int item) {
        int[] minMax =  new int[2];
        Iterator<State> xit = s.getReferences(item, minMax).iterator();
        Iterator<State> yit = r.getRoots(minMax[0],minMax[1]).iterator();
        State x = xit.next();
        State y = yit.next();
        do {
            if (x.getEnd() < y.getStart()) if (xit.hasNext()) x = xit.next(); else break;
            else
            { 
                if (x == y || y.getStart() > x.getStart() && y.getEnd() < x.getEnd()) 
                    for(State t: r.getStates(y)) addState(t,true);
                if (yit.hasNext()) y = yit.next(); else break;
            }
        } while (true);
    }

    public void Align_within_Itemset(DfaState s, DfaState r, State ref) {
        Iterator<State> xit = s.getStates(ref).iterator();
        Iterator<State> yit = r.getStates(ref).iterator(); 
        State x = xit.next();
        State y = yit.next();
        do {
            if (x.getEnd() < y.getStart()) if (xit.hasNext()) x = xit.next(); else break;
            else { 
                if (y.getStart() > x.getStart() && y.getEnd() < x.getEnd())  
                    addState(y,true);
                if (yit.hasNext()) y = yit.next(); else break;
            }
        } while (true);
    }

    /*public DfaState delta(int a, DfaState ref) {
        DfaState res = new DfaState();         // res = delta(this,a)
        if (this.getItem() == WAutomaton.itemsetDelimiter)  // this is an itemsetdelimiter a #_State
            res.Align_from_itemsetDelimiter(this,ref.getTransitions().get(a),a);     
        else {
            Set<State> l = this.getRoots();
            for (State r: l){                 // this is an itemsetState a \sigma_State
                if (ref.getTransitions().get(a).getStates().containsKey(r))
                res.Align_within_Itemset(this,ref.getTransitions().get(a),r);   
            }
        }     
        return res;
    }*/
}