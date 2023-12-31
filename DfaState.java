import java.util.*;
// one state of the wDFA is a set of states of the wNfa 
public class DfaState {   
    TreeSet<State> states;       // the set of states categorized by their (sub)roots (subtree or region)
    int root;
    BitSet follow;
    int support;                                                                                                                                                                                                                      

    public DfaState() {               
        states = new TreeSet<State>(State.BY_ROOT);
        follow = new BitSet(); 
    }

    public boolean IsDelimiterState() {   // is this DFA state a delimiter state (# state)
        return states.first().getType(); // we check the first state (DFA state is homogeneous)
    }

    public TreeSet<State> getStates(int i) {
        TreeSet<State> r = (WASMA.global)?                      
                            (i == WASMA.transactionDelimiter)?new TreeSet<State>(State.BY_START):
                                                              new TreeSet<State>(State.BY_DESC)  
                            :new TreeSet<State>(State.BY_ROOT) ;
       for (State s:states) 
                if (i < 0 || i >= 0 && s.getFollow().get(i)) r.add(s);
        return r;
    }

    public int getSupport(){ return support;}

    public void setSupport(int sprt){ support += sprt;}

    public BitSet getFollow(){ return follow;}

    public void setFollow(BitSet b){ follow.or(b);}

    public int getRoot(){ return root;}

    public void setRoot(int r){ root = r;}

    public String toString(){ return states.toString(); }

    public void addState(State s, boolean compute_weight) {       // add state to the stateset and consider its weight if it's the case                
        states.add(s);
        if (!s.getFollow().isEmpty()) this.setFollow(s.getFollow());      
        if (compute_weight && WASMA.reference.add(s)) {
            if (!this.IsDelimiterState()) WASMA.fringerprint.set(s.getOrder());
            this.setSupport(s.getWeight());
        } 
    }
    
    public DfaState Align(DfaState s, DfaState r, int item, boolean computeSupport) {
        DfaState res = new DfaState();
        State x, y;
        boolean possible = true;
        Iterator<State> xit = s.getStates(item).iterator();
        if (xit.hasNext()) x = xit.next(); else return res;
        Iterator<State> yit = r.getStates(WASMA.transactionDelimiter).iterator();
        if (yit.hasNext()) y = yit.next(); else return res;
        do {
            while ( possible && (x.root == y.root || WASMA.global)) {
                if (x.getEnd() < y.getStart()) 
                    if (xit.hasNext()) x = xit.next();else possible = false;      // if state x is less (at the left) of state y advance in x iterator       
                else { 
                    if (y.getStart() > x.getStart() && y.getEnd() < x.getEnd())   // if y is descendent from x add it to the result
                        res.addState(y,computeSupport);            
                    if (yit.hasNext()) y = yit.next();else possible = false;      // in both other cases advance in y iterator                         
                }
            } 
            if (x.root < y.root) if(xit.hasNext()) x = xit.next(); else possible = false;
            else if (yit.hasNext()) y = yit.next();else possible = false;                
        } while (possible);
        return res;         
    }

    public DfaState Delta(int item, DfaState ref, boolean compute_sprt){    // r = delta(s,i) taking ref as a reference for alignment
        // delta computation is based on the alignment between two sorted sets of states those in this and ref
        WASMA.fringerprint = new BitSet();
        WASMA.reference = new TreeSet<State>(State.BY_DESC);  // used for support computation (Only elements inserable in this reference sorted collection can contribute to the support)  
        WASMA.global = this.IsDelimiterState();
        return Align(this,ref,item,compute_sprt); 
    }
}