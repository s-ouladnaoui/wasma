import java.util.*;
// one state of the wDFA is a set of states of the wNfa 
public class DfaState {   
    int item; 
    TreeMap<Integer,TreeSet<Integer>> states;       // the set of states categorized by their (sub)roots (subtree or region)
    int root;
    BitSet follow;
    int support;                                                                                                                                                                                                                      

    public DfaState(int item){
        states = new TreeMap<Integer,TreeSet<Integer>>();
        follow = new BitSet(); 
        item  =  item;
    }

    public int getItem(){ return item; }

    public TreeSet<State> getStates(){
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (TreeSet<Integer> i:states.values()) 
            for (int j:i)
                r.add(WASMA.NFA.StateMap.get(j));
        return r;
    }

    public TreeSet<State> getStates(int root){
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (int j:states.get(root)) r.add(WASMA.NFA.StateMap.get(j));
        return r;
    }

    public int getSupport(){ return support;}

    public void setSupport(int sprt){ support += sprt;}

    public BitSet getFollow(){ return follow;}

    public void setFollow(BitSet bol){ follow.or(bol);}

    public int getRoot(){ return root;}

    public void setRoot(int r){ root = r;}

    public TreeSet<State> getRoots() { 
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (int i:states.keySet()) r.add(WASMA.NFA.State(i));
        return r;
    }

    public void addState(int s, boolean compute_weight) {      // add state to the stateset and consider its weight if it's the case 
        State tmp = WASMA.NFA.State(s);           // to avoid multiple calls to NFA.State() method
        if (states.containsKey(tmp.getRoot())) 
            states.get(tmp.getRoot()).add(s);
        else {
            TreeSet<Integer> ss = new TreeSet<Integer>();
            ss.add(s);
            states.put(tmp.getRoot(), ss);
        }
        if (compute_weight && WASMA.reference.add(tmp)) {
            this.setSupport(tmp.getWeight());
            WASMA.fringerprint.set(tmp.getOrder()); 
        }
        this.setFollow(tmp.getFollow());
    }
    
    public void Align(DfaState s, DfaState r, int ref) {
        Iterator<State> xit = (((Integer)ref == -1)?s.getStates():s.getStates(ref)).iterator();
        Iterator<State> yit = (((Integer)ref == -1)?r.getRoots():r.getStates(ref)).iterator();
        State x = xit.next(),y = yit.next();
        do {
            if (x.getEnd() < y.getStart()) if (xit.hasNext() )  x = xit.next(); else break;
            else
            { 
                if (x == y || y.getStart() > x.getStart() && y.getEnd() < x.getEnd()) 
                    if (ref == -1)   
                        for (State m :r.getStates(y.getNum())) {
                            addState(m.getNum(), true);
                        }
                    else addState(y.getNum(),true);
                if (yit.hasNext()) y = yit.next(); else break;
            }
        } while (true);
    }

    public DfaState Delta(int i, DfaState ref){    // r = delta(s,i) taking ref as a reference for alignment
        DfaState r   = new DfaState(i);
        WASMA.reference = new TreeSet<State>(State.BY_DESC);    
        WASMA.fringerprint = new BitSet();
        if (this.getItem() == WASMA.itemsetDelimiter) {     // this is an itemsetdelimiter (a #_State)
            r.Align(this,ref,-1);     
        }
        else for (State m :this.getRoots())                 // this is an itemsetState (an element of \sigma_State)
            if (ref.states.containsKey(m.getNum()))
                r.Align(this,ref,m.getNum());   
        return r;
    }
}