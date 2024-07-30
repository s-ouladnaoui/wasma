import java.util.*;
// one state of the wDFA is a set of states of the wNfa 
public class DfaState {  
    ArrayList<State> states;       // the set of states composing one state of the DFA
    BitSet follow, motif;           // follow the set of newt items; motif the current itemset as a bitset for local extension check
    int support;                                                                                                                                                                                                                      
    public DfaState(int i) {      
        states = new ArrayList<State>();
        follow = new BitSet();
        motif  = new BitSet(); 
        if (i >= 0) motif.set(i); else motif.clear();  // at each itemset separator we initialize the current motif (start a new itemset) 
    }

    public boolean IsDelimiterState() {     // is this DFA state a delimiter state (# state)
        return states.get(0).getType();     // we check the first state (our DFA is homogeneous)
    }

    public int getItem(){ return motif.previousSetBit(motif.length());}
    
    public BitSet getMotif(){ return motif;}

    public int getSupport(){ return support;}

    public void setSupport(int sprt){ support += sprt;}

    public BitSet getFollow(){ return follow;}

    public void setFollow(BitSet b){ follow.or(b);}

    public String toString(){ return states.toString(); }

    public void addState(State s, boolean computeSupport) {       // add state to the stateset and consider its follow and weight if it's the case                
        if (!s.getType() || !s.getFollow().isEmpty()) {
            states.add(s);
            this.setFollow(s.getFollow());
        }    
        if (s.getOrder() >= 0) WASMA.fingerprint.set(s.getOrder());
        if (computeSupport) this.setSupport(s.getWeight());
    }   
    
    public DfaState AlignGlobal(int item, boolean compute) {
        DfaState res = new DfaState(item);
        int i = 0; State r;
        for (State p:this.states){
            if (item >= 0 && !p.getFollow().get(item)) continue;
            i = (p.getStart() == 0) ? 0 : (item == WASMA.itemsetDelimiter) ? 
                            WASMA.itemStates.get(item).get(p.getDelim()).getOrder() : 
                            WASMA.itemsetDelimStates.get(p.getStart()).get(item);
            while ( i < WASMA.itemStates.get(item).size() && WASMA.itemStates.get(item).get(i).getEnd() <= p.getEnd()) {
                r = WASMA.itemStates.get(item).get(i);
                res.addState(r,compute);
                i = r.getlEnd();
            }  
        }
        return res;         
    }

    public DfaState AlignLocal(int item,boolean compute) {
        DfaState res = new DfaState(item);
        int i = 0; State r; BitSet t;
        for (State p:this.states){
            if (WASMA.itemsetDelimStates.get(p.getRoot()) != null && WASMA.itemsetDelimStates.get(p.getRoot()).containsKey(item) && WASMA.itemsetDelimStates.get(p.getRoot()).get(item) >= i)
                i = WASMA.itemsetDelimStates.get(p.getRoot()).get(item);
            while (i < WASMA.itemStates.get(item).size() &&  WASMA.itemStates.get(item).get(i).getEnd() <= p.getEnd()) {
                r = WASMA.itemStates.get(item).get(i);
                if (p.getStart() > r.getStart() && p.getEnd() <= r.getEnd())  i++;
                else {
                    if (r.getStart() > p.getStart()) {
                        t = (BitSet) this.getMotif().clone();
                        t.and(r.getFollow());
                        if (t.equals(this.getMotif())) res.addState(r,compute);
                    } 
                    i =  r.getlEnd();
                }
            }
        }
        return res;        
    }

    public DfaState Delta(int item, boolean compute_sprt) {    // r = delta(s,i) taking ref as a reference in the alignment
        // delta computation is based on the alignment between two sorted sets of states those in this and ref
        WASMA.fingerprint = new BitSet();
        if (this.IsDelimiterState() || item == WASMA.itemsetDelimiter) {        // global alignment 
            return this.AlignGlobal(item,compute_sprt); 
        } else {                                                                // local alignment 
            return this.AlignLocal(item,compute_sprt);
        }  
    }  
}