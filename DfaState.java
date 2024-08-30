import java.util.*;
// one state of the wDFA is a set of states of the wNfa 
public abstract class DfaState <T>{
    T Pattern;   
    ArrayList<State> states;        // the set of states composing one state of the DFA
    BitSet follow;                  // follow the set of newt items; motif the current itemset as a bitset for local extension check
    int support;                                                                                                                                                                                                                      
    
    public DfaState() {      
        states = new ArrayList<State>();
        follow = new BitSet();
    }

    public T getPattern() {return Pattern;}

    public boolean IsDelimiterState() {     // is this DFA state a delimiter state (# state)
        return (states.size() > 0)? states.get(0).getType():false; // we check the first state (our DFA is homogeneous)
    }

    public int getItem() { return states.get(0).getItem();}
    
    public ArrayList<State> getStates() { return states;}

    public int getSupport() { return support;}

    public void setSupport(int sprt) { support += sprt;}

    public BitSet getFollow() { return follow;}

    public void setFollow(BitSet b) { follow.or(b);}

    public String toString() { return states.toString(); }

    public abstract DfaState <T> AlignLocal(int item); 

    public abstract DfaState <T> AlignGlobal(int item); 

    public void addState(State t, boolean computeSupport) {       // add state to the stateset and consider its follow and weight if it's the case                
        if (!t.getType() || !t.getFollow().isEmpty()) {      
            states.add(t);
            this.setFollow(t.getFollow());
            if (WASMA.STATE_EXISTENCE_CHECK && t.getOrder() >= 0) WASMA.fingerprint.set(t.getOrder());
        }    
        if (computeSupport) this.setSupport(t.getWeight());
    }        

    public DfaState <T> Delta(int item) {    //res = this.Deltat(i,compute)
        WASMA.fingerprint = new BitSet();
        return (this.IsDelimiterState())? 
            AlignGlobal(item):               // global alignment   item(this) == #   
            AlignLocal(item);               // local alignment (item != #)  
    }  
}