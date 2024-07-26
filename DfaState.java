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

    public void addState(State s, boolean computeSupport) {       // add state to the stateset and consider its follow and weight if it's the case                
        if (!s.getType() || !s.getFollow().isEmpty()) {
            states.add(s);
            this.setFollow(s.getFollow());
        }    
        if (s.getOrder() >= 0) WASMA.fingerprint.set(s.getOrder());
        if (computeSupport) this.setSupport(s.getWeight());
    }   
    
    public DfaState AlignGlobal(DfaState s, int item, boolean compute) {
        DfaState res = new DfaState(item);
        int i = 0;
        for (State p:s.states){
            if (item >= 0 && !p.getFollow().get(item)) continue;
            i = (p.getStart() <= 0) ? 0 : (item == WASMA.itemsetDelimiter) ? WASMA.itemStates.get(item).get(p.delimiter).lstart : WASMA.itemsetDelimStates.get(p.gstart).get(item);
            while ( i < WASMA.itemStates.get(item).size() && WASMA.itemStates.get(item).get(i).gend <= p.getEnd()) {
                res.addState(WASMA.itemStates.get(item).get(i),compute);
                i =  WASMA.itemStates.get(item).get(i).lend;
            }  
        }
        return res;         
    }

    public DfaState AlignLocal(DfaState s,  int item,boolean compute) {
        DfaState res = new DfaState(item);
        int i = 0;
        for (State p:s.states){
            if (p.getRoot() == 0) i = 0;
            else 
                if (WASMA.itemsetDelimStates.get(p.getRoot()).containsKey(item)) i = WASMA.itemsetDelimStates.get(p.getRoot()).get(item);
                else continue;
            while ( i < WASMA.itemStates.get(item).size() && WASMA.itemStates.get(item).get(i).gend <= p.getEnd()) {
                    if (WASMA.itemStates.get(item).get(i).getFollow().get(p.item)) {
                        res.addState(WASMA.itemStates.get(item).get(i),compute);
                        i =  WASMA.itemStates.get(item).get(i).lend;
                    } else i++;
                }     
        }
        return res;        
    }

    public DfaState Delta(int item, boolean compute_sprt) {    // r = delta(s,i) taking ref as a reference in the alignment
        // delta computation is based on the alignment between two sorted sets of states those in this and ref
        WASMA.fingerprint = new BitSet();
        if (this.IsDelimiterState() || item == WASMA.itemsetDelimiter) {     // global alignment both source and destination statesets are ordered using start id
            return AlignGlobal(this,item,compute_sprt); 
        } else {                          // local alignment we use root subtree (local) ordering
            return AlignLocal(this,item,compute_sprt);
        }  
    }  

}