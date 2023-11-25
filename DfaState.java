import java.util.*;
// one state of the wDFA is a set of states of the wNfa used in the queue of the deteminizatiob module
public class DfaState implements Comparable<DfaState> {   
    int item; 
    //TreeSet<Integer> reference;                   // the min antichain of the stateset of the object
    TreeMap<Integer,TreeSet<Integer>> states;       // the set of states categorized by their (sub)roots (subtree or region)
    //TreeSet<DfaState> transitions;      // the set of DFA transitions from this state by item
    int root;
    BitSet follow;
    int support;                                                                                                                                                                                                                      

    public DfaState(int i){
        //reference = new TreeSet<State>(State.BY_DESC );
        states = new TreeMap<Integer,TreeSet<Integer>>();
        //transitions = new TreeSet<DfaState>();
        follow = new BitSet(); 
        item  =  i;
    }

    public int getItem(){
        return item;
    }

    
    
    public BitSet fringerprint(){
        BitSet b = new BitSet();
        //for (int i:reference) b.set(i);
        return b;
    }

    public TreeSet<State> getStates(){
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (TreeSet<Integer> i:states.values()) 
            for (int j:i)
                r.add(WASMA.NFA.State(j));
        return r;
    }

    public TreeSet<State> getStates(int root){
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (int j:states.get(root)) r.add(WASMA.NFA.State(j));
        return r;
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
    
    /*public TreeSet<DfaState> getTransitions(){
        TreeSet<DfaState> r = new TreeSet<>();
        for (int i:WAutomaton.DFA.get(this.index()).values())  r.add(WAutomaton.wDFAstateMap.get(i));
        return r;
    }

    public DfaState getTransition(int i){
        return WAutomaton.wDFAstateMap.get(WAutomaton.wDFA.get((WAutomaton.wDFA.get(this.index()).get(i))));
    }
    
    public int index(){
        return 0;
    }
    public void addTransition(int i, int s){
        WAutomaton.wDFA.get(this.index()).put(i,s);
    }*/

    public int getRoot(){
        return root;
    }

    public void setRoot(int r){
        root = r;
    }

    public TreeSet<State> getRoots() { 
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (int i:states.keySet()) r.add(WASMA.NFA.State(i));
        return r;
    }
    
    /*public TreeSet<State> getRoots(int min, int max) {
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (State s:states.keySet()) if (s.getStart() >= min && s.getEnd() <= max) r.add(s);   
        return r;
    }*/

    public String toString(){
        return (states.toString());
    }

    /*private static final int borne1(TreeSet<State> l, TreeSet<State> m){
        return (Math.max(l.first().getStart(),m.first().getStart()));
    }

    private static final int borne2(TreeSet<State> l, TreeSet<State> m){
        return (l.last().getStart() < m.last().getStart())?  l.last().getEnd(): m.last().getEnd();
    }

    public TreeSet<State> getReferences(){
        return reference; 
    }

    public TreeSet<State> getReferences(int i, int [] border){
        TreeSet<State> r = new TreeSet<>(State.BY_DESC);
        for(State s:reference) if (s.getFollow().get(i)) r.add(s);
        border[0] = r.first().getStart();
        border[1] = r.last().getEnd();
        return r; 
    }*/

    public void addState(int s, boolean compute_weight) {      // add state to the stateset and consider its weight if it's the case 
        if (states.containsKey(WASMA.NFA.State(s).getRoot())) 
            states.get(WASMA.NFA.State(s).getRoot()).add(s);
        else {
            TreeSet<Integer> ss = new TreeSet<Integer>();
            ss.add(s);
            states.put(WASMA.NFA.State(s).getRoot(), ss);
        }
        this.setFollow(WASMA.NFA.State(s).getFollow());
    }
    
    public void Align(DfaState s, DfaState r, int ref) {
        Iterator<State> xit, yit;
        xit = (((Integer)ref == null)?s.getStates():s.getStates(ref)).iterator();
        yit = (((Integer)ref == null)?r.getRoots():r.getStates(ref)).iterator();
        State x = xit.next(),y = yit.next();
        do {
            if (x.getEnd() < y.getStart()) if (xit.hasNext() )  x = xit.next(); else break;
            else
            { 
                if (x == y || y.getStart() > x.getStart() && y.getEnd() < x.getEnd()) 
                    if ((Integer)ref == null)   
                        for (State m :r.getStates(y.getRoot())) {
                            addState(m.getNum(), true);
                        }
                    else addState(y.getNum(),true);
                if (yit.hasNext()) y = yit.next(); else break;
            }
        } while (true);
    }

    public int compareTo(DfaState other) {
        return this.item - other.item;
       
    }

    /*public DfaState findTransition(int i) {
        return transitions.floor(new DfaState(i));
    }
       
    public DfaState delta(int a, DfaState ref) {
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