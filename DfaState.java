import java.util.*;
// one state of the wDFA is a set of states of the wNfa 
public class DfaState {  
    int item; 
    ArrayList<State> states;       // the set of states composing one state of the DFA
    int reference;
    BitSet follow;
    int support;                                                                                                                                                                                                                      
    public DfaState(int i) {      
        item = i;         
        states = new ArrayList<State>();
        follow = new BitSet(); 
    }

    public boolean IsDelimiterState() {     // is this DFA state a delimiter state (# state)
        return states.get(0).getType();     // we check the first state (our DFA is homogeneous)
    }

    public int getSupport(){ return support;}

    public void setSupport(int sprt){ support += sprt;}

    public BitSet getFollow(){ return follow;}

    public void setFollow(BitSet b){ follow.or(b);}

    public int getRef(){ return reference;}

    public void setRef(int r){ reference = r;}

    public String toString(){ return states.toString(); }

    public void addState(State s,boolean compute_weight) {       // add state to the stateset and consider its follow and weight if it's the case                
        if (!s.getType() || !s.getFollow().isEmpty()) {
            states.add(s);
            this.setFollow(s.getFollow());
        }
        if (WASMA.support_computation_sentinel.add(s)) {
            if (s.getOrder() >= 0) WASMA.fingerprint.set(s.getOrder());
            if (compute_weight) this.setSupport(s.getWeight());
        }
    }
    
    public DfaState AlignGlobal(DfaState s, DfaState ref, int item) {
        DfaState res = new DfaState(item);
        State x, y;
        boolean possible = true;
        Iterator<State> xit = s.states.iterator(),
                        yit = ref.states.iterator(); 
        if (xit.hasNext()) x = xit.next(); else return res;
        State sentinelx = x;
        if (yit.hasNext()) y = yit.next(); else return res;
        while (possible){ 
            if (x.getStart() > sentinelx.getStart() && x.getEnd() < sentinelx.getEnd()) 
                if (xit.hasNext()) { x = xit.next(); continue; } else possible = false;
            else  sentinelx = x;
            if (x.getEnd() < y.getStart())                                  // if state x is less (at the left) of state y advance in x iterator       
                if (xit.hasNext()) x = xit.next(); else possible = false; 
            else { 
                if (y.getStart() > x.getStart() && y.getEnd() < x.getEnd())  // if y is descendent from x add it to the result
                    res.addState(y,true);            
                if (yit.hasNext()) y = yit.next();else possible = false;      // in other cases advance in y iterator                         
            }        
        }
        return res;         
    }

    public DfaState AlignLocal_item(DfaState s, DfaState ref, int item) {
        DfaState res = new DfaState(item);
        State x, y;
        boolean possible = true;
        Iterator<State> xit = s.states.iterator(), 
                        yit = ref.states.iterator();
        if (xit.hasNext()) x = xit.next(); else return res;
        if (yit.hasNext()) y = yit.next(); else return res; 
        while (possible) { 
            while (possible && x.root == y.root){ 
                if ( x.getEnd() < y.getStart())                                 // if state x is less (at the left) of state y advance in x iterator       
                    if (xit.hasNext()) x = xit.next(); else possible = false; 
                else { 
                    if (y.getStart() > x.getStart() && y.getEnd() < x.getEnd())  // if y is descendent from x add it to the result
                            res.addState(y,item != WASMA.itemsetDelimiter);                    
                    if (yit.hasNext()) y = yit.next();else possible = false;      // in other cases advance in y iterator                         
                }   
            }
            if (x.root < y.root) if(xit.hasNext()) x = xit.next(); else possible = false;
            else if (yit.hasNext()) y = yit.next();else possible = false;              
        }        
        return res;         
    }

   
    public DfaState Delta(int item, DfaState ref, boolean compute_sprt) {    // r = delta(s,i) taking ref as a reference in the alignment
        // delta computation is based on the alignment between two sorted sets of states those in this and ref
        WASMA.fingerprint = new BitSet();
        WASMA.support_computation_sentinel = new TreeSet<State>(State.BY_DESC);
        if (this.IsDelimiterState()) {     // global alignment both source and destination statesets are ordered using start id
            Collections.sort(this.states,State.BY_START);  
            Collections.sort(ref.states,State.BY_START);
            return AlignGlobal(this,ref,item); 
        } else {                          // local alignment we use root subtree (local) ordering
            Collections.sort(this.states,State.BY_ROOT);  
            Collections.sort(ref.states,State.BY_ROOT);
            return AlignLocal_item(this,ref,item);
        }  
    }  

}










/*  ==================================================================================================


    /*public DfaState AlignLocal_item(DfaState s,DfaState ref, int item){
        DfaState res = new DfaState(item);
        Collections.sort(ref.states,State.BY_START);
        State sentinely = null;
        Boolean first = true;
        for (State t : ref.states)
            if (t.getFollow().get(s.item)) 
                    if (first) {
                            res.addState(t,item,true);
                            sentinely = t;
                            first = false;
                    } else if (!(t.getStart() > sentinely.getStart() && t.getEnd() < sentinely.getEnd())) {
                                res.addState(t,item,true);            
                                sentinely = t;
                            }
        return res;   
    }





public DfaState Delta(int item, DfaState ref, boolean compute_sprt){    // r = delta(s,i) taking ref as a reference for alignment
        // delta computation is based on the alignment between two sorted sets of states those in this and ref
        WASMA.fringerprint = new BitSet();
        //WASMA.reference = new TreeSet<State>(State.BY_DESC);  // used for support computation (Only elements inserable in this reference sorted collection can contribute to support)  
        WASMA.global = this.IsDelimiterState();
        WASMA.first = true;
        return Align(this, ref, item, compute_sprt);
    }
 delta with the three cases separated
if (WASMA.global) {   // global alignment  both source and destination statesets are ordering using start id
            return AlignGlobal(this,ref,item); 
        } else if (item != WASMA.itemsetDelimiter) {
            return AlignLocal_item(this,ref,item);  // item != itemsetDelimiter local alignment with support computation (we don't use alignment instead we use the Follow bitset)
        } else {
            return AlignLocal_itemsetDelimiter(this,ref);  // item == itemsetDelimiter local alignment without support computation
                                                            // here we sort using local subroot order
}
public void addState(State s, int item) {       // add state to the stateset. We separate the case of delta(s,a) (a != itemsetdelimter) for mre efficiency               
        if ( item != WASMA.itemsetDelimiter || !s.getFollow().isEmpty()) {
            states.add(s);
            this.setFollow(s.getFollow());      
        }
        if (WASMA.reference.add(s)) {
            WASMA.fringerprint.set(s.getOrder());
            this.setSupport(s.getWeight());
        }
    }

    public DfaState AlignGlobal(DfaState s,DfaState ref,int item){
        DfaState res = new DfaState(item);
        State x, y;
        boolean possible = true;
        TreeSet<State> xx = new TreeSet<State>(State.BY_DESC);
        xx.addAll(s.states); 
        Iterator<State> xit =xx.iterator();
        if (xit.hasNext()) x = xit.next(); else return res;
        Collections.sort(ref.states,State.BY_START);
        Iterator<State> yit = ref.states.iterator();
        if (yit.hasNext()) y = yit.next(); else return res;
        while (possible)
            if (x.getEnd() < y.getStart()) 
                if (xit.hasNext()) x = xit.next();else possible = false;      // if state x is less (at the left) of state y advance in x iterator       
            else { 
                if (y.getStart() > x.getStart() && y.getEnd() < x.getEnd())   // if y is descendent from x add it to the result
                    res.addState(y,item);            
                if (yit.hasNext()) y = yit.next();else possible = false;      // if state x is less (at the left) of state y advance in x iterator                         
            }               
        return res;         
    }

    public DfaState AlignLocal_item(DfaState s,DfaState ref, int item){
        DfaState res = new DfaState(item);
        for (State t:ref.states) 
            if (t.getFollow().get(s.item)) res.addState(t,item);
        return res;   
    }

    public DfaState AlignLocal(DfaState s,DfaState ref, int item){
        DfaState res = new DfaState(item);
        State x, y;
        boolean possible = true;
        Collections.sort(s.states,State.BY_ROOT);
        Iterator<State> xit = s.states.iterator();
        if (xit.hasNext()) x = xit.next(); else return res;
        Collections.sort(ref.states,State.BY_ROOT);
        Iterator<State> yit = ref.states.iterator();
        if (yit.hasNext()) y = yit.next(); else return res;
        while(possible){
        while (possible && (x.root == y.root || WASMA.global))
            if (x.getEnd() < y.getStart()) 
                if (xit.hasNext()) x = xit.next();else possible = false;      // if state x is less (at the left) of state y advance in x iterator       
            else { 
                if (y.getStart() > x.getStart() && y.getEnd() < x.getEnd() &&  !y.getFollow().isEmpty()) {             // if y is descendent from x add it to the result
                    res.states.add(y);                    // instead of calling addState we put the code here since there is no support computation only state insertion if usefull later 
                    res.setFollow(y.getFollow());      
                    }
                if (yit.hasNext()) y = yit.next();else possible = false;      // if state x is less (at the left) of state y advance in x iterator                         
            }
            if (x.root < y.root) if(xit.hasNext()) x = xit.next(); else possible = false;
            else if (yit.hasNext()) y = yit.next();else possible = false;              
        }    
        return res;        
    }

    public TreeSet<State> getStates(int i) {
        TreeSet<State> r;
        if (WASMA.global)
            r = (i == WASMA.transactionDelimiter)?
                new TreeSet<State>(State.BY_START):  // Natual ordering used in global exploration
                new TreeSet<State>(State.BY_DESC);  // Descendance ordering used in support computation and to avoid repetitive computation in global exploration
        else 
            r = new TreeSet<State>(State.BY_ROOT);      // local exploration use subtree root ordering 
        if (i < 0) {               // for optimisation we separate this case with a condition outside the loop
            for (State s:states) 
                r.add(s);
            return r;
        }
        for (State s:states) 
                if (s.getFollow().get(i)) r.add(s);
        return r;














        
    }*/
