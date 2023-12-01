import java.util.*;
// one state of the wDFA is a set of states of the wNfa 
public class DfaState implements Comparable<DfaState> {   
    int item; 
    TreeMap<Integer,TreeSet<Integer>> states;       // the set of states categorized by their (sub)roots (subtree or region)
    int root;
    BitSet follow;
    int support;                                                                                                                                                                                                                      

    public DfaState(int i){
        states = new TreeMap<Integer,TreeSet<Integer>>();
        follow = new BitSet(); 
        item  =  i;
    }

    public int getItem(){
        return item;
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

    public String toString(){
        return (states.toString());
    }



    public void addState(int s, boolean compute_weight) {      // add state to the stateset and consider its weight if it's the case 
        if (this.getItem() != WASMA.itemsetDelimiter) WASMA.fringerprint.set(WASMA.NFA.State(s).getStart()); 
        if (states.containsKey(WASMA.NFA.State(s).getRoot())) 
            states.get(WASMA.NFA.State(s).getRoot()).add(s);
        else {
            TreeSet<Integer> ss = new TreeSet<Integer>();
            ss.add(s);
            states.put(WASMA.NFA.State(s).getRoot(), ss);
        }
        if (compute_weight && WASMA.reference.add(WASMA.NFA.State(s))) {
            this.setSupport(WASMA.NFA.State(s).getWeight());
            if (this.getItem() == WASMA.itemsetDelimiter) WASMA.fringerprint.set(WASMA.NFA.State(s).getStart()); 
        }
        this.setFollow(WASMA.NFA.State(s).getFollow());
    }
    
    public void Align(DfaState s, DfaState r, int ref) {
        Iterator<State> xit, yit;
        xit = (((Integer)ref == -1)?s.getStates():s.getStates(ref)).iterator();
        yit = (((Integer)ref == -1)?r.getRoots():r.getStates(ref)).iterator();
        State x = xit.next(),y = yit.next();
        do {
            if (x.getEnd() < y.getStart()) if (xit.hasNext() )  x = xit.next(); else break;
            else
            { 
                if (x == y || y.getStart() > x.getStart() && y.getEnd() < x.getEnd()) 
                    if ((Integer)ref == -1)   
                        for (State m :r.getStates(y.getNum())) {
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
    /* 
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