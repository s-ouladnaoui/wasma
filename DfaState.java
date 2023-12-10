import java.util.*;
// one state of the wDFA is a set of states of the wNfa 
public class DfaState {   
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

    public int getItem(){ return item; }

    public TreeSet<State> getStates(int root){
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (int j:states.get(root)) r.add(WASMA.NFA.StateMap.get(j));
        return r;
    }

    public TreeSet<State> getStates(){
        TreeSet<State> r = (this.getItem() == WASMA.itemsetDelimiter)?
                            new TreeSet<State>(State.BY_DESC):
                            new TreeSet<State>(State.BY_START);
        for (int root:states.keySet()) 
            r.addAll(getStates(root));
        return r;
    }

    public int getSupport(){ return support;}

    public void setSupport(int sprt){ support += sprt;}

    public BitSet getFollow(){ return follow;}

    public void setFollow(BitSet b){ follow.or(b);}

    public int getRoot(){ return root;}

    public void setRoot(int r){ root = r;}

    public TreeSet<State> getRoots() { 
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (int i:states.keySet()) r.add(WASMA.NFA.State(i));
        return r;
    }

    public String toString(){
        return states.toString();
    }

    public void addState(int s, boolean compute_weight) {       // add state to the stateset and consider its weight if it's the case 
        State tmp = WASMA.NFA.State(s);                         // to avoid multiple calls to NFA.State() method
        if (WASMA.reference.add(tmp)) {
            if (compute_weight) this.setSupport(tmp.getWeight());
            if (this.getItem() != WASMA.itemsetDelimiter) WASMA.fringerprint.set(tmp.getOrder()); 
        }                
        if (states.containsKey(tmp.getRoot())) 
            states.get(tmp.getRoot()).add(s);
        else {
            TreeSet<Integer> ss = new TreeSet<Integer>();
            ss.add(s);
            states.put(tmp.getRoot(), ss);
        }
        if (!tmp.getFollow().isEmpty()) this.setFollow(tmp.getFollow());
    }
    
    public void Align(DfaState s, DfaState r, int ref, boolean computeSupport) {
        Iterator<State> xit = (((Integer)ref == -1)?s.getStates():s.getStates(ref)).iterator();
        Iterator<State> yit = (((Integer)ref == -1)?r.getRoots():r.getStates(ref)).iterator();
        State x = xit.next(),y = yit.next();
        do {
            if (x.getEnd() < y.getStart()) if (xit.hasNext() )  // if state x is less (at the left) of state y advance in x iterator
                x = xit.next(); else break;
            else
            { 
                if (x == y || y.getStart() > x.getStart() && y.getEnd() < x.getEnd())  // if y is descendent from x add it to the result
                    if (ref == -1)   
                        for (State m :r.getStates(y.getNum()))      // case where y is a subroot: insert all its descendents
                            addState(m.getNum(), computeSupport);
                    else addState(y.getNum(), computeSupport);      // y is a simple state
                if (yit.hasNext()) y = yit.next(); else break;      // otherwise: advance in the y iterator
            }
        } while (true);
    }

    public DfaState Delta(int i, DfaState ref, boolean compute_sprt){    // r = delta(s,i) taking ref as a reference for alignment
        DfaState r   = new DfaState(i);
        WASMA.reference = new TreeSet<State>(State.BY_DESC);    
        WASMA.fringerprint = new BitSet();
        if (this.getItem() == WASMA.itemsetDelimiter)       // this is an itemsetdelimiter (a #_State)
            r.Align(this,ref,-1,compute_sprt);     
        else for (State m :this.getRoots())                 // this is an itemsetState (an element of \sigma_State)
                if (ref.states.containsKey(m.getNum()))
                    r.Align(this,ref,m.getNum(),compute_sprt);   
        return r;
    }
}