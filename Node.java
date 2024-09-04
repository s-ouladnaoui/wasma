import java.util.BitSet;
// one node of DFA used in determinisation with state existence check
// Here the node records only the last reached itemset  as  a BitSet
public class Node extends DfaState <BitSet> {
    
    public Node(int i) {
        Pattern  = new BitSet(); 
        if (i >= 0) Pattern.set(i); // at each itemset separator (start a new itemset) we initialize the current motif  
    }
    
    public Node AlignLocal(int item) {
        Node res = new Node(item);
        int i = 0, m; State r; boolean found;
        for (State p:this.getStates()){
            if (!p.getFollow().get(item)) continue;
            if (p.getRoot() == 0) i = 0;
            else if (WASMA.itemsetDelimStates.get(p.getRoot()).map.get(item) >= i) 
                    i = WASMA.itemsetDelimStates.get(p.getRoot()).map.get(item);
            r = WASMA.itemStates.get(item).get(i);
            while (r.getStart() < p.getStart()) {
                i = r.getlEnd() + 1;
                r = WASMA.itemStates.get(item).get(i);
            }
            while (r.getEnd() <= p.getEnd()) {
                if (r.getRoot() == p.getRoot()) found = true;
                else {
                    found = true;
                    for (m = this.getPattern().previousSetBit(this.getPattern().size()); found && m >= 0; found = r.getFollow().get(m), m = this.getPattern().previousSetBit(m - 1));
                } 
                if (found) {
                    res.addState(r, true);  
                    i = r.getlEnd() + 1;
                } else  i++;
                if (i < WASMA.itemStates.get(item).size()) r = WASMA.itemStates.get(item).get(i); else break;
            } 
        }
        return res;        
    }

    public Node AlignGlobal(int item) {
        Node res = new Node(item);
        int i = 0; State r;
        for (State p:this.getStates()) {
            if (!p.getFollow().get(item)) continue;
            r = (p.getStart() == 0)? 
                WASMA.itemStates.get(item).get(0):
                WASMA.itemStates.get(item).get(WASMA.itemsetDelimStates.get(p.getOrder()).map.get(item));            
            while (r.getEnd() <= p.getEnd()) {
                res.addState(r,true);
                i = r.getlEnd() + 1;
                if (i < WASMA.itemStates.get(item).size()) r = WASMA.itemStates.get(item).get(i);
                else break;
            }  
        }
        return res;         
    }
    public Node Terminate_Sequence() {     //Delta(this,#) terminate a sequence by # same as global alignment (separated to save some tests)
        WASMA.fingerprint = new BitSet();
        Node res =  new Node(WASMA.itemsetDelimiter);
        int i; State r;
        for (State p:this.getStates()) {        
            r = WASMA.itemsetDelimStates.get(((iState)p).getDelim()).state;
            while (r.getEnd() <= p.getEnd()) {
                res.addState(r,false);
                i = r.getlEnd() + 1;
                if (i < WASMA.itemsetDelimStates.size()) r = WASMA.itemsetDelimStates.get(i).state;
                else break;
            }  
        }
        return res;         
    }
}