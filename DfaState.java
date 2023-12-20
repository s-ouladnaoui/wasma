import java.util.*;
// one state of the wDFA is a set of states of the wNfa 
public class DfaState {   
    TreeSet<State> states;       // the set of states categorized by their (sub)roots (subtree or region)
    int root;
    BitSet follow;
    int support;                                                                                                                                                                                                                      

    public DfaState(){               
        states = new TreeSet<State>(State.BY_ROOT);
        follow = new BitSet(); 
    }

    public boolean IsDelimiterState(){   // is this DFA state a delimiter state (# state)
        return states.first().getType(); // we check the first state (DFA state is homogeneous)
    }

    public TreeSet<State> getStates(){
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (State s:states) r.add(s);
        return r;
    }

    public TreeSet<State> getStates(boolean t){
        TreeSet<State> r = (t)? new TreeSet<State>(State.BY_DESC):
                                new TreeSet<State>(State.BY_ROOT);
        for (State s:states) r.add(s);
        return r;
    }

    public TreeSet<State> getStates(boolean t,int i){
        TreeSet<State> r = (t)? r = new TreeSet<State>(State.BY_DESC):  // sequence extension to avoid repetitive computation
                                    new TreeSet<State>(State.BY_ROOT);  // itemset extension                    
        for (State s:states)   
            if (i < 0 || i > 0 && s.getFollow().get(i))  r.add(s);    // only state having item in follow can contribute to extension
        return r;
    }

    public int getSupport(){ return support;}

    public void setSupport(int sprt){ support += sprt;}

    public BitSet getFollow(){ return follow;}

    public void setFollow(BitSet b){ follow.or(b);}

    public int getRoot(){ return root;}

    public void setRoot(int r){ root = r;}

    public String toString(){ return states.toString(); }

    public void addState(int s, boolean compute_weight) {       // add state to the stateset and consider its weight if it's the case 
        State tmp = WASMA.NFA.State(s);                         // to avoid multiple calls to NFA.State() method
        states.add(WASMA.NFA.State(s));
        if (WASMA.first) {
            WASMA.sentinelle = tmp;
            WASMA.first = false;
            if (compute_weight) this.setSupport(tmp.getWeight());
            if (!IsDelimiterState()) WASMA.fringerprint.set(tmp.getOrder());                    
        } else if (State.BY_DESC.compare(tmp,WASMA.sentinelle) != 0) {
            WASMA.sentinelle = tmp;
            if (compute_weight) this.setSupport(tmp.getWeight());
            if (!IsDelimiterState()) WASMA.fringerprint.set(tmp.getOrder()); 
        }
        if (!tmp.getFollow().isEmpty()) this.setFollow(tmp.getFollow());        
    }
    
    public void Align(DfaState s, DfaState r, int item, boolean t, boolean computeSupport) {
        State x, y;
        boolean possible = true;
        Iterator<State> xit = s.getStates(t,item).iterator();
        if (xit.hasNext()) x = xit.next(); else return;
        Iterator<State> yit = ((t)?r.getStates():r.getStates(false)).iterator();
        if (yit.hasNext()) y = yit.next(); else return;
        do {
            while (possible && (x.root == y.root || t))
                if (x.getEnd() < y.getStart())  // if state x is less (at the left) of state y advance in x iterator
                    if (xit.hasNext()) x = xit.next(); else possible = false;
                else { 
                    if (y.getStart() > x.getStart() && y.getEnd() < x.getEnd())  // if y is descendent from x add it to the result
                        addState(y.getNum(), computeSupport);     
                    if (yit.hasNext()) y = yit.next(); else  possible = false;
                }
            if (y.root < x.root) 
                if (yit.hasNext()) y = yit.next(); else possible = false;
            if (x.root < y.root) 
                if (xit.hasNext()) x = xit.next(); else possible = false;
        } while (possible);           
    }

    public DfaState Delta(int item, DfaState ref, boolean compute_sprt){    // r = delta(s,i) taking ref as a reference for alignment
        // delta computation is based on the alignment between two sorted sets of states those in this and ref
        DfaState r = new DfaState();
        WASMA.fringerprint = new BitSet();
        WASMA.first = true;
        r.Align(this,ref,item,IsDelimiterState(),compute_sprt); // IsDelimiterState distinguish itemset extension (false) and sequence extension (true)
        return r;
    }
}