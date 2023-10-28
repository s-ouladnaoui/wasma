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

    public TreeSet<State> getReferences(int i, int[] t){
        TreeSet<State> r = new TreeSet<>(State.BY_DESC);
        for(State s:reference) if (s.getFollow().get(i)) r.add(s);
        t[0] = r.first().getStart();
        t[1] = r.last().getEnd();
        return r; 
    }

    public TreeSet<State> listRoots(int inf, int sup) {
        TreeSet<State> r = new TreeSet<>(State.BY_START);
        for (State s:states.keySet()) 
            if (s.getStart() >= inf && s.getStart() <= sup) 
                r.add(s); 
        return r;
    }

    public void insertState(State s){   //    add a state without weight update used during loading to improve its time
        if (states.containsKey(s.getRoot())) {
            states.get(s.getRoot()).add(s);
        } else {
            TreeSet<State> ss = new TreeSet<State>(State.BY_START);
            ss.add(s);
            states.put(s.getRoot(), ss);
        }
        this.setFollow(s.getFollow());
    }
 
    public void addState(State s) {      // add state with weight computing used in mining phase
        if (reference.add(s)) {
            this.setSupport(s.getWeight());
        } 
        if (states.containsKey(s.getRoot())) {
            states.get(s.getRoot()).add(s);
        } else {
            TreeSet<State> ss = new TreeSet<State>(State.BY_START);
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

    /*private static final int borne1(TreeSet<State> l, TreeSet<State> m){
        return (l.first().getStart());
    }

    private static final int borne2(TreeSet<State> l, TreeSet<State> m){
        return (l.last().getStart() < m.last().getStart())?  l.last().getEnd(): m.last().getEnd();
    }*/

    public void Align_from_itemsetDelimiter(DfaState s, DfaState r,int item) {
        Iterator<State> xit, yit;
        int[] k = new int[2];
        xit = s.getReferences(item,k).iterator();
        yit = r.listRoots(k[0],k[1]).iterator();
        State x ;
        if (xit.hasNext()) x = xit.next(); else return;
        State y;
        if (yit.hasNext()) y = yit.next(); else return;
        do {
            if (x.getEnd() < y.getStart()) if (xit.hasNext()) x = xit.next(); else break;
            else { 
                if (x == y || y.getStart() > x.getStart() && y.getEnd() < x.getEnd()){
                    for(State t: r.getStates(y)) this.addState(t);
                }
                if (yit.hasNext()) y = yit.next(); else break;
            }
        } while (true);
    }

    
    
    public void Align_within_Itemset(DfaState s, DfaState r, State ref) {
        Iterator<State> xit, yit;
        xit = s.getStates(ref).iterator();
        yit = r.getStates(ref).iterator(); 
        State x = xit.next();
        State y = yit.next();
        do {
            if (x.getEnd() < y.getStart()) if (xit.hasNext()) x = xit.next(); else break;
            else { 
                if (x == y || y.getStart() > x.getStart() && y.getEnd() < x.getEnd()){
                    if (ref != null )  this.addState(y);
                    else for(State t: r.getStates(y)) this.addState(t);
                }
                if (yit.hasNext()) y = yit.next(); else break;
            }
        } while (true);
    }

    public DfaState delta(int a, DfaState ref) {
        DfaState res = new DfaState();         // res = delta(this,a)
        if (this.getItem() == WAutomaton.itemsetDelimiter)  // this is an itemsetdelimiter a #_State
            res.Align_from_itemsetDelimiter(this,ref.getTransitions().get(a),a);     
        else {
            Set<State> l = this.listRoots(-1,(int)Double.POSITIVE_INFINITY );
            for (State r: l){                 // this is an itemsetState a \sigma_State
                if (ref.getTransitions().get(a).getStates().containsKey(r))
                res.Align_within_Itemset(this,ref.getTransitions().get(a),r);   
            }
        }     
        return res;
    }
}