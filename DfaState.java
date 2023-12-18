import java.util.*;
// one state of the wDFA is a set of states of the wNfa 
public class DfaState {   
    int item; 
    TreeSet<Integer> states;       // the set of states categorized by their (sub)roots (subtree or region)
    int root;
    BitSet follow;
    int support;                                                                                                                                                                                                                      

    public DfaState(int i){               
        states = new TreeSet<Integer>();
        follow = new BitSet(); 
        item  =  i;
    }

    public int getItem(){ return item; }

    public TreeSet<State> getStates(boolean t){
        TreeSet<State> r = (t)? new TreeSet<State>(State.BY_DESC):
                                new TreeSet<State>(State.BY_ROOT);
        for (int s:states) 
            r.add(WASMA.NFA.State(s));
        return r;
    }
    public TreeSet<State> getStates(boolean t,int i){
        TreeSet<State> r = (t)? new TreeSet<State>(State.BY_DESC):
                                new TreeSet<State>(State.BY_ROOT);
        if (i == WASMA.itemsetDelimiter)
            for (int s:states) 
                r.add(WASMA.NFA.State(s));
        else for (int s:states) 
                if (WASMA.NFA.State(s).getFollow().get(i)) 
                    r.add(WASMA.NFA.State(s));
        return r;
    }
    public TreeSet<State> getStates(){
        TreeSet<State> r = new TreeSet<State>(State.BY_START);
        for (int s:states) 
            r.add(WASMA.NFA.State(s));
        return r;
    }

    public int getSupport(){ return support;}

    public void setSupport(int sprt){ support += sprt;}

    public BitSet getFollow(){ return follow;}

    public void setFollow(BitSet b){ follow.or(b);}

    public int getRoot(){ return root;}

    public void setRoot(int r){ root = r;}

    public String toString(){
        return states.toString();
    }

    public void addState(int s, boolean compute_weight) {       // add state to the stateset and consider its weight if it's the case 
        State tmp = WASMA.NFA.State(s);                         // to avoid multiple calls to NFA.State() method
        if (WASMA.reference.add(tmp)) {
            if (compute_weight) 
                this.setSupport(tmp.getWeight());
            if (this.getItem() != WASMA.itemsetDelimiter) 
                WASMA.fringerprint.set(tmp.getOrder()); 
        }                
        states.add(s);
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
            while (possible && (x.root == y.root || t)){
                if (x.getEnd() < y.getStart()) 
                    if (xit.hasNext())  // if state x is less (at the left) of state y advance in x iterator
                        x = xit.next(); 
                    else possible = false;
                else { 
                    if (y.getStart() > x.getStart() && y.getEnd() < x.getEnd())  // if y is descendent from x add it to the result
                        addState(y.getNum(), computeSupport);      // case of an itemstateset extension
                    if (yit.hasNext()) y = yit.next(); else  possible = false;
                }
            }
            if (y.root < x.root) {
                if (yit.hasNext()) y = yit.next(); else possible = false;
            }
            if (x.root < y.root) {
                if (xit.hasNext()) x = xit.next(); else possible = false;
            }
        } while (possible);           
    }

    public DfaState Delta(int item, DfaState ref, boolean compute_sprt){    // r = delta(s,i) taking ref as a reference for alignment
        DfaState r = new DfaState(item);
        WASMA.reference = new TreeSet<State>(State.BY_DESC);    
        WASMA.fringerprint = new BitSet();
        r.Align(this,ref,item,this.getItem() == WASMA.itemsetDelimiter,compute_sprt);           
        return r;
    }
}