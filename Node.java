import java.util.BitSet;
// one node of DFA used in determinisation with state existence check
public class Node extends DfaState {
    BitSet Pattern;
    public Node(int i) {
        Pattern  = new BitSet(); 
        if (i >= 0) Pattern.set(i); // at each itemset separator (start a new itemset) we initialize the current motif  
    }
    
    public BitSet getPattern() {return Pattern;}

    public Node AlignLocal(int item) {
        Node res = new Node(item);
        int i = 0, m; State r; boolean found;
        for (State p:this.getStates()){
            if (!p.getFollow().get(item)) continue;
            if (p.getRoot() == 0) i = 0;
            else if (WASMA.itemsetDelimStates.get(p.getRoot()).get(item) >= i) 
                    i = WASMA.itemsetDelimStates.get(p.getRoot()).get(item);
            r = WASMA.itemStates.get(item).get(i);
            while (r.getStart() < p.getStart()) {
                i = r.getlEnd() + 1;
                if (i < WASMA.itemStates.get(item).size()) r = WASMA.itemStates.get(item).get(i); else break;
            }
            while (r.getEnd() <= p.getEnd()) {
                if (r.getRoot() == p.getRoot()) found = true;
                else {
                    found = true;
                    for (m = this.getPattern().nextSetBit(0); found && m >= 0; found = r.getFollow().get(m), m = this.getPattern().nextSetBit(m + 1));
                }
                if (found) {
                    res.addState(r, true);  
                    i = r.getlEnd() + 1;
                } else i++;
                if (i < WASMA.itemStates.get(item).size()) r = WASMA.itemStates.get(item).get(i); else break;
            } 
        }
        return res;        
    }
    public Node AlignGlobal(int item) {
        Node res = new Node(item);
        int i = 0; State r;
        for (State p:this.getStates()) {
            if (item >= 0 && !p.getFollow().get(item)) continue;
            i = (p.getStart() == 0) ? 0 : (item == WASMA.itemsetDelimiter) ? 
                            WASMA.itemStates.get(item).get(p.getDelim()).getOrder(): 
                            WASMA.itemsetDelimStates.get(p.getStart()).get(item);
            r = WASMA.itemStates.get(item).get(i);
            while (r.getEnd() <= p.getEnd()) {
                res.addState(r,true);
                i = r.getlEnd() + 1;
                if (i < WASMA.itemStates.get(item).size()) r = WASMA.itemStates.get(item).get(i);
                else break;
            }  
        }
        return res;         
    }
    public Node Terminate_Sequence() {  //Delta(this,#) terminate a sequence by # same as global alignment (separated to save some tests)
        WASMA.fingerprint = new BitSet();
        Node res = new Node(WASMA.itemsetDelimiter);
        int i; State r;
        for (State p:this.getStates()) {        
            i = WASMA.itemStates.get(WASMA.itemsetDelimiter).get(p.getDelim()).getOrder();
            r = WASMA.itemStates.get(WASMA.itemsetDelimiter).get(i);
            while (r.getEnd() <= p.getEnd()) {
                res.addState(r,false);
                i = r.getlEnd() + 1;
                if (i < WASMA.itemStates.get(WASMA.itemsetDelimiter).size()) r = WASMA.itemStates.get(WASMA.itemsetDelimiter).get(i);
                else break;
            }  
        }
        return res;         
    }

    public Node Delta(int item) {    //res = this.Deltat(i,compute)
        WASMA.fingerprint = new BitSet();
        return (this.IsDelimiterState())? 
            AlignGlobal(item):               // global alignment   item(this) == #   
            AlignLocal(item);  // local alignment (item != #)  
    }  
}